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
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.integration.ItemAdapter;
import info.magnolia.ui.vaadin.integration.NullItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HasComponents;

/**
 * Abstract implementation of {@link CustomField} used for multi fields components.<br>
 * It expose generic methods allowing to: <br>
 * - Build a {@link Field} based on a {@link ConfiguredFieldDefinition}. <br>
 * - Retrieve the list of Fields contained into the main component <br>
 * - Override Validate and get Error Message in order to include these call to the embedded Fields.<br>
 *
 * @param <T> Property Type linked to this Field.
 * @param <D> FieldDefinition Implementation used by the implemented Field.
 */
public abstract class AbstractCustomMultiField<D extends FieldDefinition, T> extends CustomField<T> {

    protected final FieldFactoryFactory fieldFactoryFactory;
    protected final I18nContentSupport i18nContentSupport;
    protected final ComponentProvider componentProvider;
    protected final D definition;
    protected final Item relatedFieldItem;
    protected AbstractOrderedLayout root;

    protected AbstractCustomMultiField(D definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem) {
        this.definition = definition;
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
        this.relatedFieldItem = relatedFieldItem;
    }

    /**
     * Initialize the fields based on the newValues.<br>
     * Implemented logic should: <br>
     * - remove all component from the root component. <br>
     * - for every fieldValues value, add the appropriate field.<br>
     * - add all others needed component (like add button...)
     */
    protected abstract void initFields(T fieldValues);

    /**
     * Handle {@link info.magnolia.ui.api.i18n.I18NAuthoringSupport#i18nize(HasComponents, Locale)} events in order to refresh the field <br>
     * and display the new property.
     */
    @Override
    public void setLocale(Locale locale) {
        if (root != null) {
            initFields();
        }
    }

    @SuppressWarnings("unchecked")
    protected void initFields() {
        T fieldValues = (T) getPropertyDataSource().getValue();
        initFields(fieldValues);
        // Update DataSource in order to handle the fields default values
        if (relatedFieldItem instanceof ItemAdapter && ((ItemAdapter) relatedFieldItem).isNew()) {
            getPropertyDataSource().setValue(getValue());
        }
    }

    /**
     * Create a new {@link Field} based on a {@link ConfiguredFieldDefinition}.
     */
    protected Field<?> createLocalField(ConfiguredFieldDefinition fieldDefinition, Property<?> property, boolean setCaptionToNull) {

        // If the property holds an item, use this item directly for the field creation (doesn't apply to ProperysetItems)
        FieldFactory fieldfactory = fieldFactoryFactory.createFieldFactory(fieldDefinition, holdsItem(property) ? property.getValue() : new NullItem());
        fieldfactory.setComponentProvider(componentProvider);
        fieldfactory.setI18nContentSupport(i18nContentSupport);
        // FIXME change i18n setting : MGNLUI-1548
        fieldDefinition.setI18nBasename(definition.getI18nBasename());
        Field<?> field = fieldfactory.createField();

        // If the value property is not an Item but a property, set this property as datasource to the field
        // and add a value change listener in order to propagate changes
        if (!holdsItem(property)) {
            if (property != null && property.getValue() != null) {
                field.setPropertyDataSource(property);
            }
            field.addValueChangeListener(selectionListener);
        }

        if (field instanceof AbstractComponent) {
            ((AbstractComponent) field).setImmediate(true);
        }
        // Set Caption if desired
        if (setCaptionToNull) {
            field.setCaption(null);
        } else if (StringUtils.isNotBlank(fieldDefinition.getLabel())) {
            field.setCaption(fieldDefinition.getLabel());
        }

        field.setWidth(100, Unit.PERCENTAGE);
        return field;
    }

    boolean holdsItem(Property<?> property) {
        return property != null && property.getValue() instanceof Item && !(property.getValue() instanceof PropertysetItem);
    }

    /**
     * Listener used to update the Data source property.
     */
    protected Property.ValueChangeListener selectionListener = new ValueChangeListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
            fireValueChange(false);
            // In case PropertysetItem is used as property set of field's property, in case an individual field is updated, the PropertysetItem is coherent (has also the changes)
            // but the setValue on the property is never called.
            getPropertyDataSource().setValue(getValue());
        }
    };

    /**
     * Utility method that return a list of Fields embedded into a root custom field.
     *
     * @param root
     * @param onlyValid if set to true, return only the isValid() fields.
     */
    @SuppressWarnings("unchecked")
    protected List<AbstractField<T>> getFields(HasComponents root, boolean onlyValid) {
        Iterator<Component> it = root.iterator();
        List<AbstractField<T>> fields = new ArrayList<AbstractField<T>>();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof AbstractField) {
                if (!onlyValid || (onlyValid && ((AbstractField<T>) c).isValid())) {
                    fields.add((AbstractField<T>) c);
                }
            } else if (c instanceof HasComponents) {
                fields.addAll(getFields((HasComponents) c, onlyValid));
            }
        }
        return fields;
    }

    /**
     * Validate all fields from the root container.
     */
    @Override
    public boolean isValid() {
        boolean isValid = true;
        List<AbstractField<T>> fields = getFields(this, false);
        for (AbstractField<T> field : fields) {
            isValid = field.isValid();
            if (!isValid) {
                return isValid;
            }
        }
        return isValid;
    }

    /**
     * Get the error message.
     */
    @Override
    public ErrorMessage getErrorMessage() {
        ErrorMessage errorMessage = null;
        List<AbstractField<T>> fields = getFields(this, false);
        for (AbstractField<T> field : fields) {
            errorMessage = field.getErrorMessage();
            if (errorMessage != null) {
                return errorMessage;
            }
        }
        return errorMessage;
    }

    @Override
    protected boolean isEmpty() {
        boolean isEmpty = false;
        List<AbstractField<T>> fields = getFields(this, false);
        for (AbstractField<T> field : fields) {
            isEmpty = field.getValue() == null;
            if (isEmpty) {
                return isEmpty;
            }
        }
        return isEmpty;
    }

}
