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
import info.magnolia.ui.model.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;

/**
 * Abstract base class for builder that create dialog field definitions.
 */
public abstract class AbstractFieldBuilder {

    protected abstract ConfiguredFieldDefinition getDefinition();

    public AbstractFieldBuilder styleName(String styleName) {
        getDefinition().setStyleName(styleName);
        return this;
    }

    public AbstractFieldBuilder i18n(boolean i18n) {
        getDefinition().setI18n(i18n);
        return this;
    }

    public AbstractFieldBuilder i18n() {
        getDefinition().setI18n(true);
        return this;
    }

    public AbstractFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        getDefinition().setRequiredErrorMessage(requiredErrorMessage);
        return this;
    }

    public AbstractFieldBuilder readOnly(boolean readOnly) {
        getDefinition().setReadOnly(readOnly);
        return this;
    }

    public AbstractFieldBuilder readOnly() {
        getDefinition().setReadOnly(true);
        return this;
    }

    public AbstractFieldBuilder label(String label) {
        getDefinition().setLabel(label);
        return this;
    }

    public AbstractFieldBuilder i18nBasename(String i18nBasename) {
        getDefinition().setI18nBasename(i18nBasename);
        return this;
    }

    public AbstractFieldBuilder description(String description) {
        getDefinition().setDescription(description);
        return this;
    }

    public AbstractFieldBuilder type(String type) {
        getDefinition().setType(type);
        return this;
    }

    public AbstractFieldBuilder required(boolean required) {
        getDefinition().setRequired(required);
        return this;
    }

    public AbstractFieldBuilder required() {
        getDefinition().setRequired(true);
        return this;
    }

    public AbstractFieldBuilder defaultValue(String defaultValue) {
        getDefinition().setDefaultValue(defaultValue);
        return this;
    }

    public AbstractFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        getDefinition().addValidator(validatorDefinition);
        return this;
    }

    public AbstractFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        getDefinition().addValidator(validatorBuilder.exec());
        return this;
    }

    public FieldDefinition exec() {
        return getDefinition();
    }
}
