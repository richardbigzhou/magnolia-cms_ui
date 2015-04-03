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
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Deletes a role after performing a check that the role is not assigned to any user or group.
 */
public class DeleteRoleAction extends AbstractDeleteGroupOrRoleAction<DeleteRoleActionDefinition> {

    @Inject
    public DeleteRoleAction(DeleteRoleActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, SecuritySupport securitySupport) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n, securitySupport);
    }

    /**
     * @deprecated since 5.3.6 instead of use {@link #DeleteRoleAction(DeleteRoleActionDefinition, info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter, info.magnolia.commands.CommandsManager, info.magnolia.event.EventBus, info.magnolia.ui.api.context.UiContext, info.magnolia.i18nsystem.SimpleTranslator, info.magnolia.cms.security.SecuritySupport)}
     */
    @Deprecated
    public DeleteRoleAction(DeleteRoleActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n, Security.getSecuritySupport());
    }

    /**
     * @deprecated since 5.2.2 instead of use {@link #DeleteRoleAction(DeleteRoleActionDefinition, info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter, info.magnolia.commands.CommandsManager, info.magnolia.event.EventBus, info.magnolia.ui.api.context.UiContext, info.magnolia.i18nsystem.SimpleTranslator)}
     */
    @Deprecated
    public DeleteRoleAction(DeleteRoleActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, item, Components.getComponent(CommandsManager.class), eventBus, uiContext, i18n);
    }

    @Override
    protected Collection<String> getGroupsOrRoles(User user) {
        return user.getRoles();
    }

    @Override
    protected Collection<String> getGroupsOrRoles(Group group) {
        return group.getRoles();
    }

    @Override
    protected Collection<String> getUsersWithGroupOrRoleToDelete(final String groupOrRole) {
        return getSecuritySupport().getUserManager().getUsersWithRole(groupOrRole);
    }

    @Override
    protected Collection<String> getGroupsWithGroupOrRoleToDelete(final String groupOrRole) {
        return getSecuritySupport().getGroupManager().getGroupsWithRole(groupOrRole);
    }

    @Override
    protected String getBaseErrorMessage() {
        return getI18n().translate("security.delete.role.isAssignedError");
    }

    @Override
    protected String getVerificationErrorMessage() {
        return getI18n().translate("security.delete.role.cannotVerifyError");
    }
}
