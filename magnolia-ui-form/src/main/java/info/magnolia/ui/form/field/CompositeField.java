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
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

/**
 * Generic Composite Field.<br>
 * This generic Composite Field allows to handle multiple {@link ConfiguredFieldDefinition} as a single Field:<br>
 * The Field is build based on a generic {@link ConfiguredFieldDefinition}.<br>
 * The Field values are handle by a configured {@link info.magnolia.ui.form.field.property.PropertyHandler} dedicated to create/retrieve properties as {@link PropertysetItem}.<br>
 */
public class CompositeField extends AbstractCustomMultiField<CompositeFieldDefinition, PropertysetItem> {

    private HorizontalLayout root;

    public CompositeField(CompositeFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem) {
        super(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, relatedFieldItem);
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        root = new HorizontalLayout();
        root.setWidth("520px");

        // Initialize Existing field
        initFields();

        return root;
    }

    private void initFields() {
        PropertysetItem fieldValues = (PropertysetItem) getPropertyDataSource().getValue();

        for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
            Field<?> field = createLocalField(fieldDefinition, relatedFieldItem, false);
            if (fieldValues.getItemProperty(fieldDefinition.getName()) != null) {
                field.setPropertyDataSource(fieldValues.getItemProperty(fieldDefinition.getName()));
            } else {
                fieldValues.addItemProperty(fieldDefinition.getName(), field.getPropertyDataSource());
            }
            field.addValueChangeListener(selectionListener);
            root.addComponent(field);
        }
    }


    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }

}
