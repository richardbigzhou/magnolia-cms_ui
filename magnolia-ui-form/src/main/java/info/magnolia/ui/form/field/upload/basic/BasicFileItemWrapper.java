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
package info.magnolia.ui.form.field.upload.basic;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.ui.form.field.upload.FileItemWrapper;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.jcr.Binary;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Base Implementation of {@link FileItemWrapper}.
 * This class perform the bridge between an {@link com.vaadin.data.Item} and a {@link UploadReceiver}. <br>
 * During initialization, the Item passed on the constructor populate the local variables. <br>
 * These local variables are used by the Field to display File informations (like FileName, FileSize...) <br>
 * When an Upload is performed (Uploaded File is handled by the UploadReceiver), <br>
 * - the local variables are populated based on the UploadReceiver values <br>
 * - the Item is also populated based on these values. <br>
 */
public class BasicFileItemWrapper implements FileItemWrapper {
    private static final Logger log = LoggerFactory.getLogger(BasicFileItemWrapper.class);

    // File Properties
    private File uploadedFile;
    private final File tmpDirectory;
    protected long fileSize;
    private String mimeType;
    private String extension;
    private String fileName;

    protected AbstractJcrNodeAdapter item;

    public BasicFileItemWrapper(File tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
    }

    public BasicFileItemWrapper(AbstractJcrNodeAdapter jcrItem, File tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
        populateFromItem(jcrItem);
    }

    /**
     * Populate the local variable with the values of the {@link Item}.
     */
    @Override
    public void populateFromItem(Item jcrItem) {
        if (jcrItem instanceof AbstractJcrNodeAdapter) {
            item = (AbstractJcrNodeAdapter) jcrItem;
        } else {
            log.warn("Item {} is not a JcrItemAdapter. Wrapper will not be initialized", jcrItem);
            return;
        }

        // call init for both new nodes and old ones so that potentially missing properties are initialized too.
        initJcrItemProperty(item);

        if (!(item instanceof JcrNewNodeAdapter)) {
            log.debug("BaseFileWrapper will be initialized with on the current Item values");
            populateWrapperFromItem();
        }
    }

