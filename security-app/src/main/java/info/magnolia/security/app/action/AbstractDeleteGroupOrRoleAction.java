/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.framework.action.DeleteAction;
import info.magnolia.ui.framework.action.DeleteActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract common supertype for {@link DeleteGroupAction} and {@link DeleteRoleAction}.
 *
 * @param <D> the action definition type, must extend the {@link DeleteActionDefinition} class.
 */
public abstract class AbstractDeleteGroupOrRoleAction<D extends DeleteActionDefinition> extends DeleteAction {

    private static final Logger log = LoggerFactory.getLogger(AbstractDeleteGroupOrRoleAction.class);

    private final JcrItemAdapter item;
    private final SecuritySupport securitySupport;

    @Inject
    public AbstractDeleteGroupOrRoleAction(D definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, SecuritySupport securitySupport) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n);
        this.item = item;
        this.securitySupport = securitySupport;
    }

    /**
     * @deprecated since 5.3.6 instead of use {@link #AbstractDeleteGroupOrRoleAction(info.magnolia.ui.framework.action.DeleteActionDefinition, info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter, info.magnolia.commands.CommandsManager, info.magnolia.event.EventBus, info.magnolia.ui.api.context.UiContext, info.magnolia.i18nsystem.SimpleTranslator, info.magnolia.cms.security.SecuritySupport)}
     */
    @Deprecated
    public AbstractDeleteGroupOrRoleAction(D definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, item, commandsManager, eventBus, uiContext, i18n, Security.getSecuritySupport());
    }

    /**
     * @deprecated since 5.2.2 instead of use {@link #AbstractDeleteGroupOrRoleAction(info.magnolia.ui.framework.action.DeleteActionDefinition, info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter, info.magnolia.commands.CommandsManager, info.magnolia.event.EventBus, info.magnolia.ui.api.context.UiContext, info.magnolia.i18nsystem.SimpleTranslator, info.magnolia.cms.security.SecuritySupport)}
     */
    @Deprecated
    public AbstractDeleteGroupOrRoleAction(D definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, item, Components.getComponent(CommandsManager.class), eventBus, uiContext, i18n, Security.getSecuritySupport());
    }

    /**
     * @deprecated since 5.2.2 instead of use {@link #getCurrentItem()}
     */
    @Deprecated
    public JcrItemAdapter getItem() {
        return this.item;
    }

    /**
     * @return the base for the error message shown to the user in case the item is already assigned; the list of users/groups the item is assigned to is added;
     */
    protected abstract String getBaseErrorMessage();

    /**
     * @return the message to be shown to the user in case the verification ({@link #getAssignedUsersAndGroups()} method) fails.
     */
    protected abstract String getVerificationErrorMessage();

    /**
     * Gets a collection of group or role names (according to where it is implemented) assigned to the user.
     */
    protected abstract Collection<String> getGroupsOrRoles(User user);

    /**
     * Gets a collection of group or role names (according to where it is implemented) assigned to the group.
     */
    protected abstract Collection<String> getGroupsOrRoles(Group group);

    /**
     * @return Collection of users that have the group or role to delete assigned to
     */
    protected abstract Collection<String> getUsersWithGroupOrRoleToDelete(String groupOrRoleName);

    /**
     * @return Collection of groups that have the group or role to delete assigned to
     */
    protected abstract Collection<String> getGroupsWithGroupOrRoleToDelete(String groupOrRoleName);

    /**
     * @deprecated since 5.2.2 instead of use {@link #onPreExecute()}
     */
    @Deprecated
    protected void executeAfterConfirmation() {
        log.warn("This method was deprecated. Use #onPreExecute() method instead.");
    }

    @Override
    protected void onPreExecute() throws Exception {

        List<String> assignedTo;
        try {
            assignedTo = getAssignedUsersAndGroups();
        } catch (RepositoryException e) {
            throw new RepositoryException("Cannot get the users/groups the group or role is assigned to.", e);
        }
        if (assignedTo != null && !assignedTo.isEmpty()) {
            removeDependencies();
        }
        super.onPreExecute();
    }

    protected abstract String getConfirmationDialogTitle ();

    protected abstract String getConfirmationDialogBody ();

    protected abstract String getConfirmationDialogProceedLabel ();

    protected abstract String getConfirmationDialogCancelLabel ();


    @Override
    public void execute() throws ActionExecutionException {
        try {
            executeOnConfirmation();
        } catch (RepositoryException e) {
            throw new ActionExecutionException(getVerificationErrorMessage() + e.getMessage());
        }
    }

    private String getConfirmationDialogStatement() throws RepositoryException {
        final List<String> assignedTo = new ArrayList<String>();
        StringBuilder confirmMessage = new StringBuilder("<ul>");
        for (JcrItemAdapter item : (List<JcrItemAdapter>) getSortedItems(getItemComparator())) {
            final List<String> assignedToItem = new ArrayList<String>();
            try {
                List<String> dependenciesList = getAssignedUsersAndGroups(item.getJcrItem().getName());
                if (!dependenciesList.isEmpty()) {
                    confirmMessage.append("<li>");
                    confirmMessage.append(item.getJcrItem().getName());
                    confirmMessage.append("</li>");
                    assignedToItem.addAll(dependenciesList);
                }
            } catch (RepositoryException e) {
                throw new RepositoryException("Cannot get the users/groups the group or role is assigned to.", e);
            }
            confirmMessage.append(formatUserAndGroupList(assignedToItem));
            assignedTo.addAll(assignedToItem);
        }
        confirmMessage.append("</ul>");
        return !assignedTo.isEmpty() ? confirmMessage.toString() : "";
    }

    private void executeOnConfirmation() throws RepositoryException {
        final String message = getConfirmationDialogStatement();
        getUiContext().openConfirmation(MessageStyleTypeEnum.WARNING,
                getConfirmationDialogTitle(),
                (!message.isEmpty() ? "<br />" + getI18n().translate("security-app.delete.confirmationDialog.body.label", message) + "<br />" : "") + getConfirmationDialogBody(),
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
                            AbstractDeleteGroupOrRoleAction.super.execute();
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
    }

    /**
     * @return the list of user- and group-names this item (group or role) is directly assigned to.
     */
    private List<String> getAssignedUsersAndGroups() throws RepositoryException {
        return getAssignedUsersAndGroups(getCurrentItem().getJcrItem().getName());
    }

    private List<String> getAssignedUsersAndGroups(final String itemName) throws RepositoryException {
        List<String> assignedTo = new ArrayList<String>();

        final String translatedUserString = getI18n().translate("security.delete.userIdentifier");
        // users
        for (String user : getUsersWithGroupOrRoleToDelete(itemName)) {
            assignedTo.add(translatedUserString + ":" + user);
        }
        // groups
        final String translatedGroupString = getI18n().translate("security.delete.groupIdentifier");
        for (String group : getGroupsWithGroupOrRoleToDelete(itemName)) {
            assignedTo.add(translatedGroupString + ":" + group);
        }

        return assignedTo;
    }

    private static String formatUserAndGroupList(Collection<String> usersAndGroups) {
        StringBuilder message = new StringBuilder("<ul>");
        for (String name : usersAndGroups) {
            message.append("<li>").append(name).append("</li>");
        }
        message.append("</ul>");
        return message.toString();
    }

    private void removeDependencies() throws RepositoryException, ActionExecutionException {
        final String groupOrRoleName = getCurrentItem().getJcrItem().getName();
        final UserManager userManager = securitySupport.getUserManager();
        final GroupManager groupManager = securitySupport.getGroupManager();
        // users
        for (String user : getUsersWithGroupOrRoleToDelete(groupOrRoleName)) {
            if (getCurrentItem().isNode()) {
                if (NodeUtil.isNodeType((Node) getCurrentItem().getJcrItem(), NodeTypes.Group.NAME)) {
                    userManager.removeGroup(userManager.getUser(user), groupOrRoleName);
                }
                if (NodeUtil.isNodeType((Node) getCurrentItem().getJcrItem(), NodeTypes.Role.NAME)) {
                    userManager.removeRole(userManager.getUser(user), groupOrRoleName);
                }
            }
        }
        // groups
        for (String group : getGroupsWithGroupOrRoleToDelete(groupOrRoleName)) {
            if (getCurrentItem().isNode()) {
                if (NodeUtil.isNodeType((Node) getCurrentItem().getJcrItem(), NodeTypes.Group.NAME)) {
                    groupManager.removeGroup(groupManager.getGroup(group), groupOrRoleName);
                }
                if (NodeUtil.isNodeType((Node) getCurrentItem().getJcrItem(), NodeTypes.Role.NAME)) {
                    groupManager.removeRole(groupManager.getGroup(group), groupOrRoleName);
                }
            }
        }
    }

    protected SecuritySupport getSecuritySupport() {
        return securitySupport;
    }
}
