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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.admincentral.file.FileItemWrapper;
import info.magnolia.ui.admincentral.image.ImageSize;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.view.ModalCloser;
import info.magnolia.ui.vaadin.view.View;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload.StartedEvent;

/**
 * Implementation of the Abstract {@link AbstractUploadFileField}.
 * <p>
 * Define the Layout for
 * <ul>
 * <li>Initial Display (no File are yet uploaded)
 * <li>Progress Display (ProgressBar / Cancel Button...)
 * <li>Upload done Display (File Detail / Preview ...)
 * </ul>
 * Create the Preview Component.
 */
public class UploadFileFieldImpl extends AbstractUploadFileField<FileItemWrapper> {

    private static final Logger log = LoggerFactory.getLogger(UploadFileFieldImpl.class);

    private String selectImageCaption;
    private String chooseNewCaption;
    private String dragHintCaption;
    private String fileNameCaption;
    private String fileFormatCaption;
    private String fileSizeCaption;

    private String mimeTypeRegExp;

    private final CssLayout layout;

    /**
     * Initialize basic components.
     */
    public UploadFileFieldImpl(FileItemWrapper fileItem, Shell shell, SubAppContext subAppContext) {
        super(fileItem, shell, subAppContext);

        initMessages();

        layout = new CssLayout();
        layout.setSizeUndefined();
        setRootLayout(createDropZone(layout));

        addStyleName("upload-image-field");
        addStyleName("no-horizontal-drag-hints");
        addStyleName("no-vertical-drag-hints");

        createProgressIndicator();
        createCancelButton();
        createDeleteButton();
        createEditButton();
        createFileDetail();
    }

    @Override
    protected Component initContent() {
        return getRootLayout();
    }

    /**
     * Initialize the root component.
     * Build the initial layout:
     * - Default Layout if the incoming Item is empty.
     * - Upload done Layout with the Item Information if this Item is not empty.
     */
    @Override
    public void attach() {
        super.attach();
        updateDisplay();
    }

    /**
     * Define the acceptance Upload Image criteria.
     * The current implementation only check if the MimeType match the desired regExp.
     */
    @Override
    public boolean isValidFile(StartedEvent event) {
        log.debug("evaluate following regExp: " + mimeTypeRegExp + " agains " + event.getMIMEType());
        return event.getMIMEType().matches(mimeTypeRegExp);
    }

