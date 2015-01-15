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
package info.magnolia.ui.form.action;

import static org.junit.Assert.assertEquals;

import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;

import org.junit.Before;
import org.junit.Test;

/**
 * CallbackFormActionTest.
 */
public class CallbackFormActionTest extends MgnlTestCase {

    private CallbackFormAction formAction;
    private CallbackFormActionDefinition formActionDefinition;
    private TestEditorCallback presenter;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        this.formActionDefinition = new CallbackFormActionDefinition();
        this.presenter = new TestEditorCallback();
    }

    @Test
    public void executeDefaultOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, null);
        formAction = new CallbackFormAction(formActionDefinition, presenter.getCallback());

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(success)", presenter.callbackActionCalled);
    }

    @Test
    public void executeCustomOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, "reload");
        formAction = new CallbackFormAction(formActionDefinition, presenter.getCallback());

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(reload)", presenter.callbackActionCalled);
    }

    @Test
    public void executeOnCancelTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", false, null);
        formAction = new CallbackFormAction(formActionDefinition, presenter.getCallback());

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onCancel()", presenter.callbackActionCalled);
    }

    /**
     * Init the Definition.
     */
    private void initDefinition(String name, String label, Boolean callSuccess, String successActionName) {
        this.formActionDefinition.setCallSuccess((callSuccess != null) ? callSuccess : true);
        this.formActionDefinition.setLabel(label);
        this.formActionDefinition.setName(name);
        this.formActionDefinition.setSuccessActionName(successActionName != null ? successActionName : "success");
    }

    public static class TestEditorCallback implements EditorCallback {

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
        public void onSuccess(String actionName) {
            callbackActionCalled = "onSuccess(" + actionName + ")";
        }

        @Override
        public void onCancel() {
            callbackActionCalled = "onCancel()";
        }
    }

    public static class TestEditorValidator implements EditorValidator {

        @Override
        public void showValidation(boolean visible) {

        }

        @Override
        public boolean isValid() {
            return true;
        }
    }


}
