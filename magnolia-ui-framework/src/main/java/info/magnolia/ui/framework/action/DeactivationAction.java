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
package info.magnolia.ui.framework.action;

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

/**
 * UI action that allows to deactivate pages (nodes).
 *
 * @see DeactivationActionDefinition
 */
public class DeactivationAction extends AbstractCommandAction<DeactivationActionDefinition> {

    private final AbstractJcrNodeAdapter jcrNodeAdapter;
    private final EventBus eventBus;
    private final UiContext uiContext;
    private ModuleRegistry moduleRegistry;

    @Inject
    public DeactivationAction(final DeactivationActionDefinition definition, final JcrItemAdapter item, final CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, SubAppContext uiContext, ModuleRegistry moduleRegistry) {
        super(definition, item, commandsManager, uiContext);
        this.jcrNodeAdapter = (AbstractJcrNodeAdapter) item;
        this.eventBus = eventBus;
        this.uiContext = uiContext;
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    protected void onError(Exception e) {
        uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, MessagesManager.get(isWorkflowInstalled() ? getDefinition().getWorkflowErrorMessage() : getDefinition().getErrorMessage()));
    }

    @Override
    protected void onPostExecute() throws Exception {
        eventBus.fireEvent(new ContentChangedEvent(jcrNodeAdapter.getWorkspace(), jcrNodeAdapter.getItemId()));

        // Display a notification
        Context context = MgnlContext.getInstance();
        boolean result = (Boolean) context.getAttribute(COMMAND_RESULT);
        String message;
        MessageStyleTypeEnum messageStyleType;
        if (!result) {
            message = MessagesManager.get(isWorkflowInstalled() ? getDefinition().getWorkflowSuccessMessage() : getDefinition().getSuccessMessage());
            messageStyleType = MessageStyleTypeEnum.INFO;
        } else {
            message = MessagesManager.get(isWorkflowInstalled() ? getDefinition().getWorkflowFailureMessage() : getDefinition().getFailureMessage());
            messageStyleType = MessageStyleTypeEnum.ERROR;
        }

        if (StringUtils.isNotBlank(message)) {
            uiContext.openNotification(messageStyleType, true, message);
        }
    }

    private boolean isWorkflowInstalled() {
        return moduleRegistry.isModuleRegistered("workflow");
    }
}
