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

import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a hidden field definition.
 */
public class HiddenFieldBuilder extends AbstractFieldBuilder {

    private HiddenFieldDefinition definition = new HiddenFieldDefinition();

    public HiddenFieldBuilder(String name) {
        definition().setName(name);
    }

    @Override
    public HiddenFieldDefinition definition() {
        return definition;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public HiddenFieldBuilder label(String label) {
        return (HiddenFieldBuilder) super.label(label);
    }

    @Override
    public HiddenFieldBuilder i18nBasename(String i18nBasename) {
        return (HiddenFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public HiddenFieldBuilder i18n(boolean i18n) {
        return (HiddenFieldBuilder) super.i18n(i18n);
    }

    @Override
    public HiddenFieldBuilder i18n() {
        return (HiddenFieldBuilder) super.i18n();
    }

    @Override
    public HiddenFieldBuilder description(String description) {
        return (HiddenFieldBuilder) super.description(description);
    }

    @Override
    public HiddenFieldBuilder type(String type) {
        return (HiddenFieldBuilder) super.type(type);
    }

    @Override
    public HiddenFieldBuilder required(boolean required) {
        return (HiddenFieldBuilder) super.required(required);
    }

    @Override
    public HiddenFieldBuilder required() {
        return (HiddenFieldBuilder) super.required();
    }

    @Override
    public HiddenFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (HiddenFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public HiddenFieldBuilder readOnly(boolean readOnly) {
        return (HiddenFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public HiddenFieldBuilder readOnly() {
        return (HiddenFieldBuilder) super.readOnly();
    }

    @Override
    public HiddenFieldBuilder defaultValue(String defaultValue) {
        return (HiddenFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public HiddenFieldBuilder styleName(String styleName) {
        return (HiddenFieldBuilder) super.styleName(styleName);
    }

    @Override
    public HiddenFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (HiddenFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public HiddenFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (HiddenFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public HiddenFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (HiddenFieldBuilder) super.transformerClass(transformerClass);
    }
}
