/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.ui.form.field.definition.MultiFieldDefinition;
import info.magnolia.ui.form.field.definition.SaveModeType;
import info.magnolia.ui.form.field.property.MultiValueHandler;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a multi field definition.
 */
public class MultiFieldBuilder extends AbstractFieldBuilder {

    private MultiFieldDefinition definition = new MultiFieldDefinition();

    public MultiFieldBuilder(String name) {
        definition().setName(name);
    }

    @Override
    public MultiFieldDefinition definition() {
        return definition;
    }

    public MultiFieldBuilder field(AbstractFieldBuilder fieldBuilder) {
        definition().setField(fieldBuilder.definition());
        return this;
    }

    public MultiFieldBuilder multiValueHandler(Class<? extends MultiValueHandler> multiValueHandlerClass) {
        SaveModeType saveModeType = new SaveModeType();
        saveModeType.setMultiValueHandlerClass(multiValueHandlerClass);
        definition().setSaveModeType(saveModeType);
        return this;
    }

    public void buttonSelectRemoveLabel(String buttonSelectRemoveLabel) {
        definition().setButtonSelectRemoveLabel(buttonSelectRemoveLabel);
    }

    public void buttonSelectAddLabel(String buttonSelectAddLabel) {
        definition().setButtonSelectAddLabel(buttonSelectAddLabel);
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public MultiFieldBuilder label(String label) {
        return (MultiFieldBuilder) super.label(label);
    }

    @Override
    public MultiFieldBuilder i18nBasename(String i18nBasename) {
        return (MultiFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public MultiFieldBuilder i18n(boolean i18n) {
        return (MultiFieldBuilder) super.i18n(i18n);
    }

    @Override
    public MultiFieldBuilder i18n() {
        return (MultiFieldBuilder) super.i18n();
    }

    @Override
    public MultiFieldBuilder description(String description) {
        return (MultiFieldBuilder) super.description(description);
    }

    @Override
    public MultiFieldBuilder type(String type) {
        return (MultiFieldBuilder) super.type(type);
    }

    @Override
    public MultiFieldBuilder required(boolean required) {
        return (MultiFieldBuilder) super.required(required);
    }

    @Override
    public MultiFieldBuilder required() {
        return (MultiFieldBuilder) super.required();
    }

    @Override
    public MultiFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (MultiFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public MultiFieldBuilder readOnly(boolean readOnly) {
        return (MultiFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public MultiFieldBuilder readOnly() {
        return (MultiFieldBuilder) super.readOnly();
    }

    @Override
    public MultiFieldBuilder defaultValue(String defaultValue) {
        return (MultiFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public MultiFieldBuilder styleName(String styleName) {
        return (MultiFieldBuilder) super.styleName(styleName);
    }

    @Override
    public MultiFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (MultiFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public MultiFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (MultiFieldBuilder) super.validator(validatorBuilder);
    }
}
