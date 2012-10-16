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
import info.magnolia.ui.admincentral.dialog.FormDialogPresenter;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.DialogView.DialogActionListener;
import info.magnolia.ui.vaadin.dialog.FormDialog;
import info.magnolia.ui.vaadin.dialog.FormDialogView;
import info.magnolia.ui.vaadin.dialog.FormSection;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Main test class for {@link CallbackDialogAction} and
 * {@link CallbackDialogActionDefinition}.
 */

public class CallbackDialogActionTest {
    private CallbackDialogAction dialogAction;
    private CallbackDialogActionDefinition dialogActionDefinition;
    private DialogPresenterTest presenter;

    @Before
    public void setUp() {
        this.dialogActionDefinition = new CallbackDialogActionDefinition();
        this.presenter = new DialogPresenterTest();
    }

    @Test
    public void executeDefaultOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, null);
        dialogAction = new CallbackDialogAction(dialogActionDefinition, presenter);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(success)", presenter.callbackActionCalled);
    }

    @Test
    public void executeCustomOnSuccessTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", null, "reload");
        dialogAction = new CallbackDialogAction(dialogActionDefinition, presenter);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onSuccess(reload)", presenter.callbackActionCalled);
    }

    @Test
    public void executeOnCancelTest() throws ActionExecutionException {
        // GIVEN
        initDefinition("name", "label", false, null);
        dialogAction = new CallbackDialogAction(dialogActionDefinition, presenter);

        // WHEN
        dialogAction.execute();

        // THEN
        assertEquals("onCancel()", presenter.callbackActionCalled);
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

    public static class DialogPresenterTest implements FormDialogPresenter {

        private String callbackActionCalled;
        public String getCallbackActionCalled() {
            return callbackActionCalled;
        }
        private Item item;
        public void setTestItem(Item item) {
            this.item = item;
        }

        @Override
        public Callback getCallback() {
            return new Callback() {

                @Override
                public void onSuccess(String actionName) {
                    callbackActionCalled = "onSuccess("+actionName+")";
                }

                @Override
                public void onCancel() {
                    callbackActionCalled = "onCancel()";
                }
            };
        }

        @Override
        public FormDialogView getView() {
            return new FormDialogView() {

                @Override
                public FormDialog asVaadinComponent() {
                    return null;
                }

                @Override
                public void showValidation(boolean isVisible) {
                }

                @Override
                public void setItemDataSource(Item item) {
                }

                @Override
                public void setDialogDescription(String description) {
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
                public void addDialogSection(String tabName, FormSection inputFields) {
                }

                @Override
                public void addField(Field field) {
                }


                @Override
                public void setShowAllEnabled(boolean showAll) {
                }

                @Override
                public void setCaption(String caption) {

                }

                @Override
                public Item getItemDataSource() {
                    return null;
                }
            };
        }

        @Override
        public Item getItemDataSource() {
            return item;
        }

        @Override
        public EventBus getEventBus() {
            return null;
        }

        @Override
        public FormDialogView start(Item item, Callback callback) {
            return null;
        }

        @Override
        public void showValidation(boolean isVisible) {
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

    }
}
