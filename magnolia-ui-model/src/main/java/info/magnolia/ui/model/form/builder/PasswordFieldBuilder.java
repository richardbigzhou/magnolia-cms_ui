/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.model.form.builder;

import info.magnolia.ui.model.field.builder.GenericValidatorBuilder;
import info.magnolia.ui.model.field.definition.PasswordFieldDefinition;
import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for the password field.
 */
public class PasswordFieldBuilder extends AbstractFieldBuilder {

    private final PasswordFieldDefinition definition = new PasswordFieldDefinition();

    public PasswordFieldBuilder(String name) {
        this.definition.setName(name);
    }

    @Override
    protected PasswordFieldDefinition getDefinition() {
        return this.definition;
    }

    public PasswordFieldBuilder verification() {
        getDefinition().setVerification(true);
        return this;
    }

    public PasswordFieldBuilder verification(boolean verification) {
        getDefinition().setVerification(verification);
        return this;
    }

    public PasswordFieldBuilder encode(boolean encode) {
        getDefinition().setEncode(encode);
        return this;
    }

    public PasswordFieldBuilder verificationMessage(String message) {
        getDefinition().setVerificationMessage(message);
        return this;
    }

    public PasswordFieldBuilder verificationErrorMessage(String errorMessage) {
        getDefinition().setVerificationErrorMessage(errorMessage);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public PasswordFieldBuilder styleName(String styleName) {
        return (PasswordFieldBuilder) super.styleName(styleName);
    }

    @Override
    public PasswordFieldBuilder i18n(boolean i18n) {
        return (PasswordFieldBuilder) super.i18n(i18n);
    }

    @Override
    public PasswordFieldBuilder i18n() {
        return (PasswordFieldBuilder) super.i18n();
    }

    @Override
    public PasswordFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (PasswordFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public PasswordFieldBuilder readOnly(boolean readOnly) {
        return (PasswordFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public PasswordFieldBuilder readOnly() {
        return (PasswordFieldBuilder) super.readOnly();
    }

    @Override
    public PasswordFieldBuilder label(String label) {
        return (PasswordFieldBuilder) super.label(label);
    }

    @Override
    public PasswordFieldBuilder i18nBasename(String i18nBasename) {
        return (PasswordFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public PasswordFieldBuilder description(String description) {
        return (PasswordFieldBuilder) super.description(description);
    }

    @Override
    public PasswordFieldBuilder type(String type) {
        return (PasswordFieldBuilder) super.type(type);
    }

    @Override
    public PasswordFieldBuilder required(boolean required) {
        return (PasswordFieldBuilder) super.required(required);
    }

    @Override
    public PasswordFieldBuilder required() {
        return (PasswordFieldBuilder) super.required();
    }

    @Override
    public PasswordFieldBuilder defaultValue(String defaultValue) {
        return (PasswordFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public PasswordFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (PasswordFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public PasswordFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        getDefinition().addValidator(validatorBuilder.exec());
        return this;
    }
}
