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
import info.magnolia.cms.util.PathUtil;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;

/**
 * Basic Bean containing the File informations.
 */
public class FileItemWrapperImpl implements FileItemWrapper {

    // File Properties
    private byte[] binaryData;
    private long fileSize;
    private String mimeType;
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
            Property data = jcrItem.getItemProperty(JcrConstants.JCR_DATA);
            if (data != null) {
                binaryData = (byte[]) data.getValue();
                fileSize = Long.parseLong(jcrItem.getItemProperty(FileProperties.PROPERTY_SIZE).getValue().toString());
                mimeType = (String) jcrItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue();
                if (isImage()) {
                    imageSize = new ImageSize(Long.parseLong(jcrItem.getItemProperty(FileProperties.PROPERTY_WIDTH).getValue().toString()), Long.parseLong(jcrItem
                            .getItemProperty(FileProperties.PROPERTY_HEIGHT).getValue().toString()));
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
        Property data = jcrItem.getItemProperty(JcrConstants.JCR_DATA);

        if (binaryData != null) {
            data.setValue(new ByteArrayInputStream(binaryData));
        }
        jcrItem.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(StringUtils.substringBefore(fileName, "."));
        jcrItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(mimeType);
        jcrItem.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new GregorianCalendar(TimeZone.getDefault()));
        jcrItem.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(fileSize);
        jcrItem.getItemProperty(FileProperties.PROPERTY_EXTENSION).setValue(PathUtil.getExtension(fileName));
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
        jcrItem.addItemProperty(FileProperties.PROPERTY_FILENAME,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_FILENAME, "String", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_CONTENTTYPE,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_CONTENTTYPE, "String", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_LASTMODIFIED,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_LASTMODIFIED, "Date", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_SIZE,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_SIZE, "Long", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_EXTENSION,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_EXTENSION, "String", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_WIDTH,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_WIDTH, "Long", null));
        jcrItem.addItemProperty(FileProperties.PROPERTY_HEIGHT,
                DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_HEIGHT, "Long", null));

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
            imageSize = ImageSize.valueOf(new ByteArrayInputStream(getBinaryData()));
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
     * Create a preview Component object.
     */
    @Override
    public Component createPreview(Application application) {
        if (isImage()) {
            return createImagePreview(application);
        } else {
            return createFilePreview();
        }
    }

    /**
     * Create an Embedded Image.
     */
    private Component createImagePreview(Application application) {
        ImageSize scaledImageSize = imageSize.scaleToFitIfLarger(150, 150);
        final byte[] pngData = getBinaryData();
        Resource imageResource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(pngData);
            }
        }, "", application) {
            @Override
            public String getMIMEType() {
                return getMimeType();
            }
        };

        Embedded embedded = new Embedded(null, imageResource);
        embedded.setType(Embedded.TYPE_IMAGE);
        embedded.setWidth(scaledImageSize.getWidth(), Sizeable.UNITS_PIXELS);
        embedded.setHeight(scaledImageSize.getHeight(), Sizeable.UNITS_PIXELS);
        embedded.addStyleName("image");

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
        String iconeClassName = "icon-file";
        if (mimeType.contains("application/pdf")) {
            return iconeClassName + "-pdf";
        }
        if (mimeType.matches("application.*(msword)")) {
            return iconeClassName + "-word";
        }
        if (mimeType.matches("application.*(excel|xls)")) {
            return iconeClassName + "-excel";
        }
        if (mimeType.matches("application.*(powerpoint)")) {
            return iconeClassName + "-powerpoint";
        }
        if (mimeType.contains("text/")) {
            return iconeClassName + "-text";
        }
        if (mimeType.contains("image/")) {
            return iconeClassName + "-image";
        }
        return iconeClassName;
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

    protected String getMimeType() {
        return mimeType;
    }

    protected byte[] getBinaryData() {
        return binaryData;
    }

}
