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

import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a text field definition.
 */
public class TextFieldBuilder extends AbstractFieldBuilder {

    private final TextFieldDefinition definition = new TextFieldDefinition();

    public TextFieldBuilder(String name) {
        this.definition().setName(name);
    }

    @Override
    public TextFieldDefinition definition() {
        return definition;
    }

    public TextFieldBuilder rows(int rows) {
        definition().setRows(rows);
        return this;
    }

    public TextFieldBuilder maxLength(int maxLength) {
        definition().setMaxLength(maxLength);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public TextFieldBuilder label(String label) {
        return (TextFieldBuilder) super.label(label);
    }

    @Override
    public TextFieldBuilder i18nBasename(String i18nBasename) {
        return (TextFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public TextFieldBuilder i18n(boolean i18n) {
        return (TextFieldBuilder) super.i18n(i18n);
    }

    @Override
    public TextFieldBuilder i18n() {
        return (TextFieldBuilder) super.i18n();
    }

    @Override
    public TextFieldBuilder description(String description) {
        return (TextFieldBuilder) super.description(description);
    }

    @Override
    public TextFieldBuilder type(String type) {
        return (TextFieldBuilder) super.type(type);
    }

    @Override
    public TextFieldBuilder required(boolean required) {
        return (TextFieldBuilder) super.required(required);
    }

    @Override
    public TextFieldBuilder required() {
        return (TextFieldBuilder) super.required();
    }

    @Override
    public TextFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (TextFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public TextFieldBuilder readOnly(boolean readOnly) {
        return (TextFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public TextFieldBuilder readOnly() {
        return (TextFieldBuilder) super.readOnly();
    }

    @Override
    public TextFieldBuilder defaultValue(String defaultValue) {
        return (TextFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public TextFieldBuilder styleName(String styleName) {
        return (TextFieldBuilder) super.styleName(styleName);
    }

    @Override
    public TextFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (TextFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public TextFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (TextFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public TextFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (TextFieldBuilder) super.transformerClass(transformerClass);
    }
}
