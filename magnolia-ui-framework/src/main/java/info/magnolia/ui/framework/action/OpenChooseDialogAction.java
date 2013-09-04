/**
 * This file Copyright (c) 2012-2013 Magnolia International
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

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ItemChosenListener;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configurable confirmation action. Can be used to intercept the actual action with user feedback.
 * Allows configuration of a success action and a cancel action.
 *
 * @see ConfirmationActionDefinition
 */
public class OpenChooseDialogAction extends AbstractAction<OpenChooseDialogActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(OpenChooseDialogAction.class);


    private final List<JcrItemAdapter> items;
    private final UiContext uiContext;
    private final ActionExecutor actionExecutor;

    private final AppController appController;
    private final SubAppContext subAppContext;

    public OpenChooseDialogAction(OpenChooseDialogActionDefinition definition, JcrItemAdapter item, UiContext uiContext,
            ActionExecutor actionExecutor, AppController appController, SubAppContext subAppContext) {

        super(definition);
        this.items = new ArrayList<JcrItemAdapter>(1);
        this.items.add(item);
        this.uiContext = uiContext;
        this.actionExecutor = actionExecutor;

        this.subAppContext = subAppContext;
        this.appController = appController;
    }

    public OpenChooseDialogAction(OpenChooseDialogActionDefinition definition, List<JcrItemAdapter> items, UiContext uiContext,
            ActionExecutor actionExecutor, AppController appController, SubAppContext subAppContext) {

        super(definition);
        this.items = items;
        this.uiContext = uiContext;
        this.actionExecutor = actionExecutor;

        this.subAppContext = subAppContext;
        this.appController = appController;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {

            String appName = getDefinition().getAppName();
            if (appName == null) {
                // Default to current app.
                appName = appController.getCurrentAppLocation().getAppName();
            }

            String selectedItemPath = items.get(0).getJcrItem().getPath();
            appController.openChooseDialog(appName, "/", uiContext, selectedItemPath, new ItemChosenListener() {
                @Override
                public void onItemChosen(final com.vaadin.data.Item chosenValue) {
                    try {
                        actionExecutor.execute(getDefinition().getSuccessActionName(), items, chosenValue, MoveLocation.AFTER);

                    } catch (ActionExecutionException e) {
                        onError(e);
                    }
                }

                @Override
                public void onChooseCanceled() {
                    if (getDefinition().getCancelActionName() != null) {
                        try {
                            actionExecutor.execute(getDefinition().getCancelActionName(), items);
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

    /**
     * Class that implement CommansActionBase should use
     * this in order to perform tasks or notification in case of error.
     */
    protected void onError(Exception e) {
        String message = "An error occurred while attempting to choose an item.";
        log.error(message, e);
        uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, message);
    }
}
