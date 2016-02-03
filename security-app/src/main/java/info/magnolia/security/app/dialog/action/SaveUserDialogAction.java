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
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Save user dialog action.
 */
public class SaveUserDialogAction extends SaveDialogAction {

    private static final Logger log = LoggerFactory.getLogger(SaveUserDialogAction.class);

    private SecuritySupport securitySupport;

    public SaveUserDialogAction(SaveDialogActionDefinition definition, Item item, EditorValidator validator, EditorCallback callback, SecuritySupport securitySupport) {
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

            UserManager userManager = securitySupport.getUserManager();

            String newUserName = (String) userItem.getItemProperty(ModelConstants.JCR_NAME).getValue();
            String newPassword = (String) userItem.getItemProperty(PROPERTY_PASSWORD).getValue();

            User user;
            Node userNode;
            if (userItem instanceof JcrNewNodeAdapter) {

                // JcrNewNodeAdapter returns the parent JCR item here
                Node parentNode = userItem.getJcrItem();
                String parentPath = parentNode.getPath();

                if ("/".equals(parentPath)) {
                    throw new ActionExecutionException("Users cannot be created directly under root");
                }

                user = userManager.createUser(parentPath, newUserName, newPassword);
                userNode = parentNode.getNode(user.getName());
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

            String enabled = userItem.getItemProperty(PROPERTY_ENABLED).toString();
            userManager.setProperty(user, PROPERTY_ENABLED, enabled);

            String title = userItem.getItemProperty(PROPERTY_TITLE).toString();
            userManager.setProperty(user, PROPERTY_TITLE, title);

            String email = userItem.getItemProperty(PROPERTY_EMAIL).toString();
            userManager.setProperty(user, PROPERTY_EMAIL, email);

            String language = userItem.getItemProperty(PROPERTY_LANGUAGE).toString();
            userManager.setProperty(user, PROPERTY_LANGUAGE, language);

            final Collection<String> groups = (Collection<String>) userItem.getItemProperty(NODE_GROUPS).getValue();
            log.debug("Assigning user the following groups [{}]", groups);
            storeCollectionAsNodeWithProperties(userNode, NODE_GROUPS, groups);

            final Collection<String> roles = (Collection<String>) userItem.getItemProperty(NODE_ROLES).getValue();
            log.debug("Assigning user the following roles [{}]", roles);
            storeCollectionAsNodeWithProperties(userNode, NODE_ROLES, roles);

            userNode.getSession().save();

        } catch (final RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }

    private void storeCollectionAsNodeWithProperties(Node parentNode, String name, Collection<String> values) throws RepositoryException {
        try {
            // create sub node (or get it, if it already exists)
            Node node = NodeUtil.createPath(parentNode, name, NodeTypes.ContentNode.NAME);

            // remove all previous properties
            PropertyIterator pi = node.getProperties();
            while (pi.hasNext()) {
                javax.jcr.Property p = pi.nextProperty();
                if (!p.getName().startsWith(NodeTypes.JCR_PREFIX)) {
                    p.remove();
                }
            }

            int i = 0;
            for (String value : values) {
                PropertyUtil.setProperty(node, String.valueOf(i), value.trim());
                i++;
            }
        } catch (RepositoryException ex) {
            throw new RepositoryException("Error saving assigned " + name + " of the [" + parentNode.getName() + "] user.", ex);
        }
    }
}
