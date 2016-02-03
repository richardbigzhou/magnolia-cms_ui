/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.form.field.factory;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAwareProperty;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.form.AbstractFormItem;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.validator.definition.FieldValidatorDefinition;
import info.magnolia.ui.form.validator.factory.FieldValidatorFactory;
import info.magnolia.ui.form.validator.registry.FieldValidatorFactoryFactory;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;

/**
 * Abstract FieldFactory implementations. This class handle all common attributes defined in {@link FieldDefinition} and binds Vaadin {@link Field} instances created
 * by subclasses to the {@link Property} they will be reading and writing to.
 *
 * @param <D> definition type
 * @param <T> field value type
 */
public abstract class AbstractFieldFactory<D extends FieldDefinition, T> extends AbstractFormItem implements FieldFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractFieldFactory.class);
    protected Item item;
    protected Field<T> field;
    protected D definition;
    private FieldValidatorFactoryFactory fieldValidatorFactoryFactory;
    private I18nContentSupport i18nContentSupport;
    private ComponentProvider componentProvider;

    public AbstractFieldFactory(D definition, Item relatedFieldItem) {
        this.definition = definition;
        this.item = relatedFieldItem;
    }

    @Override
    public void setFieldValidatorFactoryFactory(FieldValidatorFactoryFactory fieldValidatorFactoryFactory) {
        this.fieldValidatorFactoryFactory = fieldValidatorFactoryFactory;
    }

    @Override
    public void setI18nContentSupport(I18nContentSupport i18nContentSupport) {
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    public Field<T> createField() {
        if (field == null) {
            // Create the Vaadin field
            this.field = createFieldComponent();

            Property<?> property = getOrCreateProperty();
            setPropertyDataSource(property);

            if (StringUtils.isNotBlank(definition.getStyleName())) {
                this.field.addStyleName(definition.getStyleName());
            }

            field.setWidth(100, Unit.PERCENTAGE);

            // Set label and required marker
            this.field.setCaption(getMessage(getFieldDefinition().getLabel()) + (getFieldDefinition().isRequired() ? "<span class=\"requiredfield\">*</span>" : ""));

            setConstraints();

        }
        return this.field;
    }

    @Override
    public D getFieldDefinition() {
        return this.definition;
    }

    /**
     * Set the DataSource of the current field.
     */
    public void setPropertyDataSource(Property<?> property) {
        this.field.setPropertyDataSource(property);
    }

    /**
     * Implemented by subclasses to create and initialize the Vaadin Field instance to use.
     */
    protected abstract Field<T> createFieldComponent();

    @Override
    public View getView() {
        Property<?> property = getOrCreateProperty();

        final CssLayout fieldView = new CssLayout();
        fieldView.setStyleName("field-view");

        Label label = new Label();
        label.setSizeUndefined();
        label.setCaption(getFieldDefinition().getLabel());

        if (getFieldDefinition().getClass().isAssignableFrom(TextFieldDefinition.class)) {
            final TextFieldDefinition textFieldDefinition = (TextFieldDefinition) getFieldDefinition();
            if (textFieldDefinition.getRows() > 0) {
                label.addStyleName("textarea");
            }
        }
        label.setPropertyDataSource(property);

        fieldView.addComponent(label);

        return new View() {
            @Override
            public Component asVaadinComponent() {
                return fieldView;
            }
        };
    }

    /**
     * Get a property from the current Item.
     * <p>
     *     if the field is i18n-aware - create a special property that would delegate
     *     the values to the proper localized properties. Otherwise - follow the default pattern.
     * </p>
     *
     * <p>
     * If the property already exists, return this property.
     * If the property does not exist, create a new property based on the defined type, default value, and saveInfo.
     * </p>
     */
    protected Property<?> getOrCreateProperty() {
        String propertyName = definition.getName();
        Class<?> fieldType = getFieldType(definition);
        String defaultValue = definition.getDefaultValue();
        if (definition.isI18n()) {
            I18NAwareProperty<String> property = componentProvider.newInstance(I18NAwareProperty.class, propertyName, fieldType, item);
            property.setDefaultValue(defaultValue);
            return property;

        } else {
            Property<?> property = item.getItemProperty(propertyName);
            if (property == null) {
                property = DefaultPropertyUtil.newDefaultProperty(fieldType.getSimpleName(), defaultValue);
                item.addItemProperty(propertyName, property);
            }
            return property;
        }
    }

    /**
     * Return the Class field Type if define in the configuration.
     * If the Type is not defined in the configuration or not of a supported type, throws
     * a {@link IllegalArgumentException}:
     */
    protected Class<?> getFieldType(FieldDefinition fieldDefinition) {
        if (StringUtils.isNotBlank(fieldDefinition.getType())) {
            return DefaultPropertyUtil.getFieldTypeClass(fieldDefinition.getType());
        }
        return getDefaultFieldType(fieldDefinition);
    }

    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }

    /**
     * Returns the field related node.
     * If field is of type JcrNewNodeAdapter then return the parent node.
     * Else get the node associated with the Vaadin item.
     */
    protected Node getRelatedNode(Item fieldRelatedItem) throws RepositoryException {
        return (fieldRelatedItem instanceof JcrNewNodeAdapter) ? ((JcrNewNodeAdapter) fieldRelatedItem).getJcrItem() : ((JcrNodeAdapter) fieldRelatedItem).applyChanges();
    }

    public String getPropertyName() {
        return definition.getName();
    }

    @Override
    protected String getI18nBasename() {
        return definition.getI18nBasename();
    }

    /**
     * Set all constraints linked to the field:
     * Build Validation rules.
     * Set Required field.
     * Set Read Only.
     */
    private void setConstraints() {
        // Set Validation
        for (FieldValidatorDefinition validatorDefinition : definition.getValidators()) {
            FieldValidatorFactory validatorFactory = this.fieldValidatorFactoryFactory.createFieldValidatorFactory(validatorDefinition, item);
            if (validatorFactory != null) {
                this.field.addValidator(validatorFactory.createValidator());
            } else {
                log.warn("Not able to create Validation for the following definition {}", definition.toString());
            }
        }
        // Set Required
        if (definition.isRequired()) {
            field.setRequired(true);
            field.setRequiredError(getMessage(definition.getRequiredErrorMessage()));
        }

        // Set ReadOnly (field property has to be updated)
        if (field.getPropertyDataSource() != null) {
            field.getPropertyDataSource().setReadOnly(definition.isReadOnly());
        }
    }

    @Override
    public void setComponentProvider(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }
}
