/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.upload.AbstractUploadField;
import info.magnolia.ui.form.field.upload.UploadProgressIndicator;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.vaadin.data.Property;

/**
 * Basic implementation of {@link AbstractUploadField}.<br>
 * Define the Layout components for
 * <ul>
 * <li>EmptyLayout (no File are yet uploaded)
 * <li>InProgressLayout (ProgressBar / Cancel Button...)
 * <li>CompletedLayout (File Detail / Preview ...)
 * </ul>
 * 
 * @param <T> {@link UploadReceiver} implemented class.
 */
public class BasicUploadField<T extends UploadReceiver> extends AbstractUploadField<T> {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BasicUploadField.class);

    private static final String PREFIX_MEDIA_KEY = "field.upload.media";
    private static final String MEDIA = "media";

    // Root layout
    private final CssLayout layout;
    private UploadProgressIndicator progress;
    protected final ImageProvider imageProvider;
    private boolean editFileName = false;
    private boolean editFileFormat = false;
    protected UiContext uiContext;
    private final SimpleTranslator i18n;

    public BasicUploadField(ImageProvider imageProvider, UiContext uiContext, BasicUploadFieldDefinition definition, SimpleTranslator i18n) {
        super();
        // Propagate definition.
        populateFromDefinition(definition);

        this.imageProvider = imageProvider;
        this.layout = new CssLayout();
        this.layout.setSizeUndefined();
        this.uiContext = uiContext;
        this.i18n = i18n;

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
        if (isReadOnly()) {
            return;
        }
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
        progress = new BasicUploadProgressIndicator(inProgressCaption, inProgressRatioCaption, i18n);
        progress.setVisible(true);
        progress.setProgress(0);
        layout.addComponent(progress.asVaadinComponent());

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
            progress.refreshLayout(readBytes, contentLength, fileName);
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
        if (!getValue().isEmpty()) {
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
        deleteButton.setDescription(i18n.translate(deleteCaption));

        deleteButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                resetDataSource();
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
        // Build the file name without the extension
        final String extension = "." + getValue().getExtension();
        String fileName = StringUtils.removeEnd(getValue().getFileName(), extension);

        if (this.editFileName && !isReadOnly()) {
            TextField textField = new TextField(i18n.translate(fileDetailNameCaption), fileName);
            textField.setNullRepresentation("");
            textField.setCaption(i18n.translate(fileDetailNameCaption));
            textField.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    Object newFileNameObject = event.getProperty().getValue();
                    String newFileName = (newFileNameObject != null && StringUtils.isNotBlank(newFileNameObject.toString())) ? newFileNameObject.toString() : UploadReceiver.INVALID_FILE_NAME;
                    getValue().setFileName(newFileName + extension);
                    getPropertyDataSource().setValue(getValue());
                }
            });
            return textField;
        } else {
            Label label = new Label("", ContentMode.HTML);
            label.setCaption(i18n.translate(fileDetailNameCaption));
            label.setValue(fileName);
            return label;
        }
    }

    /**
     * Add File Info.
     */
    protected Component getFileDetailSize() {
        Label label = new Label("", ContentMode.HTML);
        label.setCaption(i18n.translate(fileDetailSizeCaption));
        label.setValue(FileUtils.byteCountToDisplaySize(getValue().getFileSize()));
        return label;
    }

    /**
     * Add File Format.<br>
     * If editFileFormat is true, display an Input Text Field. <br>
     * Else display a simple label.
     */
    protected Component getFileDetailFileFormat() {
        if (this.editFileFormat && !isReadOnly()) {
            TextField textField = new TextField(i18n.translate(fileDetailFormatCaption), getValue().getExtension());
            textField.setNullRepresentation("");
            textField.setCaption(i18n.translate(fileDetailFormatCaption));
            return textField;
        } else {
            Label label = new Label("", ContentMode.HTML);
            label.setValue(getValue().getExtension());
            label.setCaption(i18n.translate(fileDetailFormatCaption));
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
        thumbnail.addStyleName(createIconStyleName());
        return thumbnail;
    }

    /**
     * Create the Icon related to a File. <br>
     * <b>Override this method in order to change the Displayed Icon .</b>
     *
     * @return
     */
    protected String createIconStyleName() {
        return "icon-" + imageProvider.resolveIconClassName(getValue().getMimeType());
    }

    @Override
    protected Component initContent() {
        return getRootLayout();
    }

    /**
     * Configure Field based on the definition.
     */
    protected void populateFromDefinition(BasicUploadFieldDefinition definition) {
        this.setMaxUploadSize(definition.getMaxUploadSize());
        this.setAllowedMimeTypePattern(definition.getAllowedMimeTypePattern());
        this.setAllowedFileExtensionPattern(definition.getAllowedFileExtensionPattern());

        this.setSelectNewCaption(definition.getSelectNewCaption());
        this.setSelectAnotherCaption(definition.getSelectAnotherCaption());
        this.setDropZoneCaption(definition.getDropZoneCaption());
        this.setInProgressCaption(definition.getInProgressCaption());
        this.setInProgressRatioCaption(definition.getInProgressRatioCaption());
        this.setFileDetailHeaderCaption(definition.getFileDetailHeaderCaption());
        this.setFileDetailNameCaption(definition.getFileDetailNameCaption());
        this.setFileDetailSizeCaption(definition.getFileDetailSizeCaption());
        this.setFileDetailFormatCaption(definition.getFileDetailFormatCaption());
        this.setFileDetailSourceCaption(definition.getFileDetailSourceCaption());
        this.setSuccessNoteCaption(definition.getSuccessNoteCaption());
        this.setWarningNoteCaption(definition.getWarningNoteCaption());
        this.setErrorNoteCaption(definition.getErrorNoteCaption());
        this.setDeteteCaption(definition.getDeleteCaption());
        this.setEditFileFormat(definition.isEditFileFormat());
        this.setEditFileName(definition.isEditFileName());
        this.setUserInterruption(definition.getUserInterruption());
        this.setTypeInterruption(definition.getTypeInterruption());
        this.setSizeInterruption(definition.getSizeInterruption());
    }

    /**
     * Caption section.
     */
    protected String captionExtension;

    protected void setCaptionExtension(String mimeType) {
        captionExtension = "";
    }

    protected String getCaption(String caption, String[] args) {
        if (StringUtils.isEmpty(caption)) {
            return "";
        }
        if (StringUtils.isNotBlank(captionExtension)) {
            String mediaName = i18n.translate(PREFIX_MEDIA_KEY + '.' + captionExtension);
            String[] paras;
            if (args != null && args.length > 0) {
                paras = new String[args.length + 1];
                paras[0] = mediaName;
                System.arraycopy(args, 0, paras, 1, args.length);
            } else {
                paras = new String[] { mediaName };
            }
            return i18n.translate(caption + '.' + MEDIA, paras);
        }
        if (args != null && args.length > 0) {
            return i18n.translate(caption, args);
        } else {
            return i18n.translate(caption);
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
        uiContext.openNotification(MessageStyleTypeEnum.WARNING, true, getCaption(warningNoteCaption, new String[] { i18n.translate(caption) }));
    }

    @Override
    protected void displayUploadFinishedNote(String fileName) {
        uiContext.openNotification(MessageStyleTypeEnum.INFO, true, getCaption(successNoteCaption, new String[] { fileName }));
    }

    @Override
    protected void displayUploadFailedNote(String fileName) {
        uiContext.openAlert(MessageStyleTypeEnum.ERROR, "ERROR", getCaption(errorNoteCaption, new String[] { fileName }), i18n.translate("button.ok"), null);
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
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (readOnly) {
            // Remove drop zone
            if (getDropZone() != null) {
                getDropZone().setDropHandler(null);
            }
            if (getUpload() != null) {
                getUpload().removeStartedListener(this);
                getUpload().removeFinishedListener(this);
                getUpload().removeProgressListener(this);
            }
            if (getValue().isEmpty()) {
                buildEmptyLayout();
            }
        }

    }
}
