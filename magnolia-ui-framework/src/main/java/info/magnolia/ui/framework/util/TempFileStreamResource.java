/**
 * This file Copyright (c) 2015 Magnolia International
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
 */
package info.magnolia.ui.framework.util;

import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;

/**
 * {@link StreamSource} version which organises streaming via a temporary file.
 *
 * Streamed content can be populated via {@link OutputStream} provided by {@link #getTempFileOutputStream()}.
 * In order to set up the resource the following attributes are required to be set:
 * <ul>
 * <li>tempFileName - name of the buffer temporary file</li>
 * <li>tempFileExtension - extension of the buffer temporary file</li>
 * <li>fileName - the name of the downloaded file</li>
 * </ul>
 *
 * <strong>Note:</strong> the streamed resource is by default configured to be not cached
 * <strong>Note:</strong> the temporary file is attempted to be eagerly deleted once the streaming is over
 */
public class TempFileStreamResource extends StreamResource {

    private static final Logger log = LoggerFactory.getLogger(TempFileStreamResource.class);

    public TempFileStreamResource(String filename) {
        super(new TempFileStreamSource(), filename);
        setCacheTime(-1);
    }

    public TempFileStreamResource() {
        this(null);
    }

    @Override
    public TempFileStreamSource getStreamSource() {
        return (TempFileStreamSource) super.getStreamSource();
    }

    @Override
    public DownloadStream getStream() {
        final DownloadStream stream = super.getStream();
        if (!StringUtils.isBlank(getFilename())) {
            stream.setParameter("Content-Disposition", "attachment; filename=" + getFilename() + "\"");
        }
        return stream;
    }

    public void setTempFileName(String name) {
        getStreamSource().tempFileName = name;
    }

    public void setTempFileExtension(String extension) {
        getStreamSource().tempFileExtension = extension;
    }

    public OutputStream getTempFileOutputStream() {
        return getStreamSource().getOutputStream();
    }

    private static class TempFileStreamSource implements StreamSource {

        private String tempFileName;

        private String tempFileExtension;

        private File tempFile = null;

        private FileOutputStream fileOutputStream = null;

        @Override
        public InputStream getStream() {
            try {
                return new DeleteOnCloseFileInputStream(tempFile);
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        OutputStream getOutputStream() {
            if (fileOutputStream == null) {
                try {
                    fileOutputStream = new FileOutputStream(getTempFile());
                } catch (FileNotFoundException e) {
                    return null;
                }
            }
            return fileOutputStream;
        }

        File getTempFile() {
            if (tempFile == null) {
                // Create a temporary file that will hold the data created by the export command.
                try {
                    tempFile = File.createTempFile(tempFileName, tempFileExtension, Path.getTempDirectory());
                } catch (IOException e) {
                    log.error("", e);
                    return null;
                }
            }
            return tempFile;
        }
    }

    private static class DeleteOnCloseFileInputStream extends FileInputStream {

        private File file;

        public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (file.exists() && !file.delete()) {
                log.warn("Could not delete temporary export file {}", file.getAbsolutePath());
            }
        }

    }
}
