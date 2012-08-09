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
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
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
 * Mainly inspired from the {@link org.vaadin.easyuploads.UploadField}
 *
 * {@link org.vaadin.easyuploads.FileFactory} is defined based on the UploadFileDirectory set.
 *  If this directory is null, {@link org.vaadin.easyuploads.TempFileFactory} is used.
 *  Else {@link org.vaadin.easyuploads.DirectoryFileFactory} is used.
 *
 * <b>Restriction:</b>
 *  In opposite to {@link org.vaadin.easyuploads.UploadField} we only support
 *   file storage mode: {@link org.vaadin.easyuploads.UploadField.StorageMode#FILE}
 *   byte[] property ({@link org.vaadin.easyuploads.UploadField.FieldType#BYTE_ARRAY})
 *
 */
public abstract class AbstractUploadFileField extends AbstractUploadField implements UploadFileField, UploadFileFieldDisplay, StartedListener, FinishedListener, ProgressListener, FailedListener, DropHandler{

    private static final Logger log = LoggerFactory.getLogger(AbstractUploadFileField.class);
    private static final String DEFAULT_UPLOAD_BUTTON_CAPTION = "Choose File";
    private static final String DEFAULT_DELETE_BUTTON_CAPTION = "Delete File";

    // Define global variable used by UploadFileField
    private boolean preview = true;
    private boolean info = true;
    private boolean fileDeletion = true;
    private boolean dragAndDrop = true;
    private boolean progressInfo;
    private List<String> fileExtensions = new ArrayList<String>();
    private String uploadButtonCaption = DEFAULT_UPLOAD_BUTTON_CAPTION;
    private String deleteButtonCaption = DEFAULT_DELETE_BUTTON_CAPTION;
    private boolean requestForFileDeletion = false;
    private String progressIndicatorCaption;
    private String dropZoneCaption;
    private File directory;
    private long maxUploadSize = Long.MAX_VALUE;

    // Define global variable used by this implementation
    private JcrItemNodeAdapter item;
    private FileBuffer receiver;
    private FileFactory fileFactory;
    private Map<AbstractUploadFileField.DefaultComponent, Component> defaultComponent = new HashMap<AbstractUploadFileField.DefaultComponent, Component>();

    // Define default component
    private Upload upload;
    private ProgressIndicator progress;
    private Label fileDetail;
    private Embedded previewImage;
    private Button deleteButton;
    private AbstractComponentContainer root;
    private DragAndDropWrapper dropZone;

    /**
     *
     * @param item: Vaadin Item representing the Jcr Node to store the file.
     */
    public AbstractUploadFileField(JcrItemNodeAdapter item) {
        super();
        this.item = item;
        setStorageMode();
        setDefaultComponent();
    }


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
            }
        };
    }
    /**
     * Set the minimal required components (upload).
     */
    private void setDefaultComponent() {
        this.upload = new Upload(null, receiver);
        this.upload.addListener((StartedListener) this);
        this.upload.addListener((FinishedListener) this);
        this.upload.addListener((ProgressListener) this);
        this.upload.setImmediate(true);
        defaultComponent.put(DefaultComponent.UPLOAD, this.upload);
    }

    /**
     * Default Components Creation.
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
        dropZone.setStyleName("v-multifileupload-dropzone");
        defaultComponent.put(DefaultComponent.DROP_ZONE, this.dropZone);
        return this.dropZone;
    }

    public Button createDeleteButton() {
        this.deleteButton = new Button(this.deleteButtonCaption);
        this.deleteButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent arg0) {
                setValue(null);
                requestForFileDeletion = true;
                handleFile();
                updateDisplay();
            }
        });
        defaultComponent.put(DefaultComponent.DELETE_BUTTON, this.deleteButton);
        return this.deleteButton;
    }

    public Label createFileDetail() {
        this.fileDetail = new Label("", Label.CONTENT_XHTML);
        defaultComponent.put(DefaultComponent.FILE_DETAIL, this.fileDetail);
        return this.fileDetail;
    }

    public ProgressIndicator createProgressIndicator() {
        progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setPollingInterval(500);
        defaultComponent.put(DefaultComponent.PROGRESS_BAR, this.progress);
        return this.progress;
    }

    /**
     * Handle the Uploaded File.
     * Used to populate Item property based on the uploaded file.
     * This method is triggered when
     *    download success --> uploadFinished(FinishedEvent event)
     *    download failed --> uploadFailed(FailedEvent event)
     *    download removed --> deleteButton click listener.
     */
    abstract protected void handleFile();

    /**
     * Create the Upload field Components layout.
     */
    abstract protected void initDisplay();

    /**
     * Initialize the root component.
     * Build Layout
     *  - Default Layout if the incoming Item is empty.
     *  - Upload Finish Layout with the Item Information if this Item is not empty.
     */
    @Override
    public void attach() {
        super.attach();
        initDisplay();
        addComponent(getRootLayout());

        //Init values with existing data.
        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);
        if(data !=null && data.getValue()!=null) {
            setValue(data.getValue());
        }
        updateDisplay();
    }

    @Override
    public AbstractComponentContainer getRootLayout() {
        return this.root;
    }

    @Override
    public void setRootLayout(AbstractComponentContainer root) {
        this.root = root;
    }

    /**
     * Default component key definition.
     */
    public enum DefaultComponent {
        UPLOAD, PROGRESS_BAR, FILE_DETAIL, PREVIEW, DELETE_BUTTON, DROP_ZONE
    }

    /**
     * Return the desired defaultComponent.
     */
    public Component getDefaultComponent(AbstractUploadFileField.DefaultComponent defaultComponent) {
        return this.defaultComponent.get(defaultComponent);
    }

    public Map<AbstractUploadFileField.DefaultComponent, Component> getDefaultComponents() {
        return this.defaultComponent;
    }

    @Override
    public FileBuffer getReceiver() {
        return this.receiver;
    }

    protected Upload getUpload() {
        return this.upload;
    }

    protected JcrItemNodeAdapter getItem() {
        return this.item;
    }

    /**
     * Define the FileFactory to Use.
     * If no directory set, use the TempFileFactory.
     */
    public FileFactory getFileFactory() {
        if (this.directory != null && fileFactory == null) {
            fileFactory = new DirectoryFileFactory(directory);
        }else {
            fileFactory = new TempFileFactory();
        }
        return fileFactory;
    }

    @Override
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    /**
     * Default {@link FileFactory} used if no directory defined.
     */
    private class TempFileFactory implements FileFactory {

        @Override
        public File createFile(String fileName, String mimeType) {
                final String tempFileName = "upload_tmpfile_"
                                + System.currentTimeMillis();
                try {
                        return File.createTempFile(tempFileName, null);
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

    }

    /**
     * Progress Listener.
     */
    @Override
    public void updateProgress(long readBytes, long contentLength) {
        // if readBytes or contentLength exceed the max upload size, then
        // interrupt it.
        if (readBytes > this.maxUploadSize || contentLength > this.maxUploadSize) {
            this.upload.interruptUpload();
            return;
        }
        refreshOnProgressUploadLayout(readBytes, contentLength);
    }

    @Override
    public void refreshOnProgressUploadLayout(long readBytes, long contentLength) {
        if(this.progressInfo && progress!=null) {
            progress.setValue(new Float(readBytes / (float) contentLength));
        }
    }

    /**
     * Called when Upload Finish.
     * Set Display for Upload Finish.
     */
    @Override
    public void uploadFinished(FinishedEvent event) {
        this.upload.setVisible(true);

        if(event instanceof FailedEvent) {
            uploadFailed((FailedEvent)event);
            return;
        }

        updateDisplay();
        fireValueChange(true);
        handleFile();
    }


    @Override
    public void buildFinishUploadLayout() {
        if(this.progressInfo && this.progress !=null) {
            progress.setVisible(false);
        }if(this.info && this.fileDetail!=null) {
            fileDetail.setValue(getDisplayDetails());
        }
    }

    /**
     * Call when Upload start.
     * Set display for Upload in progress.
     */
    @Override
    public void uploadStarted(StartedEvent event) {
        // make the upload invisible when started
        this.upload.setVisible(false);
        buildStartUploadLayout();
        //TODO Check if file is valid.
    }


    @Override
    public void buildStartUploadLayout() {
        if(this.progressInfo && this.progress !=null) {
            this.progress.setVisible(true);
            this.progress.setValue(0);
        }
    }

    /**
     * Emits the value change event. The value contained in the field is
     * validated before the event is created.
     */
    protected void fireValueChange(boolean repaintIsNotNeeded) {
        fireEvent(new AbstractField.ValueChangeEvent(this));
        if (!repaintIsNotNeeded) {
            requestRepaint();
        }
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        String message = "File: "+event.getFilename()+" was not uploaded";
        log.warn("Upload failed "+message);
//        getWindow().showNotification("Upload failed", message, Notification.TYPE_WARNING_MESSAGE);
        if(item.getItemProperty(MgnlNodeType.JCR_DATA) == null || item.getItemProperty(MgnlNodeType.JCR_DATA).getValue() == null) {
            setValue(null);
        }
        updateDisplay();
    }


    /**
     * Update the main display.
     * Called when action is done: either a Upload has be done, or
     * Upload is not yet started.
     */
    @Override
    public void updateDisplay() {
        if(this.receiver.isEmpty()) {
            buildDefaultUploadLayout();
        } else {
            buildFinishUploadLayout();
        }
    }

    @Override
    public void buildDefaultUploadLayout(){
        this.upload.setVisible(true);
        this.requestForFileDeletion = false;
        if(this.progressInfo && this.progress != null) {
            progress.setVisible(false);
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
        sb.append("(" + FileUtils.byteCountToDisplaySize(receiver.getLastFileSize())+" )");
        sb.append("</em>");
        return sb.toString();
    }

    /**
     * Drag and Drop Handler section.
     */

    @Override
    public void drop(DragAndDropEvent event) {
        DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
        Html5File[] files = transferable.getFiles();
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
                    updateProgress(event.getBytesReceived(), (long) event.getContentLength());
                }

                @Override
                public void streamingStarted(StreamingStartEvent event) {
                    name = event.getFileName();
                    mime = event.getMimeType();
                    StartedEvent startEvent = new StartedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadStarted(startEvent);
                }

                @Override
                public void streamingFinished(StreamingEndEvent event) {
                    FinishedEvent finishEvent = new FinishedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadFinished(finishEvent);
                }

                @Override
                public void streamingFailed(StreamingErrorEvent event) {
                    FailedEvent failedEvent = new FailedEvent(upload, event.getFileName(), event.getMimeType(), event.getContentLength());
                    uploadFailed(failedEvent);
                }

                @Override
                public boolean isInterrupted() {
                    return false;
                }

            });
        }
    }



    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }


    /**
     * UploadFileField implementation.
     */
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
    public void setFileExtensionFilter(List<String> fileExtensions) {
        if(fileExtensions != null) {
            this.fileExtensions = fileExtensions;
        }
    }

    @Override
    public void setFileExtensionFilter(String fileExtensionsRegExp) {
        //TODO
    }

    @Override
    public void setUploadButtonCaption(String uploadButtonCaption) {
        this.uploadButtonCaption = uploadButtonCaption;
        this.upload.setButtonCaption(this.uploadButtonCaption);
    }

    @Override
    public void setFileDeletionButtonCaption(String deleteButtonCaption) {
        this.deleteButtonCaption = deleteButtonCaption;
        if(this.deleteButton != null) {
            this.deleteButton.setCaption(this.deleteButtonCaption);
        }
    }

    @Override
    public void setProgressIndicatorCaption(String progressIndicatorCaption) {
        this.progressIndicatorCaption = progressIndicatorCaption;
    }

    @Override
    public void setDropZoneCaption(String dropZoneCaption) {
        this.dropZoneCaption = dropZoneCaption;
    }

    @Override
    public void setUploadFileDirectory(File directory) {
        this.directory = directory;
    }

    public boolean hasRequestForFileDeletion() {
        return this.requestForFileDeletion;
    }

    /**
     * Best effort to create an ImageSize.
     * If the ImageSize created based on the receiver is null
     * try to create it from the Item property.
     * @return: null if receiver and item don't have relevant information.
     */
    protected ImageSize createImageSize() throws FileNotFoundException {
        ImageSize imageSize = ImageSize.valueOf(receiver.getFile());
        if(imageSize == null && item.getItemProperty(FileProperties.PROPERTY_WIDTH) != null && item.getItemProperty(FileProperties.PROPERTY_HEIGHT) != null) {
             imageSize = new ImageSize((Long)item.getItemProperty(FileProperties.PROPERTY_WIDTH).getValue(), (Long)item.getItemProperty(FileProperties.PROPERTY_HEIGHT).getValue());
         }
        return imageSize;
    }
    /**
     * Best effort to get the File MimeType.
     * If the receiver MimeType is null
     * try to get it from the Item property.
     * @return: null if receiver and item MimeType are not define.
     */
    protected String getLastMIMEType() {
        String mimeType = receiver.getLastMimeType();
        if(mimeType == null) {
            mimeType = (String)item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue();
        }
        return mimeType;
    }
    /**
     * Best effort to get the FileName.
     * If the receiver FileName is null
     * try to get it from the Item property.
     * @return: null if receiver and item FileName are not define.
     */
    protected String getLastFileName() {
        String fileName = receiver.getLastFileName();
        if(fileName == null) {
            fileName = (String)item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue();
        }
        return fileName;
    }
}
