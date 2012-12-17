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
package info.magnolia.ui.vaadin.form;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.vaadin.data.Item;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.vaadin.gwt.client.form.VForm;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;
import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link Form}. The server side implementation of the form view.
 * Displays the form inside a {@link MagnoliaTabSheet}.
 */
@ClientWidget(value = VForm.class, loadStyle = ClientWidget.LoadStyle.EAGER)
public class Form extends AbstractComponent implements ServerSideHandler, FormView {

    private final String SHOW_ALL = MessagesUtil.get("dialogs.show.all");

    private final MagnoliaTabSheet tabSheet = new MagnoliaTabSheet() {
        @Override
        public MagnoliaFormTab addTab(final String caption, final ComponentContainer c) {
            if (c instanceof FormSection) {
                final MagnoliaFormTab tab = new MagnoliaFormTab(caption, (FormSection) c);
                tab.setSizeUndefined();
                tab.setClosable(false);
                doAddTab(tab);
                return tab;
            }
            return null;
        }
    };

    private boolean isAttached;
    
    private String description;

    private Item itemDatasource;

    private final List<Field> fields = new LinkedList<Field>();

    private final ListMultimap<String, FormView.FormActionListener> actionCallbackMap = 
            ArrayListMultimap.<String, FormView.FormActionListener> create();

    private final Map<String, String> actionMap = new HashMap<String, String>();

    private final ServerSideProxy proxy = new ServerSideProxy(this) {
        {
            register("fireAction", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    final String actionName = String.valueOf(params[0]);
                    final Iterator<FormView.FormActionListener> it = actionCallbackMap.get(actionName).iterator();
                    while (it.hasNext()) {
                        it.next().onActionExecuted(actionName);
                    }
                }
            });
        }
    };

    public Form() {
        setStyleName("v-magnolia-form");
        setImmediate(true);
        tabSheet.setParent(this);
        tabSheet.showAllTab(true, SHOW_ALL);
        tabSheet.setHeight("100%");
    }

    @Override
    public void setCaption(String caption) {
        tabSheet.setCaption(caption);
    }
    
    @Override
    public void setItemDataSource(Item newDataSource) {
        itemDatasource = newDataSource;
    }

    @Override
    public Item getItemDataSource() {
        return itemDatasource;
    }

    @Override
    public void addFormSection(String tabName, FormSection inputFields) {
        tabSheet.addTab(tabName, inputFields);
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
    public boolean isValid() {
        boolean res = true;
        for (Field field : getFields()) {
            res &= field.isValid();
        }
        return res;
    }

    @Override
    public void showValidation(boolean isVisible) {
        final Iterator<Component> it = tabSheet.getComponentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            if (c instanceof FormSection) {
                ((FormSection) c).setValidationVisible(isVisible);
            }
        }
    }

    @Override
    public void setShowAllEnabled(boolean enabled) {
        tabSheet.showAllTab(enabled, SHOW_ALL);
    }

    @Override
    public void setFormDescription(String description) {
        this.description = description;
        if (isAttached) {
            proxy.call("setDescription", description);
        }
    }

    @Override
    public void addAction(String actionName, String actionLabel, FormView.FormActionListener callback) {
        addAction(actionName, actionLabel);
        addActionCallback(actionName, callback);
    }

    public void addAction(String actionName, String actionLabel) {
        actionMap.put(actionName, actionLabel);
        if (isAttached) {
            proxy.call("addAction", actionName, actionLabel);
        }
    }

    public void addActionCallback(String actionName, FormView.FormActionListener callback) {
        actionCallbackMap.put(actionName, callback);
    }

    @Override
    public void attach() {
        this.isAttached = true;
        super.attach();
        tabSheet.attach();
    }

    @Override
    public void detach() {
        this.isAttached = false;
        super.detach();
        tabSheet.detach();
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        tabSheet.paint(target);
        proxy.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public Object[] initRequestFromClient() {
        if (description != null) {
            proxy.call("setDescription", description);
        }
        for (final Map.Entry<String, String> entry : actionMap.entrySet()) {
            proxy.call("addAction", entry.getKey(), entry.getValue());
        }
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unknown call from client " + method);
    }

}
