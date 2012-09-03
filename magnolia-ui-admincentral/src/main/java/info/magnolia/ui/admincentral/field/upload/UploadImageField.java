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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload.StartedEvent;


/**
 * Implementation of the Abstract {@link AbstractUploadFileField}.
 * Define the Layout for
 *  - Initial Display (no Images are yet uploaded)
 *  - Progress Display (ProgressBar / Cancel Button...)
 *  - Upload Finish Display (File Detail / Preview ...)
 * Create the Preview Component.
 *
 * Override update methods to add the specific images informations to the Item (Width / Height)
 */
public class UploadImageField extends AbstractUploadFileField {

    private static final Logger log = LoggerFactory.getLogger(UploadImageField.class);
    private static final String DEFAULT_UPLOAD_INITIAL_BUTTON_CAPTION = "Select an image";
    private static final String DEFAULT_UPLOAD_ANOTHERL_BUTTON_CAPTION = "Choose new";
    private static final String DEFAULT_DROP_ZONE_IMAGE_CAPTION = "or drag an image into this area";
    private CssLayout layout;
    private JcrItemNodeAdapter item;
    private long imageWidth;
    private long imageHeight;

    /**
     * Initialize basic components.
     */
    public UploadImageField(JcrItemNodeAdapter item,  Shell shell) {
        super(item, shell);
        this.item = item;
        layout = new CssLayout();
        layout.setSizeUndefined();
        //Define the GridLayout as the whole drop zone and as root element.
        setRootLayout(createDropZone(layout));
        setCompositionRoot(getRootLayout());

        //Initialize a default Progress Indicator
        createProgressIndicator();
        //Create cancel Button
        createCancelButton();
        //Create Delete Button
        createDeleteButton();
        //Init File Detail
        createFileDetail();
    }

