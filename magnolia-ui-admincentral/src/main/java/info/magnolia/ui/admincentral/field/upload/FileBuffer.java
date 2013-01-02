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
package info.magnolia.ui.admincentral.field.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * FileBuffer.
 */
@SuppressWarnings("serial")
public abstract class FileBuffer implements UploadFieldReceiver {
    String mimeType;

    String fileName;

    private File file;

    private FieldType fieldType;

    private boolean deleteFiles = true;

    public FileBuffer() {
        this(FieldType.UTF8_STRING);
    }

    public FileBuffer(FieldType fieldType) {
        setFieldType(fieldType);
    }

    /**
     * @see com.vaadin.ui.Upload.Receiver#receiveUpload(String, String)
     */
    @Override
    public OutputStream receiveUpload(String filename, String MIMEType) {
        fileName = filename;
        mimeType = MIMEType;
        try {
            if (file == null) {
                file = getFileFactory().createFile(filename, mimeType);
            }
            return new FileOutputStream(file);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for UploadField.
     * 
     * @see org.vaadin.easyuploads.UploadFieldReceiver#getValue()
     */
    @Override
    public Object getValue() {
        if (file == null || !file.exists()) {
            return null;
        }

        if (getFieldType() == FieldType.FILE) {
            return file;
        }

        InputStream valueAsStream = getContentAsStream();

        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream(
                    (int) file.length());
            Streams.copy(valueAsStream, bas);
            byte[] byteArray = bas.toByteArray();
            if (getFieldType() == FieldType.BYTE_ARRAY) {
                return byteArray;
            } else {
                return new String(byteArray);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    @Override
    public InputStream getContentAsStream() {
        try {
            return new FileInputStream(getFile());
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setValue(Object newValue) {
        if (getFieldType() == FieldType.FILE) {
            if (isDeleteFiles() && file != null && file.exists()) {
                file.delete();
            }
            file = (File) newValue;
            fileName = file != null ? file.getName() : null;
        } else {
            if (isDeleteFiles() && file != null && file.exists()) {
                file.delete();
            }
            if (newValue == null) {
                return;
            }
            // we set the contents of the file
            if (file == null || !file.exists()) {
                // TODO attributes may be nulls
                file = getFileFactory().createFile(fileName, mimeType);
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                InputStream is;
                if (getFieldType() == FieldType.UTF8_STRING) {
                    is = new ByteArrayInputStream(
                            ((String) newValue).getBytes());
                } else {
                    is = new ByteArrayInputStream((byte[]) newValue);
                }
                Streams.copy(is, fileOutputStream);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    abstract public FileFactory getFileFactory();

    @Override
    public boolean isEmpty() {
        return file == null || !file.exists();
    }

    @Override
    public long getLastFileSize() {
        return file.length();
    }

    @Override
    public String getLastMimeType() {
        return mimeType;
    }

    @Override
    public String getLastFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * @param deleteFiles
     *            true if file should be deleted when setting value to null or
     *            any other new value
     */
    public void setDeleteFiles(boolean deleteFiles) {
        this.deleteFiles = deleteFiles;
    }

    /**
     * @return true if files should be deleted when setting value to null/new
     *         value
     */
    public boolean isDeleteFiles() {
        return deleteFiles;
    }
}