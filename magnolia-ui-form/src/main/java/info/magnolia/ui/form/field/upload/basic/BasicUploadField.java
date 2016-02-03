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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.upload.AbstractUploadField;
import info.magnolia.ui.form.field.upload.FileItemWrapper;
import info.magnolia.ui.form.field.upload.UploadProgressIndicator;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

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
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

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
    private boolean editFileName = false;
    private boolean editFileFormat = false;
    protected UiContext uiContext;

    public BasicUploadField(D fileWrapper, File tmpUploadDirectory, ImageProvider imageProvider, UiContext uiContext) {
        super(fileWrapper, tmpUploadDirectory);
        this.imageProvider = imageProvider;
        this.layout = new CssLayout();
        this.layout.setSizeUndefined();
        this.uiContext = uiContext;
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
        getUpload().setButtonCaption(getCaption(selectNewCaption, null));
        layout.addComponent(getUpload());
        // Add DropZone Label
        Label uploadText = new Label(getCaption(dropZoneCaption, null), ContentMode.HTML);
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
        getUpload().setButtonCaption(getCaption(selectAnotherCaption, null));
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
                interruptUpload(InterruptionReason.USER);
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
     * Override getFileDetail...() in order to display custom info's you may want to display.
     * 
     * @return A file Info Component. Generally a {@link FormLayout}.
     */
    private Component createFileInfoComponent() {
        FormLayout fileInfo = new FormLayout();
        fileInfo.setSizeUndefined();
        fileInfo.addStyleName("file-details");
        fileInfo.addComponent(getFileDetailHeader());
        fileInfo.addComponent(getFileDetailFileName());
        fileInfo.addComponent(getFileDetailSize());
        fileInfo.addComponent(getFileDetailFileFormat());
        return fileInfo;
    }

    /**
     * Add Title.
     */
    protected Component getFileDetailHeader() {
        Label label = new Label("", ContentMode.HTML);
        label.setValue(getCaption(fileDetailHeaderCaption, null));
        return label;
    }

    /**
     * Add File Name.<br>
     * If editFileName is true, display an Input Text Field. <br>
     * Else display a simple label.
     */
    protected Component getFileDetailFileName() {
        if (this.editFileName) {
            TextField textField = new TextField(getFileWrapper().getFileNameProperty());
            textField.setNullRepresentation("");
            textField.setCaption(MessagesUtil.get(fileDetailNameCaption));
            return textField;
        } else {
            Label label = new Label("", ContentMode.HTML);
            label.setCaption(MessagesUtil.get(fileDetailNameCaption));
            label.setValue(getFileWrapper().getFileName());
            return label;
        }
    }

    /**
     * Add File Info.
     */
    protected Component getFileDetailSize() {
        Label label = new Label("", ContentMode.HTML);
        label.setCaption(MessagesUtil.get(fileDetailSizeCaption));
        label.setValue(FileUtils.byteCountToDisplaySize(getFileWrapper().getFileSize()));
        return label;
    }

    /**
     * Add File Format.<br>
     * If editFileFormat is true, display an Input Text Field. <br>
     * Else display a simple label.
     */
    protected Component getFileDetailFileFormat() {
        if (this.editFileFormat) {
            TextField textField = new TextField(getFileWrapper().getFileFormatProperty());
            textField.setNullRepresentation("");
            textField.setCaption(MessagesUtil.get(fileDetailFormatCaption));
            return textField;
        } else {
            Label label = new Label("File Format", ContentMode.HTML);
            label.setValue(getFileWrapper().getExtension());
            label.setCaption(MessagesUtil.get(fileDetailFormatCaption));
            return label;
        }
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

    protected String getCaption(String caption, String[] args) {
        if (StringUtils.isEmpty(caption)) {
            return StringUtils.EMPTY;
        }
        caption = StringUtils.isNotBlank(captionExtension) ? caption + "." + captionExtension : caption;
        if (args != null && args.length > 0) {
            return MessagesUtil.get(caption, args);
        } else {
            return MessagesUtil.get(caption);
        }
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
    private String sizeInterruption;
    private String typeInterruption;
    private String userInterruption;

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

    public void setSizeInterruption(String sizeInterruption) {
        this.sizeInterruption = sizeInterruption;
    }

    public void setTypeInterruption(String typeInterruption) {
        this.typeInterruption = typeInterruption;
    }

    public void setUserInterruption(String userInterruption) {
        this.userInterruption = userInterruption;
    }

    @Override
    protected void displayUploadInterruptNote(InterruptionReason reason) {
        String caption = "";
        if (reason.equals(InterruptionReason.USER)) {
            caption = userInterruption;
        } else if (reason.equals(InterruptionReason.FILE_SIZE)) {
            caption = sizeInterruption;
        } else {
            caption = typeInterruption;
        }
        uiContext.openNotification(MessageStyleTypeEnum.WARNING, true, getCaption(warningNoteCaption, new String[] { MessagesUtil.get(caption) }));
    }

    @Override
    protected void displayUploadFinishedNote(String fileName) {
        uiContext.openNotification(MessageStyleTypeEnum.INFO, true, getCaption(successNoteCaption, new String[]{fileName}));
    }

    @Override
    protected void displayUploadFailedNote(String fileName) {
        uiContext.openAlert(MessageStyleTypeEnum.ERROR, "ERROR", getCaption(errorNoteCaption, new String[] { fileName }), "ok", null);
    }

    public void setEditFileName(boolean editFileName) {
        this.editFileName = editFileName;
    }

    public void setEditFileFormat(boolean editFileFormat) {
        this.editFileFormat = editFileFormat;
    }

    /**
     * For test cases.
     */
    public CssLayout getCssLayout() {
        return this.layout;
    }

    @Override
    protected boolean isEmpty() {
        return getFileWrapper().isEmpty();
    }
}
