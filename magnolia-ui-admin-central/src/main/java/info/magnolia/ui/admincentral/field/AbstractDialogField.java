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
package info.magnolia.ui.admincentral.field;

import info.magnolia.ui.model.dialog.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.dialog.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.RegexpValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.ValidatorDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Field;

/**
 * Define an Abstract implementation of {@link FieldDefinition}.
 * Initialize the common attributes of a Field definition.
 * Initialize the Field wit ValidationRules, Related DataSource.
 * @param <D>.
 */
public abstract class AbstractDialogField<D extends FieldDefinition> implements DialogField {

    protected Field dialogField = null;
    private D definition;
    static final String REQUIRED_ERROR = "This field is required! (to be i18n'd)";
    protected Item item;


    public AbstractDialogField(D definition, Item relatedFieldItem) {
        this.definition = definition;
        this.item = relatedFieldItem;
        // Create the Vaadin Field
        this.dialogField = buildField();

        //Get the Datasource Field property . Id the property does not yet exist,
        //Create a new Property and Set default Value and Type
        Property property = getOrCreateProperty();
        setPropertyDataSource(property);

        addValidatorsAndRequiredElements(definition, dialogField);

    }


    @Override
    public Field getField() {
        return dialogField;
    }

    @Override
    public FieldDefinition getFieldDefinition() {
        return this.definition;
    }

    /**
     * Set the Datasource of the current field.
     */
    public void setPropertyDataSource(Property property) {
        this.dialogField.setPropertyDataSource(property);
    }

    protected abstract Field buildField();


    /**
     * Get a property from the current Item.
     * If the property already exist, return this property.
     * If the property does not exist:
     *          Create a new property based on the defined Type, default value, and saveInfo.
     */
    private Property getOrCreateProperty() {
        DefaultProperty property = (DefaultProperty)item.getItemProperty(definition.getName());
        if(property == null){
            property = DefaultPropertyUtil.newDefaultProperty(definition.getName(), getFieldType(definition).getSimpleName(), definition.getDefaultValue());
            property.setSaveInfo(definition.getSaveInfo());
            item.addItemProperty(definition.getName(), property);
        }
        return property;
    }

    /**
     * Return the Class field Type if define in the configuration.
     * If the Type is not defined in the configuration or not of a supported type, throws
     * a {@link IllegalArgumentException}:
     */
    protected Class<?> getFieldType(FieldDefinition fieldDefinition) {
        if(StringUtils.isNotBlank(fieldDefinition.getType())) {
            return DefaultPropertyUtil.getFieldTypeClass(fieldDefinition.getType());
        }
        return getDefaultFieldType(fieldDefinition);
    }

    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        throw new IllegalArgumentException("Unsupported type " + fieldDefinition.getClass().getName());
    }

    /**
     * Add Validation rules and mandatory field info
     * to the Vaadin Field.
     */
    private void addValidatorsAndRequiredElements(FieldDefinition fieldDefinition, Field input) {
        addValidators(fieldDefinition, input);
        if(fieldDefinition.isRequired()) {
            addRequired(input, null);
        }
    }

    /**
     * Add Validators.
     */
    private void addValidators(FieldDefinition fieldDefinition, final Field input) {
        Validator vaadinValidator = null;
        for (ValidatorDefinition current: ((ConfiguredFieldDefinition) fieldDefinition).getValidators()) {
            // TODO dlipp - this is what was defined for Sprint III. Of course this has to be enhanced later - when we have a better picture of how we want to validate.
            if (current instanceof EmailValidatorDefinition) {
                EmailValidatorDefinition def = (EmailValidatorDefinition) current;
                vaadinValidator = new EmailValidator(def.getErrorMessage());
            } else if (current instanceof RegexpValidatorDefinition) {
                RegexpValidatorDefinition def = (RegexpValidatorDefinition) current;
                vaadinValidator = new RegexpValidator(def.getPattern(), def.getErrorMessage());
            }

            if (vaadinValidator != null) {
                input.addValidator(vaadinValidator);
            }
        }
    }

    /**
     * Add Required on fields.
     */
    private void addRequired( Field input, String message) {
        input.setRequired(true);
        if(StringUtils.isEmpty(message)) {
            input.setRequiredError(REQUIRED_ERROR);
        } else {
            input.setRequiredError(message);
        }
    }

}
