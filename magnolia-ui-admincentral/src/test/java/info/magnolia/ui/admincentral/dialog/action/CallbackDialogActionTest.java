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
package info.magnolia.ui.admincentral.dialog.action;

import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.action.CallbackDialogAction;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.dialog.actionarea.ActionAreaPresenter;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormView;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.action.CallbackFormActionDefinition;
import info.magnolia.ui.form.action.CallbackFormActionTest;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import com.vaadin.data.Item;

/**
 * Main test class for {@link info.magnolia.ui.dialog.action.CallbackDialogAction} and {@link info.magnolia.ui.dialog.action.CallbackDialogActionDefinition}.
 *
 * @deprecated since 5.4.3, class under test is also deprecated in favor of info.magnolia.ui.framework.action.EditorCallbackAction
 */
@Deprecated
public class CallbackDialogActionTest extends CallbackFormActionTest {

    @Override
    protected CallbackFormActionDefinition createDefinition() {
        return new CallbackDialogActionDefinition();
    }

    @Override
    protected CallbackDialogAction createAction(CallbackFormActionDefinition definition, EditorCallback callback) {
        return new CallbackDialogAction((CallbackDialogActionDefinition) definition, callback);
    }

    /**
     * Form dialog presenter test.
     *
     * @deprecated since 5.4.3 use proper mocks instead.
     */
    @Deprecated
    public static class FormDialogPresenterTest implements FormDialogPresenter, EditorValidator {

        private String callbackActionCalled;

        public String getCallbackActionCalled() {
            return callbackActionCalled;
        }

        public EditorCallback getCallback() {
            return new EditorCallback() {

                @Override
                public void onSuccess(String actionName) {
                    callbackActionCalled = "onSuccess(" + actionName + ")";
                }

                @Override
                public void onCancel() {
                    callbackActionCalled = "onCancel()";
                }
            };
        }

        @Override
        public DialogView start(DialogDefinition definition, UiContext uiContext) {
            return null;
        }

        @Override
        public FormView getView() {
            return null;
        }

        @Override
        public ActionAreaPresenter getActionArea() {
            return null;
        }

        @Override
        public DialogView start(Item item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback) {
            return null;
        }

        @Override
        public void addShortcut(String actionName, int keyCode, int... modifiers) {
        }

        @Override
        public DialogView start(Item item, String dialogId, UiContext uiContext, EditorCallback callback) {
            return null;
        }

        @Override
        public DialogView start(Item item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback, ContentConnector contentConnector) {
            return null;
        }

        @Override
        public void closeDialog() {
        }

        @Override
        public void showValidation(boolean visible) {
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
}
