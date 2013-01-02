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
import info.magnolia.ui.admincentral.file.FileBufferPropertiesAdapter;
import info.magnolia.ui.admincentral.file.FileItemWrapper;
import info.magnolia.ui.framework.shell.Shell;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;
import org.vaadin.easyuploads.DirectoryFileFactory;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField.FieldType;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;

/**
 * Main implementation of the UploadFile field. This implementation used some
 * features of {@link org.vaadin.easyuploads.UploadField} and associated
 * classes.
 * <p>
 * This class handles Upload features (Open file chooser/drag and drop) and components (progress bar, cancel/delete button...), and expose functions that allows to customize the 3 main upload states:
 * <ul>
 * <li>on {@link StartedEvent}: buildStartUploadLayout() is called and allows to initialize the upload progress view.
 * <li>on {@link FinishedEvent}: buildFinishUploadLayout() is used to initialize the success Upload view (preview image, File summary..)
 * <li>on Initialization, implement abstract buildDefaultUploadLayout() to initialize the initial Upload view (Upload Button...)
 * </ul>
 * In addition, this class create basic components defined by {@link DefaultComponent}. From your code: calling createCancelButton(), will add the button to the defaultComponent Map, and later to access this button, just perform a getDefaultComponent(DefaultComponent defaultComponent).
 * <p>
 * {@link org.vaadin.easyuploads.FileFactory} is defined based on the UploadFileDirectory set. If this directory is null, {@link org.vaadin.easyuploads.TempFileFactory} is used. Else {@link org.vaadin.easyuploads.DirectoryFileFactory} is used.
 * <p>
 * <b>Restriction:</b> Unlike {@link org.vaadin.easyuploads.UploadField} we only support
 * <ul>
 * <li>file storage mode: {@link org.vaadin.easyuploads.UploadField.StorageMode#FILE}
 * <li>byte[] property ( {@link org.vaadin.easyuploads.UploadField.FieldType#BYTE_ARRAY})
 * </ul>
 * 
 * @param <D>
 *            definition type
 */
