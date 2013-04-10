/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.form.field.builder;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.ui.form.AbstractFormItem;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.vaadin.form.i18n.I18NAwareProperty;
import info.magnolia.ui.form.field.validation.FieldValidatorBuilder;
import info.magnolia.ui.form.field.validation.FieldValidatorDefinition;
import info.magnolia.ui.form.field.validation.ValidatorFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Abstract FieldBuilder implementations. This class handle all common attributes defined in {@link FieldDefinition} and binds Vaadin {@link Field} instances created
 * by subclasses to the {@link Property} they will be reading and writing to.
 *
 * @param <D> definition type
 * @param <T> field value type
 */
public abstract class AbstractFieldBuilder<D extends FieldDefinition, T> extends AbstractFormItem implements FieldBuilder {
    private static final Logger log = LoggerFactory.getLogger(AbstractFieldBuilder.class);
    protected Item item;
    protected Field<T> field;
    protected D definition;
    private ValidatorFieldFactory validatorFieldFactory;
    private I18nContentSupport i18nContentSupport;

    public AbstractFieldBuilder(D definition, Item relatedFieldItem) {
        this.definition = definition;
        this.item = relatedFieldItem;
    }

    @Override
    public void setValidatorFieldFactory(ValidatorFieldFactory validatorFieldFactory) {
        this.validatorFieldFactory = validatorFieldFactory;
    }

    @Override
    public void setI18nContentSupport(I18nContentSupport i18nContentSupport) {
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    public Field<T> getField() {
        if (field == null) {

            // Build the Vaadin field
            this.field = buildField();

            // Get and set the DataSource property
            // Set i18n property name
            Property<?> property = getOrCreateProperty();
            setPropertyDataSource(property);

            // TODO fgrilli review: do we really want to provide users with the possibility
            // of defining their custom styles risking that they screw up AdminCentral look&feel?
            if (StringUtils.isNotBlank(definition.getStyleName())) {
                this.field.addStyleName(definition.getStyleName());
            }

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
    protected abstract Field<T> buildField();

    /**
     * Get a property from the current Item.
     * If the property already exists, return this property.
     * <p>
     * If the property does not exist, create a new property based on the defined type, default value, and saveInfo.
     */
    protected Property<?> getOrCreateProperty() {
        if (definition.isI18n()) {
            return new I18NAwareProperty(definition.getName(), (JcrItemNodeAdapter) item, i18nContentSupport );
        } else {
            String propertyName = definition.getName();
            Property<?> property = item.getItemProperty(propertyName);
            if (property == null) {
                property = DefaultPropertyUtil.newDefaultProperty(propertyName, getFieldType(definition).getSimpleName(), definition.getDefaultValue());
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
        throw new IllegalArgumentException("Unsupported type " + fieldDefinition.getClass().getName());
    }

    /**
     * Returns the field related node.
     * If field is of type JcrNewNodeAdapter then return the parent node.
     * Else get the node associated with the Vaadin item.
     */
    protected Node getRelatedNode(Item fieldRelatedItem) {
        if (fieldRelatedItem instanceof JcrNewNodeAdapter) {
            return ((JcrNewNodeAdapter) fieldRelatedItem).getParentNode();
        } else {
            return ((JcrNodeAdapter) fieldRelatedItem).getNode();
        }
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
            FieldValidatorBuilder validatorBuilder = this.validatorFieldFactory.create(validatorDefinition);
            if (validatorBuilder != null) {
                this.field.addValidator(validatorBuilder.buildValidator());
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
}
