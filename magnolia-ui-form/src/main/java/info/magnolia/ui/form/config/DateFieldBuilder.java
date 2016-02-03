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
package info.magnolia.ui.form.config;

import info.magnolia.ui.form.field.definition.DateFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a date field definition.
 */
public class DateFieldBuilder extends AbstractFieldBuilder {

    private DateFieldDefinition definition = new DateFieldDefinition();

    public DateFieldBuilder(String name) {
        definition().setName(name);
    }

    @Override
    public DateFieldDefinition definition() {
        return definition;
    }

    public DateFieldBuilder time() {
        definition().setTime(true);
        return this;
    }

    public DateFieldBuilder time(boolean time) {
        definition().setTime(time);
        return this;
    }

    public DateFieldBuilder dateFormat(String dateFormat) {
        definition().setDateFormat(dateFormat);
        return this;
    }

    public DateFieldBuilder timeFormat(String timeFormat) {
        definition().setTimeFormat(timeFormat);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public DateFieldBuilder label(String label) {
        return (DateFieldBuilder) super.label(label);
    }

    @Override
    public DateFieldBuilder i18nBasename(String i18nBasename) {
        return (DateFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public DateFieldBuilder i18n(boolean i18n) {
        return (DateFieldBuilder) super.i18n(i18n);
    }

    @Override
    public DateFieldBuilder i18n() {
        return (DateFieldBuilder) super.i18n();
    }

    @Override
    public DateFieldBuilder description(String description) {
        return (DateFieldBuilder) super.description(description);
    }

    @Override
    public DateFieldBuilder type(String type) {
        return (DateFieldBuilder) super.type(type);
    }

    @Override
    public DateFieldBuilder required(boolean required) {
        return (DateFieldBuilder) super.required(required);
    }

    @Override
    public DateFieldBuilder required() {
        return (DateFieldBuilder) super.required();
    }

    @Override
    public DateFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (DateFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public DateFieldBuilder readOnly(boolean readOnly) {
        return (DateFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public DateFieldBuilder readOnly() {
        return (DateFieldBuilder) super.readOnly();
    }

    @Override
    public DateFieldBuilder defaultValue(String defaultValue) {
        return (DateFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public DateFieldBuilder styleName(String styleName) {
        return (DateFieldBuilder) super.styleName(styleName);
    }

    @Override
    public DateFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (DateFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public DateFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (DateFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public DateFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (DateFieldBuilder) super.transformerClass(transformerClass);
    }
}
