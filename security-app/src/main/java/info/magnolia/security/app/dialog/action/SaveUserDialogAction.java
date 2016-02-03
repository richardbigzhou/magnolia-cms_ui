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

import static info.magnolia.cms.security.MgnlUserManager.*;
import static info.magnolia.cms.security.SecurityConstants.*;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SilentSessionOp;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Save user dialog action.
 */
public class SaveUserDialogAction extends SaveDialogAction<SaveUserDialogActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveUserDialogAction.class);

    private SecuritySupport securitySupport;
    private final List<String> protectedProperties = Arrays.asList(PROPERTY_PASSWORD, "name", NODE_GROUPS, NODE_ROLES);

    public SaveUserDialogAction(SaveUserDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback, SecuritySupport securitySupport) {
        super(definition, item, validator, callback);
        this.securitySupport = securitySupport;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {

            final JcrNodeAdapter nodeAdapter = (JcrNodeAdapter) item;
            createOrUpdateUser(nodeAdapter);
            callback.onSuccess(getDefinition().getName());

        } else {
            // validation errors are displayed in the UI.
        }
    }

    private void createOrUpdateUser(final JcrNodeAdapter userItem) throws ActionExecutionException {
        try {
            String userManagerRealm = getDefinition().getUserManagerRealm();
            if (StringUtils.isBlank(userManagerRealm)){
                log.debug("userManagerRealm property is not defined -> will try to get realm from node path");
                userManagerRealm = resolveUserManagerRealm(userItem);
            }
            UserManager userManager = securitySupport.getUserManager(userManagerRealm);
            if (userManager == null){
                throw new ActionExecutionException("User cannot be created. No user manager with realm name " + userManagerRealm + " is defined.");
            }

            String newUserName = (String) userItem.getItemProperty(ModelConstants.JCR_NAME).getValue();
            String newPassword = (String) userItem.getItemProperty(PROPERTY_PASSWORD).getValue();

            User user;
            Session session = userItem.getJcrItem().getSession();
            final Node userNode;
            if (userItem instanceof JcrNewNodeAdapter) {

                // JcrNewNodeAdapter returns the parent JCR item here
                Node parentNode = userItem.getJcrItem();
                String parentPath = parentNode.getPath();

                if ("/".equals(parentPath)) {
                    throw new ActionExecutionException("Users cannot be created directly under root");
                }

                // Make sure this user is allowed to add a user here, the user manager would happily do it and then we'd fail to read the node
                parentNode.getSession().checkPermission(parentNode.getPath(), Session.ACTION_ADD_NODE);

                user = userManager.createUser(parentPath, newUserName, newPassword);
                userNode = session.getNodeByIdentifier(user.getIdentifier());
                // workaround that updates item id of the userItem so we can use it in OpenCreateDialogAction to fire ContentChangedEvent
                try {
                    Field f = userItem.getClass().getDeclaredField("appliedChanges");
                    f.setAccessible(true);
                    f.setBoolean(userItem, true);
                    f.setAccessible(false);
                    userItem.setItemId(new JcrNodeItemId(userNode.getIdentifier(), RepositoryConstants.USERS));
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    log.warn("Unable to set new JcrItemId for adapter {}", userItem, e);
                }
            } else {
                userNode = userItem.getJcrItem();
                String existingUserName = userNode.getName();
                user = userManager.getUser(existingUserName);

                if (!StringUtils.equals(existingUserName, newUserName)) {
                    String pathBefore = userNode.getPath();
                    NodeUtil.renameNode(userNode, newUserName);
                    userNode.setProperty("name", newUserName);
                    UsersWorkspaceUtil.updateAcls(userNode, pathBefore);
                }

                String existingPasswordHash = user.getProperty(PROPERTY_PASSWORD);
                if (!StringUtils.equals(newPassword, existingPasswordHash)) {
                    userManager.setProperty(user, PROPERTY_PASSWORD, newPassword);
                }
            }

            final Collection<String> groups = resolveItemsNamesFromIdentifiers((Collection<String>) userItem.getItemProperty(NODE_GROUPS).getValue(), RepositoryConstants.USER_GROUPS);
            log.debug("Assigning user the following groups [{}]", groups);
            storeGroupsCollection(userManager, user, groups);

            final Collection<String> roles = resolveItemsNamesFromIdentifiers((Collection<String>) userItem.getItemProperty(NODE_ROLES).getValue(), RepositoryConstants.USER_ROLES);
            log.debug("Assigning user the following roles [{}]", roles);
            storeRolesCollection(userManager, user, roles);

            Collection<?> userProperties = userItem.getItemPropertyIds();
            ValueFactory valueFactory = session.getValueFactory();
            for (Object propertyName : userProperties) {
                if (!protectedProperties.contains(propertyName)) {
                    Value propertyValue = PropertyUtil.createValue(userItem.getItemProperty(propertyName).getValue(), valueFactory);
                    userManager.setProperty(user, propertyName.toString(), propertyValue);
                }
            }

            userItem.updateChildren(userNode);

            session.save();

        } catch (final RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    private String resolveUserManagerRealm(final JcrNodeAdapter userItem) throws RepositoryException{
        String userPath = userItem.getJcrItem().getPath();
        if (userItem instanceof JcrNewNodeAdapter && !"/".equals(userPath)) {
            //parent JCR item is returned so we need enclose path with "/" to handle correctly in case when user is placed directly under realm root
            userPath += "/";
        }
        return StringUtils.substringBetween(userPath, "/");
    }

    private void storeGroupsCollection(UserManager userManager, User user, Collection<String> newGroups){
        Collection<String> oldGroups = new ArrayList<String>();
        for (String group : user.getGroups()) {
            oldGroups.add(group);
        }
        for(String newGroup : newGroups) {
            userManager.addGroup(user, newGroup);
            oldGroups.remove(newGroup);
        }
        for(String oldGroup : oldGroups) {
            userManager.removeGroup(user, oldGroup);
        }
    }

    private void storeRolesCollection(UserManager userManager, User user, Collection<String> newRoles){
        Collection<String> oldRoles = new ArrayList<String>();
        for (String role : user.getRoles()) {
            oldRoles.add(role);
        }
        for(String newRole : newRoles) {
            userManager.addRole(user, newRole);
            oldRoles.remove(newRole);
        }
        for(String oldRole : oldRoles) {
            userManager.removeRole(user, oldRole);
        }
    }

    private Collection<String> resolveItemsNamesFromIdentifiers(Collection<String> itemsIdentifiers, String repository){
        final Collection<String> itemsNames = new ArrayList<String>();
        for (final String itemIdentifier : itemsIdentifiers) {
            MgnlContext.doInSystemContext(new SilentSessionOp<Void>(repository) {

                @Override
                public Void doExec(Session session) {
                    try {
                        final String itemName =  session.getNodeByIdentifier(itemIdentifier).getName();
                        itemsNames.add(itemName);
                    } catch (RepositoryException e) {
                        log.error("Can't resolve group/role with uuid: " + itemIdentifier);
                        log.debug(e.getMessage());
                    }
                    return null;
                }
            });
        }
        return itemsNames;
    }

}
