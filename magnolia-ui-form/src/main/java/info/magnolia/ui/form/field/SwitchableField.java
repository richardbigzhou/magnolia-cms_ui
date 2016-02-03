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
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Switchable field.<br>
 * Display a field composed of two main sections <br>
 * - a Select Section (list or checkBox) <br>
 * - a Field Section displaying the field currently selected. <br>
 */
public class SwitchableField extends AbstractCustomMultiField<SwitchableFieldDefinition, PropertysetItem> {
    private static final Logger log = LoggerFactory.getLogger(SwitchableField.class);

    // - key : Field name. Should be the same as the related select value.<br>
    // - value : Related Field. Created based on the definition coming from the Fields Definition list.
    private HashMap<String, Field<?>> fieldMap;
    private AbstractSelect selectField;

    // Define layout and component
    private final VerticalLayout root = new VerticalLayout();

    public SwitchableField(SwitchableFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem) {
        super(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, relatedFieldItem);
    }

    @Override
    protected Component initContent() {
        // Initialize root
        setSizeFull();
        root.setSizeFull();
        root.setSpacing(true);

        // Create and Add Select section
        selectField = createSelectionField();
        selectField.addValueChangeListener(createSelectValueChangeListener());

        // Initialize Existing field
        initFields();
        // Register value change listener for i18n handling.
        addValueChangeListener(datasourceListener);
        return root;
    }

    @Override
    protected void initFields(PropertysetItem fieldValues) {
        root.removeAllComponents();
        // add the select Field Component.
        root.addComponent(selectField);
        // add Field section

        fieldMap = new HashMap<String, Field<?>>();
        // Create Switchable Fields
        for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
            String name = fieldDefinition.getName();
            Field<?> field = createLocalField(fieldDefinition, relatedFieldItem, false);
            if (fieldValues.getItemProperty(fieldDefinition.getName()) != null) {
                field.setPropertyDataSource(fieldValues.getItemProperty(fieldDefinition.getName()));
            } else {
                fieldValues.addItemProperty(fieldDefinition.getName(), field.getPropertyDataSource());
            }
            field.addValueChangeListener(selectionListener);
            fieldMap.put(name, field);
        }

        if (fieldValues.getItemProperty(definition.getName()) != null) {
            selectField.setPropertyDataSource(fieldValues.getItemProperty(definition.getName()));
        } else {
            fieldValues.addItemProperty(definition.getName(), selectField.getPropertyDataSource());
        }

        // Set Selected
        Property<?> switchFieldProperty = fieldValues.getItemProperty(definition.getName());
        if (switchFieldProperty != null && switchFieldProperty.getValue() != null) {
            switchField((String) switchFieldProperty.getValue());
        }
    }

    /**
     * Creates an option group or a select field based on the definition.
     */
    private AbstractSelect createSelectionField() {
        AbstractSelect field = null;
        try {
            // Create the correct definition class
            SelectFieldDefinition selectDefinition = null;
            String layout = "horizontal";
            if (definition.getSelectionType().equals("radio")) {
                selectDefinition = new OptionGroupFieldDefinition();
                if (definition.getLayout().equals(Layout.vertical)) {
                    layout = "vertical";
                }
            } else {
                selectDefinition = new SelectFieldDefinition();
            }
            // Copy options to the newly created select definition.
            BeanUtils.copyProperties(selectDefinition, definition);
            selectDefinition.setTransformerClass(null);
            selectDefinition.setLabel(StringUtils.EMPTY);
            selectDefinition.setRequired(false);

            // Create the field
            field = (AbstractSelect) createLocalField(selectDefinition, relatedFieldItem, false);
            field.addStyleName(layout);
            field.setImmediate(true);
        } catch (Exception e) {
            log.warn("Coudn't create the select field. Return null", e.getMessage());
        }
        return field;
    }


    /**
     * Change Listener bound to the select field. Once a selection is done, <br>
     * the value change listener will switch to the field linked to the current select value.
     */
    private ValueChangeListener createSelectValueChangeListener() {
        ValueChangeListener listener ;
        listener = new ValueChangeListener() {

            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                final String valueString = String.valueOf(event.getProperty()
                        .getValue());
                switchField(valueString);
            }

        };
        return listener;
    }

    /**
     * Switch to the desired field. It the field is not part of the List, display a warn label.
     */
    private void switchField(String fieldName) {
        if (root.getComponentCount() >= 2) {
            // detach previous field
            root.removeComponent(root.getComponent(1));
        }
        if (fieldMap.containsKey(fieldName)) {
            // add after combobox
            root.addComponent(fieldMap.get(fieldName), 1);
        } else {
            log.warn("{} is not associated to a field. Nothing will be displayed.", fieldName);
            root.addComponent(new Label("No field defined for the following selection: " + fieldName), 1);
        }
    }

    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }

}
