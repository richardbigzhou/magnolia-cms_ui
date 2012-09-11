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
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
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
 * <p>Define the Layout for
 * <ul>
 *  <li>Initial Display (no Images are yet uploaded)
 *  <li>Progress Display (ProgressBar / Cancel Button...)
 *  <li>Upload done Display (File Detail / Preview ...)
 * </ul>
 * Create the Preview Component.
 * Override update methods to add the specific images informations to the Item (Width / Height)
 */
public class UploadImageField extends AbstractUploadFileField {

    private static final Logger log = LoggerFactory.getLogger(UploadImageField.class);

    private String selectImageCaption;
    private String chooseNewCaption;
    private String dragHintCaption;
    private String fileNameCaption;
    private String fileSizeCaption;

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

        initMessages();

        layout = new CssLayout();
        layout.setSizeUndefined();
        setRootLayout(createDropZone(layout));
        setCompositionRoot(getRootLayout());

        addStyleName("upload-image-field");
        addStyleName("no-horizontal-drag-hints");
        addStyleName("no-vertical-drag-hints");

        createProgressIndicator();
        createCancelButton();
        createDeleteButton();
        createFileDetail();
    }

    /**
     * Initialize the root component.
     * Build the initial layout:
     *  - Default Layout if the incoming Item is empty.
     *  - Upload done Layout with the Item Information if this Item is not empty.
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
    public void clearLastUploadData() {
        super.clearLastUploadData();
        imageWidth = -1;
        imageHeight = -1;
    }

    @Override
    public void setLastUploadData() {
        super.setLastUploadData();
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
        setUploadButtonCaption(selectImageCaption);
        layout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));
        Label uploadText = new Label(dragHintCaption, Label.CONTENT_XHTML);
        uploadText.addStyleName("upload-text");
        layout.addComponent(uploadText);
        getRootLayout().removeStyleName("start");
        getRootLayout().removeStyleName("done");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");
    }

    @Override
    public void refreshOnProgressUploadLayout(long readBytes, long contentLength) {
        super.refreshOnProgressUploadLayout(readBytes, contentLength);
    }

    @Override
    public void buildUploadDoneLayout() {
        layout.removeAllComponents();

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
        setUploadButtonCaption(chooseNewCaption);
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
        getRootLayout().addStyleName("upload");
        getRootLayout().removeStyleName("in-progress");
        getRootLayout().removeStyleName("initial");
        getRootLayout().addStyleName("done");
    }

    @Override
    public void buildUploadStartedLayout() {
        super.buildUploadStartedLayout();
        layout.removeAllComponents();

        if(progressInfo) {
            layout.addComponent(getDefaultComponent(DefaultComponent.PROGRESS_BAR));
        }
        layout.addComponent(getDefaultComponent(DefaultComponent.CANCEL_BUTTON));

        getRootLayout().removeStyleName("done");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");
        getRootLayout().addStyleName("in-progress");
    }


    @Override
    public Embedded createPreview() {
        ImageSize scaledImageSize;
        scaledImageSize = createImageSize().scaleToFitIfLarger(150, 150);

        final byte[] pngData = (byte[]) getLastBytesFile();

        @SuppressWarnings("serial")
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

        sb.append("<span class=\"key\">");
        sb.append(fileNameCaption);
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(getLastFileName());
        sb.append("</span>");

        sb.append("</br>");

        ImageSize imageSize = createImageSize();
        sb.append("<span class=\"key\">");
        sb.append(fileSizeCaption);
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(imageSize.getWidth() + " x " + imageSize.getHeight() + ", ");
        sb.append(FileUtils.byteCountToDisplaySize(getLastFileSize()));
        sb.append("</span>");

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

    private void initMessages() {
        selectImageCaption = MessagesUtil.get("field.upload.select.image");
        chooseNewCaption = MessagesUtil.get("field.upload.choose.new");
        dragHintCaption = MessagesUtil.get("field.upload.drag.hint");
        fileNameCaption = MessagesUtil.get("field.upload.file.name");
        fileSizeCaption = MessagesUtil.get("field.upload.file.size");
    }
}
