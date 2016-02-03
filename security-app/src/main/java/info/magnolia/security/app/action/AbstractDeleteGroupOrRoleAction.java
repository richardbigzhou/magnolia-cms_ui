/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DeleteItemAction;
import info.magnolia.ui.framework.action.DeleteItemActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract common supertype for {@link DeleteGroupAction} and {@link DeleteRoleAction}.
 *
 * @param <D> the action definition type, must extend the {@link DeleteItemActionDefinition} class.
 */
public abstract class AbstractDeleteGroupOrRoleAction<D extends DeleteItemActionDefinition> extends DeleteItemAction {

    private static final Logger log = LoggerFactory.getLogger(AbstractDeleteGroupOrRoleAction.class);

    private final JcrItemAdapter item;
    private final UiContext uiContext;



    private final SimpleTranslator i18n;

    @Inject
    public AbstractDeleteGroupOrRoleAction(D definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, eventBus, uiContext, i18n);
        this.item = item;
        this.uiContext = uiContext;
        this.i18n = i18n;
    }

    protected SimpleTranslator getI18n() {
        return i18n;
    }

    public JcrItemAdapter getItem() {
        return this.item;
    }

    /**
     * @return the base for the error message shown to the user in case the item is already assigned; the list of users/groups the item is assigned to is added;
     */
    protected abstract String getBaseErrorMessage();

    /**
     * @return the message to be shown to the user in case the verification ({@link #getUsersAndGroupsThisItemIsAssignedTo()} method) fails.
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

    @Override
    protected void executeAfterConfirmation() {
        List<String> assignedTo;
        try {
            assignedTo = getUsersAndGroupsThisItemIsAssignedTo();
        } catch (RepositoryException e) {
            log.error("Cannot get the users/groups the group or role is assigned to.", e);
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, getVerificationErrorMessage() + e.getMessage());
            return;
        }
        if (assignedTo == null || assignedTo.isEmpty()) {
            super.executeAfterConfirmation();
        } else {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, getBaseErrorMessage() + getUserAndGroupListForErrorMessage(assignedTo));
        }
    }

    /**
     * @return the list of user- and group-names this item (group or role) is directly assigned to.
     */
    private List<String> getUsersAndGroupsThisItemIsAssignedTo() throws RepositoryException {
        List<String> assignedTo = new ArrayList<String>();

        String groupName = getItem().getJcrItem().getName();
        // users
        for (User user : Security.getUserManager().getAllUsers()) {
            if (getGroupsOrRoles(user).contains(groupName)) {
                assignedTo.add(i18n.translate("security.delete.userIdentifier", user.getName()));
            }
        }
        // groups
        for (Group group : Security.getGroupManager().getAllGroups()) {
            if (getGroupsOrRoles(group).contains(groupName)) {
                assignedTo.add(i18n.translate("security.delete.groupIdentifier", group.getName()));
            }
        }

        return assignedTo;
    }

    private static String getUserAndGroupListForErrorMessage(List<String> usersAndGroups) {
        StringBuilder message = new StringBuilder("<ul>");
        for (String name : usersAndGroups) {
            message.append("<li>").append(name).append("</li>");
        }
        message.append("</ul>");
        return message.toString();
    }

}
