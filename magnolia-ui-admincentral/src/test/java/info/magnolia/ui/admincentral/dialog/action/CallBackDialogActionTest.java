/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.widget.dialog.MagnoliaDialogView;
import info.magnolia.ui.widget.dialog.MagnoloaDialogPresenter;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

/**
 * Main test class for {@link CallBackDialogAction} and
 * {@link CallBackDialogActionDefinition}.
 */

public class CallBackDialogActionTest {
    private CallBackDialogAction dialogAction;
    private CallBackDialogActionDefinition dialogActionDefinition;
    private DialogPresenterTest presenter;

    @Before
    public void setUp() {
        this.dialogActionDefinition = new CallBackDialogActionDefinition();
        this.presenter = new DialogPresenterTest();
    }

    @Test
    public void executeDefaultOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, null);
        dialogAction = new CallBackDialogAction(dialogActionDefinition, presenter);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(success)", presenter.callBackActionCalled);
    }

    @Test
    public void executeCustomOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, "reload");
        dialogAction = new CallBackDialogAction(dialogActionDefinition, presenter);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(reload)", presenter.callBackActionCalled);
    }

    @Test
    public void executeOnCancelTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", false, null);
        dialogAction = new CallBackDialogAction(dialogActionDefinition, presenter);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onCancel()", presenter.callBackActionCalled);
    }

    /**
     * Init the Definition.
     */
    private void initDefinition(String name, String label, Boolean callSuccess, String successActionName) {
        this.dialogActionDefinition.setCallSuccess((callSuccess!=null)?callSuccess:true);
        this.dialogActionDefinition.setLabel(label);
        this.dialogActionDefinition.setName(name);
        this.dialogActionDefinition.setSuccessActionName(successActionName!=null?successActionName:"success");
    }

    public static class DialogPresenterTest implements MagnoloaDialogPresenter.Presenter {

        private String callBackActionCalled;
        public String getCallBackActionCalled() {
            return callBackActionCalled;
        }
        private Item item;
        public void setTestItem(Item item) {
            this.item = item;
        }

        @Override
        public CallBack getCallBack() {
            return new CallBack() {

                @Override
                public void onSuccess(String actionName) {
                    callBackActionCalled = "onSuccess("+actionName+")";
                }

                @Override
                public void onCancel() {
                    callBackActionCalled = "onCancel()";
                }
            };
        }

        @Override
        public MagnoliaDialogView getView() {
            return new MagnoliaDialogView() {

                @Override
                public Component asVaadinComponent() {
                    return null;
                }

                @Override
                public void showValidation(boolean isVisible) {
                }

                @Override
                public void setListener(Listener listener) {
                }

                @Override
                public void setItemDataSource(Item item) {
                }

                @Override
                public void setDescription(String description) {
                }

                @Override
                public boolean isValid() {
                    return true;
                }

                @Override
                public List<Field> getFields() {
                    return null;
                }

                @Override
                public void addTab(ComponentContainer inputFields, String tabName) {
                }

                @Override
                public void addField(Field field) {
                }

                @Override
                public void addAction(String actionName, String actionLabel) {
                }
            };
        }

        @Override
        public Item getItem() {
            return item;
        }

        @Override
        public EventBus getEventBus() {
            return null;
        }

        @Override
        public MagnoliaDialogView start(Item item, CallBack callBack) {
            return null;
        }

        @Override
        public void showValidation(boolean isVisible) {
        }

        @Override
        public void closeDialog() {
        }

    }
}
