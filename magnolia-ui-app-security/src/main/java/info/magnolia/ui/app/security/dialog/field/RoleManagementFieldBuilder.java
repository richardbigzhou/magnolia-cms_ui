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
package info.magnolia.ui.app.security.dialog.field;

import info.magnolia.ui.form.config.GenericValidatorBuilder;
import info.magnolia.ui.form.field.validation.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.form.config.OptionBuilder;
import info.magnolia.ui.form.config.TwinColSelectFieldBuilder;

/**
 * Config-by-code builder for the Role Management field.
 */
public class RoleManagementFieldBuilder extends TwinColSelectFieldBuilder {

    private final RoleManagementFieldDefinition definition = new RoleManagementFieldDefinition();

    public RoleManagementFieldBuilder(String name) {
        super(name);
        this.definition.setName(name);
    }

    @Override
    public RoleManagementFieldDefinition getDefinition() {
        return this.definition;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public RoleManagementFieldBuilder leftColumnCaption(String caption) {
        return (RoleManagementFieldBuilder) super.leftColumnCaption(caption);
    }

    @Override
    public RoleManagementFieldBuilder rightColumnCaption(String caption) {
        return (RoleManagementFieldBuilder) super.rightColumnCaption(caption);
    }

    @Override
    public RoleManagementFieldBuilder multiselect() {
        return (RoleManagementFieldBuilder) super.multiselect(true);
    }

    @Override
    public RoleManagementFieldBuilder multiselect(boolean multiselect) {
        return (RoleManagementFieldBuilder) super.multiselect(multiselect);
    }

    @Override
    public RoleManagementFieldBuilder options(OptionBuilder... builders) {
        return (RoleManagementFieldBuilder) super.options(builders);
    }

    @Override
    public RoleManagementFieldBuilder path(String path) {
        return (RoleManagementFieldBuilder) super.path(path);
    }

    @Override
    public RoleManagementFieldBuilder repository(String repository) {
        return (RoleManagementFieldBuilder) super.repository(repository);
    }

    @Override
    public RoleManagementFieldBuilder valueNodeData(String valueNodeData) {
        return (RoleManagementFieldBuilder) super.valueNodeData(valueNodeData);
    }

    @Override
    public RoleManagementFieldBuilder labelNodeData(String labelNodeData) {
        return (RoleManagementFieldBuilder) super.labelNodeData(labelNodeData);
    }

    @Override
    public RoleManagementFieldBuilder filteringMode(int filteringMode) {
        return (RoleManagementFieldBuilder) super.filteringMode(filteringMode);
    }

    @Override
    public RoleManagementFieldBuilder styleName(String styleName) {
        return (RoleManagementFieldBuilder) super.styleName(styleName);
    }

    @Override
    public RoleManagementFieldBuilder i18n(boolean i18n) {
        return (RoleManagementFieldBuilder) super.i18n(i18n);
    }

    @Override
    public RoleManagementFieldBuilder i18n() {
        return (RoleManagementFieldBuilder) super.i18n();
    }

    @Override
    public RoleManagementFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (RoleManagementFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public RoleManagementFieldBuilder readOnly(boolean readOnly) {
        return (RoleManagementFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public RoleManagementFieldBuilder readOnly() {
        return (RoleManagementFieldBuilder) super.readOnly();
    }

    @Override
    public RoleManagementFieldBuilder label(String label) {
        return (RoleManagementFieldBuilder) super.label(label);
    }

    @Override
    public RoleManagementFieldBuilder i18nBasename(String i18nBasename) {
        return (RoleManagementFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public RoleManagementFieldBuilder description(String description) {
        return (RoleManagementFieldBuilder) super.description(description);
    }

    @Override
    public RoleManagementFieldBuilder type(String type) {
        return (RoleManagementFieldBuilder) super.type(type);
    }

    @Override
    public RoleManagementFieldBuilder required(boolean required) {
        return (RoleManagementFieldBuilder) super.required(required);
    }

    @Override
    public RoleManagementFieldBuilder required() {
        return (RoleManagementFieldBuilder) super.required();
    }

    @Override
    public RoleManagementFieldBuilder defaultValue(String defaultValue) {
        return (RoleManagementFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public RoleManagementFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (RoleManagementFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public RoleManagementFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        getDefinition().addValidator(validatorBuilder.exec());
        return this;
    }
}
