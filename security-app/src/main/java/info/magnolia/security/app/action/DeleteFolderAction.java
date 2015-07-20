/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
import info.magnolia.cms.security.MgnlGroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.framework.action.DeleteAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void execute() throws ActionExecutionException {
        StringBuilder confirmMessage = new StringBuilder("<ul>");
        final Map<String, List<String>> assignedTo = new HashMap<String, List<String>>();
        for (JcrItemAdapter item : getSortedItems(getItemComparator())) {
            final Map<String, List<String>> assignedToItem = new HashMap<String, List<String>>();
            try {
                setCurrentItem(item);
                confirmMessage.append("<li>");
                confirmMessage.append(item.getJcrItem().getName());
                confirmMessage.append("</li>");
                assignedToItem.putAll(getAssignedUsersAndGroupsMap());
            } catch (RepositoryException e) {
                log.error("Cannot get the users/groups the group or role is assigned to.", e);
                throw new ActionExecutionException(getVerificationErrorMessage() + e.getMessage());
            }
            confirmMessage.append(getUserAndGroupListForErrorMessage(assignedToItem));
            assignedTo.putAll(assignedToItem);
        }
        setCurrentItem(null);
        getUiContext().openConfirmation(MessageStyleTypeEnum.WARNING,
                getConfirmationDialogTitle(),
                getConfirmationDialogBody() + (!assignedTo.isEmpty() ? "<br />" + getI18n().translate("security-app.delete.confirmationDialog.body.label", confirmMessage) : ""),
                getConfirmationDialogProceedLabel(),
                getConfirmationDialogCancelLabel(),
                true,
                new ConfirmationCallback() {
                    @Override
                    public void onCancel() {
                        // do nothing
                    }

                    @Override
                    public void onSuccess() {
                        try {
                            DeleteFolderAction.super.execute();
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
    }

    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        final Map<String, List<String>> assignedTo = getAssignedUsersAndGroupsMap();
        if (!assignedTo.isEmpty()) {
            if (getCurrentItem().isNode()) {
                Node folder = (Node) getCurrentItem().getJcrItem();

                NodeUtil.visit(folder, new NodeVisitor() {
                    @Override
                    public void visit(Node node) throws RepositoryException {
                        if (NodeUtil.isNodeType(node, NodeTypes.Role.NAME) || NodeUtil.isNodeType(node, NodeTypes.Group.NAME)) {
                            try {
                                removeDependencies(node);
                            } catch (Exception e) {
                                onError(e);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * @return the list of user- and group-names this item (group or role) is directly assigned to.
     */
    private List<String> getUsersAndGroupsThisItemIsAssignedTo(Node node) throws RepositoryException {
        List<String> assignedTo = new ArrayList<String>();

        final String groupOrRoleName = node.getName();

        final String translatedUserString = getI18n().translate("security.delete.userIdentifier");
        // users
        // TODO MGNLUI-3286: depending on being in GROUPS or ROLES subapp we only have to check those dependencies
        for (String user : securitySupport.getUserManager().getUsersWithGroup(groupOrRoleName)) {
            assignedTo.add(translatedUserString + ":" + user);
        }
        for (String user : securitySupport.getUserManager().getUsersWithRole(groupOrRoleName)) {
            assignedTo.add(translatedUserString + ":" + user);
        }

        // groups
        final String translatedGroupString = getI18n().translate("security.delete.groupIdentifier");
        // TODO MGNLUI-3286: depending on being in GROUPS or ROLES subapp we only have to check those dependencies
        for (String group : securitySupport.getGroupManager().getGroupsWithGroup(groupOrRoleName)) {
            assignedTo.add(translatedGroupString + ":" + group);
        }
        for (String group : securitySupport.getGroupManager().getGroupsWithRole(groupOrRoleName)) {
            assignedTo.add(translatedGroupString + ":" + group);
        }

        return assignedTo;
    }

    protected String getVerificationErrorMessage() {
        return getI18n().translate("security.delete.folder.cannotVerifyError");
    }

    /**
     * @deprecated since 5.3.6 - will be removed without replacement
     */
    @Deprecated
    protected Collection<String> getGroupsOrRoles(User user) {
        List<String> groupsAndRoles = new ArrayList<String>();
        groupsAndRoles.addAll(user.getGroups());
        groupsAndRoles.addAll(user.getRoles());
        return groupsAndRoles;
    }

    /**
     * @deprecated since 5.3.6 - will be removed without replacement
     */
    @Deprecated
    protected Collection<String> getGroupsOrRoles(Group group) {
        List<String> groupsAndRoles = new ArrayList<String>();
        groupsAndRoles.addAll(group.getGroups());
        groupsAndRoles.addAll(group.getRoles());
        return groupsAndRoles;
    }

    protected String getUserAndGroupListForErrorMessage(List<String> usersAndGroups) {
        StringBuilder message = new StringBuilder("<ul>");
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

    protected String getConfirmationDialogTitle() {
        return getI18n().translate("security.folders.actions.confirmDeleteFolder.confirmationHeader");
    }

    protected String getConfirmationDialogBody() {
        return getI18n().translate("security.folders.actions.confirmDeleteFolder.confirmationMessage");
    }

    protected String getConfirmationDialogProceedLabel() {
        return getI18n().translate("security.folders.actions.confirmDeleteFolder.proceedLabel");
    }

    protected String getConfirmationDialogCancelLabel() {
        return getI18n().translate("security.folders.actions.confirmDeleteFolder.cancelLabel");
    }

    protected String getBaseErrorMessage() {
        return getI18n().translate("security.delete.folder.roleOrGroupInfolderStillInUse");
    }

    private void removeDependencies(Node node) throws Exception {
        final String groupOrRoleName = node.getName();
        final UserManager mgnlUserManager = securitySupport.getUserManager();
        //this is needed only for 5.3.x, in 5.4.x use securitySupport.getGroupManager() only
        final MgnlGroupManager mgnlGroupManager = securitySupport.getGroupManager() instanceof MgnlGroupManager ? (MgnlGroupManager) securitySupport.getGroupManager() : null;
        if (NodeUtil.isNodeType(node, NodeTypes.Group.NAME)) {
            // group - user, group - group
            for (String user : securitySupport.getUserManager().getUsersWithGroup(groupOrRoleName)) {
                mgnlUserManager.removeGroup(mgnlUserManager.getUser(user), groupOrRoleName);
            }
            //this is needed only for 5.3.x, in 5.4.x remove null check
            if (mgnlGroupManager != null) {
                for (String group : securitySupport.getGroupManager().getGroupsWithGroup(groupOrRoleName)) {
                    mgnlGroupManager.removeGroup(mgnlGroupManager.getGroup(group), groupOrRoleName);
                }
            }
        } else if (NodeUtil.isNodeType(node, NodeTypes.Role.NAME)) {
            // role - user, role - group
            for (String user : securitySupport.getUserManager().getUsersWithRole(groupOrRoleName)) {
                mgnlUserManager.removeRole(mgnlUserManager.getUser(user), groupOrRoleName);
            }
            //this is needed only for 5.3.x, in 5.4.x remove null check
            if (mgnlGroupManager != null) {
                for (String group : securitySupport.getGroupManager().getGroupsWithRole(groupOrRoleName)) {
                    mgnlGroupManager.removeRole(mgnlGroupManager.getGroup(group), groupOrRoleName);
                }
            }
        }
        if (mgnlGroupManager == null) {
            final Map<String, List<String>> assignedTo = getAssignedUsersAndGroupsMap();
            String errorMessage = getUserAndGroupListForErrorMessage(assignedTo);
            log.error("Cannot get MgnlGroupManager, dependencies in groups cannot be removed. {}", errorMessage);
            throw new ActionExecutionException(getBaseErrorMessage() + errorMessage);
        }
    }

    private Map<String, List<String>> getAssignedUsersAndGroupsMap() throws ActionExecutionException {
        final Map<String, List<String>> assignedTo = new HashMap<String, List<String>>();
        try {
            if (getCurrentItem().isNode()) {
                Node folder = (Node) getCurrentItem().getJcrItem();

                NodeUtil.visit(folder, new NodeVisitor() {
                    @Override
                    public void visit(Node node) throws RepositoryException {
                        if (NodeUtil.isNodeType(node, NodeTypes.Role.NAME) || NodeUtil.isNodeType(node, NodeTypes.Group.NAME)) {
                            List<String> assignedToItem = getUsersAndGroupsThisItemIsAssignedTo(node);
                            if (!assignedToItem.isEmpty()) {
                                assignedTo.put(node.getName(), assignedToItem);
                            }
                        }
                    }
                });
            }
        } catch (RepositoryException e) {
            log.error("Cannot get the users/groups the group or role is assigned to.", e);
            throw new ActionExecutionException(getVerificationErrorMessage() + e.getMessage());
        }
        return assignedTo;
    }
}