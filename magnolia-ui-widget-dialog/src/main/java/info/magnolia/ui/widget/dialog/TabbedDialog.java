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
package info.magnolia.ui.widget.dialog;

import info.magnolia.ui.vaadin.integration.widget.tabsheet.ShellTabSheet;
import info.magnolia.ui.widget.dialog.gwt.client.VTabbedDialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.data.Item;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

/**
 * Tab Dialog.
 *
 */
@ClientWidget(VTabbedDialog.class)
public class TabbedDialog extends ShellTabSheet implements DialogView, ServerSideHandler, Item.Editor {

    private final String SHOW_ALL = "show all";

    private List<DialogTab> dialogTabs = new ArrayList<DialogTab>();
    
    private List<Field> fields = new LinkedList<Field>();

    private Presenter presenter;

    private Item itemDatasource;
    
    public TabbedDialog() {
        setImmediate(true);
        setShowAllEnabled(true);
        setHeight("500px");
    }

    @Override
    protected ServerSideProxy createProxy() {
        ServerSideProxy proxy = super.createProxy();
        proxy.register("fireAction", new Method() {

            @Override
            public void invoke(String methodName, Object[] params) {
                final String actionName = String.valueOf(params[0]);
                presenter.executeAction(actionName);
            }
        });
        proxy.register("closeDialog", new Method() {

            @Override
            public void invoke(String methodName, Object[] params) {
                presenter.closeDialog();
            }
        });
        return proxy;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void addTab(ComponentContainer cc, String caption) {
        if (!(cc instanceof DialogLayout)) {
            throw new IllegalArgumentException();
        }
        final DialogTab tab = new DialogTab(caption, (DialogLayout)cc);
        dialogTabs.add(tab);
        tab.setSizeUndefined();
        tab.setClosable(false);
        doAddTab(tab);
    }

    public void setShowAllEnabled(boolean showAll) {
        showAllTab(showAll, SHOW_ALL);
    }

    @Override
    public void addAction(String actionName, String actionLabel) {
        proxy.call("addAction", actionName, actionLabel);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
        this.itemDatasource = newDataSource;
    }

    @Override
    public Item getItemDataSource() {
        return itemDatasource;
    }

    @Override
    public void addField(Field field) {
        fields.add(field);
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    @Override
    public void setDescription(String description) {
        proxy.call("setDescription", description);
    }

    @Override
    public void showValidation(boolean isVisible) {
        for (final DialogTab tab : dialogTabs) {
            tab.setValidationVisibe(isVisible);
        }
    }
    
    
    @Override
    public boolean isValid() {
        boolean res = true;
        for (Field field : getFields()) {
            res &= field.isValid();
        }
        return res;
    }
}
