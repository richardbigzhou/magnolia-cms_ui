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
package info.magnolia.ui.app.contacts.cconf.dialog;

import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;
import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a file upload field definition.
 */
public class FileUploadFieldBuilder extends AbstractFieldBuilder {

    private final FileUploadFieldDefinition definition = new FileUploadFieldDefinition();

    public FileUploadFieldBuilder(String name) {
        this.definition.setName(name);
    }

    @Override
    protected FileUploadFieldDefinition getDefinition() {
        return definition;
    }

    public FileUploadFieldBuilder preview() {
        getDefinition().setPreview(true);
        return this;
    }

    public FileUploadFieldBuilder imageNodeName(String imageNodeName) {
        getDefinition().setImageNodeName(imageNodeName);
        return this;
    }

    public FileUploadFieldBuilder allowedMimeType(String allowedMimeType) {
        getDefinition().setAllowedMimeType(allowedMimeType);
        return this;
    }

    public FileUploadFieldBuilder maxUploadSize(long maxUploadSize) {
        getDefinition().setMaxUploadSize(maxUploadSize);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public FileUploadFieldBuilder styleName(String styleName) {
        return (FileUploadFieldBuilder) super.styleName(styleName);
    }

    @Override
    public FileUploadFieldBuilder i18n(boolean i18n) {
        return (FileUploadFieldBuilder) super.i18n(i18n);
    }

    @Override
    public FileUploadFieldBuilder i18n() {
        return (FileUploadFieldBuilder) super.i18n();
    }

    @Override
    public FileUploadFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (FileUploadFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public FileUploadFieldBuilder readOnly(boolean readOnly) {
        return (FileUploadFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public FileUploadFieldBuilder readOnly() {
        return (FileUploadFieldBuilder) super.readOnly();
    }

    @Override
    public FileUploadFieldBuilder label(String label) {
        return (FileUploadFieldBuilder) super.label(label);
    }

    @Override
    public FileUploadFieldBuilder i18nBasename(String i18nBasename) {
        return (FileUploadFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public FileUploadFieldBuilder description(String description) {
        return (FileUploadFieldBuilder) super.description(description);
    }

    @Override
    public FileUploadFieldBuilder type(String type) {
        return (FileUploadFieldBuilder) super.type(type);
    }

    @Override
    public FileUploadFieldBuilder required(boolean required) {
        return (FileUploadFieldBuilder) super.required(required);
    }

    @Override
    public FileUploadFieldBuilder required() {
        return (FileUploadFieldBuilder) super.required();
    }

    @Override
    public FileUploadFieldBuilder defaultValue(String defaultValue) {
        return (FileUploadFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public FileUploadFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (FileUploadFieldBuilder) super.validator(validatorDefinition);
    }
}
