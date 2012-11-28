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

import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.model.form.builder.OptionBuilder;
import info.magnolia.ui.model.form.builder.TwinColSelectFieldBuilder;

/**
 * Config-by-code builder for the Group Management field.
 */
public class GroupManagementFieldBuilder extends TwinColSelectFieldBuilder {

    private final GroupManagementFieldDefinition definition = new GroupManagementFieldDefinition();

    public GroupManagementFieldBuilder(String name) {
        super(name);
        this.definition.setName(name);
    }

    @Override
    public GroupManagementFieldDefinition getDefinition() {
        return this.definition;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public GroupManagementFieldBuilder leftColumnCaption(String caption) {
        return (GroupManagementFieldBuilder) super.leftColumnCaption(caption);
    }

    @Override
    public GroupManagementFieldBuilder rightColumnCaption(String caption) {
        return (GroupManagementFieldBuilder) super.rightColumnCaption(caption);
    }

    @Override
    public GroupManagementFieldBuilder multiselect() {
        return (GroupManagementFieldBuilder) super.multiselect(true);
    }

    @Override
    public GroupManagementFieldBuilder multiselect(boolean multiselect) {
        return (GroupManagementFieldBuilder) super.multiselect(multiselect);
    }

    @Override
    public GroupManagementFieldBuilder options(OptionBuilder... builders) {
        return (GroupManagementFieldBuilder) super.options(builders);
    }

    @Override
    public GroupManagementFieldBuilder path(String path) {
        return (GroupManagementFieldBuilder) super.path(path);
    }

    @Override
    public GroupManagementFieldBuilder repository(String repository) {
        return (GroupManagementFieldBuilder) super.repository(repository);
    }

    @Override
    public GroupManagementFieldBuilder valueNodeData(String valueNodeData) {
        return (GroupManagementFieldBuilder) super.valueNodeData(valueNodeData);
    }

    @Override
    public GroupManagementFieldBuilder labelNodeData(String labelNodeData) {
        return (GroupManagementFieldBuilder) super.labelNodeData(labelNodeData);
    }

    @Override
    public GroupManagementFieldBuilder filteringMode(int filteringMode) {
        return (GroupManagementFieldBuilder) super.filteringMode(filteringMode);
    }

    @Override
    public GroupManagementFieldBuilder styleName(String styleName) {
        return (GroupManagementFieldBuilder) super.styleName(styleName);
    }

    @Override
    public GroupManagementFieldBuilder i18n(boolean i18n) {
        return (GroupManagementFieldBuilder) super.i18n(i18n);
    }

    @Override
    public GroupManagementFieldBuilder i18n() {
        return (GroupManagementFieldBuilder) super.i18n();
    }

    @Override
    public GroupManagementFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (GroupManagementFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public GroupManagementFieldBuilder readOnly(boolean readOnly) {
        return (GroupManagementFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public GroupManagementFieldBuilder readOnly() {
        return (GroupManagementFieldBuilder) super.readOnly();
    }

    @Override
    public GroupManagementFieldBuilder label(String label) {
        return (GroupManagementFieldBuilder) super.label(label);
    }

    @Override
    public GroupManagementFieldBuilder i18nBasename(String i18nBasename) {
        return (GroupManagementFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public GroupManagementFieldBuilder description(String description) {
        return (GroupManagementFieldBuilder) super.description(description);
    }

    @Override
    public GroupManagementFieldBuilder type(String type) {
        return (GroupManagementFieldBuilder) super.type(type);
    }

    @Override
    public GroupManagementFieldBuilder required(boolean required) {
        return (GroupManagementFieldBuilder) super.required(required);
    }

    @Override
    public GroupManagementFieldBuilder required() {
        return (GroupManagementFieldBuilder) super.required();
    }

    @Override
    public GroupManagementFieldBuilder defaultValue(String defaultValue) {
        return (GroupManagementFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public GroupManagementFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (GroupManagementFieldBuilder) super.validator(validatorDefinition);
    }

}
