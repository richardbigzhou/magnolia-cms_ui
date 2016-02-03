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

import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a rich text field definition.
 */
public class RichTextFieldBuilder extends AbstractFieldBuilder {

    private final RichTextFieldDefinition definition = new RichTextFieldDefinition();

    public RichTextFieldBuilder(String name) {
        this.definition().setName(name);
    }

    @Override
    public RichTextFieldDefinition definition() {
        return definition;
    }

    public RichTextFieldBuilder images(boolean images) {
        definition().setImages(images);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public RichTextFieldBuilder label(String label) {
        return (RichTextFieldBuilder) super.label(label);
    }

    @Override
    public RichTextFieldBuilder i18nBasename(String i18nBasename) {
        return (RichTextFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public RichTextFieldBuilder i18n(boolean i18n) {
        return (RichTextFieldBuilder) super.i18n(i18n);
    }

    @Override
    public RichTextFieldBuilder i18n() {
        return (RichTextFieldBuilder) super.i18n();
    }

    @Override
    public RichTextFieldBuilder description(String description) {
        return (RichTextFieldBuilder) super.description(description);
    }

    @Override
    public RichTextFieldBuilder type(String type) {
        return (RichTextFieldBuilder) super.type(type);
    }

    @Override
    public RichTextFieldBuilder required(boolean required) {
        return (RichTextFieldBuilder) super.required(required);
    }

    @Override
    public RichTextFieldBuilder required() {
        return (RichTextFieldBuilder) super.required();
    }

    @Override
    public RichTextFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (RichTextFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public RichTextFieldBuilder readOnly(boolean readOnly) {
        return (RichTextFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public RichTextFieldBuilder readOnly() {
        return (RichTextFieldBuilder) super.readOnly();
    }

    @Override
    public RichTextFieldBuilder defaultValue(String defaultValue) {
        return (RichTextFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public RichTextFieldBuilder styleName(String styleName) {
        return (RichTextFieldBuilder) super.styleName(styleName);
    }

    @Override
    public RichTextFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (RichTextFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public RichTextFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (RichTextFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public RichTextFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (RichTextFieldBuilder) super.transformerClass(transformerClass);
    }
}
