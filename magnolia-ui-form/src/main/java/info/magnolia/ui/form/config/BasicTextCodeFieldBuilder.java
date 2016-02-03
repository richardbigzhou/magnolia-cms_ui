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

import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a basic text code field definition.
 */
public class BasicTextCodeFieldBuilder extends AbstractFieldBuilder {

    private BasicTextCodeFieldDefinition definition = new BasicTextCodeFieldDefinition();

    public BasicTextCodeFieldBuilder(String name) {
        definition.setName(name);
    }

    @Override
    public BasicTextCodeFieldDefinition definition() {
        return definition;
    }

    public BasicTextCodeFieldBuilder language(String language) {
        definition().setLanguage(language);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public BasicTextCodeFieldBuilder label(String label) {
        return (BasicTextCodeFieldBuilder) super.label(label);
    }

    @Override
    public BasicTextCodeFieldBuilder i18nBasename(String i18nBasename) {
        return (BasicTextCodeFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public BasicTextCodeFieldBuilder i18n(boolean i18n) {
        return (BasicTextCodeFieldBuilder) super.i18n(i18n);
    }

    @Override
    public BasicTextCodeFieldBuilder i18n() {
        return (BasicTextCodeFieldBuilder) super.i18n();
    }

    @Override
    public BasicTextCodeFieldBuilder description(String description) {
        return (BasicTextCodeFieldBuilder) super.description(description);
    }

    @Override
    public BasicTextCodeFieldBuilder type(String type) {
        return (BasicTextCodeFieldBuilder) super.type(type);
    }

    @Override
    public BasicTextCodeFieldBuilder required(boolean required) {
        return (BasicTextCodeFieldBuilder) super.required(required);
    }

    @Override
    public BasicTextCodeFieldBuilder required() {
        return (BasicTextCodeFieldBuilder) super.required();
    }

    @Override
    public BasicTextCodeFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (BasicTextCodeFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public BasicTextCodeFieldBuilder readOnly(boolean readOnly) {
        return (BasicTextCodeFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public BasicTextCodeFieldBuilder readOnly() {
        return (BasicTextCodeFieldBuilder) super.readOnly();
    }

    @Override
    public BasicTextCodeFieldBuilder defaultValue(String defaultValue) {
        return (BasicTextCodeFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public BasicTextCodeFieldBuilder styleName(String styleName) {
        return (BasicTextCodeFieldBuilder) super.styleName(styleName);
    }

    @Override
    public BasicTextCodeFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (BasicTextCodeFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public BasicTextCodeFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (BasicTextCodeFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public BasicTextCodeFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (BasicTextCodeFieldBuilder) super.transformerClass(transformerClass);
    }
}
