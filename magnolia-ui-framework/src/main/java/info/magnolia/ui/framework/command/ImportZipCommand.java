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
package info.magnolia.ui.framework.command;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.Path;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.form.field.upload.UploadReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.easyuploads.UploadField;

/**
 * Imports ZIP archive contents into JCR repository.
 */
public class ImportZipCommand extends BaseRepositoryCommand {

    public static final String STREAM_PROPERTY = "inputStream";

    public static final String ENCODING_PROPERTY = "encoding";

    public static final String ZIP_TMP_FILE_PREFIX = "zipupload";

    public static final String ZIP_TMP_FILE_SUFFIX = ".zip";

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static final String BACKSLASH_DUMMY = "________backslash________";

    private SimpleTranslator translator;

    private InputStream inputStream;

    protected Context context;

    private String encoding;

    @Inject
    public ImportZipCommand(SimpleTranslator translator) {
        this.translator = translator;
    }

    @Override
    public boolean execute(Context context) throws Exception {
        this.context = context;
        File tmpFile =  null;
        FileOutputStream tmpStream = null;
        try {
            tmpFile = File.createTempFile(ZIP_TMP_FILE_PREFIX, ZIP_TMP_FILE_SUFFIX);
            tmpStream = new FileOutputStream(tmpFile);
            IOUtils.copy(inputStream, tmpStream);
        } catch (IOException e) {
            log.error("Failed to dump zip file to temp file: ", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(tmpStream);
            IOUtils.closeQuietly(inputStream);
        }

        if (isValid(tmpFile)) {
            ZipFile zip = new ZipFile(tmpFile, getEncoding());
            // We use the ant-1.6.5 zip package to workaround encoding issues of the sun implementation (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499)
            // For some reason, entries are not in the opposite order as how they appear in most tools - reversing here.
            // Note that java.util.zip does not show this behaviour, and ant-1.7.1 seems to enumerate entries in alphabetical or random order.
            // Another alternative might be http://truezip.dev.java.net
            final List zipEntries = EnumerationUtils.toList(zip.getEntries());
            Collections.sort(zipEntries, new Comparator() {
                @Override
                public int compare(Object first, Object second) {
                    Boolean isFirstDirectory = ((ZipArchiveEntry)first).isDirectory();
                    Boolean isSecondDirectory = ((ZipArchiveEntry)second).isDirectory();
                    return isFirstDirectory.compareTo(isSecondDirectory);
                }
            });

            Collections.reverse(zipEntries);
            final Iterator it = zipEntries.iterator();
            while (it.hasNext()) {
                ZipArchiveEntry entry = (ZipArchiveEntry) it.next();
                processEntry(zip, entry);
            }
            context.getJCRSession(getRepository()).save();
        }
        return false;
    }

    private void processEntry(ZipFile zip, ZipArchiveEntry entry) throws IOException, RepositoryException {
            if (entry.getName().startsWith("__MACOSX")) {
                return;
            }
            else if (entry.getName().endsWith(".DS_Store")) {
                return;
            }

            if (entry.isDirectory()) {
                ensureFolder(entry);
            } else {
                handleFileEntry(zip, entry);
            }

    }

    protected void handleFileEntry(ZipFile zip, ZipArchiveEntry entry) throws IOException, RepositoryException {
        String fileName = entry.getName();
        if (StringUtils.contains(fileName, "/")) {
            fileName = StringUtils.substringAfterLast(fileName, "/");
        }

        String extension = StringUtils.substringAfterLast(fileName, ".");
        InputStream stream = zip.getInputStream(entry);
        FileOutputStream os = null;
        try {
            UploadReceiver receiver = createReceiver();

            String folderPath = extractEntryPath(entry);
            if (folderPath.startsWith("/")) {
                folderPath = folderPath.substring(1);
            }
            Node folder = getJCRNode(context);
            if (StringUtils.isNotBlank(folderPath)) {
                if (folder.hasNode(folderPath)) {
                    folder = folder.getNode(folderPath);
                } else {
                    folder = NodeUtil.createPath(folder, folderPath, NodeTypes.Folder.NAME, true);
                }
            }
            receiver.setFieldType(UploadField.FieldType.BYTE_ARRAY);
            receiver.receiveUpload(fileName, StringUtils.defaultIfEmpty(MIMEMapping.getMIMEType(extension), DEFAULT_MIME_TYPE));
            receiver.setValue(IOUtils.toByteArray(stream));

            doHandleEntryFromReceiver(folder, receiver);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(os);
        }
    }

    protected UploadReceiver createReceiver() {
        return new UploadReceiver(Path.getTempDirectory(), translator);
    }

    /**
     * Actually produce an asset or a node based on the entry data aggregated into an
     * {@link UploadReceiver} object.
     * @param folder parent folder node for a new entry.
     * @param receiver wraps the entry file and its basic meta-information.
     * @throws RepositoryException
     */
    protected void doHandleEntryFromReceiver(Node folder, UploadReceiver receiver) throws RepositoryException {}

    private void ensureFolder(ZipArchiveEntry entry) throws RepositoryException {
        String path = extractEntryPath(entry);
        NodeUtil.createPath(getJCRNode(context), path, NodeTypes.Folder.NAME, true);
    }

    private String extractEntryPath(ZipArchiveEntry entry) {
        String entryName = entry.getName();
        String path = (StringUtils.contains(entryName, "/")) ?  StringUtils.substringBeforeLast(entryName, "/") : "/";

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // make proper name, the path was already created
        path = StringUtils.replace(path, "/", BACKSLASH_DUMMY);
        path = Path.getValidatedLabel(path);
        path = StringUtils.replace(path, BACKSLASH_DUMMY, "/");
        return path;
    }

    private boolean isValid(File tmpFile) {
        return tmpFile != null;
    }


    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
