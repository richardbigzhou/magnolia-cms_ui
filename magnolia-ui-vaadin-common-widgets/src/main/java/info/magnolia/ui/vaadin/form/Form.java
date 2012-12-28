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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.vaadin.form.tab.MagnoliaFormTab;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.ActionFiringServerRpc;
import info.magnolia.ui.vaadin.gwt.client.form.connector.FormState;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.thirdparty.guava.common.collect.ArrayListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.ListMultimap;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HasComponents;

/**
 * {@link Form}. The server side implementation of the form view. Displays the
 * form inside a {@link MagnoliaTabSheet}.
 * 
 * TODO: TAKE CARE OF FIELDGROUP IN THE FORM BUILDER LATER ON!
 */
public class Form extends AbstractSingleComponentContainer implements FormView {

    private final String SHOW_ALL = MessagesUtil.get("dialogs.show.all");

    private final FieldGroup fieldGroup = new FieldGroup();
    
    private final MagnoliaTabSheet tabSheet = new MagnoliaTabSheet() {
        @Override
        public MagnoliaFormTab addTab(final String caption, final HasComponents c) {
            if (c instanceof FormSection) {
                final FormSection section = (FormSection) c;
                final MagnoliaFormTab tab = new MagnoliaFormTab(caption, section);
                tab.setClosable(false);
                doAddTab(tab);
                return tab;
            }
            return null;
        }
    };

    private final ListMultimap<String, FormView.FormActionListener> actionCallbackMap = ArrayListMultimap
            .<String, FormView.FormActionListener> create();

    public Form() {
        setStyleName("v-magnolia-form");
        setImmediate(true);
        tabSheet.setSizeFull();
        tabSheet.showAllTab(true, SHOW_ALL);
        setContent(tabSheet);
        
        registerRpc(new ActionFiringServerRpc() {
            @Override
            public void fireAction(String actionId) {
                final Iterator<FormActionListener> it = actionCallbackMap.get(actionId).iterator();
                while (it.hasNext()) {
                    it.next().onActionExecuted(actionId);
                }
            }

            @Override
            public void closeSelf() {
                
            }
            
        });
    }

    @Override
    public void setCaption(String caption) {
        tabSheet.setCaption(caption);
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
        fieldGroup.setItemDataSource(newDataSource);
    }

    @Override
    public Item getItemDataSource() {
        return fieldGroup.getItemDataSource();
    }

    @Override
    public void addFormSection(String tabName, FormSection inputFields) {
        tabSheet.addTab(tabName, inputFields);
    }

    @Override
    public Collection<Field<?>> getFields() {
        return fieldGroup.getFields();
    }

    @Override
    public boolean isValid() {
        boolean res = true;
        for (Field<?> field : getFields()) {
            res &= field.isValid();
        }
        return res;
    }

    @Override
    public void showValidation(boolean isVisible) {
        final Iterator<Component> it = tabSheet.getComponentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            if (c instanceof MagnoliaFormTab) {
                ((MagnoliaFormTab) c).getContent().setValidationVisible(isVisible);
            }
        }
    }

    @Override
    public void setShowAllEnabled(boolean enabled) {
        tabSheet.showAllTab(enabled, SHOW_ALL);
    }

    @Override
    public void setFormDescription(String description) {
        getState().componentDescription = description;
    }

    @Override
    public void addAction(String actionName, String actionLabel, FormView.FormActionListener callback) {
        addAction(actionName, actionLabel);
        addActionCallback(actionName, callback);
    }

    public void addAction(String actionName, String actionLabel) {
        getState().actions.put(actionName, actionLabel);
    }

    public void addActionCallback(String actionName, FormView.FormActionListener callback) {
        actionCallbackMap.put(actionName, callback);
    }

    @Override
    protected FormState getState() {
        return (FormState)super.getState();
    }
    
    @Override
    protected FormState getState(boolean markAsDirty) {
        return (FormState)super.getState(markAsDirty);
    }
    
    @Override
    public Form asVaadinComponent() {
        return this;
    }

    @Override
    public void suppressOwnActions() {
        getState().actionsSuppressed = true;
    }

}
