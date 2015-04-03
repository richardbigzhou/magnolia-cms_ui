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
package info.magnolia.ui.form.field.upload;

import info.magnolia.cms.util.PathUtil;
import info.magnolia.i18nsystem.SimpleTranslator;

import java.io.File;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField.FieldType;

/**
 * Implementation of {@link FileBuffer}.<br>
 * Expose need variables.<br>
 * Currently only support {@link FieldType.FILE}.
 */
public class UploadReceiver extends FileBuffer {

    private static final long serialVersionUID = 1L;
    private File directory;
    private final SimpleTranslator i18n;
    public static final String INVALID_FILE_NAME = "untitled";
    private String fileName;

    @Inject
    public UploadReceiver(File directory, SimpleTranslator i18n) {
        this.directory = directory;
        this.i18n = i18n;
        setFieldType(FieldType.FILE);
        setDeleteFiles(true);
    }

    @Override
    public OutputStream receiveUpload(String filename, String MIMEType) {
        setFileName(filename);
        return super.receiveUpload(filename, MIMEType);
    }

    @Override
    public FileFactory getFileFactory() {
        return new DefaultFileFactory(directory, i18n);
    }

    public String getFileName() {
        if (StringUtils.isBlank(this.fileName) || StringUtils.isBlank(PathUtil.getExtension(this.fileName))) {
            return INVALID_FILE_NAME;
        }
        return this.fileName;
    }

    public long getFileSize() {
        return this.getFile() != null ? this.getLastFileSize() : 0l;
    }

    public String getMimeType() {
        return this.getLastMimeType();
    }

    public String getExtension() {
        return PathUtil.getExtension(getFileName());
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
