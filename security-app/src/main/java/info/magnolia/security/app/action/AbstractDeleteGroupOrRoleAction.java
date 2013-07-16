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

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DeleteItemAction;
import info.magnolia.ui.framework.action.DeleteItemActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;

/**
 * Abstract common supertype for {@link DeleteGroupAction} and {@link DeleteRoleAction}.
 * 
 * @param <D> the action definition type, must extend the {@link DeleteItemActionDefinition} class.
 */
public abstract class AbstractDeleteGroupOrRoleAction<D extends DeleteItemActionDefinition> extends DeleteItemAction {

    public static final String PREFIX_USER = "user:";
    public static final String PREFIX_GROUP = "group:";

    private final JcrItemAdapter item;
    private final UiContext uiContext;

    @Inject
    public AbstractDeleteGroupOrRoleAction(D definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, item, eventBus, uiContext);
        this.item = item;
        this.uiContext = uiContext;
    }

    public JcrItemAdapter getItem() {
        return this.item;
    }

    public UiContext getUiContext() {
        return this.uiContext;
    }

    /**
     * @return the list of user- and group-names this item (group or role) is directly assigned to.
     */
    protected abstract List<String> getUsersAndGroupsThisItemIsAssignedTo() throws RepositoryException;

    /**
     * @return the message to write to the log if the {@link #getUsersAndGroupsThisItemIsAssignedTo()} fails.
     */
    protected abstract String getLogMessage();

    /**
     * @return the base for the error message shown to the user in case the item is already assigned; the list of users/groups the item is assigned to is added;
     */
    protected abstract String getBaseErrorMessage();

    /**
     * @return the message to be shown to the user in case the verification ({@link #getUsersAndGroupsThisItemIsAssignedTo()} method) fails.
     */
    protected abstract String getVerificationErrorMessage();

    /**
     * @return the logger of the actual implementation.
     */
    protected abstract Logger getLog();

    @Override
    protected void executeAfterConfirmation() {
        List<String> assignedTo;
        try {
            assignedTo = getUsersAndGroupsThisItemIsAssignedTo();
        } catch (RepositoryException e) {
            getLog().error(getLogMessage(), e);
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, getVerificationErrorMessage() + e.getMessage());
            return;
        }
        if (assignedTo == null || assignedTo.isEmpty()) {
            super.executeAfterConfirmation();
        } else {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, getBaseErrorMessage() + getUserAndGroupListForErrorMessage(assignedTo));
        }
    }

    private static String getUserAndGroupListForErrorMessage(List<String> usersAndGroups) {
        StringBuffer message = new StringBuffer("<ul>");
        for (String name : usersAndGroups) {
            message.append("<li>").append(name).append("</li>");
        }
        message.append("</ul>");
        return message.toString();
    }

}
