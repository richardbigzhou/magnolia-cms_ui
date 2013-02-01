/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;

/**
 * {@link DialogPresenter} takes care of {@link DialogView} presentation, the main responsibility
 * of it is to take care of dialog actions/events.
 */
public interface DialogPresenter {

    Callback getCallback();

    DialogView getView();

    EventBus getEventBus();

    void closeDialog();

    void addDialogCloseHandler(final DialogCloseEvent.Handler listener);

    void addAction(String actionName, String actionLabel, EditorLikeActionListener callback);

    void addActionCallback(String actionName, EditorLikeActionListener callback);

    /**
     * Callback interface for DialogView.Presenter.
     */
    interface Callback {

        void onCancel();

        void onSuccess(String actionName);

        /**
         * Dummy adapter class that allows to skip overriding e.g. onCancel method in actual
         * implementors.
         */
        public static class Adapter implements Callback {

            @Override
            public void onSuccess(String actionName) {
            }

            @Override
            public void onCancel() {
            }

        }

    }

    /**
     * A Helper class for operations with {@link DialogPresenter}.
     */
    public class DialogPresenterUtil {

        public static void addActionFromDefinition(final DialogPresenter presenter, final DialogActionDefinition definition, final DialogActionFactory factory) {
            presenter.addAction(definition.getName(), definition.getLabel(), new EditorLikeActionListener() {
                @Override
                public void onActionExecuted(final String actionName) {
                    final ActionDefinition actionDefinition = definition.getActionDefinition();
                    final Action action = factory.createAction(actionDefinition, presenter);
                    try {
                        action.execute();
                    } catch (final ActionExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
