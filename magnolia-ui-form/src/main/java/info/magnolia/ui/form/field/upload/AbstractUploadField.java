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
package info.magnolia.ui.form.field.upload;

import java.io.File;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.UploadException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Html5File;
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
 * This class handles Upload events and expose functions that allows to customize the 3 main upload states (link to a view Component):
 * <ul>
 * <li>Empty: Display the initial view (No Upload was performed)</li>
 * <li>InProgress: Display the progress view (Progress Bar) <br>
 * This view is triggered by a {@link StartedEvent}.</li>
 * <li>Completed: Display the File detail and Icon <br>
 * This event is triggered by <br>
 * {@link FinishedEvent} <br>
 * {@link FailedEvent}</li>
 * </ul>
 * <b>Important exposed method</b><br>
 * {@link Upload} getUpload() : Return the Vaadin Upload Component responsible for the Uploading a File based on a folder. <br>
 * createDropZone(Component c) : Give the Drop ability to the passed Component.<br>
 *
 * @param <D> {@link FileItemWrapper} implemented class.
 */
public abstract class AbstractUploadField<D extends FileItemWrapper> extends CustomField<Byte[]> implements StartedListener, FinishedListener, ProgressListener, FailedListener, DropHandler, UploadField {

    private static final Logger log = LoggerFactory.getLogger(AbstractUploadField.class);

    private long maxUploadSize = Long.MAX_VALUE;

    private String allowedMimeTypePattern = ".*";

    // Used to handle Cancel / Interrupted upload in the DragAndDrop
    // implementation.
    private boolean interruptedDragAndDropUpload = false;

    // Define global variable used by this implementation
    private final D fileWrapper;

    private UploadReceiver receiver;

    private Upload upload;

    private DragAndDropWrapper dropZone;

    private HasComponents root;

    public AbstractUploadField(D fileWrapper, File tmpUploadDirectory) {
        this.fileWrapper = fileWrapper;
        this.receiver = new UploadReceiver(tmpUploadDirectory);

        createUpload();
    }

    /**
     * Build the Empty Layout.<br>
     * Use the fileWrapper to display file information and Status.
     */
    protected abstract void buildEmptyLayout();

    /**
     * Build the in Progress Layout.<br>
     * Generally display a progress bar {@link UploadProgressIndicator} and some file information.<br>
     * Refresh of the action bar is handled by refreshInProgressLayout(...)<br>
     * Use the fileWrapper to display file information and Status.
     */
    protected abstract void buildInProgressLayout(String uploadedFileMimeType);

    /**
     * Update the in Progress Layout.<br>
     */
    protected abstract void refreshInProgressLayout(long readBytes, long contentLength, String fileName);

    /**
     * Build the Completed Layout.<br>
     * Use the fileWrapper to display file information and Status.
     */
    protected abstract void buildCompletedLayout();

    protected abstract void displayUploadInterruptNote(InterruptionReason reason);

    protected abstract void displayUploadFinishedNote(String fileName);

    protected abstract void displayUploadFailedNote(String fileName);

    /**
     * Call the correct layout.
     * <ul>
     * <li>- Empty: --> buildEmptyLayout()
     * <li>- Completed: --> buildCompletedLayout()
     * </ul>
     */
    protected void updateDisplay() {
        if (this.fileWrapper.isEmpty()) {
            buildEmptyLayout();
        } else {
            buildCompletedLayout();
        }
        markAsDirty();
    }

    /**
     * Interrupt upload based on a user Action.
     * An {@link UploadInterruptedException} will be thrown by the underlying Vaadin classes.
     */
    protected void interruptUpload(InterruptionReason reason) {
        displayUploadInterruptNote(reason);
        if (upload.isUploading()) {
            upload.interruptUpload();
        } else {
            setDragAndDropUploadInterrupted(true);
        }
    }

    private void setDragAndDropUploadInterrupted(boolean isInterrupted) {
        interruptedDragAndDropUpload = isInterrupted;
    }

    private boolean isDragAndDropUploadInterrupted() {
        return interruptedDragAndDropUpload;
    }

    /**
     * Simple Enumeration listing all available Interruption reason.
     */
    protected enum InterruptionReason {
        USER, FILE_NOT_ALLOWED, FILE_SIZE;
    }

    /**
     * Define the acceptance Upload Image criteria.
     * The current implementation only check if the MimeType match the desired regExp.
     */
    protected boolean isValidFile(StartedEvent event) {
        log.debug("Evaluate following regExp: {} against {}", allowedMimeTypePattern, event.getMIMEType());
        return event.getMIMEType().matches(allowedMimeTypePattern);
    }

