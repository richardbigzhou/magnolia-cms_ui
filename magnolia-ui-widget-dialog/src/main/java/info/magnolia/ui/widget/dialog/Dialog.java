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



import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.widget.dialog.action.Action;
import info.magnolia.ui.widget.dialog.event.DialogCommitEvent;
import info.magnolia.ui.widget.dialog.gwt.client.VDialog;
import info.magnolia.ui.widget.tabsheet.ShellTab;
import info.magnolia.ui.widget.tabsheet.ShellTabSheet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.data.Buffered;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
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
 *
 * @author apchelintcev
 */
@SuppressWarnings("serial")
@ClientWidget(value=VDialog.class, loadStyle = LoadStyle.EAGER)
public class Dialog extends AbstractComponent implements ServerSideHandler, Item.Editor, Buffered {

    private ShellTabSheet tabsheet = new ShellTabSheet();
    private final String SHOW_ALL = "show all";
    private EventBus eventBus;

    /**
     * Item connected to this dialog as datasource.
     */
    private Item itemDatasource;
    private final LinkedList<Object> propertyIds = new LinkedList<Object>();
    Map<Object, Field> fields = new HashMap<Object, Field>();

    Map<String, Action> actionMap = new HashMap<String, Action>();


    protected ServerSideProxy proxy = new ServerSideProxy(this) {
            {
                register("fireAction", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String action = String.valueOf(params[0]);
                        actionMap.get(action).execute();
                    }
                });
            }
        };

    private boolean readThrough;
    private boolean writeThrough;

    public Dialog(EventBus eventBus) {
        this.eventBus = eventBus;
        setImmediate(true);
        showAllTab(true);
        registerActions();
    }

    @Override
    public void attach() {
        this.tabsheet.attach();
        this.tabsheet.setParent(this);
        super.attach();
    }

    @Override
    public void detach() {
        super.detach();
        this.tabsheet.detach();
    }

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

    public void addAction(Action action) {
        actionMap.put(action.getName(), action);
        proxy.call("addAction", action.getName(), action.getLabel());
    }

    public void registerActions() {

        Action commit = new Action("commit", "save changes") {
            @Override
            public void execute() {
                commit();
            }
        };
        addAction(commit);

        Action discard = new Action("discard", "cancel") {
            @Override
            public void execute() {
                discard();
            }
        };
        addAction(discard);



    }

    @Override
    public void setCaption(String caption) {

    }

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

    /* (non-Javadoc)
     * @see org.vaadin.rpc.ServerSideHandler#callFromClient(java.lang.String, java.lang.Object[])
     */
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

    public void addField(Property property, Field field) {
        fields.put(property, field);
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Buffered#commit()
     */
    @Override
    public void commit() throws SourceException, InvalidValueException {
        eventBus.fireEvent(new DialogCommitEvent(itemDatasource));
        closeDialog();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.Buffered#discard()
     */
    @Override
    public void discard() throws SourceException {
        closeDialog();
    }

    /*
     * Is the object modified but not committed? Don't add a JavaDoc comment
     * here, we use the default one from the interface.
     */
    @Override
    public boolean isModified() {
        for (final Iterator<Object> i = propertyIds.iterator(); i.hasNext();) {
            final Field f = fields.get(i.next());
            if (f != null && f.isModified()) {
                return true;
            }

        }
        return false;
    }

    /*
     * Is the editor in a read-through mode? Don't add a JavaDoc comment here,
     * we use the default one from the interface.
     */
    @Override
    public boolean isReadThrough() {
        return readThrough;
    }

    /*
     * Is the editor in a write-through mode? Don't add a JavaDoc comment here,
     * we use the default one from the interface.
     */
    @Override
    public boolean isWriteThrough() {
        return writeThrough;
    }

    /*
     * Sets the editor's read-through mode to the specified status. Don't add a
     * JavaDoc comment here, we use the default one from the interface.
     */
    @Override
    public void setReadThrough(boolean readThrough) {
        if (readThrough != this.readThrough) {
            this.readThrough = readThrough;
            for (final Iterator<Object> i = propertyIds.iterator(); i.hasNext();) {
                (fields.get(i.next())).setReadThrough(readThrough);
            }
        }
    }

    /*
     * Sets the editor's read-through mode to the specified status. Don't add a
     * JavaDoc comment here, we use the default one from the interface.
     */
    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException,
            InvalidValueException {
        if (writeThrough != this.writeThrough) {
            this.writeThrough = writeThrough;
            for (final Iterator<Object> i = propertyIds.iterator(); i.hasNext();) {
                (fields.get(i.next())).setWriteThrough(writeThrough);
            }
        }
    }

    /**
     *
     */
    public void closeDialog() {
        // TODO Auto-generated method stub

    }

}