    /**
     * Populate the wrapper variable based on the current Item.
     */
    protected void populateWrapperFromItem() {
        if (item.getParent() != null) {
            item.getParent().addChild(item);
        }
        fileName = item.getItemProperty(FileProperties.PROPERTY_FILENAME) != null ? (String) item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue() : "";
        Property<?> data = item.getItemProperty(JcrConstants.JCR_DATA);
        if (data != null) {
            fileSize = item.getItemProperty(FileProperties.PROPERTY_SIZE) != null ? Long.parseLong(String.valueOf(item.getItemProperty(FileProperties.PROPERTY_SIZE).getValue())) : 0;
            mimeType = item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE) != null ? String.valueOf(item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue()) : "";
            extension = item.getItemProperty(FileProperties.PROPERTY_EXTENSION) != null ? String.valueOf(item.getItemProperty(FileProperties.PROPERTY_EXTENSION).getValue()) : "";
            // Create a file based on the Items Binary informations.
            setFile(data);
        }
    }

    /**
     * Create a tmp {@link File} based on the {@link Item} data.
     */
    private void setFile(Property<?> data) {
        FileOutputStream fileOuputStream = null;
        try {
            uploadedFile = File.createTempFile(StringUtils.rightPad(fileName, 5, "x"), null, tmpDirectory);
            fileOuputStream = new FileOutputStream(uploadedFile);
            IOUtils.copy(((BinaryImpl) data.getValue()).getStream(), fileOuputStream);
            fileOuputStream.close();
            uploadedFile.deleteOnExit();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOuputStream != null) {
                try {
                    fileOuputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Populate the local variables with the values of the {UploadReceiver receiver}.
     * Update the related {@link Item} with this new values.
     */
    @Override
    public void populateFromReceiver(UploadReceiver receiver) {
        populateWrapperFromReceiver(receiver);
        populateItem();
    }

    /**
     * Clear the local variables.
     * Clear the Item.
     */
    @Override
    public void clearProperties() {
        uploadedFile = null;
        fileName = null;
        extension = null;
        fileSize = -1;
        mimeType = null;
        uploadedFile = null;
        // Remove reference between the File Item and his parent.
        // Doing so, when the parent Item is saved, no child File Item will be created.
        item.getParent().removeChild(item);
    }


    @Override
    public void reloadPrevious() {
        if (!isEmpty()) {
            populateWrapperFromItem();
        }
    }

    @Override
    public boolean isEmpty() {
        return uploadedFile == null;
    }

    /**
     * Populate this {@link FileItemWrapper} with the {@link UploadReceiver} informations.
     */
    protected void populateWrapperFromReceiver(UploadReceiver receiver) {
        uploadedFile = receiver.getFile();
        fileName = StringUtils.substringBeforeLast(receiver.getFileName(), ".");
        extension = receiver.getExtension();
        fileSize = receiver.getFileSize();
        mimeType = receiver.getMimeType();
    }

    /**
     * Update the {@link Item} based on the local values.
     */
    @SuppressWarnings("unchecked")
    protected void populateItem() {
        // Attach the Item to the parent in order to be stored.
        item.getParent().addChild(item);
        // Populate Data
        Property<Object> data = item.getItemProperty(JcrConstants.JCR_DATA);

        if (uploadedFile != null) {
            try {
                data.setValue(ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadedFile)));
            } catch (Exception re) {
                log.error("Could not get Binary. Upload will not be performed", re);
                item.getParent().removeChild(item);
                return;
            }
        }
        item.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(fileName);
        item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(mimeType);
        item.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new Date());

        if (!item.getItemProperty(FileProperties.PROPERTY_SIZE).getType().isAssignableFrom(Long.class)) {
            item.removeItemProperty(FileProperties.PROPERTY_SIZE);
            item.addItemProperty(FileProperties.PROPERTY_SIZE, new DefaultProperty<Long>(fileSize));
        } else {
            item.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(fileSize);
        }

        item.getItemProperty(FileProperties.PROPERTY_EXTENSION).setValue(extension);
    }

    /**
     * Initializes the JCR node adapter with the mandatory default properties.
     */
    protected void initJcrItemProperty(AbstractJcrNodeAdapter jcrItem) {
        if (jcrItem.getItemProperty(JcrConstants.JCR_DATA) == null) {
            jcrItem.addItemProperty(JcrConstants.JCR_DATA, DefaultPropertyUtil.newDefaultProperty(Binary.class, null));
        }
        if (jcrItem.getItemProperty(FileProperties.PROPERTY_FILENAME) == null) {
            jcrItem.addItemProperty(FileProperties.PROPERTY_FILENAME, DefaultPropertyUtil.newDefaultProperty(String.class, null));
        }
        if (jcrItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE) == null) {
            jcrItem.addItemProperty(FileProperties.PROPERTY_CONTENTTYPE, DefaultPropertyUtil.newDefaultProperty(String.class, null));
        }
        if (jcrItem.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED) == null) {
            jcrItem.addItemProperty(FileProperties.PROPERTY_LASTMODIFIED, DefaultPropertyUtil.newDefaultProperty(Date.class, null));
        }
        if (jcrItem.getItemProperty(FileProperties.PROPERTY_SIZE) == null) {
            jcrItem.addItemProperty(FileProperties.PROPERTY_SIZE, DefaultPropertyUtil.newDefaultProperty(Long.class, null));
        }
        if (jcrItem.getItemProperty(FileProperties.PROPERTY_EXTENSION) == null) {
            jcrItem.addItemProperty(FileProperties.PROPERTY_EXTENSION, DefaultPropertyUtil.newDefaultProperty(String.class, null));
        }
    }


    @Override
    public long getFileSize() {
        return this.fileSize;
    }

    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    @Override
    public String getExtension() {
        return this.extension;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public File getFile() {
        return this.uploadedFile;
    }

    /**
     * Used to access the Item property in order to set the input dataSource of a TextField.
     */
    protected Property<?> getFileNameProperty() {
        return this.item.getItemProperty(FileProperties.PROPERTY_FILENAME);
    }
    protected Property<?> getFileFormatProperty() {
        return this.item.getItemProperty(FileProperties.PROPERTY_EXTENSION);
    }

}