    @Override
    protected void buildDefaultUploadLayout() {
        layout.removeAllComponents();
        setUploadButtonCaption(selectImageCaption);
        layout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));
        Label uploadText = new Label(dragHintCaption, ContentMode.HTML);
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
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    @Override
    public void buildUploadDoneLayout() {
        layout.removeAllComponents();

        if (info) {
            Label detail = (Label) getDefaultComponent(DefaultComponent.FILE_DETAIL);
            detail.setValue(getDisplayDetails());
            layout.addComponent(detail);
        }
        // Action Button. Default is always Upload
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setSizeUndefined();
        actionLayout.addStyleName("buttons");
        actionLayout.setSpacing(true);

        // if an Image was already uploaded, give the ability to edit it.
        if (fileItem.getJcrItem().getParent() != null && fileDeletion) {
            actionLayout.addComponent(getDefaultComponent(DefaultComponent.EDIT_BUTTON));
        }

        // Change the Label of the Upload Button
        setUploadButtonCaption(chooseNewCaption);
        actionLayout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));

        // if an Image was already uploaded, give the ability to remove it.
        if (fileItem.getJcrItem().getParent() != null && fileDeletion) {
            actionLayout.addComponent(getDefaultComponent(DefaultComponent.DELETE_BUTTON));
            actionLayout.setComponentAlignment(getDefaultComponent(DefaultComponent.DELETE_BUTTON), Alignment.MIDDLE_LEFT);
        }
        layout.addComponent(actionLayout);

        // Create preview Image
        if (preview && !fileItem.isEmpty()) {
            Component preview = fileItem.createPreview();
            Resource previewResource = fileItem.getResource();

            Component previewComponent = createFullPreviewComponent(preview, previewResource);
            layout.addComponent(previewComponent);
        }
        getRootLayout().addStyleName("upload");
        getRootLayout().removeStyleName("in-progress");
        getRootLayout().removeStyleName("initial");
        getRootLayout().addStyleName("done");
    }

    /**
     * Create a preview image with a button in lower-left to open the media in a lightbox,
     * and a button in the lower-right to open the MediaEditor.
     */
    protected Component createFullPreviewComponent(Component preview, final Resource previewResource) {

        AbsoluteLayout previewLayout = new AbsoluteLayout();
        previewLayout.addStyleName("file-preview-area");
        previewLayout.setWidth("150px");
        previewLayout.setHeight("150px");

        previewLayout.addComponent(preview, "top: 0px; left: 0px; right: 0px; bottom: 0px; z-index: 0;");

        // Add buttons to the preview layout

        Button lightboxButton = new Button();
        lightboxButton.setHtmlContentAllowed(true);
        lightboxButton.setCaption("<span class=\"" + "icon-search" + "\"></span>");
        lightboxButton.setDescription(lightboxCaption);
        previewLayout.addComponent(lightboxButton, "left: 0px; bottom: 0px; z-index: 1;");

        Button editButton = new Button();
        editButton.setHtmlContentAllowed(true);
        editButton.setCaption("<span class=\"" + "icon-edit" + "\"></span>");
        editButton.setDescription(editFileCaption);
        previewLayout.addComponent(editButton, "right: 0px; bottom: 0px; z-index: 1;");

        // Button handlers

        editButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // Launch MediaEditor for this item.
                openMediaEditor();
                updateDisplay();
            }
        });

        lightboxButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // Launch Lightbox component
                openLightbox(previewResource);
            }
        });

        return previewLayout;
    }

    /**
     * Open a lightbox with the media of this file.
     */
    protected void openLightbox(final Resource imageResource) {

        final Embedded imageComponent = new Embedded("", imageResource);
        imageComponent.addStyleName("lightbox-image");
        View lightboxView = new View() {
            @Override
            public Component asVaadinComponent() {
                return imageComponent;
            }
        };

        final ModalCloser lightbox = subAppContext.openModal(lightboxView);
        imageComponent.addClickListener(new ClickListener() {

            @Override
            public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                lightbox.close();
            }
        });
    }

    @Override
    public void buildUploadStartedLayout() {
        super.buildUploadStartedLayout();
        layout.removeAllComponents();

        if (progressInfo) {
            layout.addComponent(getDefaultComponent(DefaultComponent.PROGRESS_BAR));
        }
        layout.addComponent(getDefaultComponent(DefaultComponent.CANCEL_BUTTON));

        getRootLayout().removeStyleName("done");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");
        getRootLayout().addStyleName("in-progress");
    }

    @Override
    public Component createPreviewComponent() {
        Component preview = this.fileItem.createPreview();
        getDefaultComponents().put(DefaultComponent.PREVIEW, preview);
        return preview;
    }

    @Override
    protected String getDisplayDetails() {
        StringBuilder sb = new StringBuilder();

        // Title
        sb.append("<span class=\"value\">");
        sb.append("Image details");
        sb.append("</span>");
        sb.append("<br/><br/>");

        // Name
        sb.append("<span class=\"key\">");
        sb.append(fileNameCaption);
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(this.fileItem.getFileName());
        sb.append("</span>");
        sb.append("<br/>");

        // Size
        sb.append("<span class=\"key\">");
        sb.append(fileSizeCaption);
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        if (this.fileItem.isImage()) {
            ImageSize imageSize = this.fileItem.getImageSize();
            sb.append(imageSize.getWidth() + " x " + imageSize.getHeight() + ", ");
        }
        sb.append(FileUtils.byteCountToDisplaySize(this.fileItem.getFileSize()));
        sb.append("</span>");
        sb.append("<br/>");

        // Format
        sb.append("<span class=\"key\">");
        sb.append(fileFormatCaption);
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(this.fileItem.getFormat());
        sb.append("</span>");
        sb.append("<br/>");

        return sb.toString();
    }

    private void initMessages() {
        selectImageCaption = MessagesUtil.get("field.upload.select.image");
        chooseNewCaption = MessagesUtil.get("field.upload.choose.new");
        dragHintCaption = MessagesUtil.get("field.upload.drag.hint");
        fileNameCaption = MessagesUtil.get("field.upload.file.name");
        fileSizeCaption = MessagesUtil.get("field.upload.file.size");
        fileFormatCaption = MessagesUtil.get("field.upload.file.format");
    }

    /**
     * Set regExp used to validate the uploaded mimeType.
     * In case of the regExp do not match, interrupt the upload.
     */
    public void setMimeTypeRegExp(String regExp) {
        this.mimeTypeRegExp = regExp;
    }
}
