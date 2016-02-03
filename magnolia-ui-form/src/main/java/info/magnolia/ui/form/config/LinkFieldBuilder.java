/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.ui.form.field.definition.ContentPreviewDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a link field definition.
 */
public class LinkFieldBuilder extends AbstractFieldBuilder {

    private final LinkFieldDefinition definition = new LinkFieldDefinition();

    public LinkFieldBuilder(String name) {
        this.definition().setName(name);
    }

    @Override
    public LinkFieldDefinition definition() {
        return definition;
    }

    public LinkFieldBuilder targetPropertyToPopulate(String targetPropertyToPopulate) {
        definition().setTargetPropertyToPopulate(targetPropertyToPopulate);
        return this;
    }

    public LinkFieldBuilder targetWorkspace(String targetWorkspace) {
        definition().setTargetWorkspace(targetWorkspace);
        return this;
    }

    public LinkFieldBuilder targetTreeRootPath(String targetTreeRootPath) {
        definition().setTargetTreeRootPath(targetTreeRootPath);
        return this;
    }

    public LinkFieldBuilder appName(String appName) {
        definition().setAppName(appName);
        return this;
    }

    public LinkFieldBuilder buttonSelectNewLabel(String buttonSelectNewLabel) {
        definition().setButtonSelectNewLabel(buttonSelectNewLabel);
        return this;
    }

    public LinkFieldBuilder buttonSelectOtherLabel(String buttonSelectOtherLabel) {
        definition().setButtonSelectOtherLabel(buttonSelectOtherLabel);
        return this;
    }

    public LinkFieldBuilder identifierToPathConverter(IdentifierToPathConverter identifierToPathConverter) {
        definition().setIdentifierToPathConverter(identifierToPathConverter);
        return this;
    }

    public LinkFieldBuilder contentPreviewClass(Class<? extends ContentPreviewComponent<?>> contentPreviewClass) {
        ContentPreviewDefinition contentPreviewDefinition = definition().getContentPreviewDefinition();
        if (contentPreviewDefinition == null) {
            contentPreviewDefinition = new ContentPreviewDefinition();
            definition().setContentPreviewDefinition(contentPreviewDefinition);
        }
        contentPreviewDefinition.setContentPreviewClass(contentPreviewClass);
        return this;
    }

    public LinkFieldBuilder fieldEditable() {
        definition().setFieldEditable(true);
        return this;
    }

    public LinkFieldBuilder fieldEditable(boolean fieldEditable) {
        definition().setFieldEditable(fieldEditable);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public LinkFieldBuilder label(String label) {
        return (LinkFieldBuilder) super.label(label);
    }

    @Override
    public LinkFieldBuilder i18nBasename(String i18nBasename) {
        return (LinkFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public LinkFieldBuilder i18n(boolean i18n) {
        return (LinkFieldBuilder) super.i18n(i18n);
    }

    @Override
    public LinkFieldBuilder i18n() {
        return (LinkFieldBuilder) super.i18n();
    }

    @Override
    public LinkFieldBuilder description(String description) {
        return (LinkFieldBuilder) super.description(description);
    }

    @Override
    public LinkFieldBuilder type(String type) {
        return (LinkFieldBuilder) super.type(type);
    }

    @Override
    public LinkFieldBuilder required(boolean required) {
        return (LinkFieldBuilder) super.required(required);
    }

    @Override
    public LinkFieldBuilder required() {
        return (LinkFieldBuilder) super.required();
    }

    @Override
    public LinkFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (LinkFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public LinkFieldBuilder readOnly(boolean readOnly) {
        return (LinkFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public LinkFieldBuilder readOnly() {
        return (LinkFieldBuilder) super.readOnly();
    }

    @Override
    public LinkFieldBuilder defaultValue(String defaultValue) {
        return (LinkFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public LinkFieldBuilder styleName(String styleName) {
        return (LinkFieldBuilder) super.styleName(styleName);
    }

    @Override
    public LinkFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (LinkFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public LinkFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (LinkFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public LinkFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (LinkFieldBuilder) super.transformerClass(transformerClass);
    }
}
