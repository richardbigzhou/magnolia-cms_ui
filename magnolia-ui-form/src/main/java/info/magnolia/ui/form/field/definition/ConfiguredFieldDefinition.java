/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.form.field.definition;

import info.magnolia.ui.form.validator.definition.FieldValidatorDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a field in a dialog.
 */
public class ConfiguredFieldDefinition implements FieldDefinition {

    private String name;

    private String label;

    private String i18nBasename;

    private boolean i18n = false;

    private String description; // not relevant for controlType=static

    private String type; // JCR Property type name see javax.jcr.PropertyType

    private boolean required = false; // Not relevant for checkbox

    private String requiredErrorMessage = "validation.message.required";

    private boolean readOnly = false; // Specify if the property has to be saved

    private String defaultValue; // Specify the default value

    private String styleName;

    private List<FieldValidatorDefinition> validators = new ArrayList<FieldValidatorDefinition>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public String getRequiredErrorMessage() {
        return requiredErrorMessage;
    }

    @Override
    public List<FieldValidatorDefinition> getValidators() {
        return validators;
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public boolean isI18n() {
        return i18n;
    }

    @Override
    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public void setI18n(boolean i18n) {
        this.i18n = i18n;
    }

    public void setRequiredErrorMessage(String requiredErrorMessage) {
        this.requiredErrorMessage = requiredErrorMessage;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setValidators(List<FieldValidatorDefinition> validators) {
        this.validators = validators;
    }

    public void addValidator(FieldValidatorDefinition validator) {
        validators.add(validator);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