public abstract class AbstractUploadFileField<D extends FileItemWrapper> extends CustomField implements StartedListener, FinishedListener,
        ProgressListener, FailedListener, DropHandler, UploadFileField {

    private static final Logger log = LoggerFactory.getLogger(AbstractUploadFileField.class);

    protected boolean preview = true;
    protected boolean info = true;
    protected boolean progressInfo = true;
    protected boolean fileDeletion = true;
    protected boolean dragAndDrop = true;

    // Define global variable used by UploadFileField
    private File directory;
    private long maxUploadSize = Long.MAX_VALUE;
    private final String deleteFileCaption;

    // Define global variable used by this implementation
    protected D fileItem;

    private FileBuffer receiver;
    private FileFactory fileFactory;
    private final Map<DefaultComponent, Component> defaultComponent = new HashMap<DefaultComponent, Component>();

    // Define default component
    private Upload upload;
    private ProgressIndicatorComponent progress;
    private Label fileDetail;
    private Component previewComponent;
    private Button deleteButton;
    private Button cancelButton;
    private AbstractComponentContainer root;
    private DragAndDropWrapper dropZone;

    // Used to force the refresh of the Uploading view in case of Drag and Drop.
    private final Shell shell;

    /**
     * Basic constructor.
     * 
     * @param item
     *            used to store the File properties like binary data, file name,
     *            etc.
     */
    public AbstractUploadFileField(D fileItem, Shell shell) {
        this.fileItem = fileItem;
        this.shell = shell;
        deleteFileCaption = MessagesUtil.get("field.upload.remove.file");
        setStorageMode();
        createUpload();
    }

    /**
     * On Detach, clean Item, and interrupt upload.
     */
    @Override
    public void detach() {
        super.detach();
        fileItem.clearProperties();
        interruptUpload();
    }

    /**
     * Set the Upload field Components layout based on the current state.
     * <ul>
     * <li>- Initial: --> buildDefaultUploadLayout()
     * <li>- Complete: --> buildDoneUploadLayout()
     * </ul>
     */
    protected void updateDisplay() {
        if (fileItem.isEmpty()) {
            buildDefaultUploadLayout();
        } else {
            buildUploadDoneLayout();
        }
    }

    /**
     * Define the Default Upload Layout.
     */
    abstract protected void buildDefaultUploadLayout();

    /**
     * Define the Default Storage Mode.
     */
    private void setStorageMode() {
        receiver = new FileBuffer() {
            @Override
            public FileFactory getFileFactory() {
                return AbstractUploadFileField.this.getFileFactory();
            }

            @Override
            public FieldType getFieldType() {
                return FieldType.BYTE_ARRAY;
            };
        };
    }

    /**
     * Define the FileFactory to Use. <b>If no directory set, use the
     * TempFileFactory.</b>
     */
    public FileFactory getFileFactory() {
        if (this.directory != null && fileFactory == null) {
            fileFactory = new DirectoryFileFactory(directory);
        } else {
            fileFactory = new DefaultFileFactory();
        }
        return fileFactory;
    }

    @Override
    public Class<?> getType() {
        return Byte[].class;
    }

    /**
     * Drop zone Handler.
     */
    @Override
    public void drop(DragAndDropEvent event) {
        DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
        final Html5File[] files = transferable.getFiles();
        if (files == null) {
            return;
        }
        for (final Html5File html5File : files) {
            html5File.setStreamVariable(new StreamVariable() {

                private String name;
                private String mime;

                @Override
                public OutputStream getOutputStream() {
                    return receiver.receiveUpload(name, mime);
                }

                @Override
                public boolean listenProgress() {
                    return true;
                }

                @Override
                public void onProgress(StreamingProgressEvent event) {
                    updateProgress(event.getBytesReceived(), event.getContentLength());
                }

                @Override
                public void streamingStarted(StreamingStartEvent event) {
                    setDragAndDropUploadInterrupted(false);
                    name = event.getFileName();
                    mime = event.getMimeType();
                    StartedEvent startEvent = new StartedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadStarted(startEvent);
                    shell.pushToClient();
                }

                @Override
                public void streamingFinished(StreamingEndEvent event) {
                    FinishedEvent uploadEvent = new FinishedEvent(upload, event.getFileName(), event.getMimeType(), event
                            .getContentLength());
                    uploadFinished(uploadEvent);
                }

                @Override
                public void streamingFailed(StreamingErrorEvent event) {
                    FailedEvent failedEvent = new FailedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadFailed(failedEvent);
                }

                @Override
                public synchronized boolean isInterrupted() {
                    return isDragAndDropUploadInterrupted();
                }

            });
        }
    }

    // Used to handle Cancel / Interrupted upload in the DragAndDrop
    // implementation.
    private boolean interruptedDragAndDropUpload = false;

    protected void setDragAndDropUploadInterrupted(boolean isInterrupetd) {
        interruptedDragAndDropUpload = isInterrupetd;
    }

    private boolean isDragAndDropUploadInterrupted() {
        return interruptedDragAndDropUpload;
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }

    /**
     * Create the Upload component.
     */
    private void createUpload() {
        this.upload = new Upload(null, receiver);
        this.upload.addListener((StartedListener) this);
        this.upload.addListener((FinishedListener) this);
        this.upload.addListener((ProgressListener) this);
        this.upload.setImmediate(true);
        defaultComponent.put(DefaultComponent.UPLOAD, this.upload);
    }

    /**
     * Create a dummy Preview component. Sub class should override this method
     * to define their own preview display.
     */
    protected Component createPreviewComponent() {
        this.previewComponent = new Embedded(null);
        defaultComponent.put(DefaultComponent.PREVIEW, this.previewComponent);
        return this.previewComponent;
    }

    /**
     * The dropZone is a wrapper around a Component.
     */
    protected DragAndDropWrapper createDropZone(Component c) {
        dropZone = new DragAndDropWrapper(c);
        dropZone.setDropHandler(this);
        defaultComponent.put(DefaultComponent.DROP_ZONE, this.dropZone);
        return this.dropZone;
    }

    /**
     * Create Delete button.
     */
    protected Button createDeleteButton() {
        this.deleteButton = new Button(deleteFileCaption);
        this.deleteButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                // Remove link between item and parent. In this case the child
                // File Item will not be persisted.
                fileItem.unLinkItemFromParent();
                fileItem.clearProperties();
                updateDisplay();
            }
        });
        this.deleteButton.addStyleName("delete");
        defaultComponent.put(DefaultComponent.DELETE_BUTTON, this.deleteButton);
        return this.deleteButton;
    }

    /**
     * Create Cancel button.
     */
    protected Button createCancelButton() {
        this.cancelButton = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                interruptUpload();
            }
        });
        this.cancelButton.addStyleName("cancel");
        defaultComponent.put(DefaultComponent.CANCEL_BUTTON, this.cancelButton);
        return this.cancelButton;
    }

    /**
     * Create the Default File Detail.
     */
    public Label createFileDetail() {
        this.fileDetail = new Label("", Label.CONTENT_XHTML);
        this.fileDetail.setSizeUndefined();
        this.fileDetail.addStyleName("file-details");
        defaultComponent.put(DefaultComponent.FILE_DETAIL, this.fileDetail);
        return this.fileDetail;
    }

    /**
     * Create the ProgressIndicator component.
     */
    public ProgressIndicatorComponent createProgressIndicator() {
        progress = new ProgressIndicatorComponentDefaultImpl();
        defaultComponent.put(DefaultComponent.PROGRESS_BAR, (Component) this.progress);
        return this.progress;
    }

    public AbstractComponentContainer getRootLayout() {
        return this.root;
    }

    public void setRootLayout(AbstractComponentContainer root) {
        this.root = root;
    }

    /**
     * Default component key definition.
     */
    public enum DefaultComponent {
        UPLOAD, PROGRESS_BAR, FILE_DETAIL, PREVIEW, DELETE_BUTTON, CANCEL_BUTTON, DROP_ZONE
    }

    /**
     * Return the desired defaultComponent.
     */
    public Component getDefaultComponent(DefaultComponent defaultComponent) {
        return this.defaultComponent.get(defaultComponent);
    }

    public Map<DefaultComponent, Component> getDefaultComponents() {
        return this.defaultComponent;
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        updateDisplay();
        log.warn("Upload failed for file {} ", event.getFilename());
    }

    /**
     * Update the Progress Component. At the same time, check if the uploaded
     * File is not bigger as expected. Interrupt the Upload in this case.
     */
    @Override
    public void updateProgress(long readBytes, long contentLength) {
        if (readBytes > this.maxUploadSize || contentLength > this.maxUploadSize) {
            this.upload.interruptUpload();
            return;
        }
        refreshOnProgressUploadLayout(readBytes, contentLength);
    }

    public void refreshOnProgressUploadLayout(long readBytes, long contentLength) {
        if (progress != null && progressInfo) {
            progress.refreshOnProgressUploadLayout(readBytes, contentLength, receiver.getLastFileName());
        }
    }

    /**
     * Handle the {@link FinishedEvent}. In case of success: - Populate the
     * Uploaded Information to the local variables used in the later steps. -
     * Build the Finish Upload Layout. - Populate the Uploaded data to the Item
     * (Binary / File name / size...) In case of {@link FailedEvent} (this event
     * is send on a Cancel upload) - Do not populate data and call indirectly
     * updateDisplay().
     */
    @Override
    public void uploadFinished(FinishedEvent event) {
        if (event instanceof FailedEvent) {
            uploadFailed((FailedEvent) event);
            return;
        }
        this.fileItem.updateProperties(FileBufferPropertiesAdapter.adapt(receiver));
        buildUploadDoneLayout();
        fireValueChange(true);
        this.fileItem.populateJcrItemProperty();
    }

    public void buildUploadDoneLayout() {
        if (this.fileDetail != null) {
            fileDetail.setValue(getDisplayDetails());
        }
    }

    /**
     * @return a string representing relevant file info. By default returns an
     *         empty string.
     **/
    protected String getDisplayDetails() {
        return "";
    }

    /**
     * Start Upload if the file is supported. In case of not supported file,
     * interrupt the Upload.
     */
    @Override
    public void uploadStarted(StartedEvent event) {
        if (isValidFile(event)) {
            buildUploadStartedLayout();
        } else {
            interruptUpload();
            getWindow().showNotification("Upload canceled due to unsupported file type " + event.getMIMEType());
        }
    }

    protected void buildUploadStartedLayout() {
        if (this.progress != null) {
            this.progress.setVisible(true);
            this.progress.setProgressIndicatorValue(0);
        }
    }

    /**
     * Interrupt upload.
     */
    public void interruptUpload() {
        getRootLayout().removeStyleName("in-progress");
        if (upload.isUploading()) {
            upload.interruptUpload();
        } else {
            setDragAndDropUploadInterrupted(true);
        }
    }

    /**
     * Default implementation returns always true. Extending classes should
     * always override this method.
     */
    public boolean isValidFile(StartedEvent event) {
        return true;
    }

    /**
     * Define the maximum file size in bite.
     */
    @Override
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    /**
     * Set the caption of the Upload Button.
     */
    @Override
    public void setUploadButtonCaption(String uploadButtonCaption) {
        this.upload.setButtonCaption(uploadButtonCaption);
    }

    @Override
    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    @Override
    public void setInfo(boolean info) {
        this.info = info;
    }

    @Override
    public void setProgressInfo(boolean progressInfo) {
        this.progressInfo = progressInfo;
    }

    @Override
    public void setFileDeletion(boolean fileDeletion) {
        this.fileDeletion = fileDeletion;
    }

    @Override
    public void setDragAndDrop(boolean dragAndDrop) {
        this.dragAndDrop = dragAndDrop;
    }

    @Override
    public void setUploadFileDirectory(File directory) {
        this.directory = directory;
    }
}
