/**
 * This file Copyright (c) 2013 Magnolia International
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
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.integration.NullItem;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

/**
 * .
 */
public class CompositeField extends CustomField<PropertysetItem> {

    private HorizontalLayout root;
    private final FieldFactoryFactory fieldFactoryFactory;
    private final I18nContentSupport i18nContentSupport;
    private final ComponentProvider componentProvider;
    private final CompositeFieldDefinition definition;

    public CompositeField(CompositeFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        this.definition = definition;
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        root = new HorizontalLayout();
        root.setSizeUndefined();
        root.addStyleName("compositefield");
        // Initialize Existing field
        initFields();

        return root;
    }

    private void initFields() {
        PropertysetItem fieldValues = (PropertysetItem) getPropertyDataSource().getValue();

        for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
            Field<?> field = createLocalField(fieldDefinition);
            if (fieldValues.getItemProperty(fieldDefinition.getName()) != null) {
                field.setPropertyDataSource(fieldValues.getItemProperty(fieldDefinition.getName()));
            } else {
                fieldValues.addItemProperty(fieldDefinition.getName(), field.getPropertyDataSource());
            }
            field.addValueChangeListener(selectionListener);
            root.addComponent(field);
        }
    }

    /**
     * Listener used to update the Data source property.
     */
    private Property.ValueChangeListener selectionListener = new ValueChangeListener() {
        @Override
        public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
            PropertysetItem fieldValues = (PropertysetItem) getPropertyDataSource().getValue();
            getPropertyDataSource().setValue(fieldValues);
        }
    };

    private Field<?> createLocalField(ConfiguredFieldDefinition fieldDefinition) {
        NullItem item = new NullItem();
        FieldFactory fieldfactory = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
        fieldfactory.setComponentProvider(componentProvider);
        fieldfactory.setI18nContentSupport(i18nContentSupport);
        // Dirty Hack
        fieldDefinition.setI18nBasename(definition.getI18nBasename());
        Field<?> field = fieldfactory.createField();
        return (Field<?>) field;
    }


    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }


}
