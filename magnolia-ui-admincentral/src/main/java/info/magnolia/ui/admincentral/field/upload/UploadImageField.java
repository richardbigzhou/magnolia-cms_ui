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
import info.magnolia.cms.util.PathUtil;
import info.magnolia.jcr.util.BinaryInFile;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.easyuploads.FileBuffer;

import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window.Notification;


/**
 * .
 */
public class UploadImageField extends AbstractUploadFileField{

    private static final Logger log = LoggerFactory.getLogger(UploadImageField.class);
    private GridLayout layout;
    private Label uploadFileLocation;
    private Label uploadFileRatio;
    private Label uploadFileProgress;

    public UploadImageField(JcrItemNodeAdapter item) {
        super(item);

        layout = new GridLayout(2,3);
        layout.setSizeFull();
        //Define the root Layout.
        setRootLayout(createDropZone(layout));
        //Initialize a default Progress Indicator
        createProgressIndicator();
        //Initialize a default Delete Button
        createDeleteButton();
        //Init File Detail
        createFileDetail();
        //Init Upload Progress Label
        uploadFileLocation = new Label("Uploading File ");
        uploadFileRatio = new Label("Uploaded ");
        uploadFileProgress = new Label("");

    }


    /**
     * Handle the Uploaded or Removed Image.
     */
    @Override
    protected void handleFile() {
        if(hasRequestForFileDeletion() || getReceiver().isEmpty()){
            //remove Item from the parent --> this item will not be persisted
            getItem().getParent().removeChild(getItem());
        } else {
            populateItemProperty();
        }
    }

