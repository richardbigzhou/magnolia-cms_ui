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
package info.magnolia.ui.form.config;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.validation.ConfiguredFieldValidatorDefinition;

/**
 * Builder for creating a field using only the standard properties.
 *
 * @param <T> type of field definition
 * @see FieldsConfig#custom(info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition)
 */
public class GenericFieldBuilder<T extends ConfiguredFieldDefinition> extends AbstractFieldBuilder {

    private final T definition;

    public GenericFieldBuilder(T definition) {
        this.definition = definition;
    }

    @Override
    protected T getDefinition() {
        return definition;
    }

    public GenericFieldBuilder<T> name(String name) {
        this.definition.setName(name);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public GenericFieldBuilder<T> styleName(String styleName) {
        super.styleName(styleName);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> i18n(boolean i18n) {
        super.i18n(i18n);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> i18n() {
        super.i18n();
        return this;
    }

    @Override
    public GenericFieldBuilder<T> requiredErrorMessage(String requiredErrorMessage) {
        super.requiredErrorMessage(requiredErrorMessage);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> readOnly(boolean readOnly) {
        super.readOnly(readOnly);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> readOnly() {
        super.readOnly();
        return this;
    }

    @Override
    public GenericFieldBuilder<T> label(String label) {
        super.label(label);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> i18nBasename(String i18nBasename) {
        super.i18nBasename(i18nBasename);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> description(String description) {
        super.description(description);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> type(String type) {
        super.type(type);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> required(boolean required) {
        super.required(required);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> required() {
        super.required();
        return this;
    }

    @Override
    public GenericFieldBuilder<T> defaultValue(String defaultValue) {
        super.defaultValue(defaultValue);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        super.validator(validatorDefinition);
        return this;
    }

    @Override
    public GenericFieldBuilder<T> validator(GenericValidatorBuilder validatorBuilder) {
        getDefinition().addValidator(validatorBuilder.exec());
        return this;
    }
}
