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
package info.magnolia.ui.form.config;

import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

import java.util.Collection;

/**
 * Builder for the OptionGroup field.
 */
public class OptionGroupFieldBuilder extends SelectFieldBuilder {

    private final OptionGroupFieldDefinition definition = new OptionGroupFieldDefinition();

    public OptionGroupFieldBuilder(String name) {
        super();
        this.definition().setName(name);
    }

    public OptionGroupFieldBuilder() {
    }

    @Override
    public OptionGroupFieldDefinition definition() {
        return this.definition;
    }

    public OptionGroupFieldBuilder multiselect() {
        definition().setMultiselect(true);
        return this;
    }

    public OptionGroupFieldBuilder multiselect(boolean multiselect) {
        definition().setMultiselect(multiselect);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public OptionGroupFieldBuilder path(String path) {
        return (OptionGroupFieldBuilder) super.path(path);
    }

    @Override
    public OptionGroupFieldBuilder repository(String repository) {
        return (OptionGroupFieldBuilder) super.repository(repository);
    }

    @Override
    public OptionGroupFieldBuilder valueProperty(String valueProperty) {
        return (OptionGroupFieldBuilder) super.valueProperty(valueProperty);
    }

    @Override
    public OptionGroupFieldBuilder labelProperty(String labelProperty) {
        return (OptionGroupFieldBuilder) super.labelProperty(labelProperty);
    }

    @Override
    public OptionGroupFieldBuilder filteringMode(int filteringMode) {
        return (OptionGroupFieldBuilder) super.filteringMode(filteringMode);
    }

    @Override
    public OptionGroupFieldBuilder options(OptionBuilder... builders) {
        return (OptionGroupFieldBuilder) super.options(builders);
    }

    @Override
    public OptionGroupFieldBuilder options(Collection<?> options) {
        return (OptionGroupFieldBuilder) super.options(options);
    }

    @Override
    public OptionGroupFieldBuilder label(String label) {
        return (OptionGroupFieldBuilder) super.label(label);
    }

    @Override
    public OptionGroupFieldBuilder i18nBasename(String i18nBasename) {
        return (OptionGroupFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public OptionGroupFieldBuilder i18n(boolean i18n) {
        return (OptionGroupFieldBuilder) super.i18n(i18n);
    }

    @Override
    public OptionGroupFieldBuilder i18n() {
        return (OptionGroupFieldBuilder) super.i18n();
    }

    @Override
    public OptionGroupFieldBuilder description(String description) {
        return (OptionGroupFieldBuilder) super.description(description);
    }

    @Override
    public OptionGroupFieldBuilder type(String type) {
        return (OptionGroupFieldBuilder) super.type(type);
    }

    @Override
    public OptionGroupFieldBuilder required(boolean required) {
        return (OptionGroupFieldBuilder) super.required(required);
    }

    @Override
    public OptionGroupFieldBuilder required() {
        return (OptionGroupFieldBuilder) super.required();
    }

    @Override
    public OptionGroupFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (OptionGroupFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public OptionGroupFieldBuilder readOnly(boolean readOnly) {
        return (OptionGroupFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public OptionGroupFieldBuilder readOnly() {
        return (OptionGroupFieldBuilder) super.readOnly();
    }

    @Override
    public OptionGroupFieldBuilder defaultValue(String defaultValue) {
        return (OptionGroupFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public OptionGroupFieldBuilder styleName(String styleName) {
        return (OptionGroupFieldBuilder) super.styleName(styleName);
    }

    @Override
    public OptionGroupFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (OptionGroupFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public OptionGroupFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (OptionGroupFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public OptionGroupFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (OptionGroupFieldBuilder) super.transformerClass(transformerClass);
    }

}
