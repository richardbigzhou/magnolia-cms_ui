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

import info.magnolia.cms.beans.config.MIMEMapping;

import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
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
import com.vaadin.ui.UI;
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
 * @param <T> {@link UploadReceiver} implemented class.
 */
public abstract class AbstractUploadField<T extends UploadReceiver> extends CustomField<T> implements StartedListener, FinishedListener, ProgressListener, FailedListener, DropHandler, UploadField {

    private static final Logger log = LoggerFactory.getLogger(AbstractUploadField.class);

    private long maxUploadSize = Long.MAX_VALUE;

    private String allowedMimeTypePattern = ".*";

    // Used to handle Cancel / Interrupted upload in the DragAndDrop
    // implementation.
    private boolean interruptedDragAndDropUpload = false;

    private Upload upload;

    private DragAndDropWrapper dropZone;

    private HasComponents root;

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

    @Override
    public void setLocale(Locale locale) {
        if (root != null) {
            updateDisplay();
        }
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        createUpload();
    }

    /**
     * Call the correct layout.
     * <ul>
     * <li>- Empty: --> buildEmptyLayout()
     * <li>- Completed: --> buildCompletedLayout()
     * </ul>
     */
    protected void updateDisplay() {
        if (getValue() != null) {
            if (this.getValue().isEmpty()) {
                buildEmptyLayout();
            } else {
                buildCompletedLayout();
            }
            markAsDirty();
        }
    }

    /**
     * Interrupt upload based on a user Action.
     * An {@link com.vaadin.server.communication.FileUploadHandler.UploadInterruptedException} will be thrown by the underlying Vaadin classes.
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
        this.upload = new Upload(null, getValue());
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
     * @return the initialized DragAndDropWrapper.
     */
    protected DragAndDropWrapper getDropZone() {
        return this.dropZone;
    }

    /**
     * The dropZone is a wrapper around a Component.
     */
    protected DragAndDropWrapper createDropZone(Component c) {
        dropZone = new DragAndDropWrapper(c) {

        };
        dropZone.setDropHandler(this);
        return this.dropZone;
    }

    /**
     * Drop zone Handler.
     */
    @Override
    public void drop(DragAndDropEvent event) {
        final DragAndDropWrapper.WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
        final Html5File[] files = transferable.getFiles();
        if (files == null) {
            return;
        }

        // start polling immediately on drop
        startPolling();

        for (final Html5File html5File : files) {
            html5File.setStreamVariable(new StreamVariable() {

                private String name;
                private String mime;

                @Override
                public OutputStream getOutputStream() {
                    return getValue().receiveUpload(name, mime);
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
                    if (StringUtils.isEmpty(mime)) {
                        String extension = StringUtils.substringAfterLast(name, ".");
                        mime = MIMEMapping.getMIMEType(extension);
                        if(StringUtils.isEmpty(mime)) {
                            log.warn("Couldn't find mimeType in MIMEMappings for file extension: {}", extension);
                        }
                    }
                    StartedEvent startEvent = new StartedEvent(upload, name, mime, event.getContentLength());
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

            // start polling here in case of upload through file input
            startPolling();
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
        refreshInProgressLayout(readBytes, contentLength, getValue().getLastFileName());
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

        stopPolling();

        if (event instanceof FailedEvent) {
            uploadFailed((FailedEvent) event);
            return;
        }
        // Post check Upload.
        if (!postFileValidation()) {
            FailedEvent newEvent = new FailedEvent(upload, getValue().getFileName(), getValue().getMimeType(), getValue().getFileSize());
            uploadFailed(newEvent);
            return;
        }
        displayUploadFinishedNote(event.getFilename());
        this.getPropertyDataSource().setValue(event.getUpload().getReceiver());
        buildCompletedLayout();
        fireValueChange(false);
    }

    @Override
    public void uploadFailed(FailedEvent event) {

        stopPolling();

        if (event.getReason() instanceof UploadException) {
            displayUploadFailedNote(event.getFilename());
        }
        resetDataSource();
        updateDisplay();
        log.warn("Upload failed for file {} ", event.getFilename());
    }

    @Override
    public Class getType() {
        return UploadReceiver.class;
    }

    protected void resetDataSource() {
        getValue().setValue(null);
        getPropertyDataSource().setValue(getValue());
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

    private void startPolling() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(new Runnable() {

                @Override
                public void run() {
                    UI.getCurrent().setPollInterval(200);
                }
            });
        }
    }

    private void stopPolling() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(new Runnable() {

                @Override
                public void run() {
                    UI.getCurrent().setPollInterval(-1);
                }
            });
        }
    }

}
