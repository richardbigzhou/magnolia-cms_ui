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
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;
import org.vaadin.easyuploads.DirectoryFileFactory;
import org.vaadin.easyuploads.FileBuffer;
import org.vaadin.easyuploads.FileFactory;
import org.vaadin.easyuploads.UploadField.FieldType;

import com.vaadin.data.Property;
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
 * Main implementation of the UploadFile field.
 * This implementation used some features of {@link org.vaadin.easyuploads.UploadField} and associated classes.
 *<p>
 * This class handle Upload features (Open file chooser/drag and drop) and components (progress bar, cancel/delete button...),
 * and expose functions that allows to customize the 3 main upload states:
 * <ul>
 *   <li>on {@link StartedEvent}:   buildStartUploadLayout() is called and allows to initialize the upload progress view.
 *   <li>on {@link FinishedEvent}:  buildFinishUploadLayout() is used to initialize the success Upload view (preview image, File summary..)
 *   <li>on Initialization, implement abstract buildDefaultUploadLayout() to initialize the initial Upload view (Upload Button...)
 * </ul>
 * In addition, this class create basic components defined by {@link DefaultComponent}.
 * From your code: calling createCancelButton(), will add the button to the defaultComponent Map,
 * and later to access this button, just perform a getDefaultComponent(DefaultComponent defaultComponent).
 *<p>
 * {@link org.vaadin.easyuploads.FileFactory} is defined based on the UploadFileDirectory set.
 *  If this directory is null, {@link org.vaadin.easyuploads.TempFileFactory} is used.
 *  Else {@link org.vaadin.easyuploads.DirectoryFileFactory} is used.
 *<p>
 * <b>Restriction:</b>
 *  Unlike {@link org.vaadin.easyuploads.UploadField} we only support
 *  <ul>
 *  <li>file storage mode: {@link org.vaadin.easyuploads.UploadField.StorageMode#FILE}
 *  <li>byte[] property ({@link org.vaadin.easyuploads.UploadField.FieldType#BYTE_ARRAY})
 *  </ul>
 */
public abstract class AbstractUploadFileField extends CustomField implements StartedListener, FinishedListener, ProgressListener, FailedListener, DropHandler, UploadFileField {

    private static final Logger log = LoggerFactory.getLogger(AbstractUploadFileField.class);
    protected static final String DEFAULT_DELETE_BUTTON_CAPTION = "Delete File";
    protected static final String DEFAULT_DROP_ZONE_CAPTION = "Drag and Drop a File";

    protected boolean preview = true;
    protected boolean info = true;
    protected boolean progressInfo = true;
    protected boolean fileDeletion = true;
    protected boolean dragAndDrop = true;


    // Define global variable used by UploadFileField
    private File directory;
    private long maxUploadSize = Long.MAX_VALUE;

    // Define global variable used by this implementation
    private JcrItemNodeAdapter item;
    private FileBuffer receiver;
    private FileFactory fileFactory;
    private Map<DefaultComponent, Component> defaultComponent = new HashMap<DefaultComponent, Component>();


    // Define default component
    private Upload upload;
    private ProgressIndicatorComponent progress;
    private Label fileDetail;
    private Embedded previewImage;
    private Button deleteButton;
    private Button cancelButton;
    private AbstractComponentContainer root;
    private DragAndDropWrapper dropZone;

    // Define last successful Upload datas
    private byte[] lastBytesFile;
    private String lastMimeType;
    private String lastFileName;
    private long lastFileSize;
    //Used to force the refresh of the Uploading view in cse of Drag and Drop.
    private Shell shell;

    /**
     * Basic constructor.
     * @param item used to store the File properties like binary data, file name....
     */
    public AbstractUploadFileField(JcrItemNodeAdapter item, Shell shell) {
        this.item = item;
        this.shell = shell;
        setStorageMode();
        createUpload();
    }


    /**
     * Set the Upload field Components layout based on the current state.
     * <ul>
     * <li>- Initial:  --> buildDefaultUploadLayout()
     * <li>- Complete: --> buildFinishUploadLayout()
     * </ul>
     */
    protected void updateDisplay() {
        if(getLastBytesFile()==null) {
            buildDefaultUploadLayout();
        } else {
            buildFinishUploadLayout();
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
     * Define the FileFactory to Use.
     * <b>If no directory set, use the TempFileFactory.</b>
     */
    public FileFactory getFileFactory() {
        if (this.directory != null && fileFactory == null) {
            fileFactory = new DirectoryFileFactory(directory);
        }
        else {
            fileFactory = new DefaultFileFactory();
        }
        return fileFactory;
    }

    @Override
    public Class< ? > getType() {
        return Byte[].class;
    }

    /**
     * Drop zone Handler.
     */
    @Override
    public void drop(DragAndDropEvent event) {
        DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
        Html5File[] files = transferable.getFiles();
        if(files == null) {
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
                    updateProgress((long) event.getBytesReceived(), (long) event.getContentLength());
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
                    FinishedEvent uploadEvent = new FinishedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadFinished(uploadEvent);
                }

                @Override
                public void streamingFailed(StreamingErrorEvent event) {
                    FailedEvent failedEvent = new FailedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadFailed(failedEvent);
                }

                @Override
                public boolean isInterrupted() {
                    return isDragAndDropUploadInterrupted();
                }

            });
        }
    }

    // Used to handle Cancel / Interrupted upload in the DragAndDrop implementation.
    private boolean interruptedDragAndDropUpload = false;

    private void setDragAndDropUploadInterrupted(boolean isInterrupetd) {
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
     * Create a dummy Preview component.
     * Sub class should override this method to define their own
     * preview display.
     */
    public Embedded createPreview() {
        this.previewImage = new Embedded();
        defaultComponent.put(DefaultComponent.PREVIEW, this.previewImage);
        return this.previewImage;
    }
    /**
     * The dropZone is a wrapper around a Component.
     */
    public DragAndDropWrapper createDropZone(Component c) {
        dropZone = new DragAndDropWrapper(c);
        dropZone.setDropHandler(this);
        defaultComponent.put(DefaultComponent.DROP_ZONE, this.dropZone);
        return this.dropZone;
    }

    /**
     * Create Delete button.
     */
    public Button createDeleteButton() {
        this.deleteButton = new Button(DEFAULT_DELETE_BUTTON_CAPTION);
        this.deleteButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                //Remove link between item and parent. In this case the child File Item will not be persisted.
                item.getParent().removeChild(item);
                clearLastUploadData();
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
    public Button createCancelButton() {
        this.cancelButton = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                upload.interruptUpload();
                // Also inform DragAndDrop
                setDragAndDropUploadInterrupted(true);
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
        defaultComponent.put(DefaultComponent.PROGRESS_BAR, (Component)this.progress);
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
        //TODO Inform the end user.
        updateDisplay();
        log.info("Upload failed for file {} ", event.getFilename());
    }

    /**
     * Update the Progress Component.
     * At the same time, check if the uploaded File
     * is not bigger as expected. Interrupt the Upload in this case.
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
     * Handle the {@link FinishedEvent}.
     * In case of success:
     *  - Populate the Uploaded Information to the local variables used in the later steps.
     *  - Build the Finish Upload Layout.
     *  - Populate the Uploaded data to the Item (Binary / File name / size...)
     *  In case of {@link FailedEvent} (this event is send on a Cancel upload)
     *  - Do not populate data and call indirectly updateDisplay().
     */
    @Override
    public void uploadFinished(FinishedEvent event) {
        if (event instanceof FailedEvent) {
            uploadFailed((FailedEvent) event);
            return;
        }
        setLastUploadData();
        buildFinishUploadLayout();
        fireValueChange(true);
        populateItemProperty();
    }

    public void buildFinishUploadLayout() {
        if (this.fileDetail != null) {
            fileDetail.setValue(getDisplayDetails());
        }
    }

    /**
     * @return the string representing the file. The default implementation
     **/
    protected String getDisplayDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ");
        sb.append(getLastFileName());
        sb.append("</br> <em>");
        sb.append("(" + FileUtils.byteCountToDisplaySize(getLastFileSize()) + " )");
        sb.append("</em>");
        return sb.toString();
    }

    /**
     * Start Upload if the file is supported.
     * In case of not supported file, interrupt the Upload.
     */
    @Override
    public void uploadStarted(StartedEvent event) {
        if(isValidFile(event)) {
            buildStartUploadLayout();
        } else {
            setDragAndDropUploadInterrupted(true);
            getWindow().showNotification("Upload cancelled due to unsupported file type "+ event.getMIMEType());
            upload.interruptUpload();
        }
    }

    public void buildStartUploadLayout() {
        if (this.progress != null) {
            this.progress.setVisible(true);
            this.progress.setProgressIndicatorValue(0);
        }
    }


    /**
     * Clear local Uploaded file Info.
     * Mainly called by the Delete Action Button.
     */
    public void clearLastUploadData() {
        setLastBytesFile(null);
        setLastFileName(null);
        setLastFileSize(-1);
        setLastMimeType(null);
    }


    public void setLastUploadData() {
        setLastBytesFile((byte[])receiver.getValue());
        setLastFileName(receiver.getLastFileName());
        setLastFileSize(receiver.getLastFileSize());
        setLastMimeType(receiver.getLastMimeType());
    }

    /**
     * Convenience method used to populate FileUpload informations
     * coming from a previously stored File.
     */
    public void setLastUploadData(JcrItemNodeAdapter item) {
        setLastFileName((String) item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());

        Property data = item.getItemProperty(MgnlNodeType.JCR_DATA);
        if (data != null) {
            byte[] binaryData = (byte[]) data.getValue();
            setLastBytesFile(binaryData);
            setLastFileSize((Long) item.getItemProperty(FileProperties.PROPERTY_SIZE).getValue());
            setLastMimeType((String) item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue());
        }
    }

    /**
     * Default implementation return always true.
     * Child class should always override this method.
     */
    public boolean isValidFile(StartedEvent event) {
        return true;
    }

    /**
     * Populate the Item property (data/image name/...) Data is stored as a JCR Binary object.
     */
    protected void populateItemProperty() {
        // Attach the Item to the parent in order to be stored.
        item.getParent().addChild(item);
        // Populate Data
        Property data = item.getItemProperty(MgnlNodeType.JCR_DATA);

        if (getLastBytesFile() != null) {
            data.setValue(new ByteArrayInputStream(getLastBytesFile()) );
        }
        item.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(StringUtils.substringBefore(getLastFileName(), "."));
        item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(getLastMimeType());
        item.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new GregorianCalendar(TimeZone.getDefault()));
        item.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(getLastFileSize());
        item.getItemProperty(FileProperties.PROPERTY_EXTENSION).setValue(PathUtil.getExtension(getLastFileName()));
    }

    public byte[] getLastBytesFile() {
        return lastBytesFile;
    }

    public void setLastBytesFile(byte[] lastBytesFile) {
        this.lastBytesFile = lastBytesFile;
    }

    public String getLastMimeType() {
        return lastMimeType;
    }

    public void setLastMimeType(String lastMimeType) {
        this.lastMimeType = lastMimeType;
    }

    public String getLastFileName() {
        return lastFileName;
    }

    public void setLastFileName(String lastFileName) {
        this.lastFileName = lastFileName;
    }

    public long getLastFileSize() {
        return lastFileSize;
    }

    public void setLastFileSize(long lastFileSize) {
        this.lastFileSize = lastFileSize;
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
    public void setFileDeletionButtonCaption(String deleteButtonCaption) {
        if(this.deleteButton != null) {
            this.deleteButton.setCaption(deleteButtonCaption);
        }
    }

    @Override
    public void setUploadFileDirectory(File directory) {
        this.directory = directory;
    }
}
