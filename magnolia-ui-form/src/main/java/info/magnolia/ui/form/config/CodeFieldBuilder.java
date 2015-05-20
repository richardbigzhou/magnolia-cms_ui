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

import info.magnolia.ui.form.field.definition.CodeFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for a {@link CodeFieldDefinition}.
 */
public class CodeFieldBuilder extends AbstractFieldBuilder {

    private CodeFieldDefinition definition = new CodeFieldDefinition();

    public CodeFieldBuilder(String name) {
        definition.setName(name);
    }

    @Override
    public CodeFieldDefinition definition() {
        return definition;
    }

    public CodeFieldBuilder language(String language) {
        definition().setLanguage(language);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public CodeFieldBuilder label(String label) {
        return (CodeFieldBuilder) super.label(label);
    }

    @Override
    public CodeFieldBuilder i18nBasename(String i18nBasename) {
        return (CodeFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public CodeFieldBuilder i18n(boolean i18n) {
        return (CodeFieldBuilder) super.i18n(i18n);
    }

    @Override
    public CodeFieldBuilder i18n() {
        return (CodeFieldBuilder) super.i18n();
    }

    @Override
    public CodeFieldBuilder description(String description) {
        return (CodeFieldBuilder) super.description(description);
    }

    @Override
    public CodeFieldBuilder type(String type) {
        return (CodeFieldBuilder) super.type(type);
    }

    @Override
    public CodeFieldBuilder required(boolean required) {
        return (CodeFieldBuilder) super.required(required);
    }

    @Override
    public CodeFieldBuilder required() {
        return (CodeFieldBuilder) super.required();
    }

    @Override
    public CodeFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (CodeFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public CodeFieldBuilder readOnly(boolean readOnly) {
        return (CodeFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public CodeFieldBuilder readOnly() {
        return (CodeFieldBuilder) super.readOnly();
    }

    @Override
    public CodeFieldBuilder defaultValue(String defaultValue) {
        return (CodeFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public CodeFieldBuilder styleName(String styleName) {
        return (CodeFieldBuilder) super.styleName(styleName);
    }

    @Override
    public CodeFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (CodeFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public CodeFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (CodeFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public CodeFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (CodeFieldBuilder) super.transformerClass(transformerClass);
    }
}
