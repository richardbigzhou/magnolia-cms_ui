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
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Generic Composite Field.<br>
 * This generic Composite Field allows to handle multiple {@link ConfiguredFieldDefinition} as a single Field:<br>
 * The Field is build based on a generic {@link ConfiguredFieldDefinition}.<br>
 * The Field values are handle by a configured {@link info.magnolia.ui.form.field.transformer.Transformer} dedicated to create/retrieve properties as {@link PropertysetItem}.<br>
 */
public class CompositeField extends AbstractCustomMultiField<CompositeFieldDefinition, PropertysetItem> {

    public CompositeField(CompositeFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, Item relatedFieldItem, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, i18nAuthoringSupport);
    }

    /**
     * @deprecated since 5.3.5 removing i18nContentSupport dependency (actually unused way before that). Besides, fields should use i18nAuthoringSupport for internationalization.
     */
    @Deprecated
    public CompositeField(CompositeFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem) {
        this(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, componentProvider.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        if (definition.getLayout() == Layout.horizontal) {
            root = new HorizontalLayout();
        } else {
            root = new VerticalLayout();
        }

        // Initialize Existing field
        initFields();
        return root;
    }

    @Override
    protected void initFields(PropertysetItem fieldValues) {
        root.removeAllComponents();

        for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
            // Only propagate read only if the parent definition is read only
            if (definition.isReadOnly()) {
                fieldDefinition.setReadOnly(true);
            }
            Field<?> compositeField = createLocalField(fieldDefinition, fieldValues.getItemProperty(fieldDefinition.getName()), false);
            if (fieldValues.getItemProperty(fieldDefinition.getName()) == null) {
                fieldValues.addItemProperty(fieldDefinition.getName(), compositeField.getPropertyDataSource());
            }
            compositeField.setWidth(100, Unit.PERCENTAGE);

            root.addComponent(compositeField);
        }
    }

    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }
}
