/**
 * This file Copyright (c) 2012-2013 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.security.app.dialog.action;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.security.app.dialog.field.AccessControlList;
import info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Save role dialog action. Transforms nodes added by {@link info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory} to its final representation.
 */
public class SaveRoleDialogAction extends SaveDialogAction {


    private final SecuritySupport securitySupport;

    public SaveRoleDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback, SecuritySupport securitySupport) {
        super(definition, item, validator, callback);
        this.securitySupport = securitySupport;

    }

    /**
     * @deprecated since 5.2.1 - use {@link SaveRoleDialogAction#SaveRoleDialogAction(info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition, com.vaadin.data.Item, info.magnolia.ui.form.EditorValidator, info.magnolia.ui.form.EditorCallback, info.magnolia.cms.security.SecuritySupport)} instead.
     */
    public SaveRoleDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback) {
        this(definition, item, validator, callback, Components.getComponent(SecuritySupport.class));
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            final JcrNodeAdapter nodeAdapter = (JcrNodeAdapter) item;
            createOrUpdateRole(nodeAdapter);
            callback.onSuccess(getDefinition().getName());

        } else {
            // validation errors are displayed in the UI.
        }
    }

    private void createOrUpdateRole(JcrNodeAdapter roleItem) throws ActionExecutionException {
        try {

            final RoleManager roleManager = securitySupport.getRoleManager();

            final String newRoleName = (String) roleItem.getItemProperty(ModelConstants.JCR_NAME).getValue();

            Role role;
            Node roleNode;
            if (roleItem instanceof JcrNewNodeAdapter) {

                // JcrNewNodeAdapter returns the parent JCR item here
                Node parentNode = roleItem.getJcrItem();
                String parentPath = parentNode.getPath();

                // Make sure this user is allowed to add a role here, the role manager would happily do it and then we'd fail to read the node
                parentNode.getSession().checkPermission(parentNode.getPath(), Session.ACTION_ADD_NODE);

                role = roleManager.createRole(parentPath, newRoleName);
                roleNode = parentNode.getNode(role.getName());

                // Repackage the JcrNewNodeAdapter as a JcrNodeAdapter so we can update the node
                JcrNodeAdapter adapter = new JcrNodeAdapter(roleNode);

                for (Object propertyId : roleItem.getItemPropertyIds()) {
                    Property property = adapter.getItemProperty(propertyId);
                    if (property == null) {
                        adapter.addItemProperty(propertyId, roleItem.getItemProperty(propertyId));
                    } else {
                        property.setValue(roleItem.getItemProperty(propertyId).getValue());
                    }
                }
                adapter.getChildren().clear();
                adapter.getChildren().putAll(roleItem.getChildren());

                roleItem = adapter;

            } else {
                roleNode = roleItem.getJcrItem();
                String existingRoleName = roleNode.getName();

                if (!StringUtils.equals(existingRoleName, newRoleName)) {
                    String pathBefore = roleNode.getPath();
                    NodeUtil.renameNode(roleNode, newRoleName);
                    roleNode.setProperty("name", newRoleName);
                    UsersWorkspaceUtil.updateAcls(roleNode, pathBefore);
                }
            }

            roleNode = roleItem.applyChanges();

            for (Node aclNode : NodeUtil.getNodes(roleNode)) {

                // Any node marked as using the intermediary format we read in, remove all its sub nodes and then
                // add new sub nodes based on the read in ACL
                if (aclNode.hasProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME)) {

                    AccessControlList acl = new AccessControlList();

                    for (Node entryNode : NodeUtil.getNodes(aclNode)) {

                        if (entryNode.hasProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME)) {
                            String path = entryNode.getProperty(AccessControlList.PATH_PROPERTY_NAME).getString();
                            long accessType = (int) entryNode.getProperty(WorkspaceAccessFieldFactory.ACCESS_TYPE_PROPERTY_NAME).getLong();
                            long permissions = entryNode.getProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME).getLong();

                            if (path.equals("/")) {
                            } else if (path.equals("/*")) {
                                path = "/";
                            } else {
                                path = StringUtils.removeEnd(path, "/*");
                                path = StringUtils.removeEnd(path, "/");
                            }

                            if (!isCurrentUserEntitledToGrantRights(aclNode, permissions, path)) {
                                throw new ActionExecutionException("Access violation: could not create role. Have you the necessary grants to create such a role?");
                            }

                            if (StringUtils.isNotBlank(path)) {
                                acl.addEntry(new AccessControlList.Entry(permissions, accessType, path));
                            }
                        }

                        entryNode.remove();
                    }

                    aclNode.setProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME, (Value)null);
                    acl.saveEntries(aclNode);
                }
            }

            roleNode.getSession().save();

        } catch (final Exception e) {
            throw new ActionExecutionException(e);
        }
    }

    /**
     * Ensures that the current user creating/editing a role has he himself at least the grants he wants to give. See MGNLUI-2357.
     * The method has package visibility for testing purposes only.
     */
    final boolean isCurrentUserEntitledToGrantRights(Node node, long permission, String path) throws RepositoryException {
        if (permission == Permission.NONE) {
            return true;
        }
        String workspaceName = StringUtils.replace(node.getName(), "acl_", "");

        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        if (!repositoryManager.hasWorkspace(workspaceName)) {
            return true;
        }

        if ("uri".equals(workspaceName)) {
            String permissionString = PermissionImpl.getPermissionAsName(permission);
            return PermissionUtil.isGranted("uri", path, permissionString);
        } else {
            return PermissionUtil.isGranted(MgnlContext.getJCRSession(workspaceName), path, permission);
        }
    }
}