    /**
     * Populate the Item property (data/image name/...)
     * Data is stored as a JCR Binary object.
     */
    private void populateItemProperty() {
        JcrItemNodeAdapter item = getItem();
        FileBuffer receiver = getReceiver();
        //Attach the Item to the parent in order to be stored.
        item.getParent().addChild(item);
        //Populate Data
        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);
        try {
            if(data!=null) {
                BinaryInFile binaryImpl;
                binaryImpl = new BinaryInFile(getReceiver().getFile());
                data.setValue(binaryImpl);
            }
            item.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(StringUtils.substringBefore(receiver.getLastFileName(), "."));
            item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(receiver.getLastMimeType());
            item.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new GregorianCalendar(TimeZone.getDefault()));
            item.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(receiver.getLastFileSize());
            item.getItemProperty(FileProperties.PROPERTY_EXTENSION).setValue(PathUtil.getExtension(receiver.getLastFileName()));
            ImageSize imageSize = ImageSize.valueOf(receiver.getFile());
            item.getItemProperty(FileProperties.PROPERTY_WIDTH).setValue( imageSize.getWidth());
            item.getItemProperty(FileProperties.PROPERTY_HEIGHT).setValue(imageSize.getHeight());
        } catch (IOException e) {
            log.error("Not able to store the binary information ", e);
            getWindow().showNotification("Upload failed", "Filenot persisted", Notification.TYPE_WARNING_MESSAGE);
        }
    }


    /**
     * Initialization of the display.
     * Called once when the Field is attached.
     */
    @Override
    protected void initDisplay() {
        buildDefaultUploadLayout();
    }

    /**
     * Default View.
     */
    @Override
    public void buildDefaultUploadLayout() {
        layout.removeAllComponents();
        setUploadButtonCaption("Choose an image");
        layout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD),0,1);
        Label uploadText = new Label("Drag and Drop an Image");
        layout.addComponent(uploadText,1,1);
        //set Color
        getRootLayout().setStyleName("upload-initial");
        super.buildDefaultUploadLayout();
    }

    /**
     * Handle Progress View.
     */
    @Override
    public void refreshOnProgressUploadLayout(long readBytes, long contentLength) {
        super.refreshOnProgressUploadLayout(readBytes, contentLength);
        ((Label)layout.getComponent(0,0)).setValue("Uploading File "+getReceiver().getLastFileName());
        ((Label)layout.getComponent(1,1)).setValue(createPercentage(readBytes, contentLength));
        ((Label)layout.getComponent(0,2)).setValue("Uploaded "+FileUtils.byteCountToDisplaySize(readBytes)+" of "+FileUtils.byteCountToDisplaySize(contentLength));

        //JUST TO MAKE THE PROGRESS BAR VISIBLE
        try {
            Thread.sleep(5);
        }
        catch (InterruptedException e) {
            log.error("",e);
        }
    }

    private String createPercentage(long readBytes, long contentLength ) {
        double read = Double.valueOf(readBytes);
        double from = Double.valueOf(contentLength);

        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(2);

        return defaultFormat.format((read/from));
    }

    /**
     * Handle View when the Upload is finished.
     */
    @Override
    public void buildFinishUploadLayout() {
        layout.removeAllComponents();
        // Display Detail
        Label detail = (Label)getDefaultComponent(DefaultComponent.FILE_DETAIL);
        detail.setValue(getDisplayDetails());
        layout.addComponent(detail,0,0,0,1);

        // Action Button. Default is always Upload
        HorizontalLayout actionLayout = new HorizontalLayout();

        //Change the Label of the Upload Button
        setUploadButtonCaption("Choose different image");
        actionLayout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));
        // if an Image was already uploaded, give the ability to remove it.
        if(getItem().getParent() != null) {
            actionLayout.addComponent(getDefaultComponent(DefaultComponent.DELETE_BUTTON));
        }
        layout.addComponent(actionLayout,0,2);

        //Create preview Image
        if(!getReceiver().isEmpty()) {
            Embedded preview = createPreview();
            layout.addComponent(preview,1,0,1,2);
        }
        //set Color
        getRootLayout().setStyleName("upload-finish");
    }

    /**
     * Handle View before Upload start.
     */
    @Override
    public void buildStartUploadLayout() {
        layout.removeAllComponents();
        // Add Uploading path
        layout.addComponent(uploadFileLocation,0,0);
        // Add Progress Bar
        layout.addComponent(getDefaultComponent(DefaultComponent.PROGRESS_BAR),0,1);
        // Add ratio
        layout.addComponent(uploadFileRatio,0,2);
        // Add Stop Button
//        final Button cancelProcessing = new Button("Cancel");
//        cancelProcessing.addListener(new Button.ClickListener() {
//            @Override
//            public void buttonClick(ClickEvent event) {
//                getUpload().interruptUpload();
//                setValue(null);
//            }
//        });
//        cancelProcessing.setVisible(true);
//        cancelProcessing.setStyleName("small");
//
//        layout.addComponent(cancelProcessing,1,1);
        layout.addComponent(uploadFileProgress,1,1);
        //set Color
        getRootLayout().setStyleName("upload-start");

        super.buildStartUploadLayout();
    }


    @Override
    protected String getDisplayDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getDisplayDetails());
        ImageSize imageSize;
        try {
            imageSize = createImageSize();
            sb.append("</br>width: " + imageSize.getWidth() + " height: " + imageSize.getHeight());
        }
        catch (FileNotFoundException e) {
            log.error("",e);
        }
        return sb.toString();
    }



    /**
     * Create the Preview Image.
     */
    @Override
    public Embedded createPreview() {
        Embedded embedded = null;
        //Set Image Size
        ImageSize scaledImageSize;
        try {
            scaledImageSize = createImageSize().scaleToFitIfLarger(150, 150);
        }
        catch (FileNotFoundException e) {
            log.error("",e);
            return embedded;
        }
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
                return getLastMIMEType();
            }
        };

        embedded = new Embedded("", imageResource);
        embedded.setWidth(scaledImageSize.getWidth() + "px");
        embedded.setHeight(scaledImageSize.getHeight() + "px");

        getDefaultComponents().remove(DefaultComponent.PREVIEW);
        getDefaultComponents().put(DefaultComponent.PREVIEW, embedded);

        return embedded;
    }


}