    /**
     * Initialize the root component.
     * Build the initial layout:
     *  - Default Layout if the incoming Item is empty.
     *  - Upload Finish Layout with the Item Information if this Item is not empty.
     */
    @Override
    public void attach() {
        super.attach();

        //Init values with existing data.
        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);
        if(data !=null && data.getValue()!=null) {
            setLastUploadData(item);
        }
        updateDisplay();
        getParent().addStyleName("no-horizontal-drag-hints");
        getParent().addStyleName("no-vertical-drag-hints");
    }

    /**
     * Populate specific file informations to the Item.
     */
    @Override
    protected void populateItemProperty() {
        super.populateItemProperty();
        item.getItemProperty(FileProperties.PROPERTY_WIDTH).setValue(imageWidth);
        item.getItemProperty(FileProperties.PROPERTY_HEIGHT).setValue(imageHeight);
    }
    /**
     * Clear specific file informations.
     */
    @Override
    public void clearLastUploadDatas() {
        super.clearLastUploadDatas();
        imageWidth = -1;
        imageHeight = -1;
    }

    @Override
    public void setLastUploadDatas() {
        super.setLastUploadDatas();
        ImageSize imageSize;
        imageSize = ImageSize.valueOf(new ByteArrayInputStream(getLastBytesFile()));
        imageWidth = imageSize.getWidth();
        imageHeight = imageSize.getHeight();
    }

    @Override
    public void setLastUploadData(JcrItemNodeAdapter item) {
        super.setLastUploadData(item);
        ImageSize imageSize = new ImageSize((Long)item.getItemProperty(FileProperties.PROPERTY_WIDTH).getValue(), (Long)item.getItemProperty(FileProperties.PROPERTY_HEIGHT).getValue());
        imageWidth = imageSize.getWidth();
        imageHeight = imageSize.getHeight();
    }

    /**
     * Define the acceptance Upload Image criteria.
     * The current implementation only check if the MimeType start with image.
     */
    @Override
    public boolean isValidFile(StartedEvent event) {
        return event.getMIMEType().startsWith("image/");
    }


    @Override
    protected void buildDefaultUploadLayout() {
        layout.removeAllComponents();
        setUploadButtonCaption(DEFAULT_UPLOAD_INITIAL_BUTTON_CAPTION);
        layout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));
        Label uploadText = new Label(DEFAULT_DROP_ZONE_IMAGE_CAPTION);
        uploadText.addStyleName("upload-text");
        layout.addComponent(uploadText);
        getRootLayout().removeStyleName("finish");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");
    }

    @Override
    public void refreshOnProgressUploadLayout(long readBytes, long contentLength) {
        super.refreshOnProgressUploadLayout(readBytes, contentLength);
        //JUST TO MAKE THE PROGRESS BAR VISIBLE
        try {
            Thread.sleep(10);
        }
        catch (InterruptedException e) {
            log.error("",e);
        }
    }

    /**
     * Handle View when the Upload is finished.
     */
    @Override
    public void buildFinishUploadLayout() {
        layout.removeAllComponents();
        // Display Detail
        if (info) {
            Label detail = (Label)getDefaultComponent(DefaultComponent.FILE_DETAIL);
            detail.setValue(getDisplayDetails());
            layout.addComponent(detail);
        }
        // Action Button. Default is always Upload
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setSizeUndefined();
        actionLayout.addStyleName("buttons");

        //Change the Label of the Upload Button
        setUploadButtonCaption(DEFAULT_UPLOAD_ANOTHERL_BUTTON_CAPTION);
        actionLayout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));
        // if an Image was already uploaded, give the ability to remove it.
        if(item.getParent() != null && fileDeletion) {
            actionLayout.addComponent(getDefaultComponent(DefaultComponent.DELETE_BUTTON));
        }
        layout.addComponent(actionLayout);

        //Create preview Image
        if(preview && getLastBytesFile() != null) {
            Embedded preview = createPreview();
            layout.addComponent(preview);
        }
        //set Color
        getRootLayout().addStyleName("upload");
        getRootLayout().removeStyleName("start");
        getRootLayout().removeStyleName("initial");
        getRootLayout().addStyleName("finish");
    }

    @Override
    public void buildStartUploadLayout() {
        super.buildStartUploadLayout();
        layout.removeAllComponents();
        // Add Progress Bar
        if(progressInfo) {
            layout.addComponent(getDefaultComponent(DefaultComponent.PROGRESS_BAR));
        }
        // Add Cancel Button
        layout.addComponent(getDefaultComponent(DefaultComponent.CANCEL_BUTTON));

        getRootLayout().removeStyleName("finish");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");
        getRootLayout().addStyleName("start");
    }


    /**
     * Create the Preview Image.
     */
    @Override
    public Embedded createPreview() {
        ImageSize scaledImageSize;
        scaledImageSize = createImageSize().scaleToFitIfLarger(150, 150);

        //Create resource
        final byte[] pngData = (byte[]) getLastBytesFile();
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

        Embedded embedded = new Embedded(null, imageResource);
        embedded.setType(Embedded.TYPE_IMAGE);
        embedded.setWidth(scaledImageSize.getWidth(), Sizeable.UNITS_PIXELS);
        embedded.setHeight(scaledImageSize.getHeight(), Sizeable.UNITS_PIXELS);
        embedded.addStyleName("image");

        getDefaultComponents().put(DefaultComponent.PREVIEW, embedded);

        return embedded;
    }

    @Override
    protected String getDisplayDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getDisplayDetails());
        ImageSize imageSize;
        imageSize = createImageSize();
        sb.append("</br>width: " + imageSize.getWidth() + " height: " + imageSize.getHeight());
        return sb.toString();
    }

    /**
     * Best effort to create an ImageSize.
     * If the ImageSize created based on the receiver is null
     * try to create it from the Item property.
     * @return: null if receiver and item don't have relevant information.
     */
    protected ImageSize createImageSize() {
        ImageSize imageSize = ImageSize.valueOf(new ByteArrayInputStream(getLastBytesFile()));
        if(imageSize == null && item.getItemProperty(FileProperties.PROPERTY_WIDTH) != null && item.getItemProperty(FileProperties.PROPERTY_HEIGHT) != null) {
             imageSize = new ImageSize((Long)item.getItemProperty(FileProperties.PROPERTY_WIDTH).getValue(), (Long)item.getItemProperty(FileProperties.PROPERTY_HEIGHT).getValue());
         }
        return imageSize;
    }
}
