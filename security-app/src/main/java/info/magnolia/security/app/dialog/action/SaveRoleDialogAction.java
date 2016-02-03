/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.security.app.dialog.field.AccessControlList;
import info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang3.StringUtils;

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

        final JcrNodeAdapter nodeAdapter = (JcrNodeAdapter) item;

        // First validate
        validator.showValidation(true);
        if (validator.isValid() && validateAccessControlLists(nodeAdapter)) {
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

                role = roleManager.createRole(parentPath, newRoleName);
                roleNode = parentNode.getNode(role.getName());

                // Repackage the JcrNewNodeAdapter as a JcrNodeAdapter so we can update the node
                roleItem = convertNewNodeAdapterForUpdating((JcrNewNodeAdapter) roleItem, roleNode);
                roleNode = roleItem.applyChanges();
            } else {
                // First fetch the initial name (changes not applied yet here).
                String existingRoleName = roleItem.getJcrItem().getName();
                String pathBefore = roleItem.getJcrItem().getPath();
                // Apply changes now since the further operations on ACL's are done on nodes.
                roleNode = roleItem.applyChanges();
                if (!StringUtils.equals(existingRoleName, newRoleName)) {
                    NodeUtil.renameNode(roleNode, newRoleName);
                    roleNode.setProperty("name", newRoleName);
                    UsersWorkspaceUtil.updateAcls(roleNode, pathBefore);
                }
            }

            if (roleNode.hasNode("acl_userroles/0")) {
                Node entryNode = roleNode.getNode("acl_userroles/0");
                entryNode.setProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME, "true");
                entryNode.setProperty(WorkspaceAccessFieldFactory.ACCESS_TYPE_PROPERTY_NAME, AccessControlList.ACCESS_TYPE_NODE);
                entryNode.getSession().save();
            }

            for (Node aclNode : NodeUtil.getNodes(roleNode)) {

                if (aclNode.getName().startsWith("acl_") && !aclNode.getName().equals("acl_uri")) {

                    AccessControlList acl = new AccessControlList();

                    for (Node entryNode : NodeUtil.getNodes(aclNode)) {

                        if (entryNode.hasProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME)) {
                            String path = entryNode.getProperty(AccessControlList.PATH_PROPERTY_NAME).getString();
                            long accessType = entryNode.getProperty(WorkspaceAccessFieldFactory.ACCESS_TYPE_PROPERTY_NAME).getLong();
                            long permissions = entryNode.getProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME).getLong();

                            path = stripWildcardsFromPath(path);

                            if (StringUtils.isNotBlank(path)) {
                                acl.addEntry(new AccessControlList.Entry(permissions, accessType, path));
                            }
                        }
                        entryNode.remove();
                    }

                    aclNode.setProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME, (Value) null);
                    acl.saveEntries(aclNode);
                }
            }

            roleNode.getSession().save();
        } catch (final Exception e) {
            throw new ActionExecutionException(e);
        }
    }

    private JcrNodeAdapter convertNewNodeAdapterForUpdating(JcrNewNodeAdapter newNodeAdapter, Node node) throws RepositoryException {

        JcrNodeAdapter adapter = new JcrNodeAdapter(node);

        for (Object propertyId : newNodeAdapter.getItemPropertyIds()) {
            Property property = adapter.getItemProperty(propertyId);
            if (property == null) {
                adapter.addItemProperty(propertyId, newNodeAdapter.getItemProperty(propertyId));
            } else {
                property.setValue(newNodeAdapter.getItemProperty(propertyId).getValue());
            }
        }

        adapter.getChildren().clear();
        for (AbstractJcrNodeAdapter child : newNodeAdapter.getChildren().values()) {

            if (child instanceof JcrNewNodeAdapter) {
                if (node.hasNode(child.getNodeName())) {
                    if (child.getNodeName().startsWith("acl_")) {
                        child = convertNewNodeAdapterForUpdating((JcrNewNodeAdapter) child, node.getNode(child.getNodeName()));
                        adapter.addChild(child);
                    } else {
                        child.setNodeName(getUniqueNodeNameForChild(child.getParent()));
                        child.setParent(adapter);
                        child.setItemId(adapter.getItemId());
                    }
                } else {
                    child.setParent(adapter);
                    child.setItemId(adapter.getItemId());
                }
            }
            adapter.addChild(child);
        }

        return adapter;
    }

    private String getUniqueNodeNameForChild(AbstractJcrNodeAdapter parentItem) throws RepositoryException {

        // The adapter cannot handle more than one unnamed child, see MGNLUI-1459, so we have to generate unique ones

        Node parentNode = null;
        if (!(parentItem instanceof JcrNewNodeAdapter)) {
            parentNode = parentItem.getJcrItem();
        }

        int newNodeName = 0;
        while (true) {
            if (parentItem.getChild(String.valueOf(newNodeName)) != null) {
                newNodeName++;
                continue;
            }
            if (parentNode != null && parentNode.hasNode(String.valueOf(newNodeName))) {
                newNodeName++;
                continue;
            }
            break;
        }

        return String.valueOf(newNodeName);
    }

    /**
     * Validates the ACLs present in the dialog. The validation is done on the JcrNodeAdapter because we have to validate
     * before calling applyChanges. applyChanges() modifies the adapter and it needs to be untouched when validation
     * fails because it is then still used in the dialog.
     */
    private boolean validateAccessControlLists(JcrNodeAdapter roleItem) throws ActionExecutionException {

        if (MgnlContext.getUser().hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)) {
            return true;
        }

        try {
            if (roleItem instanceof JcrNewNodeAdapter) {
                Node parentNode = roleItem.getJcrItem();

                // Make sure this user is allowed to add a role here, the role manager would happily do it and then we'd fail to read the node
                parentNode.getSession().checkPermission(parentNode.getPath(), Session.ACTION_ADD_NODE);
            }

            for (AbstractJcrNodeAdapter aclItem : roleItem.getChildren().values()) {

                String aclNodeName = aclItem.getNodeName();

                if (aclNodeName.startsWith("acl_")) {

                    if (aclItem.getItemProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME) != null) {

                        // This is an ACL added using WorkspaceAccessFieldFactory

                        for (AbstractJcrNodeAdapter entryItem : aclItem.getChildren().values()) {

                            String path = (String) entryItem.getItemProperty(AccessControlList.PATH_PROPERTY_NAME).getValue();
                            long accessType = (Long) entryItem.getItemProperty(WorkspaceAccessFieldFactory.ACCESS_TYPE_PROPERTY_NAME).getValue();
                            long permissions = (Long) entryItem.getItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME).getValue();

                            String workspaceName = StringUtils.replace(aclItem.getNodeName(), "acl_", "");

                            if (!isCurrentUserEntitledToGrantRights(workspaceName, path, accessType, permissions)) {
                                throw new ActionExecutionException("Access violation: could not create role. Have you the necessary grants to create such a role?");
                            }
                        }
                    } else if (aclNodeName.equals("acl_uri")) {

                        // This is an ACL added using WebAccessFieldFactory

                        for (AbstractJcrNodeAdapter entryItem : aclItem.getChildren().values()) {

                            String path = (String) entryItem.getItemProperty(AccessControlList.PATH_PROPERTY_NAME).getValue();
                            long permissions = (Long) entryItem.getItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME).getValue();

                            if (!isCurrentUserEntitledToGrantUriRights(path, permissions)) {
                                throw new ActionExecutionException("Access violation: could not create role. Have you the necessary grants to create such a role?");
                            }
                        }
                    }
                }
            }

            return true;

        } catch (AccessControlException e) {
            throw new ActionExecutionException(e);
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    /**
     * Examines whether the current user creating/editing a role has himself the required permissions to the workspaces
     * he's specifying in the ACLs. We See MGNLUI-2357.
     */
    private boolean isCurrentUserEntitledToGrantRights(String workspaceName, String path, long accessType, long permissions) throws RepositoryException {

        if (MgnlContext.getUser().hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)) {
            return true;
        }

        // Granting DENY access is only allowed if the user has READ access to the node
        if (permissions == Permission.NONE) {
            permissions = Permission.READ;
        }

        ACL acl = PrincipalUtil.findAccessControlList(MgnlContext.getSubject(), workspaceName);
        if (acl == null) {
            return false;
        }

        Permission ownPermissions = findBestMatchingPermissions(acl.getList(), stripWildcardsFromPath(path));
        if (ownPermissions == null) {
            return false;
        }

        boolean recursive = (accessType & AccessControlList.ACCESS_TYPE_CHILDREN) != 0;

        if (recursive && !ownPermissions.getPattern().getPatternString().endsWith("/*")) {
            return false;
        }

        return granted(ownPermissions, permissions);
    }

    /**
     * Examines whether the current user creating/editing a role has himself the required permissions to the URIs
     * he's specifying in the ACLs. We See MGNLUI-2357.
     */
    private boolean isCurrentUserEntitledToGrantUriRights(String path, long permissions) throws RepositoryException {

        if (MgnlContext.getUser().hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)) {
            return true;
        }

        // Granting DENY access is only allowed if the user has READ access to the path
        if (permissions == Permission.NONE) {
            permissions = Permission.READ;
        }

        ACL acl = PrincipalUtil.findAccessControlList(MgnlContext.getSubject(), "uri");
        if (acl == null) {
            return false;
        }

        boolean recursive = path.endsWith("*");

        Permission ownPermissions = findBestMatchingPermissions(acl.getList(), stripWildcardsFromPath(path));
        if (ownPermissions == null) {
            return false;
        }

        if (recursive && !ownPermissions.getPattern().getPatternString().endsWith("*")) {
            return false;
        }

        return granted(ownPermissions, permissions);
    }

    private String stripWildcardsFromPath(String path) {
        path = StringUtils.stripEnd(path, "/*");
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        return path;
    }

    private boolean granted(Permission permissionsGranted, long permissionsNeeded) {
        return (permissionsGranted.getPermissions() & permissionsNeeded) == permissionsNeeded;
    }

    private Permission findBestMatchingPermissions(List<Permission> permissions, String path) {
        if (permissions == null) {
            return null;
        }
        Permission bestMatch = null;
        long permission = 0;
        int patternLength = 0;
        ArrayList<Permission> temp = new ArrayList<Permission>();
        temp.addAll(permissions);
        for (Permission p : temp) {
            if (p.match(path)) {
                int l = p.getPattern().getLength();
                if (patternLength == l && (permission < p.getPermissions())) {
                    permission = p.getPermissions();
                    bestMatch = p;
                } else if (patternLength < l) {
                    patternLength = l;
                    permission = p.getPermissions();
                    bestMatch = p;
                }
            }
        }
        return bestMatch;
    }
}
