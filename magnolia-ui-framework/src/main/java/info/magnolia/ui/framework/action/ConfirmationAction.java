/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.util.MessageFormatterUtils;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Configurable confirmation action. Can be used to intercept the actual action with user feedback.
 * Allows configuration of a success action and a cancel action.
 * 
 * @param <D> {@link ConfirmationActionDefinition}.
 */
public class ConfirmationAction<D extends ConfirmationActionDefinition> extends AbstractAction<D> {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationAction.class);

    private final List<? extends Item> items;
    private final UiContext uiContext;
    private final ActionExecutor actionExecutor;
    private final SimpleTranslator i18n;
    private ContentConnector contentConnector;

    public ConfirmationAction(D definition, Item item, UiContext uiContext, ActionExecutor actionExecutor, SimpleTranslator i18n, ContentConnector contentConnector) {
        super(definition);
        this.contentConnector = contentConnector;
        this.items = Arrays.asList(item);
        this.uiContext = uiContext;
        this.actionExecutor = actionExecutor;
        this.i18n = i18n;
    }

    @Inject
    public ConfirmationAction(D definition, List<? extends Item> items, UiContext uiContext, ActionExecutor actionExecutor, SimpleTranslator i18n, ContentConnector contentConnector) {
        super(definition);
        this.items = items;
        this.uiContext = uiContext;
        this.actionExecutor = actionExecutor;
        this.i18n = i18n;
        this.contentConnector = contentConnector;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            uiContext.openConfirmation(
                    MessageStyleTypeEnum.WARNING, getConfirmationHeader(), getConfirmationMessage(), getDefinition().getProceedLabel(), getDefinition().getCancelLabel(), getDefinition().isDefaultCancel(),
                    new ConfirmationCallback() {
                        @Override
                        public void onSuccess() {
                            if (getDefinition().getSuccessActionName() != null) {
                                try {
                                    if (items.size() == 1) {
                                        actionExecutor.execute(getDefinition().getSuccessActionName(), items.get(0));
                                    } else {
                                        actionExecutor.execute(getDefinition().getSuccessActionName(), items);
                                    }
                                } catch (ActionExecutionException e) {
                                    onError(e);
                                }
                            }
                        }

                        @Override
                        public void onCancel() {
                            if (getDefinition().getCancelActionName() != null) {
                                try {
                                    if (items.size() == 1) {
                                        actionExecutor.execute(getDefinition().getCancelActionName(), items.get(0));
                                    } else {
                                        actionExecutor.execute(getDefinition().getCancelActionName(), items);
                                    }
                                } catch (ActionExecutionException e) {
                                    onError(e);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            throw new ActionExecutionException(e);
        }
    }

    protected String getConfirmationHeader() throws Exception {
        return formatMessage(getDefinition().getConfirmationHeader());
    }

    protected String getConfirmationMessage() throws Exception {
        return formatMessage(getDefinition().getConfirmationMessage());
    }

    protected String getListOfItemPaths() {
        StringBuilder builder = new StringBuilder("<ul>");
        for (Item item : items) {
            Object itemId = contentConnector.getItemId(item);
            String path = contentConnector.getItemUrlFragment(itemId);
            builder.append("<li>").append(path).append("</li>");
        }
        builder.append("</ul>");
        return builder.toString();
    }

    /**
     * Class that implement CommansActionBase should use
     * this in order to perform tasks or notification in case of error.
     */
    protected void onError(Exception e) {
        // it would be possible to use here i18n-izer framework: String message = getDefinition().getErrorMessage();
        // but this would require a key for every confirm-action; to keep things simple we are using SimpleTranslator now
        // enhancement of ActionDefinitionKeyGenerator may allow to use i18n-izer but just with one key ...
        String message = i18n.translate("ui-framework.actions.confirmAction.errorMessage");
        log.error(message, e);
        uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, message);
    }

    private String formatMessage(final String message) throws RepositoryException {
        long howMany = items.size();
        return MessageFormatterUtils.format(message, howMany, howMany);
    }

    /**
     * @return the Item linked to this action. If this action is used for multi Item return null.
     */
    protected Item getItem() {
        if (items != null && items.size() == 1) {
            return items.get(0);
        } else {
            return null;
        }
    }
}
