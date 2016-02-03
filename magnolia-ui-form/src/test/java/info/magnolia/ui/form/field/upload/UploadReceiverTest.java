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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.i18nsystem.SimpleTranslator;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.easyuploads.FileFactory;

import com.google.common.io.Files;

/**
 * Main test class for {@link UploadReceiver}.
 */
public class UploadReceiverTest {

    private UploadReceiver uploadReceiver;
    private File directory;
    private File uploadFile;
    private SimpleTranslator i18n;

    @Before
    public void setUp() throws Exception {
        directory = Files.createTempDir();
        directory.deleteOnExit();
        URL resource = ClasspathResourcesUtil.getResource("me.jpg");
        uploadFile = new File(resource.toURI());
        i18n = mock(SimpleTranslator.class);
    }

    @After
    public void tearDown() throws Exception {
        if (directory != null) {
            directory.delete();
        }
    }


    @Test
    public void testGetFileFactory() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);

        // WHEN
        FileFactory factory = uploadReceiver.getFileFactory();

        // THEN
        assertNotNull(factory);
        assertTrue(factory instanceof DefaultFileFactory);
    }

    @Test
    public void testReceiveUpload() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);

        // WHEN
        OutputStream res = uploadReceiver.receiveUpload("me.jpg", "image/jpeg");

        // THEN
        assertNotNull(res);
        assertNotNull(uploadReceiver.getFile());

        assertTrue(uploadReceiver.getFile().getPath().startsWith(directory.getPath()));
        assertTrue(uploadReceiver.getFile().getName().startsWith("me"));

    }

    @Test
    public void testSetValue() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("me.jpg", "image/jpeg");

        // WHEN
        uploadReceiver.setValue(uploadFile);

        // THEN
        File tmp = uploadReceiver.getFile();
        assertNotNull(tmp);
        assertEquals(uploadFile.getName(), tmp.getName());
        assertEquals(uploadFile.length(), tmp.length());
    }

    @Test
    public void testGetFileName() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("me.jpg", "image/jpeg");
        uploadReceiver.setValue(uploadFile);

        // WHEN
        String res = uploadReceiver.getFileName();

        // THEN
        assertEquals(uploadFile.getName(), res);
    }

    @Test
    public void testGetDefaultFileName() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("", "image/jpeg");
        uploadReceiver.setValue(uploadFile);

        // WHEN
        String res = uploadReceiver.getFileName();

        // THEN
        assertEquals(UploadReceiver.INVALID_FILE_NAME, res);
    }

    @Test
    public void testSetFileName() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("me.jpg", "image/jpeg");
        uploadReceiver.setValue(uploadFile);
        uploadReceiver.setFileName("newMe.jpg");

        // WHEN
        String res = uploadReceiver.getFileName();

        // THEN
        assertNotNull(res);
        assertEquals("newMe.jpg", res);
    }

    @Test
    public void testGetFileSize() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("me.jpg", "image/jpeg");
        uploadReceiver.setValue(uploadFile);

        // WHEN
        long res = uploadReceiver.getFileSize();

        // THEN
        assertEquals(uploadFile.length(), res);
    }

    @Test
    public void testGetMimeType() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("me.jpg", "image/jpeg");
        uploadReceiver.setValue(uploadFile);

        // WHEN
        String res = uploadReceiver.getMimeType();

        // THEN
        assertNotNull(res);
        assertEquals("image/jpeg", res);
    }

    @Test
    public void testGetExtension() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("me.jpg", "image/jpeg");
        uploadReceiver.setValue(uploadFile);

        // WHEN
        String res = uploadReceiver.getExtension();

        // THEN
        assertNotNull(res);
        assertEquals("jpg", res);
    }

    @Test
    public void testFilenameWithoutExtensionIsValid() {
        // GIVEN
        uploadReceiver = new UploadReceiver(directory, i18n);
        uploadReceiver.receiveUpload("foo", "image/jpeg");
        uploadReceiver.setValue(uploadFile);

        // WHEN
        String res = uploadReceiver.getFileName();

        // THEN
        assertEquals("foo", res);
    }
}
