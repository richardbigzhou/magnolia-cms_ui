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
import info.magnolia.ui.framework.shell.Shell;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
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
    private String fileSizeCaption;
    private String mimeTypeRegExp;

    private final CssLayout layout;

    /**
     * Initialize basic components.
     */
    public UploadFileFieldImpl(FileItemWrapper fileItem, Shell shell) {
        super(fileItem, shell);

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

        // Change the Label of the Upload Button
        setUploadButtonCaption(chooseNewCaption);
        actionLayout.addComponent(getDefaultComponent(DefaultComponent.UPLOAD));
        // if an Image was already uploaded, give the ability to remove it.
        if (fileItem.getJcrItem().getParent() != null && fileDeletion) {
            actionLayout.addComponent(getDefaultComponent(DefaultComponent.DELETE_BUTTON));
        }
        layout.addComponent(actionLayout);

        // Create preview Image
        if (preview && !fileItem.isEmpty()) {
            Component preview = fileItem.createPreview(getApplication());
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
        Component preview = this.fileItem.createPreview(getApplication());
        getDefaultComponents().put(DefaultComponent.PREVIEW, preview);
        return preview;
    }

    @Override
    protected String getDisplayDetails() {
        StringBuilder sb = new StringBuilder();

        sb.append("<span class=\"key\">");
        sb.append(fileNameCaption);
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(this.fileItem.getFileName());
        sb.append("</span>");
        sb.append("</br>");
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

        return sb.toString();
    }

    private void initMessages() {
        selectImageCaption = MessagesUtil.get("field.upload.select.image");
        chooseNewCaption = MessagesUtil.get("field.upload.choose.new");
        dragHintCaption = MessagesUtil.get("field.upload.drag.hint");
        fileNameCaption = MessagesUtil.get("field.upload.file.name");
        fileSizeCaption = MessagesUtil.get("field.upload.file.size");
    }

    /**
     * Set regExp used to validate the uploaded mimeType.
     * In case of the regExp do not match, interrupt the upload.
     */
    public void setMimeTypeRegExp(String regExp) {
        this.mimeTypeRegExp = regExp;
    }
}
