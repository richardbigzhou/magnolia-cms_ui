/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.form.connector.FormState;
import info.magnolia.ui.vaadin.gwt.client.form.rpc.FormServerRpc;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTab;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * {@link Form}. The server side implementation of the form view. Displays the
 * form inside a {@link MagnoliaTabSheet}.
 *
 * TODO: TAKE CARE OF FIELDGROUP IN THE FORM BUILDER LATER ON!
 */
public class Form extends AbstractSingleComponentContainer implements FormViewReduced {

    private final String SHOW_ALL = MessagesUtil.get("dialogs.show.all");

    private Item itemDataSource;

    private FieldGroup fieldGroup = new FieldGroup();

    private List<Field<?>> fields = new LinkedList<Field<?>>();

    private boolean isValidationVisible = false;

    private final MagnoliaTabSheet tabSheet = new MagnoliaTabSheet() {
        @Override
        public MagnoliaFormTab addTab(final String caption, final Component c) {
            if (c instanceof FormSection) {
                final FormSection section = (FormSection) c;
                final MagnoliaFormTab tab = new MagnoliaFormTab(caption, section);
                tab.setClosable(false);
                doAddTab(tab);
                return tab;
            }
            throw new IllegalArgumentException("TabSheet inside a Form should only receive the FormSection objects as tab content.");
        }
    };

    public Form() {
        super();
        setStyleName("v-magnolia-form");
        tabSheet.setSizeFull();
        tabSheet.showAllTab(true, SHOW_ALL);
        setContent(tabSheet);
        registerRpc(new FormServerRpc() {
            @Override
            public void focusNextProblematicField(Connector currentFocused) {
                doFocusNextProblematicField(currentFocused);
            }
        });
    }

    public void focusFirstField() {
        if (fields.isEmpty()) {
            return;
        }
        fields.get(0).focus();
    }

    private void doFocusNextProblematicField(Connector currentFocused) {
        /**
         * In case the remaining issues are in the current tab above current focus -
         * we need to wrap the search to check the current tab once we've investigated all th eothers
         */
        int tabsToIterate = tabSheet.getComponentCount() + 1;
        MagnoliaTab tab = tabSheet.getActiveTab();
        FormSection section;
        Component nextProblematic;
        do {
            section = (FormSection) tab.getContent();
            nextProblematic = section.getNextProblematicField(currentFocused);
            if (nextProblematic == null) {
                tab = tabSheet.getNextTab(tab);
                tabsToIterate--;
                // After testing the first section - we want to check ALL fields per section.
                currentFocused = null;
            }

        } while (nextProblematic == null && tabsToIterate > 0);

        // focus next tab and field
        if (nextProblematic != null) {
            tabSheet.setActiveTab(tab);
            section.focusField(nextProblematic);
        }
    }

    @Override
    public void setDescriptionVisibility(boolean isVisible) {
        getState().descriptionsVisible = isVisible;
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
        this.itemDataSource = newDataSource;
    }

    @Override
    public Item getItemDataSource() {
        return itemDataSource;
    }

    @Override
    public void addFormSection(String tabName, FormSection inputFields) {
        tabSheet.addTab(tabName, inputFields);
    }

    @Override
    public void addField(Field<?> field) {
        fields.add(field);
        field.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                invalidateErrorAmount();
            }
        });
    }

    private void invalidateErrorAmount() {
        getState().errorAmount = -1;
    }

    @Override
    public Collection<Field<?>> getFields() {
        return fields;
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
        isValidationVisible = isVisible;

        if (isVisible) {
            invalidateErrorAmount();
        }

        final Iterator<Component> it = tabSheet.getComponentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            if (c instanceof MagnoliaFormTab) {
                ((MagnoliaFormTab) c).setValidationVisible(isVisible);
            }
        }
    }

    @Override
    public void setShowAllEnabled(boolean enabled) {
        tabSheet.showAllTab(enabled, SHOW_ALL);
    }

    @Override
    protected FormState getState() {
        return (FormState) super.getState();
    }

    @Override
    protected FormState getState(boolean markAsDirty) {
        return (FormState) super.getState(markAsDirty);
    }

    @Override
    public Form asVaadinComponent() {
        return this;
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        getState().errorAmount = 0;
        if (isValidationVisible) {
            for (Field<?> field : fields) {
                if (!field.isValid()) {
                    ++getState().errorAmount;
                }
            }
        }
    }
}