    /**
     * Perform a post validation based on the File MimeType.
     */
    private boolean postFileValidation() {
        // TODO check for the best solution
        // http://www.rgagnon.com/javadetails/java-0487.html
        return true;
    }

    /**
     * Create the Upload component.
     */
    private void createUpload() {
        this.upload = new Upload(null, receiver);
        this.upload.addStartedListener(this);
        this.upload.addFinishedListener(this);
        this.upload.addProgressListener(this);
        this.upload.setImmediate(true);
    }

    /**
     * @return the initialized Upload component.
     */
    protected Upload getUpload() {
        return this.upload;
    }

    /**
     * Used to access the current File Wrapper in order to access the current File Informations.
     */
    protected D getFileWrapper() {
        return this.fileWrapper;
    }

    /**
     * The dropZone is a wrapper around a Component. {@link Refresher} is set to force refresh during drag and drop operation.
     */
    protected DragAndDropWrapper createDropZone(Component c) {
        dropZone = new DragAndDropWrapper(c) {

        };
        dropZone.setDropHandler(this);
        // add refresher
        final Refresher refresher = new Refresher();
        refresher.setRefreshInterval(500);
        refresher.addListener(new InProgressRefreshListener());
        addExtension(refresher);
        return this.dropZone;
    }

    /**
     * Basic implementation of {@link RefreshListener} in order to refresh the UI during drag and drop operation.
     */
    protected class InProgressRefreshListener implements RefreshListener {
        private static final long serialVersionUID = 1L;
        @Override
        public void refresh(final Refresher source) {
            // Do nothing
        }
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
                    setDragAndDropUploadInterrupted(false);
                }

                @Override
                public synchronized boolean isInterrupted() {
                    return isDragAndDropUploadInterrupted();
                }

            });
        }
    }

    /**
     * Handled by isValidFile().
     */
    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }

    /**
     * Start Upload if the file is supported. <br>
     * In case of not a supported file, interrupt the Upload.
     */
    @Override
    public void uploadStarted(StartedEvent event) {
        if (isValidFile(event)) {
            buildInProgressLayout(event.getMIMEType());
        } else {
            interruptUpload(InterruptionReason.FILE_NOT_ALLOWED);
        }
    }

    /**
     * Update the Progress Component. At the same time, check if the uploaded
     * File is not bigger as expected. Interrupt the Upload in this case.
     */
    @Override
    public void updateProgress(long readBytes, long contentLength) {
        if (readBytes > this.maxUploadSize || contentLength > this.maxUploadSize) {
            this.interruptUpload(InterruptionReason.FILE_SIZE);
            return;
        }
        refreshInProgressLayout(readBytes, contentLength, receiver.getLastFileName());
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }


    /**
     * Handle the {@link FinishedEvent}. In case of success: <br>
     * - Populate the Uploaded Information to the fileWrapper. <br>
     * - Build the Completed Layout. <br>
     * In case of {@link FailedEvent} (this event
     * is send on a Cancel upload) <br>
     * - Do not populate data and call uploadFailed().
     */
    @Override
    public void uploadFinished(FinishedEvent event) {
        if (event instanceof FailedEvent) {
            uploadFailed((FailedEvent) event);
            return;
        }
        // Post check Upload.
        if (!postFileValidation()) {
            FailedEvent newEvent = new FailedEvent(upload, receiver.getFileName(), receiver.getMimeType(), receiver.getFileSize());
            uploadFailed(newEvent);
            return;
        }
        displayUploadFinishedNote(event.getFilename());
        this.fileWrapper.populateFromReceiver(receiver);
        buildCompletedLayout();
        fireValueChange(false);
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        if (event.getReason() instanceof UploadException) {
            displayUploadFailedNote(event.getFilename());
        }
        this.fileWrapper.reloadPrevious();
        updateDisplay();
        log.warn("Upload failed for file {} ", event.getFilename());
    }

    @Override
    public Class<? extends Byte[]> getType() {
        return Byte[].class;
    }


    protected HasComponents getRootLayout() {
        return this.root;
    }

    protected void setRootLayout(HasComponents root) {
        this.root = root;
    }

    @Override
    public void setAllowedMimeTypePattern(String allowedMimeTypePattern) {
        this.allowedMimeTypePattern = allowedMimeTypePattern;

    }

    @Override
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    @Override
    public void detach() {
        if (upload.isUploading()) {
            upload.interruptUpload();
        } else {
            setDragAndDropUploadInterrupted(true);
        }
        super.detach();
    }


}
