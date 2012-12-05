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

import info.magnolia.ui.model.field.definition.SelectFieldDefinition;
import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a select field definition.
 */
public class SelectFieldBuilder extends AbstractFieldBuilder {

    private final SelectFieldDefinition definition = new SelectFieldDefinition();

    public SelectFieldBuilder(String name) {
        this.definition.setName(name);
    }

    @Override
    protected SelectFieldDefinition getDefinition() {
        return definition;
    }

    public SelectFieldBuilder options(OptionBuilder... builders) {
        for (OptionBuilder builder : builders) {
            getDefinition().addOption(builder.exec());
        }
        return this;
    }

    public SelectFieldBuilder path(String path) {
        getDefinition().setPath(path);
        return this;
    }

    public SelectFieldBuilder repository(String repository) {
        getDefinition().setRepository(repository);
        return this;
    }

    public SelectFieldBuilder valueNodeData(String valueNodeData) {
        getDefinition().setValueProperty(valueNodeData);
        return this;
    }

    public SelectFieldBuilder labelNodeData(String labelNodeData) {
        getDefinition().setLabelProperty(labelNodeData);
        return this;
    }

    public SelectFieldBuilder filteringMode(int filteringMode) {
        getDefinition().setFilteringMode(filteringMode);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public SelectFieldBuilder styleName(String styleName) {
        return (SelectFieldBuilder) super.styleName(styleName);
    }

    @Override
    public SelectFieldBuilder i18n(boolean i18n) {
        return (SelectFieldBuilder) super.i18n(i18n);
    }

    @Override
    public SelectFieldBuilder i18n() {
        return (SelectFieldBuilder) super.i18n();
    }

    @Override
    public SelectFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (SelectFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public SelectFieldBuilder readOnly(boolean readOnly) {
        return (SelectFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public SelectFieldBuilder readOnly() {
        return (SelectFieldBuilder) super.readOnly();
    }

    @Override
    public SelectFieldBuilder label(String label) {
        return (SelectFieldBuilder) super.label(label);
    }

    @Override
    public SelectFieldBuilder i18nBasename(String i18nBasename) {
        return (SelectFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public SelectFieldBuilder description(String description) {
        return (SelectFieldBuilder) super.description(description);
    }

    @Override
    public SelectFieldBuilder type(String type) {
        return (SelectFieldBuilder) super.type(type);
    }

    @Override
    public SelectFieldBuilder required(boolean required) {
        return (SelectFieldBuilder) super.required(required);
    }

    @Override
    public SelectFieldBuilder required() {
        return (SelectFieldBuilder) super.required();
    }

    @Override
    public SelectFieldBuilder defaultValue(String defaultValue) {
        return (SelectFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public SelectFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (SelectFieldBuilder) super.validator(validatorDefinition);
    }
}
