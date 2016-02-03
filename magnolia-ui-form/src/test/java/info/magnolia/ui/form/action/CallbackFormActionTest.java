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
package info.magnolia.ui.form.action;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;

import org.junit.Before;
import org.junit.Test;

/**
 * CallbackFormActionTest.
 *
 * @deprecated since 5.4.3, class under test is also deprecated in favor of info.magnolia.ui.framework.action.EditorCallbackAction
 */
@Deprecated
public class CallbackFormActionTest extends MgnlTestCase {

    private CallbackFormAction action;
    private CallbackFormActionDefinition definition;
    private EditorCallback callback;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        definition = createDefinition();
        callback = mock(EditorCallback.class);
    }

    /**
     * Use similar inheritance scheme in tests, as for the class under test (i.e. thus inheriting test-cases).
     */
    protected CallbackFormActionDefinition createDefinition() {
        return new CallbackFormActionDefinition();
    }

    /**
     * Use similar inheritance scheme in tests, as for the class under test (i.e. thus inheriting test-cases).
     */
    protected CallbackFormAction createAction(CallbackFormActionDefinition definition, EditorCallback callback) {
        return new CallbackFormAction(definition, callback);
    }

    @Test
    public void executeDefaultOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        action = createAction(definition, callback);

        // WHEN
        action.execute();

        // THEN
        verify(callback, only()).onSuccess(eq("success"));
    }


    @Test
    public void executeCustomOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        definition.setSuccessActionName("reload");
        action = createAction(definition, callback);

        // WHEN
        action.execute();

        // THEN
        verify(callback, only()).onSuccess(eq("reload"));
    }

    @Test
    public void executeOnCancelTest() throws ActionExecutionException {
        // GIVEN
        definition.setCallSuccess(false);
        action = createAction(definition, callback);

        // WHEN
        action.execute();

        // THEN
        verify(callback, only()).onCancel();
    }

    /**
     * @deprecated since 5.4.3 use proper mocks instead.
     */
    @Deprecated
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

    /**
     * @deprecated since 5.4.3 use proper mocks instead.
     */
    @Deprecated
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
