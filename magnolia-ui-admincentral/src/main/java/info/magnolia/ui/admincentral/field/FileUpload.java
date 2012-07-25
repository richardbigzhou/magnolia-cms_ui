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
package info.magnolia.ui.admincentral.field;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.PathUtil;
import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.easyuploads.UploadField;

import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload.FinishedEvent;

/**
 * File Upload Field.
 * Initialize File information if one file was already stored (Name/Thumbnail/...).
 * On success Set all information needed to store the image to the Item.
 *
 */
public class FileUpload extends UploadField {

    private static final Logger log = LoggerFactory.getLogger(FileUpload.class);
    private JcrNodeAdapter item;
    private String lastFileExtension;
    private long lastImageWidth;
    private long lastImageHeight;
    private List<String> imageExtensions = new ArrayList<String>();
    private FileUploadFieldDefinition definition;
    private Embedded embedded;
    private Label displayDetail = new Label("", Label.CONTENT_XHTML);


    public FileUpload ( JcrNodeAdapter item, FileUploadFieldDefinition definition) {
        super();
        this.item = item;
        this.definition = definition;
        // Set Storage and FileType
        setStorageMode(UploadField.StorageMode.MEMORY);
        setFieldType(UploadField.FieldType.BYTE_ARRAY);

        initImageExtensions();
    }

    /**
     * Init Display with existing Data.
     */
    @Override
    public void attach() {
        super.attach();
        //TODO SCRUM-1401: Reactivate after resolution of this issue (Applicationis null)
        //Init values with existing data.
//        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);
//        if(data !=null && data.getValue()!=null) {
//            setValue(data.getValue());
//            updateDisplay();
//        }
    }
    /**
     * Refresh the view when upload is finished.
     * First update some basic File property like the extension
     * Then call super.
     * At the end Populate the file Properties and Binary into the related Item.
     */
    @Override
    public void uploadFinished(FinishedEvent event) {
        updateProperty();
        super.uploadFinished(event);
        populateItemProperty();
    }


    @Override
    protected void updateDisplay() {
        if (showPreviewForExtension()) {
            if(embedded!=null && getRootLayout().getComponentIndex(embedded)!=-1) {
                getRootLayout().removeComponent(embedded);
                getRootLayout().removeComponent(displayDetail);
            }
            displayDetail.setValue(getDisplayDetails());
            embedded = createThumbnail();
            getRootLayout().addComponent(embedded);
            getRootLayout().addComponent(displayDetail);
        } else {
            super.updateDisplay();
        }
    }

    /**
     * Populate the Item property (data/image name/...)
     * Data is stored as a JCR Binary object.
     */
    private void populateItemProperty() {
        //Populate Data
        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);

        if(data!=null) {
            BinaryImpl binaryImpl;
            try {
                binaryImpl = new BinaryImpl(getContentAsStream());
                data.setValue(binaryImpl);
            }
            catch (IOException e) {
                log.error("Not able to store the binery information ", e);
            }
        }
        item.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(getLastFileName());
        item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(getLastMimeType());
        item.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new GregorianCalendar(TimeZone.getDefault()));
        item.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(getLastFileSize());
        item.getItemProperty(FileProperties.PROPERTY_EXTENSION).setValue(lastFileExtension);
        item.getItemProperty(FileProperties.PROPERTY_WIDTH).setValue(lastImageWidth);
        item.getItemProperty(FileProperties.PROPERTY_HEIGHT).setValue(lastImageHeight);
    }

    @Override
    protected String getDisplayDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ");
        sb.append(getLastFileName());
        sb.append("</br> <em>");
        sb.append("(" + getLastFileSize() + " bytes)");
        sb.append("</em>");
        sb.append("</br>width: " + lastImageWidth + " height: " + lastImageHeight);
        return sb.toString();
    }

    /**
     * Create a Thumbnail image as Embedded component.
     */
    private Embedded createThumbnail() {
        Embedded embedded = null;
        //Set Image Size
        ImageSize scaledImageSize = ImageSize.valueOf(getContentAsStream()).scaleToFitIfLarger(150, 150);
        //Create ressource
        final byte[] pngData = (byte[]) getValue();
        Resource imageResource = new StreamResource(
            new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(pngData);
                }
            }, "", this.getApplication()){
            @Override
            public String getMIMEType() {
                return getLastMimeType();
            }
        };

        embedded = new Embedded("", imageResource);
        embedded.setWidth(scaledImageSize.getWidth() + "px");
        embedded.setHeight(scaledImageSize.getHeight() + "px");
        return embedded;
    }

    /**
     * Define if we have to display the Thumbnail regarding
     *   the file extension and
     *   the fieldDefinition.
     */
    private boolean showPreviewForExtension() {
        return definition.isPreview() && this.getImageExtensions().contains(lastFileExtension.toLowerCase());
    }

    /**
     * Update basic property of the uploaded file.
     *  Define File extension, Image Height and Width.
     */
    private void updateProperty() {
        lastFileExtension = PathUtil.getExtension(getLastFileName());
        ImageSize imageSize = ImageSize.valueOf(getContentAsStream());
        lastImageWidth = imageSize.getWidth();
        lastImageHeight = imageSize.getHeight();
    }

    private List<String> getImageExtensions() {
        return this.imageExtensions;
    }

    private void initImageExtensions() {
        this.getImageExtensions().add("jpg");
        this.getImageExtensions().add("jpeg");
        this.getImageExtensions().add("gif");
        this.getImageExtensions().add("png");
        this.getImageExtensions().add("bmp");
        this.getImageExtensions().add("swf");
    }
}
