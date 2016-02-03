/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.form.field;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

import java.util.Iterator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * Generic Multi Field.<br>
 * This generic MultiField allows to handle a Field Set. It handle :<br>
 * - The creation of new Field<br>
 * - The removal of Field<br>
 * The Field is build based on a generic {@link ConfiguredFieldDefinition}.<br>
 * The Field values are handle by a configured {@link info.magnolia.ui.form.field.transformer.Transformer} dedicated to create/retrieve properties as {@link PropertysetItem}.<br>
 */
public class MultiField extends AbstractCustomMultiField<MultiValueFieldDefinition, PropertysetItem> {

    private VerticalLayout root;
    private final Button addButton = new NativeButton();

    private final ConfiguredFieldDefinition fieldDefinition;
    private String buttonCaptionAdd;
    private String buttonCaptionRemove;

    public MultiField(MultiValueFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem) {
        super(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, relatedFieldItem);
        this.fieldDefinition = definition.getField();
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        root = new VerticalLayout();
        root.setSizeUndefined();

        // Init addButton
        addButton.setCaption(buttonCaptionAdd);
        addButton.addStyleName("magnoliabutton");
        addButton.addClickListener(addButtonClickListener());

        // Initialize Existing field
        initFields();
        // Register value change listener for i18n handling.
        addValueChangeListener(datasourceListener);

        return root;
    }

    /**
     * Initialize the MultiField. <br>
     * Create as many configured Field as we have related values already stored.
     */
    @Override
    protected void initFields(PropertysetItem newValue) {
        root.removeAllComponents();
        Iterator<?> it = newValue.getItemPropertyIds().iterator();
        while (it.hasNext()) {
            Property<?> property = newValue.getItemProperty(it.next());
            root.addComponent(createEntryComponent(property));
        }
        root.addComponent(addButton);
    }

    /**
     * Create a single element.<br>
     * This single element is composed of:<br>
     * - a configured field <br>
     * - a remove Button<br>
     */
    private Component createEntryComponent(Property<?> property) {
        HorizontalLayout layout = new HorizontalLayout();
        Field<?> field = createLocalField(fieldDefinition, relatedFieldItem, true);
        layout.addComponent(field);
        if (property != null) {
            field.setPropertyDataSource(property);
        } else {
            int position = root.getComponentCount() - 1;
            ((PropertysetItem) getPropertyDataSource().getValue()).addItemProperty(position, field.getPropertyDataSource());
        }
        field.addValueChangeListener(selectionListener);

        // Delete Button
        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("remove");
        deleteButton.setDescription(buttonCaptionRemove);
        deleteButton.addClickListener(removeButtonClickListener(layout));
        layout.addComponent(deleteButton);

        return layout;
    }

    /**
     * Create a button listener bound to the delete Button.
     */
    private Button.ClickListener removeButtonClickListener(final HorizontalLayout layout) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                int position = root.getComponentIndex(layout);
                root.removeComponent(layout);
                removeValueProperty(position);
                getPropertyDataSource().setValue(getValue());
            };
        };
    }

    /**
     * Create a button listener bound to the add Button.
     */
    private Button.ClickListener addButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                root.addComponent(createEntryComponent(null), root.getComponentCount() - 1);
            };
        };
    }

    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }


    /**
     * Caption section.
     */
    public void setButtonCaptionAdd(String buttonCaptionAdd) {
        this.buttonCaptionAdd = buttonCaptionAdd;
    }

    public void setButtonCaptionRemove(String buttonCaptionRemove) {
        this.buttonCaptionRemove = buttonCaptionRemove;
    }

    /**
     * Ensure that id of the {@link PropertysetItem} stay coherent.<br>
     * Assume that we have 3 values 0:a, 1:b, 2:c, and 1 is removed <br>
     * If we just remove 1, the {@link PropertysetItem} will contain 0:a, 2:c, .<br>
     * But we should have : 0:a, 1:c, .
     */
    private void removeValueProperty(int fromIndex) {
        getValue().removeItemProperty(fromIndex);
        int toIndex = fromIndex;
        int valuesSize = getValue().getItemPropertyIds().size();
        if (fromIndex == valuesSize) {
            return;
        }
        while (fromIndex < valuesSize) {
            toIndex = fromIndex;
            fromIndex +=1;
            getValue().addItemProperty(toIndex, getValue().getItemProperty(fromIndex));
            getValue().removeItemProperty(fromIndex);
        }
    }

}
