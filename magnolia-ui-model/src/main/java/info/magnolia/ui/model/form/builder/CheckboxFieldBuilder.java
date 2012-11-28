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
package info.magnolia.ui.model.form.builder;

import info.magnolia.ui.model.field.definition.CheckboxFieldDefinition;
import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for the Checkbox field.
 */
public class CheckboxFieldBuilder extends AbstractFieldBuilder {

    private final CheckboxFieldDefinition definition = new CheckboxFieldDefinition();

    public CheckboxFieldBuilder(String name) {
        this.definition.setName(name);
    }

    @Override
    protected CheckboxFieldDefinition getDefinition() {
        return definition;
    }

    public CheckboxFieldBuilder selected(String selected) {
        getDefinition().setSelected(selected);
        return this;
    }

    public CheckboxFieldBuilder buttonLabel(String buttonLabel) {
        getDefinition().setButtonLabel(buttonLabel);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public CheckboxFieldBuilder styleName(String styleName) {
        return (CheckboxFieldBuilder) super.styleName(styleName);
    }

    @Override
    public CheckboxFieldBuilder i18n(boolean i18n) {
        return (CheckboxFieldBuilder) super.i18n(i18n);
    }

    @Override
    public CheckboxFieldBuilder i18n() {
        return (CheckboxFieldBuilder) super.i18n();
    }

    @Override
    public CheckboxFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (CheckboxFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public CheckboxFieldBuilder readOnly(boolean readOnly) {
        return (CheckboxFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public CheckboxFieldBuilder readOnly() {
        return (CheckboxFieldBuilder) super.readOnly();
    }

    @Override
    public CheckboxFieldBuilder label(String label) {
        return (CheckboxFieldBuilder) super.label(label);
    }

    @Override
    public CheckboxFieldBuilder i18nBasename(String i18nBasename) {
        return (CheckboxFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public CheckboxFieldBuilder description(String description) {
        return (CheckboxFieldBuilder) super.description(description);
    }

    @Override
    public CheckboxFieldBuilder type(String type) {
        return (CheckboxFieldBuilder) super.type(type);
    }

    @Override
    public CheckboxFieldBuilder required(boolean required) {
        return (CheckboxFieldBuilder) super.required(required);
    }

    @Override
    public CheckboxFieldBuilder required() {
        return (CheckboxFieldBuilder) super.required();
    }

    @Override
    public CheckboxFieldBuilder defaultValue(String defaultValue) {
        return (CheckboxFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public CheckboxFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (CheckboxFieldBuilder) super.validator(validatorDefinition);
    }

}
