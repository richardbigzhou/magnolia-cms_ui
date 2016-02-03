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
 * Deletes a group after performing a check that the group is not assigned to any user or another group.
 */
public class DeleteGroupAction extends AbstractDeleteGroupOrRoleAction<DeleteGroupActionDefinition> {

    @Inject
    public DeleteGroupAction(DeleteGroupActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, SecuritySupport securitySupport) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n, securitySupport);
    }

    /**
     * @deprecated since 5.3.6 instead of use {@link #DeleteGroupAction(DeleteGroupActionDefinition, info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter, info.magnolia.commands.CommandsManager, info.magnolia.event.EventBus, info.magnolia.ui.api.context.UiContext, info.magnolia.i18nsystem.SimpleTranslator, info.magnolia.cms.security.SecuritySupport)}
     */
    @Deprecated
    public DeleteGroupAction(DeleteGroupActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n, Security.getSecuritySupport());
    }

    /**
     * @deprecated since 5.2.2 instead of use {@link #DeleteGroupAction(DeleteGroupActionDefinition, info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter, info.magnolia.commands.CommandsManager, info.magnolia.event.EventBus, info.magnolia.ui.api.context.UiContext, info.magnolia.i18nsystem.SimpleTranslator)}
     */
    @Deprecated
    public DeleteGroupAction(DeleteGroupActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, item, Components.getComponent(CommandsManager.class), eventBus, uiContext, i18n);
    }

    @Override
    protected Collection<String> getGroupsOrRoles(User user) {
        return user.getGroups();
    }

    @Override
    protected Collection<String> getGroupsOrRoles(Group group) {
        return group.getGroups();
    }

    @Override
    protected Collection<String> getUsersWithGroupOrRoleToDelete(final String groupOrRole) {
        return getSecuritySupport().getUserManager().getUsersWithGroup(groupOrRole);
    }

    @Override
    protected Collection<String> getGroupsWithGroupOrRoleToDelete(final String groupOrRole) {
        return getSecuritySupport().getGroupManager().getGroupsWithGroup(groupOrRole);
    }

    @Override
    protected String getConfirmationDialogTitle() {
        return getI18n().translate("security.groups.actions.confirmDeleteGroup.confirmationHeader");
    }

    @Override
    protected String getConfirmationDialogBody() {
        return getI18n().translate("security.groups.actions.confirmDeleteGroup.confirmationMessage");
    }

    @Override
    protected String getConfirmationDialogProceedLabel() {
        return getI18n().translate("security.groups.actions.confirmDeleteGroup.proceedLabel");
    }

    @Override
    protected String getConfirmationDialogCancelLabel() {
        return getI18n().translate("security.groups.actions.confirmDeleteGroup.cancelLabel");
    }

    @Override
    protected String getBaseErrorMessage() {
        return getI18n().translate("security.delete.group.isAssignedError");
    }

    @Override
    protected String getVerificationErrorMessage() {
        return getI18n().translate("security.delete.group.cannotVerifyError");
    }
}
