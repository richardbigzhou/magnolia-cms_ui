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
package info.magnolia.ui.admincentral.dialog.action;

import static org.junit.Assert.assertEquals;

import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.dialog.FormDialogPresenter;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.dialog.FormDialogView;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Main test class for {@link CallbackDialogAction} and {@link CallbackDialogActionDefinition}.
 */

public class CallbackDialogActionTest extends MgnlTestCase {
    private CallbackDialogAction dialogAction;
    private CallbackDialogActionDefinition dialogActionDefinition;
    private FormDialogPresenterTest presenter;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        this.dialogActionDefinition = new CallbackDialogActionDefinition();
        this.presenter = new FormDialogPresenterTest();
    }

    @Test
    public void executeDefaultOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, null);
        dialogAction = new CallbackDialogAction(dialogActionDefinition, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(success)", presenter.callbackActionCalled);
    }

    @Test
    public void executeCustomOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, "reload");
        dialogAction = new CallbackDialogAction(dialogActionDefinition, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(reload)", presenter.callbackActionCalled);
    }

    @Test
    public void executeOnCancelTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", false, null);
        dialogAction = new CallbackDialogAction(dialogActionDefinition, presenter.getCallback());

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onCancel()", presenter.callbackActionCalled);
    }

    /**
     * Init the Definition.
     */
    private void initDefinition(String name, String label, Boolean callSuccess, String successActionName) {
        this.dialogActionDefinition.setCallSuccess((callSuccess != null) ? callSuccess : true);
        this.dialogActionDefinition.setLabel(label);
        this.dialogActionDefinition.setName(name);
        this.dialogActionDefinition.setSuccessActionName(successActionName != null ? successActionName : "success");
    }



    /**
     * Form dialog presenter test.
     */
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
        public FormDialogView getView() {
            return null;
        }

        @Override
        public DialogView start(Item item, DialogDefinition dialogDefinition, OverlayLayer overlayLayer, EditorCallback callback) {
            return null;
        }

        @Override
        public void showCloseButton() {
        }

        @Override
        public DialogView start(Item item, String dialogName, OverlayLayer overlayLayer, EditorCallback callback) {
            return null;
        }

        @Override
        public void closeDialog() {
        }

        @Override
        public void addDialogCloseHandler(DialogCloseEvent.Handler listener) {
        }

        @Override
        public void addAction(String actionName, String actionLabel, DialogActionListener callback) {
            // TODO Auto-generated method stub

        }

        @Override
        public void addActionCallback(String actionName, DialogActionListener callback) {
            // TODO Auto-generated method stub

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
