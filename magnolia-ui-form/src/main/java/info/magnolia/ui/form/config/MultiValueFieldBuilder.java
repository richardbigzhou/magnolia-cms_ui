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

import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a multi field definition.
 */
public class MultiValueFieldBuilder extends AbstractFieldBuilder {

    private MultiValueFieldDefinition definition = new MultiValueFieldDefinition();

    public MultiValueFieldBuilder(String name) {
        definition().setName(name);
    }

    @Override
    public MultiValueFieldDefinition definition() {
        return definition;
    }

    public MultiValueFieldBuilder field(AbstractFieldBuilder fieldBuilder) {
        definition().setField(fieldBuilder.definition());
        return this;
    }

    public MultiValueFieldBuilder buttonSelectRemoveLabel(String buttonSelectRemoveLabel) {
        definition().setButtonSelectRemoveLabel(buttonSelectRemoveLabel);
        return this;
    }

    public MultiValueFieldBuilder buttonSelectAddLabel(String buttonSelectAddLabel) {
        definition().setButtonSelectAddLabel(buttonSelectAddLabel);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public MultiValueFieldBuilder label(String label) {
        return (MultiValueFieldBuilder) super.label(label);
    }

    @Override
    public MultiValueFieldBuilder i18nBasename(String i18nBasename) {
        return (MultiValueFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public MultiValueFieldBuilder i18n(boolean i18n) {
        return (MultiValueFieldBuilder) super.i18n(i18n);
    }

    @Override
    public MultiValueFieldBuilder i18n() {
        return (MultiValueFieldBuilder) super.i18n();
    }

    @Override
    public MultiValueFieldBuilder description(String description) {
        return (MultiValueFieldBuilder) super.description(description);
    }

    @Override
    public MultiValueFieldBuilder type(String type) {
        return (MultiValueFieldBuilder) super.type(type);
    }

    @Override
    public MultiValueFieldBuilder required(boolean required) {
        return (MultiValueFieldBuilder) super.required(required);
    }

    @Override
    public MultiValueFieldBuilder required() {
        return (MultiValueFieldBuilder) super.required();
    }

    @Override
    public MultiValueFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (MultiValueFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public MultiValueFieldBuilder readOnly(boolean readOnly) {
        return (MultiValueFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public MultiValueFieldBuilder readOnly() {
        return (MultiValueFieldBuilder) super.readOnly();
    }

    @Override
    public MultiValueFieldBuilder defaultValue(String defaultValue) {
        return (MultiValueFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public MultiValueFieldBuilder styleName(String styleName) {
        return (MultiValueFieldBuilder) super.styleName(styleName);
    }

    @Override
    public MultiValueFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (MultiValueFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public MultiValueFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (MultiValueFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public MultiValueFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (MultiValueFieldBuilder) super.transformerClass(transformerClass);
    }
}
