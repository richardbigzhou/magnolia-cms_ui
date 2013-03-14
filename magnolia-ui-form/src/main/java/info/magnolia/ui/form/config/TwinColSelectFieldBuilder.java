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
package info.magnolia.ui.form.config;

import info.magnolia.ui.form.field.definition.TwinColSelectFieldDefinition;
import info.magnolia.ui.form.field.validation.ConfiguredFieldValidatorDefinition;

/**
 * Builder for the TwinColSelect field.
 */
public class TwinColSelectFieldBuilder extends OptionGroupFieldBuilder {

    private final TwinColSelectFieldDefinition definition = new TwinColSelectFieldDefinition();

    public TwinColSelectFieldBuilder(String name) {
        super(name);
        this.definition.setName(name);
    }

    @Override
    protected TwinColSelectFieldDefinition getDefinition() {
        return this.definition;
    }

    public TwinColSelectFieldBuilder leftColumnCaption(String caption) {
        getDefinition().setLeftColumnCaption(caption);
        return this;
    }

    public TwinColSelectFieldBuilder rightColumnCaption(String caption) {
        getDefinition().setRightColumnCaption(caption);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public TwinColSelectFieldBuilder multiselect() {
        return (TwinColSelectFieldBuilder) super.multiselect(true);
    }

    @Override
    public TwinColSelectFieldBuilder multiselect(boolean multiselect) {
        return (TwinColSelectFieldBuilder) super.multiselect(multiselect);
    }

    @Override
    public TwinColSelectFieldBuilder options(OptionBuilder... builders) {
        return (TwinColSelectFieldBuilder) super.options(builders);
    }

    @Override
    public TwinColSelectFieldBuilder path(String path) {
        return (TwinColSelectFieldBuilder) super.path(path);
    }

    @Override
    public TwinColSelectFieldBuilder repository(String repository) {
        return (TwinColSelectFieldBuilder) super.repository(repository);
    }

    @Override
    public TwinColSelectFieldBuilder valueNodeData(String valueNodeData) {
        return (TwinColSelectFieldBuilder) super.valueNodeData(valueNodeData);
    }

    @Override
    public TwinColSelectFieldBuilder labelNodeData(String labelNodeData) {
        return (TwinColSelectFieldBuilder) super.labelNodeData(labelNodeData);
    }

    @Override
    public TwinColSelectFieldBuilder filteringMode(int filteringMode) {
        return (TwinColSelectFieldBuilder) super.filteringMode(filteringMode);
    }

    @Override
    public TwinColSelectFieldBuilder styleName(String styleName) {
        return (TwinColSelectFieldBuilder) super.styleName(styleName);
    }

    @Override
    public TwinColSelectFieldBuilder i18n(boolean i18n) {
        return (TwinColSelectFieldBuilder) super.i18n(i18n);
    }

    @Override
    public TwinColSelectFieldBuilder i18n() {
        return (TwinColSelectFieldBuilder) super.i18n();
    }

    @Override
    public TwinColSelectFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (TwinColSelectFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public TwinColSelectFieldBuilder readOnly(boolean readOnly) {
        return (TwinColSelectFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public TwinColSelectFieldBuilder readOnly() {
        return (TwinColSelectFieldBuilder) super.readOnly();
    }

    @Override
    public TwinColSelectFieldBuilder label(String label) {
        return (TwinColSelectFieldBuilder) super.label(label);
    }

    @Override
    public TwinColSelectFieldBuilder i18nBasename(String i18nBasename) {
        return (TwinColSelectFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public TwinColSelectFieldBuilder description(String description) {
        return (TwinColSelectFieldBuilder) super.description(description);
    }

    @Override
    public TwinColSelectFieldBuilder type(String type) {
        return (TwinColSelectFieldBuilder) super.type(type);
    }

    @Override
    public TwinColSelectFieldBuilder required(boolean required) {
        return (TwinColSelectFieldBuilder) super.required(required);
    }

    @Override
    public TwinColSelectFieldBuilder required() {
        return (TwinColSelectFieldBuilder) super.required();
    }

    @Override
    public TwinColSelectFieldBuilder defaultValue(String defaultValue) {
        return (TwinColSelectFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public TwinColSelectFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (TwinColSelectFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public TwinColSelectFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        getDefinition().addValidator(validatorBuilder.exec());
        return this;
    }
}
