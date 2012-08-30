/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.model.field.definition;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.model.dialog.definition.ValidatorDefinition;

/**
 * Describes a field in a dialog.
 */
public class ConfiguredFieldDefinition implements FieldDefinition {

    private String name;

    private String label;

    private String i18nBasename;

    private String description; // not relevant for controlType=static

    private String type; // JCR Property type name see javax.jcr.PropertyType

    private boolean required; // Not relevant for checkbox

    private boolean saveInfo = true; // Specify if the property has to be saved

    private String defaultValue; // Specify the default value

    private List<ValidatorDefinition> validators = new ArrayList<ValidatorDefinition>();

    private boolean hideLabel = false;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public String getRequiredErrorMessage() {
        return MessagesUtil.get("validation.message.required", "info.magnolia.ui.model.messages");
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public List<ValidatorDefinition> getValidators() {
        return validators;
    }

    public void setValidators(List<ValidatorDefinition> validators) {
        this.validators = validators;
    }

    public void addValidator(ValidatorDefinition validator) {
        validators.add(validator);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setSaveInfo(boolean saveInfo) {
        this.saveInfo = saveInfo;
    }

    @Override
    public boolean getSaveInfo() {
        return this.saveInfo;
    }

    @Override
    public boolean isHideLabel() {
        return hideLabel;
    }

    public void setHideLabel(boolean hideLabel) {
        this.hideLabel = hideLabel;
    }

}
