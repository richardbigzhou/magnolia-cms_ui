/**
 * This file Copyright (c) 2013 Magnolia International
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
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DeleteItemAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a group after performing a check that the group is not assignet to any user or another group.
 */
public class DeleteGroupAction extends DeleteItemAction {

    // TODO MGNLUI-1826 replace with a message bundle key
    private static final String ERROR_MESSAGE_GROUP_IS_ASSIGNED = "Cannot delete the group. It is already assigned to the following users/groups:<br />";
    private static final String ERROR_MESSAGE_CANNOT_VERIFY = "Cannot verify that the group you want to delete is not assigned: ";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UiContext uiContext;
    private final JcrItemAdapter item;

    @Inject
    public DeleteGroupAction(DeleteGroupActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, item, eventBus, uiContext);
        this.item = item;
        this.uiContext = uiContext;
    }

    @Override
    protected void executeAfterConfirmation() {
        List<String> assignedTo;
        try {
            assignedTo = getUsersAndGroupsThisGroupIsAssignedTo();
        } catch (RepositoryException e) {
            log.error("Cannot verify the users/groups the group is assigned to.", e);
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, ERROR_MESSAGE_CANNOT_VERIFY + e.getMessage());
            return;
        }
        if (assignedTo == null || assignedTo.isEmpty()) {
            super.executeAfterConfirmation();
        } else {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, getErrorMessage(assignedTo));
        }
    }

    /**
     * Creates an error message that the group is already assigned to users/groups in the <code>assignedTo</code> list.
     */
    private String getErrorMessage(List<String> assignedTo) {
        if (assignedTo == null || assignedTo.isEmpty()) {
            log.error("Trying to generate an error message, but the assignedTo list is null/empty.");
            return null;
        }
        String message = ERROR_MESSAGE_GROUP_IS_ASSIGNED;
        message += "<ul>";
        for (String name : assignedTo) {
            message += "<li>" + name + "</li>";
        }
        message += "</ul>";
        return message;
    }

    private List<String> getUsersAndGroupsThisGroupIsAssignedTo() throws RepositoryException {
        List<String> assignedTo = new ArrayList<String>();

        String groupName = item.getJcrItem().getName();
        // users
        for (User user : Security.getUserManager().getAllUsers()) {
            if (user.getGroups().contains(groupName)) {
                assignedTo.add("user:" + user.getName());
            }
        }
        // groups
        for (Group group : Security.getGroupManager().getAllGroups()) {
            if (group.getGroups().contains(groupName)) {
                assignedTo.add("group:" + group.getName());
            }
        }

        return assignedTo;
    }

}
