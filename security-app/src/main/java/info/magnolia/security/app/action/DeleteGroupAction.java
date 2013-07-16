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
import info.magnolia.cms.security.User;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a group after performing a check that the group is not assignet to any user or another group.
 */
public class DeleteGroupAction extends AbstractDeleteGroupOrRoleAction<DeleteGroupActionDefinition> {

    // TODO MGNLUI-1826 replace with a message bundle key
    private static final String ERROR_MESSAGE_GROUP_IS_ASSIGNED = "Cannot delete the group. It is already assigned to the following users/groups:<br />";
    private static final String ERROR_MESSAGE_CANNOT_VERIFY = "Cannot verify that the group you want to delete is not assigned: ";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public DeleteGroupAction(DeleteGroupActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, item, eventBus, uiContext);
    }

    @Override
    protected Collection<String> getGroupsOrRoles(Object userOrGroup) throws IllegalArgumentException {
        if (userOrGroup instanceof User) {
            return ((User) userOrGroup).getGroups();
        }
        if (userOrGroup instanceof Group) {
            return ((Group) userOrGroup).getGroups();
        }
        throw new IllegalArgumentException("The userOrGroup parameter must be of either info.magnolia.cms.security.User or info.magnolia.cms.security.Group type.");
    }

    @Override
    protected String getBaseErrorMessage() {
        return ERROR_MESSAGE_GROUP_IS_ASSIGNED;
    }

    @Override
    protected String getVerificationErrorMessage() {
        return ERROR_MESSAGE_CANNOT_VERIFY;
    }

    @Override
    protected Logger getLog() {
        return this.log;
    }
}
