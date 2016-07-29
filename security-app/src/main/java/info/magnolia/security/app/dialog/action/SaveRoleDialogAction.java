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

import static info.magnolia.cms.security.operations.AccessDefinition.DEFAULT_SUPERUSER_ROLE;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.security.app.dialog.field.AccessControlList;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.lang.reflect.Field;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Save action for the role dialog.
 *
 * <p>Saving of ACL entries to the role node is delegated to {@link AccessControlList AccessControlLists}.
 * These typed ACLs are carried over from the form, as properties of the dialog item.
 * They get removed from the item here, not to interfere with the JCR adapter.</p>
 *
 * <p>Other properties of the role item are saved by the regular JCR adapter.</p>
 *
 * @see info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory
 * @see info.magnolia.security.app.dialog.field.WebAccessFieldFactory
 */
public class SaveRoleDialogAction extends SaveDialogAction {

    private static final Logger log = LoggerFactory.getLogger(SaveRoleDialogAction.class);
    private final SecuritySupport securitySupport;

    public SaveRoleDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback, SecuritySupport securitySupport) {
        super(definition, item, validator, callback);
        this.securitySupport = securitySupport;
    }

    /**
     * @deprecated since 5.2.1 - use {@link #SaveRoleDialogAction(SaveDialogActionDefinition, Item, EditorValidator, EditorCallback, SecuritySupport)} instead.
     */
    @Deprecated
    public SaveRoleDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback) {
        this(definition, item, validator, callback, Components.getComponent(SecuritySupport.class));
    }

    @Override
    public void execute() throws ActionExecutionException {
        final JcrNodeAdapter nodeAdapter = (JcrNodeAdapter) item;

        if (validateForm() && validateNewRolePermission(nodeAdapter)) {
            createOrUpdateRole(nodeAdapter);
            callback.onSuccess(getDefinition().getName());
        }
    }

    /**
     * Override this function to make sure all previous errors are clean before calling validate.
     */
    protected boolean validateForm() {
        validator.showValidation(false);
        boolean isValid = validator.isValid();
        validator.showValidation(!isValid);
        if (!isValid) {
            log.info("Validation error(s) occurred. No save performed.");
        }
        return isValid;
    }

    private boolean validateNewRolePermission(JcrNodeAdapter roleItem) throws ActionExecutionException {
        if (MgnlContext.getUser().hasRole(DEFAULT_SUPERUSER_ROLE)) {
            return true;
        }

        // Make sure this user is allowed to add a role here, the role manager would happily do it and then we'd fail to read the node
        try {
            if (roleItem instanceof JcrNewNodeAdapter) {
                Node parentNode = roleItem.getJcrItem();
                parentNode.getSession().checkPermission(parentNode.getPath(), Session.ACTION_ADD_NODE);
            }
            return true;
        } catch (AccessControlException | RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    private void createOrUpdateRole(JcrNodeAdapter roleItem) throws ActionExecutionException {
        try {

            final RoleManager roleManager = securitySupport.getRoleManager();
            final String newRoleName = Path.getValidatedLabel((String) roleItem.getItemProperty(ModelConstants.JCR_NAME).getValue());

            Node roleNode;

            // Remove ACL properties from JCR adapter so that it doesn't try to save them (and fail because JCR doesn't know about this type)
            Map<String, AccessControlList<AccessControlList.Entry>> aclsProperties = removeTransientAclProperties(roleItem);

            if (roleItem instanceof JcrNewNodeAdapter) {
                // JcrNewNodeAdapter returns the parent JCR item here
                Node parentNode = roleItem.getJcrItem();
                String parentPath = parentNode.getPath();

                Role role = roleManager.createRole(parentPath, newRoleName);
                roleNode = parentNode.getNode(role.getName());
                // Repackage the JcrNewNodeAdapter as a JcrNodeAdapter so we can update the node
                JcrNodeAdapter newRoleItem = convertNewNodeAdapterForUpdating((JcrNewNodeAdapter) roleItem, roleNode, newRoleName);
                roleNode = newRoleItem.applyChanges();
                // Workaround that updates item id of the roleItem so we can use it in OpenAddRoleDialogAction to fire ContentChangedEvent
                try {
                    Field f = roleItem.getClass().getDeclaredField("appliedChanges");
                    f.setAccessible(true);
                    f.setBoolean(roleItem, true);
                    f.setAccessible(false);
                    roleItem.setItemId(newRoleItem.getItemId());
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    log.warn("Unable to set new JcrItemId for adapter {}", roleItem, e);
                }

                updateAcls(roleNode, aclsProperties, false);

            } else {
                // First fetch the initial name (changes not applied yet here).
                String existingRoleName = roleItem.getJcrItem().getName();
                String pathBefore = roleItem.getJcrItem().getPath();
                // Apply changes now since the further operations on ACL's are done on nodes.
                roleNode = roleItem.applyChanges();

                // apply ACL changes before renaming node (so that it doesn't add old entries again)
                updateAcls(roleNode, aclsProperties, true);

                if (!StringUtils.equals(existingRoleName, newRoleName)) {
                    UsersWorkspaceUtil.updateAcls(roleNode, pathBefore);
                }
            }

            roleNode.getSession().save();
        } catch (final RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    private void updateAcls(Node roleNode, Map<String, AccessControlList<AccessControlList.Entry>> acls, boolean removeOldEntries) throws RepositoryException {
        for (Entry<String, AccessControlList<AccessControlList.Entry>> aclEntry : acls.entrySet()) {
            String aclNodeName = aclEntry.getKey();
            AccessControlList<AccessControlList.Entry> acl = aclEntry.getValue();

            Node aclNode;
            if (roleNode.hasNode(aclNodeName)) {
                aclNode = roleNode.getNode(aclNodeName);
                if (removeOldEntries) {
                    // Clean up all exiting child before saving all entries
                    for (Node entryNode : NodeUtil.getNodes(aclNode)) {
                        entryNode.remove();
                    }
                }
            } else {
                aclNode = roleNode.addNode(aclNodeName, NodeTypes.ContentNode.NAME);
            }

            acl.saveEntries(aclNode);

            // Prevent saving of empty ACL nodes
            if (!aclNode.hasNodes()) {
                aclNode.remove();
            }
        }
    }

    private JcrNodeAdapter convertNewNodeAdapterForUpdating(JcrNewNodeAdapter newNodeAdapter, Node node, String newRoleName) throws RepositoryException {

        JcrNodeAdapter adapter = new JcrNodeAdapter(node);

        for (Object propertyId : newNodeAdapter.getItemPropertyIds()) {
            Property property = adapter.getItemProperty(propertyId);
            if (property == null) {
                adapter.addItemProperty(propertyId, newNodeAdapter.getItemProperty(propertyId));
            } else if (ModelConstants.JCR_NAME.equals(propertyId) && newRoleName != null) {
                property.setValue(node.getName());
            } else {
                property.setValue(newNodeAdapter.getItemProperty(propertyId).getValue());
            }
        }
        return adapter;
    }

    /**
     * Filter the role's Vaadin item for propertyIds starting with "acl_", put 'em in the return map and remove 'em from the adapter.
     */
    private Map<String, AccessControlList<AccessControlList.Entry>> removeTransientAclProperties(final JcrNodeAdapter roleItem) {
        // make a copy, because we're gonna remove properties while iterating on them further below
        Collection<?> propertyIds = ImmutableList.copyOf(roleItem.getItemPropertyIds());
        Collection<?> aclPropertyIds = Collections2.filter(propertyIds, new Predicate<Object>() {
            @Override
            public boolean apply(Object propertyId) {
                return propertyId instanceof String && ((String) propertyId).startsWith("acl_");
            }
        });

        Map<String, AccessControlList<AccessControlList.Entry>> acls = new HashMap<>();
        for (Object aclPropertyId : aclPropertyIds) {
            AccessControlList<AccessControlList.Entry> acl = (AccessControlList<AccessControlList.Entry>) roleItem.getItemProperty(aclPropertyId).getValue();
            acls.put(aclPropertyId.toString(), acl);
            roleItem.removeItemProperty(aclPropertyId);
        }

        return acls;
    }
}