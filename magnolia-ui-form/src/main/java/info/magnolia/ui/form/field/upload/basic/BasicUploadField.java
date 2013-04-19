/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.form.field.upload.AbstractUploadField;
import info.magnolia.ui.form.field.upload.FileItemWrapper;
import info.magnolia.ui.form.field.upload.UploadProgressIndicator;
import info.magnolia.ui.model.imageprovider.definition.ImageProvider;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeButton;

/**
 * Basic implementation of {@link AbstractUploadField}.<br>
 * Define the Layout components for
 * <ul>
 * <li>EmptyLayout (no File are yet uploaded)
 * <li>InProgressLayout (ProgressBar / Cancel Button...)
 * <li>CompletedLayout (File Detail / Preview ...)
 * </ul>
 *
 * @param <D> {@link FileItemWrapper} implemented class.
 */
public class BasicUploadField<D extends BasicFileItemWrapper> extends AbstractUploadField<D> {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BasicUploadField.class);


    // Root layout
    private final CssLayout layout;
    private UploadProgressIndicator progress;
    protected final ImageProvider imageProvider;

    public BasicUploadField(D fileWrapper, File tmpUploadDirectory, ImageProvider imageProvider) {
        super(fileWrapper, tmpUploadDirectory);
        this.imageProvider = imageProvider;
        this.layout = new CssLayout();
        this.layout.setSizeUndefined();
        setRootLayout(createDropZone(layout));

        // Update Style Name
        addStyleName("upload-image-field");
        addStyleName("no-horizontal-drag-hints");
        addStyleName("no-vertical-drag-hints");

    }


    /**
     * Initialize the root component.
     * Build the initial layout:
     * - Empty Layout if the incoming Item is empty.
     * - Completed Layout with the Item Informations if this Item is not empty.
     */
    @Override
    public void attach() {
        super.attach();
        updateDisplay();
        log.debug("Component was attached ...");
    }

    /**
     * Main entry point to create the Empty Layout.
     * This Basic implementation of Empty layout is composed of <br>
     * - An Upload button <br>
     * - A Label inviting to Drag files <br>
     */
    @Override
    protected void buildEmptyLayout() {
        layout.removeAllComponents();
        // Add Upload Button
        getUpload().setButtonCaption(getCaption(selectNewCaption));
        layout.addComponent(getUpload());
        // Add DropZone Label
        Label uploadText = new Label(getCaption(dropZoneCaption), ContentMode.HTML);
        uploadText.addStyleName("upload-text");
        layout.addComponent(uploadText);

        // Update Style Name
        getRootLayout().removeStyleName("start");
        getRootLayout().removeStyleName("done");
        getRootLayout().removeStyleName("in-progress");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");

        log.debug("buildEmptyLayout() called ...");
    }

    /**
     * Main entry point to create the In Progress Layout.
     * This Basic implementation of In Progress is composed of <br>
     * - A Progress Bar <br>
     * - A Cancel Button <br>
     */
    @Override
    protected void buildInProgressLayout(String uploadedFileMimeType) {
        layout.removeAllComponents();
        // Update the caption Extension
        setCaptionExtension(uploadedFileMimeType);
        // Create the process Indigator
        progress = new BasicUploadProcessIndicator(inProgressCaption, inProgressRatioCaption);
        progress.setVisible(true);
        progress.setProgressIndicatorValue(0);
        layout.addComponent(progress);

        // Add the Cancel Button
        layout.addComponent(createCancelButton());

        // Update Style Name
        getRootLayout().removeStyleName("done");
        getRootLayout().addStyleName("upload");
        getRootLayout().addStyleName("initial");
        getRootLayout().addStyleName("in-progress");

        log.debug("buildInProgressLayout() called ...");
    }


    @Override
    protected void refreshInProgressLayout(long readBytes, long contentLength, String fileName) {
        if (progress != null) {
            progress.refreshOnProgressUploadLayout(readBytes, contentLength, fileName);
        }
    }

    /**
     * Main entry point to create the Completed Layout.
     * This Basic implementation of Completed Layout is composed of <br>
     * - An Icon representing the File type <br>
     * - A Detail text information <br>
     * - An Action bar: UploadNew and Delete Actions <br>
     * <b>Override</b><br>
     * getFileInfo() In order to change the Displayed Text <br>
     * createIconStyleName() In order to change the Icon Style displayed.
     */
    @Override
    protected void buildCompletedLayout() {
        layout.removeAllComponents();
        // Update the caption Extension
        setCaptionExtension(null);
        // Create the File Detail Component
        layout.addComponent(createFileInfoComponent());
        // Create the Action Layout
        layout.addComponent(createCompletedActionLayout());

        // Create Preview
        layout.addComponent(createThumbnailComponent());

        // Update Style Name
        getRootLayout().addStyleName("upload");
        getRootLayout().removeStyleName("in-progress");
        getRootLayout().removeStyleName("initial");
        getRootLayout().addStyleName("done");

        log.debug("buildCompletedLayout() called ...");
    }

    /**
     * Build the Completed Action Layout.
     */
    protected Layout createCompletedActionLayout() {
        // Action Button (Upload new or delete). Default is always Upload
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setSizeUndefined();
        actionLayout.addStyleName("buttons");
        actionLayout.setSpacing(true);
        // Add Upload Button
        getUpload().setButtonCaption(getCaption(selectAnotherCaption));
        actionLayout.addComponent(getUpload());
        // Add Remove Button if a file is present.
        if (!getFileWrapper().isEmpty()) {
            Button delete = createDeleteButton();
            actionLayout.addComponent(delete);
            actionLayout.setComponentAlignment(delete, Alignment.MIDDLE_RIGHT);
        }
        return actionLayout;
    }


    /**
     * Create the Cancel Button.
     * Used to cancel an ongoing Upload.
     */
    private Button createCancelButton() {
        Button cancelButton = new NativeButton(null, new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                interruptUpload();
            }
        });
        cancelButton.addStyleName("cancel");
        return cancelButton;
    }

    /**
     * Create Delete button.
     */
    protected Button createDeleteButton() {
        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription(MessagesUtil.get(deleteCaption));

        deleteButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                getFileWrapper().clearProperties();
                updateDisplay();
            }
        });
        return deleteButton;
    }

    /**
     * Initialize a Component displaying some File Informations.
     * Override getFileInfo() in order to display the infos you may want to display.
     *
     * @return A file Info Component. Generally a {@link Label}.
     */
    private Component createFileInfoComponent() {
        Label fileDetail = new Label("", ContentMode.HTML);
        fileDetail.setSizeUndefined();
        fileDetail.addStyleName("file-details");
        fileDetail.setValue(getFileInfo());
        return fileDetail;
    }

    /**
     * Create the Detail File Message. <br>
     * <b>Override this method in order to change the Displayed detail values.</b>
     *
     * @return File Informations.
     */
    protected String getFileInfo() {
        StringBuilder sb = new StringBuilder();

        // Title
        sb.append("<span class=\"value\">");
        sb.append(getCaption(fileDetailHeaderCaption));
        sb.append("</span>");
        sb.append("<br/><br/>");

        // Name
        sb.append("<span class=\"key\">");
        sb.append(getCaption(fileDetailNameCaption));
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(getFileWrapper().getFileName());
        sb.append("</span>");
        sb.append("<br/>");

        // Size
        sb.append("<span class=\"key\">");
        sb.append(getCaption(fileDetailSizeCaption));
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(FileUtils.byteCountToDisplaySize(getFileWrapper().getFileSize()));
        sb.append("</span>");
        sb.append("<br/>");

        // Format
        sb.append("<span class=\"key\">");
        sb.append(getCaption(fileDetailFormatCaption));
        sb.append("</span>");
        sb.append("<span class=\"value\">");
        sb.append(getFileWrapper().getExtension());
        sb.append("</span>");
        sb.append("<br/>");

        return sb.toString();
    }

    /**
     * @return Thumbnail Component.
     */
    protected Component createThumbnailComponent() {
        Label thumbnail = new Label("", ContentMode.HTML);
        thumbnail.setSizeUndefined();
        thumbnail.addStyleName("preview-image");
        thumbnail.addStyleName("file-preview");
        thumbnail.addStyleName(createIconStyleName(getFileWrapper()));
        return thumbnail;
    }

    /**
     * Create the Icon related to a File. <br>
     * <b>Override this method in order to change the Displayed Icon .</b>
     *
     * @param fileWrapper
     * @return
     */
    protected String createIconStyleName(FileItemWrapper fileWrapper) {
        return "icon-" + imageProvider.resolveIconClassName(fileWrapper.getMimeType());
    }

    @Override
    protected Component initContent() {
        return getRootLayout();
    }

    /**
     * Caption section.
     */
    protected String captionExtension;

    protected void setCaptionExtension(String mimeType) {
        captionExtension = StringUtils.EMPTY;
    }

    protected String getCaption(String caption) {
        if (StringUtils.isEmpty(caption)) {
            return StringUtils.EMPTY;
        }
        caption = StringUtils.isNotBlank(captionExtension) ? caption + "." + captionExtension : caption;
        return MessagesUtil.get(caption);
    }

    protected String selectNewCaption;
    protected String selectAnotherCaption;
    protected String deleteCaption;
    protected String dropZoneCaption;
    protected String inProgressCaption;
    protected String inProgressRatioCaption;
    protected String fileDetailHeaderCaption;
    protected String fileDetailNameCaption;
    protected String fileDetailSizeCaption;
    protected String fileDetailFormatCaption;
    protected String fileDetailSourceCaption;
    protected String successNoteCaption;
    protected String warningNoteCaption;
    protected String errorNoteCaption;

    public void setSelectNewCaption(String selectNewCaption) {
        this.selectNewCaption = selectNewCaption;
    }
    public void setSelectAnotherCaption(String selectAnotherCaption) {
        this.selectAnotherCaption = selectAnotherCaption;
    }
    public void setDropZoneCaption(String dropZoneCaption) {
        this.dropZoneCaption = dropZoneCaption;
    }
    public void setInProgressCaption(String inProgressCaption) {
        this.inProgressCaption = inProgressCaption;
    }
    public void setInProgressRatioCaption(String inProgressRatioCaption) {
        this.inProgressRatioCaption = inProgressRatioCaption;
    }
    public void setFileDetailHeaderCaption(String fileDetailHeaderCaption) {
        this.fileDetailHeaderCaption = fileDetailHeaderCaption;
    }
    public void setFileDetailNameCaption(String fileDetailNameCaption) {
        this.fileDetailNameCaption = fileDetailNameCaption;
    }
    public void setFileDetailSizeCaption(String fileDetailSizeCaption) {
        this.fileDetailSizeCaption = fileDetailSizeCaption;
    }
    public void setFileDetailFormatCaption(String fileDetailFormatCaption) {
        this.fileDetailFormatCaption = fileDetailFormatCaption;
    }
    public void setFileDetailSourceCaption(String fileDetailSourceCaption) {
        this.fileDetailSourceCaption = fileDetailSourceCaption;
    }
    public void setSuccessNoteCaption(String successNoteCaption) {
        this.successNoteCaption = successNoteCaption;
    }
    public void setWarningNoteCaption(String warningNoteCaption) {
        this.warningNoteCaption = warningNoteCaption;
    }
    public void setErrorNoteCaption(String errorNoteCaption) {
        this.errorNoteCaption = errorNoteCaption;
    }
    public void setDeteteCaption(String deleteCaption) {
        this.deleteCaption = deleteCaption;
    }

    @Override
    protected void displayUploadInterruptNote() {
        // Do Nothing
    }

    @Override
    protected void displayUploadFinisheddNote(String fileName) {
        // Do Nothing
    }

    @Override
    protected void displayUploadFaildNote(String fileName) {
        // Do Nothing
    }

    /**
     * For test cases.
     */
    public CssLayout getCssLayout() {
        return this.layout;
    }

}
