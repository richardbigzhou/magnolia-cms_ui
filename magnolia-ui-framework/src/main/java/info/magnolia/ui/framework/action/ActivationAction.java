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

import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;

import org.apache.commons.lang.StringUtils;

/**
 * UI action that allows to activate a single page (node) or recursively with all its sub-nodes depending on the value of {@link ActivationActionDefinition#isRecursive()}.
 *
 * @param <D> {@link ActivationActionDefinition}.
 */
public class ActivationAction<D extends ActivationActionDefinition> extends AbstractCommandAction<D> {

    private final JcrItemAdapter jcrItemAdapter;

    private final EventBus admincentralEventBus;
    private final UiContext uiContext;


    @Inject
    public ActivationAction(final D definition, final JcrItemAdapter item, final CommandsManager commandsManager,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, SubAppContext uiContext, final SimpleTranslator i18n) {
        super(definition, item, commandsManager, uiContext, i18n);
        this.jcrItemAdapter = item;
        this.admincentralEventBus = admincentralEventBus;
        this.uiContext = uiContext;
    }

    @Override
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.put(Context.ATTRIBUTE_RECURSIVE, getDefinition().isRecursive());

        return params;
    }

    @Override
    protected void onError(Exception e) {
        String errorMessage = null;
        if (e.getCause() != null && e.getCause() instanceof ExchangeException) {
            errorMessage = e.getCause().getLocalizedMessage();
            errorMessage = StringUtils.substring(errorMessage, StringUtils.indexOf(errorMessage, "error detected:"));
        } else {
            errorMessage = getErrorMessage();
        }
        uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, errorMessage);
    }

    @Override
    protected void onPostExecute() throws Exception {
        admincentralEventBus.fireEvent(new ContentChangedEvent(jcrItemAdapter.getWorkspace(), jcrItemAdapter.getItemId()));

        Context context = MgnlContext.getInstance();
        // yes, this is inverted, because a chain returns false when it is finished.
        boolean success = !(Boolean) context.getAttribute(COMMAND_RESULT);
        String message = getMessage(success);
        MessageStyleTypeEnum messageStyleType = success ? MessageStyleTypeEnum.INFO : MessageStyleTypeEnum.ERROR;

        if (StringUtils.isNotBlank(message)) {
            uiContext.openNotification(messageStyleType, true, message);
        }
    }

    protected String getMessage(boolean success) {
        return success ? getDefinition().getSuccessMessage() : getDefinition().getFailureMessage();
    }

    protected String getErrorMessage() {
        return getDefinition().getErrorMessage();
    }
}
