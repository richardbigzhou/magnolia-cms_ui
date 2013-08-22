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

import info.magnolia.ui.form.field.component.ContentPreviewComponent;
import info.magnolia.ui.form.field.converter.IdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;
import info.magnolia.ui.form.field.definition.SaveModeType;
import info.magnolia.ui.form.field.property.MultiValueHandler;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a multi link field definition.
 */
public class MultiLinkFieldBuilder extends LinkFieldBuilder {

    private MultiLinkFieldDefinition definition = new MultiLinkFieldDefinition();

    public MultiLinkFieldBuilder(String name) {
        super(name);
    }

    @Override
    public MultiLinkFieldDefinition definition() {
        return definition;
    }

    public MultiLinkFieldBuilder multiValueHandler(Class<? extends MultiValueHandler> multiValueHandlerClass) {
        SaveModeType saveModeType = definition().getSaveModeType();
        if (saveModeType == null) {
            saveModeType = new SaveModeType();
            definition().setSaveModeType(saveModeType);
        }
        saveModeType.setMultiValueHandlerClass(multiValueHandlerClass);
        return this;
    }

    public MultiLinkFieldBuilder buttonSelectAddLabel(String buttonSelectAddLabel) {
        definition().setButtonSelectAddLabel(buttonSelectAddLabel);
        return this;
    }

    public MultiLinkFieldBuilder buttonSelectRemoveLabel(String buttonSelectRemoveLabel) {
        definition().setButtonSelectRemoveLabel(buttonSelectRemoveLabel);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public MultiLinkFieldBuilder targetPropertyToPopulate(String targetPropertyToPopulate) {
        return (MultiLinkFieldBuilder) super.targetPropertyToPopulate(targetPropertyToPopulate);
    }

    @Override
    public MultiLinkFieldBuilder targetWorkspace(String targetWorkspace) {
        return (MultiLinkFieldBuilder) super.targetWorkspace(targetWorkspace);
    }

    @Override
    public MultiLinkFieldBuilder targetTreeRootPath(String targetTreeRootPath) {
        return (MultiLinkFieldBuilder) super.targetTreeRootPath(targetTreeRootPath);
    }

    @Override
    public MultiLinkFieldBuilder appName(String appName) {
        return (MultiLinkFieldBuilder) super.appName(appName);
    }

    @Override
    public MultiLinkFieldBuilder buttonSelectNewLabel(String buttonSelectNewLabel) {
        return (MultiLinkFieldBuilder) super.buttonSelectNewLabel(buttonSelectNewLabel);
    }

    @Override
    public MultiLinkFieldBuilder buttonSelectOtherLabel(String buttonSelectOtherLabel) {
        return (MultiLinkFieldBuilder) super.buttonSelectOtherLabel(buttonSelectOtherLabel);
    }

    @Override
    public MultiLinkFieldBuilder identifierToPathConverter(IdentifierToPathConverter identifierToPathConverter) {
        return (MultiLinkFieldBuilder) super.identifierToPathConverter(identifierToPathConverter);
    }

    @Override
    public MultiLinkFieldBuilder contentPreviewClass(Class<ContentPreviewComponent<?>> contentPreviewClass) {
        return (MultiLinkFieldBuilder) super.contentPreviewClass(contentPreviewClass);
    }

    @Override
    public MultiLinkFieldBuilder fieldEditable() {
        return (MultiLinkFieldBuilder) super.fieldEditable();
    }

    @Override
    public MultiLinkFieldBuilder fieldEditable(boolean fieldEditable) {
        return (MultiLinkFieldBuilder) super.fieldEditable(fieldEditable);
    }

    @Override
    public MultiLinkFieldBuilder label(String label) {
        return (MultiLinkFieldBuilder) super.label(label);
    }

    @Override
    public MultiLinkFieldBuilder i18nBasename(String i18nBasename) {
        return (MultiLinkFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public MultiLinkFieldBuilder i18n(boolean i18n) {
        return (MultiLinkFieldBuilder) super.i18n(i18n);
    }

    @Override
    public MultiLinkFieldBuilder i18n() {
        return (MultiLinkFieldBuilder) super.i18n();
    }

    @Override
    public MultiLinkFieldBuilder description(String description) {
        return (MultiLinkFieldBuilder) super.description(description);
    }

    @Override
    public MultiLinkFieldBuilder type(String type) {
        return (MultiLinkFieldBuilder) super.type(type);
    }

    @Override
    public MultiLinkFieldBuilder required(boolean required) {
        return (MultiLinkFieldBuilder) super.required(required);
    }

    @Override
    public MultiLinkFieldBuilder required() {
        return (MultiLinkFieldBuilder) super.required();
    }

    @Override
    public MultiLinkFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (MultiLinkFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public MultiLinkFieldBuilder readOnly(boolean readOnly) {
        return (MultiLinkFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public MultiLinkFieldBuilder readOnly() {
        return (MultiLinkFieldBuilder) super.readOnly();
    }

    @Override
    public MultiLinkFieldBuilder defaultValue(String defaultValue) {
        return (MultiLinkFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public MultiLinkFieldBuilder styleName(String styleName) {
        return (MultiLinkFieldBuilder) super.styleName(styleName);
    }

    @Override
    public MultiLinkFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (MultiLinkFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public MultiLinkFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (MultiLinkFieldBuilder) super.validator(validatorBuilder);
    }
}
