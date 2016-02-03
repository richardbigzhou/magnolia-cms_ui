/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.security.app.action;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DeleteAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Action that will only delete a folder if sub nodes are not in use.
 *
 * @see DeleteFolderActionDefinition
 */
public class DeleteFolderAction extends DeleteAction<DeleteFolderActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(DeleteFolderAction.class);

    private final SecuritySupport securitySupport;

    @Inject
    public DeleteFolderAction(DeleteFolderActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, SecuritySupport securitySupport) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n);
        this.securitySupport = securitySupport;
    }

    @Inject
    public DeleteFolderAction(DeleteFolderActionDefinition definition, List<JcrItemAdapter> items, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, SecuritySupport securitySupport) {
        super(definition, items, commandsManager, eventBus, uiContext, i18n);
        this.securitySupport = securitySupport;
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        final List<String> assignedTo = new ArrayList<String>();
        try {
            if (getCurrentItem().isNode()) {
                Node folder = (Node) getCurrentItem().getJcrItem();

                NodeUtil.visit(folder, new NodeVisitor() {
                    @Override
                    public void visit(Node node) throws RepositoryException {
                        if (NodeUtil.isNodeType(node, NodeTypes.Role.NAME) || NodeUtil.isNodeType(node, NodeTypes.Group.NAME)) {
                            assignedTo.addAll(getUsersAndGroupsThisItemIsAssignedTo(node));
                        }
                    }
                });
            }
        } catch (RepositoryException e) {
            log.error("Cannot get the users/groups the group or role is assigned to.", e);
            throw new ActionExecutionException(getVerificationErrorMessage() + e.getMessage());
        }
        if (!assignedTo.isEmpty()) {
            throw new ActionExecutionException(getUserAndGroupListForErrorMessage(assignedTo));
        }
    }

    /**
     * @return the list of user- and group-names this item (group or role) is directly assigned to.
     */
    private List<String> getUsersAndGroupsThisItemIsAssignedTo(Node node) throws RepositoryException {
        List<String> assignedTo = new ArrayList<String>();

        String groupOrRoleName = node.getName();
        // users
        for (User user : securitySupport.getUserManager().getAllUsers()) {
            if (getGroupsOrRoles(user).contains(groupOrRoleName)) {
                assignedTo.add(getI18n().translate("security.delete.userIdentifier", user.getName()) + " " + getI18n().translate("security.delete.folder.uses") + " " + groupOrRoleName);
            }
        }
        // groups
        for (Group group : securitySupport.getGroupManager().getAllGroups()) {
            if (getGroupsOrRoles(group).contains(groupOrRoleName)) {
                assignedTo.add(getI18n().translate("security.delete.groupIdentifier", group.getName()) + " " + getI18n().translate("security.delete.folder.uses") + " " + groupOrRoleName);
            }
        }

        return assignedTo;
    }

    protected String getVerificationErrorMessage() {
        return getI18n().translate("security.delete.folder.cannotVerifyError");
    }

    protected Collection<String> getGroupsOrRoles(User user) {
        List<String> groupsAndRoles = new ArrayList<String>();
        groupsAndRoles.addAll(user.getGroups());
        groupsAndRoles.addAll(user.getRoles());
        return groupsAndRoles;
    }

    protected Collection<String> getGroupsOrRoles(Group group) {
        List<String> groupsAndRoles = new ArrayList<String>();
        groupsAndRoles.addAll(group.getGroups());
        groupsAndRoles.addAll(group.getRoles());
        return groupsAndRoles;
    }

    protected String getUserAndGroupListForErrorMessage(List<String> usersAndGroups) {
        StringBuilder message = new StringBuilder(getI18n().translate("security.delete.folder.roleOrGroupInfolderStillInUse"));
        message.append("<ul>");
        int i = 0;
        for (String name : usersAndGroups) {
            message.append("<li>").append(name).append("</li>");
            if (i > 4) {
                message.append("<li>...</li>");
                break;
            }
            i++;
        }
        message.append("</ul>");
        return message.toString();
    }
}