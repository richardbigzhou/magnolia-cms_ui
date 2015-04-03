/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.ui.form.field.definition.StaticFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for the StaticField.
 */
public class StaticFieldBuilder extends AbstractFieldBuilder {

    private final StaticFieldDefinition definition = new StaticFieldDefinition();

    public StaticFieldBuilder(String name) {
        this.definition().setName(name);
    }

    @Override
    public StaticFieldDefinition definition() {
        return this.definition;
    }

    public StaticFieldBuilder value(String value) {
        definition().setValue(value);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public StaticFieldBuilder label(String label) {
        return (StaticFieldBuilder) super.label(label);
    }

    @Override
    public StaticFieldBuilder i18nBasename(String i18nBasename) {
        return (StaticFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public StaticFieldBuilder i18n(boolean i18n) {
        return (StaticFieldBuilder) super.i18n(i18n);
    }

    @Override
    public StaticFieldBuilder i18n() {
        return (StaticFieldBuilder) super.i18n();
    }

    @Override
    public StaticFieldBuilder description(String description) {
        return (StaticFieldBuilder) super.description(description);
    }

    @Override
    public StaticFieldBuilder type(String type) {
        return (StaticFieldBuilder) super.type(type);
    }

    @Override
    public StaticFieldBuilder required(boolean required) {
        return (StaticFieldBuilder) super.required(required);
    }

    @Override
    public StaticFieldBuilder required() {
        return (StaticFieldBuilder) super.required();
    }

    @Override
    public StaticFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (StaticFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public StaticFieldBuilder readOnly(boolean readOnly) {
        return (StaticFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public StaticFieldBuilder readOnly() {
        return (StaticFieldBuilder) super.readOnly();
    }

    @Override
    public StaticFieldBuilder defaultValue(String defaultValue) {
        return (StaticFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public StaticFieldBuilder styleName(String styleName) {
        return (StaticFieldBuilder) super.styleName(styleName);
    }

    @Override
    public StaticFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (StaticFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public StaticFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (StaticFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public StaticFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (StaticFieldBuilder) super.transformerClass(transformerClass);
    }
}
