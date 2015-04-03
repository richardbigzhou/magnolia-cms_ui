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

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Abstract base class for builder that create dialog field definitions.
 */
public abstract class AbstractFieldBuilder {

    public abstract ConfiguredFieldDefinition definition();

    public AbstractFieldBuilder label(String label) {
        definition().setLabel(label);
        return this;
    }

    public AbstractFieldBuilder i18nBasename(String i18nBasename) {
        definition().setI18nBasename(i18nBasename);
        return this;
    }

    public AbstractFieldBuilder i18n(boolean i18n) {
        definition().setI18n(i18n);
        return this;
    }

    public AbstractFieldBuilder i18n() {
        definition().setI18n(true);
        return this;
    }

    public AbstractFieldBuilder description(String description) {
        definition().setDescription(description);
        return this;
    }

    public AbstractFieldBuilder type(String type) {
        definition().setType(type);
        return this;
    }

    public AbstractFieldBuilder required(boolean required) {
        definition().setRequired(required);
        return this;
    }

    public AbstractFieldBuilder required() {
        definition().setRequired(true);
        return this;
    }

    public AbstractFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        definition().setRequiredErrorMessage(requiredErrorMessage);
        return this;
    }

    public AbstractFieldBuilder readOnly(boolean readOnly) {
        definition().setReadOnly(readOnly);
        return this;
    }

    public AbstractFieldBuilder readOnly() {
        definition().setReadOnly(true);
        return this;
    }

    public AbstractFieldBuilder defaultValue(String defaultValue) {
        definition().setDefaultValue(defaultValue);
        return this;
    }

    public AbstractFieldBuilder styleName(String styleName) {
        definition().setStyleName(styleName);
        return this;
    }

    public AbstractFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        definition().addValidator(validatorDefinition);
        return this;
    }

    public AbstractFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        definition().addValidator(validatorBuilder.definition());
        return this;
    }

    public AbstractFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        definition().setTransformerClass(transformerClass);
        return this;
    }
}
