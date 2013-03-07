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
package info.magnolia.ui.admincentral.file;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;

/**
 * Basic Bean containing the File informations.
 */
public class FileItemWrapperImpl implements FileItemWrapper {
    private static final Logger log = LoggerFactory.getLogger(FileItemWrapperImpl.class);

    // File Properties
    private byte[] binaryData;
    private long fileSize;
    private String mimeType;
    private String extension;
    private String fileName;
    private ImageSize imageSize;
    private long width;
    private long height;

    protected JcrItemNodeAdapter jcrItem;

    /**
     * Initialize bean properties based on the jcrItem. If the jcrItem is new,
     * create all necessary properties.
     */
    public FileItemWrapperImpl(JcrItemNodeAdapter jcrItem) {
        this.jcrItem = jcrItem;
        if (jcrItem instanceof JcrNewNodeAdapter) {
            initJcrItemProperty(jcrItem);
        } else {
            fileName = (String) jcrItem.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue();
            Property<?> data = jcrItem.getItemProperty(JcrConstants.JCR_DATA);
            if (data != null) {
                binaryData = (byte[]) data.getValue();
                fileSize = Long.parseLong(jcrItem.getItemProperty(FileProperties.PROPERTY_SIZE).getValue().toString());
                mimeType = String.valueOf(jcrItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue());
                if (jcrItem.getItemProperty(FileProperties.PROPERTY_EXTENSION) != null) {
                    extension = String.valueOf(jcrItem.getItemProperty(FileProperties.PROPERTY_EXTENSION).getValue());
                }
                if (isImage()) {
                    imageSize = new ImageSize(Long.parseLong(jcrItem.getItemProperty(FileProperties.PROPERTY_WIDTH).getValue().toString()), Long.parseLong(jcrItem.getItemProperty(FileProperties.PROPERTY_HEIGHT).getValue().toString()));
                    width = imageSize.getWidth();
                    height = imageSize.getHeight();
                }
            }
        }
    }

