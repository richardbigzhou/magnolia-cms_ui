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
package info.magnolia.ui.form.action;

import static org.junit.Assert.assertEquals;

import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.FormPresenter;
import info.magnolia.event.EventBus;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;
import info.magnolia.ui.vaadin.form.FormView;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * CallbackFormActionTest.
 */
public class CallbackFormActionTest {

    private CallbackFormAction formAction;
    private CallbackFormActionDefinition formActionDefinition;
    private FormPresenterTest presenter;

    @Before
    public void setUp() {
        this.formActionDefinition = new CallbackFormActionDefinition();
        this.presenter = new FormPresenterTest();
    }

    @Test
    public void executeDefaultOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, null);
        formAction = new CallbackFormAction(formActionDefinition, presenter);

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(success)", presenter.callbackActionCalled);
    }

    @Test
    public void executeCustomOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, "reload");
        formAction = new CallbackFormAction(formActionDefinition, presenter);

        // WHEN
        formAction.execute();

        // THEN
        assertEquals("onSuccess(reload)", presenter.callbackActionCalled);
    }

    @Test
    public void executeOnCancelTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", false, null);
        formAction = new CallbackFormAction(formActionDefinition, presenter);

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

    public static class FormPresenterTest implements FormPresenter {

        private Item item;

        private String callbackActionCalled;

        public String getCallbackActionCalled() {
            return callbackActionCalled;
        }

        public void setTestItem(Item item) {
            this.item = item;
        }

        @Override
        public FormPresenter.Callback getCallback() {
            return new FormPresenter.Callback() {

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
        public EventBus getEventBus() {
            return null;
        }

        @Override
        public FormView start(Item item, Callback callback) {
            return null;
        }

        @Override
        public FormView start(Item item, FormItem parent) {
            return null;
        }

        @Override
        public void addAction(String actionName, String actionLabel, EditorLikeActionListener callback) {

        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void showValidation(boolean isVisible) {

        }

        @Override
        public Item getItemDataSource() {
            return item;
        }

        @Override
        public FormView getView() {
            return null;
        }
    }
}
