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

import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a basic file upload field definition.
 */
public class BasicUploadFieldBuilder extends AbstractFieldBuilder {

    private final BasicUploadFieldDefinition definition = new BasicUploadFieldDefinition();

    public BasicUploadFieldBuilder() {
    }

    public BasicUploadFieldBuilder(String name) {
        this.definition().setName(name);
    }

    @Override
    public BasicUploadFieldDefinition definition() {
        return definition;
    }

    public BasicUploadFieldBuilder binaryNodeName(String binaryNodeName) {
        definition().setBinaryNodeName(binaryNodeName);
        return this;
    }

    public BasicUploadFieldBuilder maxUploadSize(long maxUploadSize) {
        definition().setMaxUploadSize(maxUploadSize);
        return this;
    }

    public BasicUploadFieldBuilder allowedMimeTypePattern(String allowedMimeTypePattern) {
        definition().setAllowedMimeTypePattern(allowedMimeTypePattern);
        return this;
    }

    public BasicUploadFieldBuilder allowedFileExtensionPattern(String allowedFileExtensionPattern) {
        definition().setAllowedFileExtensionPattern(allowedFileExtensionPattern);
        return this;
    }

    public BasicUploadFieldBuilder allowedFallbackMimeType(String fallbackMimeType) {
        definition().setFallbackMimeType(fallbackMimeType);
        return this;
    }

    public BasicUploadFieldBuilder editFileName(boolean editFileName) {
        definition().setEditFileName(editFileName);
        return this;
    }

    public BasicUploadFieldBuilder editFileFormat(boolean editFileFormat) {
        definition().setEditFileFormat(editFileFormat);
        return this;
    }

    public BasicUploadFieldBuilder selectAnotherCaption(String selectAnotherCaption) {
        definition().setSelectAnotherCaption(selectAnotherCaption);
        return this;
    }

    public BasicUploadFieldBuilder userInterruption(String userInterruption) {
        definition().setUserInterruption(userInterruption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailNameCaption(String fileDetailNameCaption) {
        definition().setFileDetailNameCaption(fileDetailNameCaption);
        return this;
    }

    public BasicUploadFieldBuilder inProgressRatioCaption(String inProgressRatioCaption) {
        definition().setInProgressRatioCaption(inProgressRatioCaption);
        return this;
    }

    public BasicUploadFieldBuilder dropZoneCaption(String dropZoneCaption) {
        definition().setDropZoneCaption(dropZoneCaption);
        return this;
    }

    public BasicUploadFieldBuilder warningNoteCaption(String warningNoteCaption) {
        definition().setWarningNoteCaption(warningNoteCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailSourceCaption(String fileDetailSourceCaption) {
        definition().setFileDetailSourceCaption(fileDetailSourceCaption);
        return this;
    }

    public BasicUploadFieldBuilder inProgressCaption(String inProgressCaption) {
        definition().setInProgressCaption(inProgressCaption);
        return this;
    }

    public BasicUploadFieldBuilder deleteCaption(String deleteCaption) {
        definition().setDeleteCaption(deleteCaption);
        return this;
    }

    public BasicUploadFieldBuilder typeInterruption(String typeInterruption) {
        definition().setTypeInterruption(typeInterruption);
        return this;
    }

    public BasicUploadFieldBuilder selectNewCaption(String selectNewCaption) {
        definition().setSelectNewCaption(selectNewCaption);
        return this;
    }

    public BasicUploadFieldBuilder sizeInterruption(String sizeInterruption) {
        definition().setSizeInterruption(sizeInterruption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailHeaderCaption(String fileDetailHeaderCaption) {
        definition().setFileDetailHeaderCaption(fileDetailHeaderCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailFormatCaption(String fileDetailFormatCaption) {
        definition().setFileDetailFormatCaption(fileDetailFormatCaption);
        return this;
    }

    public BasicUploadFieldBuilder errorNoteCaption(String errorNoteCaption) {
        definition().setErrorNoteCaption(errorNoteCaption);
        return this;
    }

    public BasicUploadFieldBuilder successNoteCaption(String successNoteCaption) {
        definition().setSuccessNoteCaption(successNoteCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailSizeCaption(String fileDetailSizeCaption) {
        definition().setFileDetailSizeCaption(fileDetailSizeCaption);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public BasicUploadFieldBuilder label(String label) {
        return (BasicUploadFieldBuilder) super.label(label);
    }

    @Override
    public BasicUploadFieldBuilder i18nBasename(String i18nBasename) {
        return (BasicUploadFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public BasicUploadFieldBuilder i18n(boolean i18n) {
        return (BasicUploadFieldBuilder) super.i18n(i18n);
    }

    @Override
    public BasicUploadFieldBuilder i18n() {
        return (BasicUploadFieldBuilder) super.i18n();
    }

    @Override
    public BasicUploadFieldBuilder description(String description) {
        return (BasicUploadFieldBuilder) super.description(description);
    }

    @Override
    public BasicUploadFieldBuilder type(String type) {
        return (BasicUploadFieldBuilder) super.type(type);
    }

    @Override
    public BasicUploadFieldBuilder required(boolean required) {
        return (BasicUploadFieldBuilder) super.required(required);
    }

    @Override
    public BasicUploadFieldBuilder required() {
        return (BasicUploadFieldBuilder) super.required();
    }

    @Override
    public BasicUploadFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (BasicUploadFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public BasicUploadFieldBuilder readOnly(boolean readOnly) {
        return (BasicUploadFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public BasicUploadFieldBuilder readOnly() {
        return (BasicUploadFieldBuilder) super.readOnly();
    }

    @Override
    public BasicUploadFieldBuilder defaultValue(String defaultValue) {
        return (BasicUploadFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public BasicUploadFieldBuilder styleName(String styleName) {
        return (BasicUploadFieldBuilder) super.styleName(styleName);
    }

    @Override
    public BasicUploadFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (BasicUploadFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public BasicUploadFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (BasicUploadFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public BasicUploadFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (BasicUploadFieldBuilder) super.transformerClass(transformerClass);
    }
}