    /**
     * Populate the jcrItem with the bean informations.
     */
    @Override
    public void populateJcrItemProperty() {
        // Attach the Item to the parent in order to be stored.
        jcrItem.getParent().addChild(jcrItem);
        // Populate Data
        Property<Object> data = jcrItem.getItemProperty(JcrConstants.JCR_DATA);

        if (binaryData != null) {
            try {
                data.setValue(ValueFactoryImpl.getInstance().createBinary(new ByteArrayInputStream(binaryData)));
            } catch (RepositoryException re) {
                log.error("Could not get Binary. Upload will not be performed", re);
                jcrItem.getParent().removeChild(jcrItem);
                return;
            }
        }
        jcrItem.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(StringUtils.substringBefore(fileName, "."));
        jcrItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(mimeType);
        jcrItem.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new Date());
        jcrItem.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(fileSize);
        if (isImage()) {
            jcrItem.getItemProperty(FileProperties.PROPERTY_WIDTH).setValue(width);
            jcrItem.getItemProperty(FileProperties.PROPERTY_HEIGHT).setValue(height);
        }
    }

    /**
     * Initialize a JcrNode Adapter with the mandatory File property.
     */
    protected void initJcrItemProperty(JcrItemNodeAdapter jcrItem) {
        jcrItem.addItemProperty(JcrConstants.JCR_DATA, DefaultPropertyUtil.newDefaultProperty(JcrConstants.JCR_DATA, "Binary", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_FILENAME, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_FILENAME, "String", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_CONTENTTYPE, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_CONTENTTYPE, "String", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_LASTMODIFIED, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_LASTMODIFIED, "Date", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_SIZE, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_SIZE, "Long", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_EXTENSION, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_EXTENSION, "String", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_WIDTH, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_WIDTH, "Long", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_HEIGHT, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_HEIGHT, "Long", null));
    }

    /**
     * Update properties.
     */
    @Override
    public void updateProperties(FilePropertiesAdapter receiver) {
        binaryData = receiver.getBinaryData();
        fileName = receiver.getFileName();
        fileSize = receiver.getFileSize();
        mimeType = receiver.getMimeType();
        if (isImage()) {
            updateImageProperties();
        }
    }

    @Override
    public void updateMediaWithStream(InputStream inputStream) {
        try {
            binaryData = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("Could not update media with stream. ", e);
            return;
        }

        if (isImage()) {
            updateImageProperties();
        }
    }

    /**
     * Update the imageSize, width, height members based on the binaryData.
     */
    private void updateImageProperties() {
        imageSize = ImageSize.valueOf(new ByteArrayInputStream(getBinaryData()));
        if (imageSize != null) {
            width = imageSize.getWidth();
            height = imageSize.getHeight();   
        }
    }

    /**
     * Clear all properties.
     */
    @Override
    public void clearProperties() {
        binaryData = null;
        fileName = null;
        fileSize = -1;
        if (isImage()) {
            imageSize = null;
            width = -1;
            height = -1;
        }
        mimeType = null;
    }

    /**
     * Get a reference to the file as a Resource.
     */
    @Override
    public Resource getResource() {
        return isImage() ? getImageResource() : getFileResource();
    }

    @Override
    public ByteArrayInputStream getStream() {
        return new ByteArrayInputStream(getBinaryData());
    }

    /**
     * Create an Resource from the contents.
     */
    private Resource getImageResource() {
        ImageSize scaledImageSize = imageSize.scaleToFitIfLarger(1000, 1000);

        final StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(getBinaryData());
            }
        };

        final Resource imageResource = new StreamResource(source, "") {
            @Override
            public String getMIMEType() {
                return getMimeType();
            }
        };

        return imageResource;
    }

    /**
     * Create a resource for the contents when its a file.
     */
    private Resource getFileResource() {
        return null;
    }

    /**
     * Create a preview Component object.
     */
    @Override
    public Component createPreview() {
        return isImage() ? createImagePreview() : createFilePreview();
    }

    /**
     * Create an Embedded Image.
     */
    private Component createImagePreview() {
        ImageSize scaledImageSize = imageSize.scaleToFitIfLarger(150, 150);

        final StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(getBinaryData());
            }
        };

        final Resource imageResource = new StreamResource(source, "") {
            @Override
            public String getMIMEType() {
                return getMimeType();
            }
        };

        final Image embedded = new Image(null, imageResource);
        embedded.setWidth(scaledImageSize.getWidth(), Unit.PIXELS);
        embedded.setHeight(scaledImageSize.getHeight(), Unit.PIXELS);
        // TODO: CLZ: embedded.addStyleName("image");

        return embedded;
    }


    /**
     * Create a Icon Component.
     */
    private Component createFilePreview() {
        Label preview = new Label();
        preview.addStyleName("image");
        preview.addStyleName("file-preview");
        preview.addStyleName(resolveIconClassName());
        return preview;
    }

    /**
     * Simple MimeType to Icon Mapping.
     */
    private String resolveIconClassName() {
        String fileType = resolveFileTypeFromMimeType(mimeType);

        if (!"".equals(fileType)) {
            return "icon-file-" + fileType;
        }

        return "icon-file";
    }

    /**
     * Simple MimeType to FileType Mapping.
     */
    private String resolveFileTypeFromMimeType(String mimeType) {
        if (mimeType.contains("application/pdf")) {
            return "pdf";
        }
        if (mimeType.matches("application.*(msword)")) {
            return "word";
        }
        if (mimeType.matches("application.*(excel|xls)")) {
            return "excel";
        }
        if (mimeType.matches("application.*(powerpoint)")) {
            return "powerpoint";
        }
        if (mimeType.contains("text/")) {
            return "text";
        }
        if (mimeType.contains("image/")) {
            return "image";
        }
        if (mimeType.contains("video/")) {
            return "video";
        }
        if (mimeType.contains("audio/")) {
            return "audio";
        }
        if (mimeType.matches(".*(zip|compress)")) {
            return "";
        }

        return "";
    }

    /**
     * Simple MimeType to MediaType Mapping.
     */
    private String resolveMediaTypeFromMimeType(String mimeType) {
        if (mimeType.contains("application/pdf")) {
            return "document";
        }
        if (mimeType.matches("application.*(msword)")) {
            return "document";
        }
        if (mimeType.matches("application.*(excel|xls)")) {
            return "document";
        }
        if (mimeType.matches("application.*(powerpoint)")) {
            return "document";
        }
        if (mimeType.contains("text/")) {
            return "text";
        }
        if (mimeType.contains("image/")) {
            return "image";
        }
        if (mimeType.contains("video/")) {
            return "video";
        }
        if (mimeType.contains("audio/")) {
            return "audio";
        }
        if (mimeType.matches(".*(zip|compress)")) {
            return "";
        }

        return "";
    }

    @Override
    public boolean isImage() {
        return mimeType != null && mimeType.matches("image.*");
    }

    @Override
    public ImageSize getImageSize() {
        return imageSize;
    }

    @Override
    public JcrItemNodeAdapter getJcrItem() {
        return jcrItem;
    }

    @Override
    public boolean isEmpty() {
        return binaryData == null;
    }

    @Override
    public void unLinkItemFromParent() {
        jcrItem.getParent().removeChild(jcrItem);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String getFormat() {
        return this.extension;
    }

    @Override
    public String getMediaTypeName() {
        return resolveMediaTypeFromMimeType(mimeType);
    }

    protected String getMimeType() {
        return mimeType;
    }

    protected byte[] getBinaryData() {
        return binaryData;
    }

}
