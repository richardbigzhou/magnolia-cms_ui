/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurable confirmation action. Can be used to intercept the actual action with user feedback.
 * Allows configuration of a success action and a cancel action.
 *
 * @see ConfirmationActionDefinition
 */
public class ConfirmationAction extends AbstractAction<ConfirmationActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationAction.class);


    private final JcrItemAdapter item;
    private final UiContext uiContext;
    private final ActionExecutor actionExecutor;



    @Inject
    public ConfirmationAction(ConfirmationActionDefinition definition, JcrItemAdapter item, UiContext uiContext, ActionExecutor actionExecutor) {
        super(definition);
        this.item = item;
        this.uiContext = uiContext;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            uiContext.openConfirmation(
                    MessageStyleTypeEnum.WARNING, getConfirmationHeader(), getConfirmationMessage(), MessagesUtil.get(getDefinition().getProceedLabel()), MessagesUtil.get(getDefinition().getCancelLabel()), getDefinition().isDefaultCancel(),
                    new ConfirmationCallback() {
                        @Override
                        public void onSuccess() {
                            if (getDefinition().getSuccessActionName() != null) {
                                try {
                                    actionExecutor.execute(getDefinition().getSuccessActionName(), item);
                                } catch (ActionExecutionException e) {
                                    onError(e);
                                }
                            }
                        }

                        @Override
                        public void onCancel() {
                            if (getDefinition().getCancelActionName() != null) {
                                try {
                                    actionExecutor.execute(getDefinition().getCancelActionName(), item);
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
        boolean isNode = getItem().getJcrItem().isNode();

        return MessagesUtil.getWithDefault(getDefinition().getConfirmationHeader(),  getDefinition().getI18nBasename(), new String[]{(isNode) ? "item" : "property"});
    }

    protected String getConfirmationMessage() throws Exception {
        boolean isNode = getItem().getJcrItem().isNode();
        String path = getItem().getJcrItem().getPath();

        return MessagesUtil.get(getDefinition().getConfirmationMessage(), getDefinition().getI18nBasename(), new String[]{(isNode) ? "item" : "property", path});
    }

    protected JcrItemAdapter getItem() {
        return item;
    }

    /**
     * Class that implement CommansActionBase should use
     * this in order to perform tasks or notification in case of error.
     */
    protected void onError(Exception e) {
        String message = "Action execution failed.";
        log.error(message, e);
        uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, message);
    }
}
