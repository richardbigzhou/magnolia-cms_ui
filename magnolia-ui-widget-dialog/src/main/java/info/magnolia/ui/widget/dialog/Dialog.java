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



import info.magnolia.ui.widget.dialog.gwt.client.VDialog;
import info.magnolia.ui.widget.tabsheet.ShellTab;
import info.magnolia.ui.widget.tabsheet.ShellTabSheet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.data.Item;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

/**
 * Server side implementation of the MagnoliaShell container.
 */
@SuppressWarnings("serial")
@ClientWidget(value=VDialog.class, loadStyle = LoadStyle.EAGER)
public class Dialog extends AbstractComponent implements DialogView, ServerSideHandler, Item.Editor {

    private ShellTabSheet tabsheet = new ShellTabSheet();
    private final String SHOW_ALL = "show all";

    /**
     * Item connected to this dialog as datasource.
     */
    private Item itemDatasource;
    List<Field> fields = new LinkedList<Field>();

    private Presenter presenter;


    protected ServerSideProxy proxy = new ServerSideProxy(this) {
            {
                register("fireAction", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String actionName = String.valueOf(params[0]);
                        presenter.executeAction(actionName);
                    }
                });
                register("closeDialog", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        presenter.closeDialog();
                    }
                });
            }
        };


    public Dialog() {
        setImmediate(true);
        showAllTab(true);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void attach() {
        super.attach();
        this.tabsheet.setParent(this);
        this.tabsheet.attach();
    }

    @Override
    public void detach() {
        super.detach();
        this.tabsheet.detach();
    }

    @Override
    public void addTab(ComponentContainer cc, String caption) {
        final ShellTab tab = new ShellTab(caption, cc);
        tab.setSizeUndefined();
        tabsheet.addComponent(tab);
        tabsheet.setTabClosable(tab, false);
        tabsheet.setActiveTab(tab);
    }

    public void setForm(Item.Editor form) {

    }

    public void closeTab(ComponentContainer c) {
        tabsheet.removeComponent(c);
    }

    public void showAllTab(boolean showAll, String label) {
        tabsheet.showAllTab(showAll, label);
    }

    public void showAllTab(boolean showAll) {
        showAllTab(showAll, SHOW_ALL);
    }

    @Override
    public void addAction(String actionName, String actionLabel) {
        proxy.call("addAction", actionName, actionLabel);
    }

    @Override
    public void setCaption(String caption) {

    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.startTag("tabsheet");
        this.tabsheet.paint(target);
        target.endTag("tabsheet");
        proxy.paintContent(target);

    }
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public Object[] initRequestFromClient() {
        return new Object[] {};

    }

    @Override
    public void callFromClient(String method, Object[] params) {
        System.out.println("Client called " + method);
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
    public boolean isValid() {
        boolean res = true;
        for(Field field:getFields()) {
            res &= field.isValid();
        }
        return res;
    }

}
