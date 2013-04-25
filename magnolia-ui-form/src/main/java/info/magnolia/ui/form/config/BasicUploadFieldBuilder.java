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

import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;

/**
 * Builder for building a basic file upload field definition.
 */
public class BasicUploadFieldBuilder extends AbstractFieldBuilder {

    private final BasicUploadFieldDefinition definition = new BasicUploadFieldDefinition();

    public BasicUploadFieldBuilder(String name) {
        this.definition.setName(name);
    }

    @Override
    protected BasicUploadFieldDefinition getDefinition() {
        return definition;
    }

    public BasicUploadFieldBuilder binaryNodeName(String binaryNodeName) {
        getDefinition().setBinaryNodeName(binaryNodeName);
        return this;
    }

    public BasicUploadFieldBuilder maxUploadSize(long maxUploadSize) {
        getDefinition().setMaxUploadSize(maxUploadSize);
        return this;
    }

    public BasicUploadFieldBuilder allowedMimeTypePattern(String allowedMimeTypePattern) {
        getDefinition().setAllowedMimeTypePattern(allowedMimeTypePattern);
        return this;
    }

    public BasicUploadFieldBuilder selectNewCaption(String selectNewCaption) {
        getDefinition().setSelectNewCaption(selectNewCaption);
        return this;
    }

    public BasicUploadFieldBuilder selectAnotherCaption(String selectAnotherCaption) {
        getDefinition().setSelectAnotherCaption(selectAnotherCaption);
        return this;
    }

    public BasicUploadFieldBuilder dropZoneCaption(String dropZoneCaption) {
        getDefinition().setDropZoneCaption(dropZoneCaption);
        return this;
    }

    public BasicUploadFieldBuilder inProgressCaption(String inProgressCaption) {
        getDefinition().setInProgressCaption(inProgressCaption);
        return this;
    }

    public BasicUploadFieldBuilder inProgressRatioCaption(String inProgressRatioCaption) {
        getDefinition().setInProgressRatioCaption(inProgressRatioCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailHeaderCaption(String fileDetailHeaderCaption) {
        getDefinition().setFileDetailHeaderCaption(fileDetailHeaderCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailNameCaption(String fileDetailNameCaption) {
        getDefinition().setFileDetailNameCaption(fileDetailNameCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailSizeCaption(String fileDetailSizeCaption) {
        getDefinition().setFileDetailSizeCaption(fileDetailSizeCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailFormatCaption(String fileDetailFormatCaption) {
        getDefinition().setFileDetailFormatCaption(fileDetailFormatCaption);
        return this;
    }

    public BasicUploadFieldBuilder fileDetailSourceCaption(String fileDetailSourceCaption) {
        getDefinition().setFileDetailSourceCaption(fileDetailSourceCaption);
        return this;
    }

    public BasicUploadFieldBuilder successNoteCaption(String successNoteCaption) {
        getDefinition().setSuccessNoteCaption(successNoteCaption);
        return this;
    }

    public BasicUploadFieldBuilder warningNoteCaption(String warningNoteCaption) {
        getDefinition().setWarningNoteCaption(warningNoteCaption);
        return this;
    }

    public BasicUploadFieldBuilder errorNoteCaption(String errorNoteCaption) {
        getDefinition().setErrorNoteCaption(errorNoteCaption);
        return this;
    }

    public BasicUploadFieldBuilder deleteCaption(String deleteCaption) {
        getDefinition().setDeleteCaption(deleteCaption);
        return this;
    }

    public BasicUploadFieldBuilder editFileName() {
        getDefinition().setEditFileName(true);
        return this;
    }

    public BasicUploadFieldBuilder editFileFormat() {
        getDefinition().setEditFileFormat(true);
        return this;
    }

}
