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
package info.magnolia.ui.form.field.definition;

import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.item.FileTransformer;

/**
 * Field definition for a the basic upload field.
 */
public class BasicUploadFieldDefinition extends ConfiguredFieldDefinition {

    // Define the upload Binary Node name.
    private String binaryNodeName = "binaryNodeName";
    // Define the maximum file size in bite.
    private long maxUploadSize = Long.MAX_VALUE;
    // Define allowed uploadMimeType
    private String allowedMimeTypePattern = ".*";
    // Define MimeType to fall back to if file has allowed extension
    private String fallbackMimeType;
    // Define allowed extension to check against if browser send mimeType==""
    private String allowedFileExtensionPattern = ".*";
    // Define if the File Name can be edited
    private boolean editFileName = false;
    // Define if the File Format can be edited
    private boolean editFileFormat = false;

    // Define the Captions
    private String selectNewCaption = "field.upload.basic.select.new";
    private String selectAnotherCaption = "field.upload.basic.select.another";
    private String deleteCaption = "field.upload.basic.select.delete";
    private String dropZoneCaption = "field.upload.basic.drop.hint";
    private String inProgressCaption = "field.upload.basic.uploading.file";
    private String inProgressRatioCaption = "field.upload.basic.uploaded.file";
    private String fileDetailHeaderCaption = "field.upload.basic.file.detail.header";
    private String fileDetailNameCaption = "field.upload.basic.file.detail.name";
    private String fileDetailSizeCaption = "field.upload.basic.file.detail.size";
    private String fileDetailFormatCaption = "field.upload.basic.file.detail.format";
    private String fileDetailSourceCaption = "field.upload.basic.file.detail.source";
    private String successNoteCaption = "field.upload.basic.note.success";
    private String warningNoteCaption = "field.upload.basic.note.warning";
    private String errorNoteCaption = "field.upload.basic.note.error";
    private String sizeInterruption = "field.upload.interupted.size";
    private String typeInterruption = "field.upload.interupted.type";
    private String userInterruption = "field.upload.interupted.user";

    public BasicUploadFieldDefinition() {
        setTransformerClass((Class<? extends Transformer<?>>) (Object) FileTransformer.class);
    }

    public String getBinaryNodeName() {
        return binaryNodeName;
    }

    public void setBinaryNodeName(String binaryNodeName) {
        this.binaryNodeName = binaryNodeName;
    }

    public long getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public String getAllowedMimeTypePattern() {
        return allowedMimeTypePattern;
    }

    public void setAllowedMimeTypePattern(String allowedMimeTypePattern) {
        this.allowedMimeTypePattern = allowedMimeTypePattern;
    }

    public String getFallbackMimeType() {
        return fallbackMimeType;
    }

    public void setFallbackMimeType(String fallbackMimeType) {
        this.fallbackMimeType = fallbackMimeType;
    }

    public String getAllowedFileExtensionPattern() {
        return allowedFileExtensionPattern;
    }

    public void setAllowedFileExtensionPattern(String allowedFileExtensionPattern) {
        this.allowedFileExtensionPattern = allowedFileExtensionPattern;
    }

    public String getSelectNewCaption() {
        return selectNewCaption;
    }

    public void setSelectNewCaption(String selectNewCaption) {
        this.selectNewCaption = selectNewCaption;
    }

    public String getSelectAnotherCaption() {
        return selectAnotherCaption;
    }

    public void setSelectAnotherCaption(String selectAnotherCaption) {
        this.selectAnotherCaption = selectAnotherCaption;
    }

    public String getDropZoneCaption() {
        return dropZoneCaption;
    }

    public void setDropZoneCaption(String dropZoneCaption) {
        this.dropZoneCaption = dropZoneCaption;
    }

    public String getInProgressCaption() {
        return inProgressCaption;
    }

    public void setInProgressCaption(String inProgressCaption) {
        this.inProgressCaption = inProgressCaption;
    }

    public String getInProgressRatioCaption() {
        return inProgressRatioCaption;
    }

    public void setInProgressRatioCaption(String inProgressRatioCaption) {
        this.inProgressRatioCaption = inProgressRatioCaption;
    }

    public String getFileDetailHeaderCaption() {
        return fileDetailHeaderCaption;
    }

    public void setFileDetailHeaderCaption(String fileDetailHeaderCaption) {
        this.fileDetailHeaderCaption = fileDetailHeaderCaption;
    }

    public String getFileDetailNameCaption() {
        return fileDetailNameCaption;
    }

    public void setFileDetailNameCaption(String fileDetailNameCaption) {
        this.fileDetailNameCaption = fileDetailNameCaption;
    }

    public String getFileDetailSizeCaption() {
        return fileDetailSizeCaption;
    }

    public void setFileDetailSizeCaption(String fileDetailSizeCaption) {
        this.fileDetailSizeCaption = fileDetailSizeCaption;
    }

    public String getFileDetailFormatCaption() {
        return fileDetailFormatCaption;
    }

    public void setFileDetailFormatCaption(String fileDetailFormatCaption) {
        this.fileDetailFormatCaption = fileDetailFormatCaption;
    }

    public String getFileDetailSourceCaption() {
        return fileDetailSourceCaption;
    }

    public void setFileDetailSourceCaption(String fileDetailSourceCaption) {
        this.fileDetailSourceCaption = fileDetailSourceCaption;
    }

    public String getSuccessNoteCaption() {
        return successNoteCaption;
    }

    public void setSuccessNoteCaption(String successNoteCaption) {
        this.successNoteCaption = successNoteCaption;
    }

    public String getWarningNoteCaption() {
        return warningNoteCaption;
    }

    public void setWarningNoteCaption(String warningNoteCaption) {
        this.warningNoteCaption = warningNoteCaption;
    }

    public String getErrorNoteCaption() {
        return errorNoteCaption;
    }

    public void setErrorNoteCaption(String errorNoteCaption) {
        this.errorNoteCaption = errorNoteCaption;
    }

    public String getDeleteCaption() {
        return deleteCaption;
    }

    public void setDeleteCaption(String deleteCaption) {
        this.deleteCaption = deleteCaption;
    }

    public boolean isEditFileName() {
        return editFileName;
    }

    public void setEditFileName(boolean editFileName) {
        this.editFileName = editFileName;
    }

    public boolean isEditFileFormat() {
        return editFileFormat;
    }

    public void setEditFileFormat(boolean editFileFormat) {
        this.editFileFormat = editFileFormat;
    }

    public String getSizeInterruption() {
        return sizeInterruption;
    }

    public void setSizeInterruption(String sizeInterruption) {
        this.sizeInterruption = sizeInterruption;
    }

    public String getTypeInterruption() {
        return typeInterruption;
    }

    public void setTypeInterruption(String typeInterruption) {
        this.typeInterruption = typeInterruption;
    }

    public String getUserInterruption() {
        return userInterruption;
    }

    public void setUserInterruption(String userInterruption) {
        this.userInterruption = userInterruption;
    }
}
