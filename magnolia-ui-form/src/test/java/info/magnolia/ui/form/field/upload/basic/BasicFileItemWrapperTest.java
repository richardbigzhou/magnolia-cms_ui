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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.vaadin.data.Property;

/**
 * Main test class for {@link BasicFileItemWrapper}.
 */
public class BasicFileItemWrapperTest {

    private final String workspaceName = "workspace";
    private MockSession session;
    private File uploadFileMe;
    private File uploadFileYou;
    private BasicFileItemWrapper basicFileItemWrapper;
    private File directory;
    private SimpleTranslator i18n;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);

        URL resource = ClasspathResourcesUtil.getResource("me.jpg");
        uploadFileMe = new File(resource.toURI());
        resource = ClasspathResourcesUtil.getResource("you.jpg");
        uploadFileYou = new File(resource.toURI());

        directory = Files.createTempDir();
        directory.deleteOnExit();
        i18n = mock(SimpleTranslator.class);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);

        if (directory != null) {
            directory.delete();
        }
    }

    @Test
    public void testPopulateFromItemJcrItemNodeAdapter() throws RepositoryException, FileNotFoundException {
        // GIVEN
        // Create an image Node
        Node underlyingNode = session.getRootNode().addNode("photo", "nt:resource");
        underlyingNode.setProperty("fileName", "me.jpg");
        underlyingNode.setProperty("size", uploadFileMe.length());
        underlyingNode.setProperty("extension", "jpg");
        underlyingNode.setProperty("jcr:mimeType", "image/jpeg");
        underlyingNode.setProperty("jcr:data", ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadFileMe)));

        // Create an Image Item
        JcrNodeAdapter jcrItem = new JcrNodeAdapter(underlyingNode);

        // WHEN
        basicFileItemWrapper = new BasicFileItemWrapper(jcrItem, directory);

        // THEN
        assertEquals("me.jpg", basicFileItemWrapper.getFileName());
        assertEquals("jpg", basicFileItemWrapper.getExtension());
        assertEquals(uploadFileMe.length(), basicFileItemWrapper.getFileSize());
        assertEquals("image/jpeg", basicFileItemWrapper.getMimeType());
    }

    @Test
    public void testPopulateFromItemJcrItemNodeAdapterWithMissingProperties() throws RepositoryException, FileNotFoundException {
        // GIVEN
        // Create an image Node
        Node underlyingNode = session.getRootNode().addNode("photo", "nt:resource");
        underlyingNode.setProperty("fileName", "me.jpg");
        underlyingNode.setProperty("size", uploadFileMe.length());
        underlyingNode.setProperty("jcr:data", ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadFileMe)));

        // Create an Image Item
        JcrNodeAdapter jcrItem = new JcrNodeAdapter(underlyingNode);

        // WHEN
        basicFileItemWrapper = new BasicFileItemWrapper(jcrItem, directory);

        // THEN
        assertEquals("me.jpg", basicFileItemWrapper.getFileName());
        assertEquals(uploadFileMe.length(), basicFileItemWrapper.getFileSize());
    }

    @Test
    public void testPopulateFromItemJcrItemNewNodeAdapter() throws RepositoryException, FileNotFoundException {
        // GIVEN
        // Create an image Node
        Node root = session.getRootNode();

        // Create an Image Item
        JcrNewNodeAdapter jcrItem = new JcrNewNodeAdapter(root, "nt:resource", "photo");

        // WHEN
        basicFileItemWrapper = new BasicFileItemWrapper(jcrItem, directory);

        // THEN
        assertEquals(null, basicFileItemWrapper.getFileName());
        assertEquals(null, basicFileItemWrapper.getExtension());
        assertEquals(0l, basicFileItemWrapper.getFileSize());
        assertEquals(null, basicFileItemWrapper.getMimeType());
        assertTrue(jcrItem.getItemProperty("size") != null);
        assertTrue(jcrItem.getItemProperty("fileName") != null);
        assertTrue(jcrItem.getItemProperty("extension") != null);
        assertTrue(jcrItem.getItemProperty("jcr:mimeType") != null);
        assertTrue(jcrItem.getItemProperty("jcr:data") != null);
        assertTrue(jcrItem.getItemProperty("jcr:lastModified") != null);
    }

    @Test
    public void testPopulateFromReceiverForJcrItemNodeAdapterTestFileWrapper() throws RepositoryException, FileNotFoundException {
        // GIVEN
        // Create an image Node
        Node root = session.getRootNode();
        Node underlyingNode = root.addNode("photo", "nt:resource");
        underlyingNode.setProperty("fileName", "me.jpg");
        underlyingNode.setProperty("size", uploadFileMe.length());
        underlyingNode.setProperty("extension", "jpg");
        underlyingNode.setProperty("jcr:mimeType", "image/jpeg");
        underlyingNode.setProperty("jcr:lastModified", Calendar.getInstance());
        underlyingNode.setProperty("jcr:data", ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadFileMe)));

        // Create an Image Item
        JcrNodeAdapter jcrItem = new JcrNodeAdapter(underlyingNode);
        JcrNodeAdapter jcrParentItem = new JcrNodeAdapter(root);
        jcrParentItem.addChild(jcrItem);
        basicFileItemWrapper = new BasicFileItemWrapper(jcrItem, directory);

        // Create a receiver
        UploadReceiver receiver = new UploadReceiver(directory, i18n);
        receiver.receiveUpload("you.jpg", "image/jpeg");
        receiver.setValue(uploadFileYou);

        // WHEN
        basicFileItemWrapper.populateFromReceiver(receiver);

        // THEN
        assertEquals("you", basicFileItemWrapper.getFileName());
        assertEquals("jpg", basicFileItemWrapper.getExtension());
        assertEquals(uploadFileYou.length(), basicFileItemWrapper.getFileSize());
        assertEquals("image/jpeg", basicFileItemWrapper.getMimeType());
    }

    @Test
    public void testPopulateFromReceiverForJcrItemNodeAdapterTestRelatedItem() throws RepositoryException, IOException {
        // GIVEN
        // Create an image Node
        Node root = session.getRootNode();
        Node underlyingNode = root.addNode("photo", "nt:resource");
        underlyingNode.setProperty("fileName", "me.jpg");
        underlyingNode.setProperty("size", uploadFileMe.length());
        underlyingNode.setProperty("extension", "jpg");
        underlyingNode.setProperty("jcr:mimeType", "image/jpeg");
        underlyingNode.setProperty("jcr:lastModified", Calendar.getInstance());
        underlyingNode.setProperty("jcr:data", ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadFileMe)));

        // Create an Image Item
        JcrNodeAdapter jcrItem = new JcrNodeAdapter(underlyingNode);
        JcrNodeAdapter jcrParentItem = new JcrNodeAdapter(root);
        jcrParentItem.addChild(jcrItem);
        basicFileItemWrapper = new BasicFileItemWrapper(jcrItem, directory);

        // Create a receiver
        UploadReceiver receiver = new UploadReceiver(directory, i18n);
        receiver.receiveUpload("you.jpg", "image/jpeg");
        receiver.setValue(uploadFileYou);

        // WHEN
        basicFileItemWrapper.populateFromReceiver(receiver);

        // THEN
        assertEquals("you", String.valueOf(jcrItem.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue()));
        assertEquals("jpg", String.valueOf(jcrItem.getItemProperty(FileProperties.PROPERTY_EXTENSION).getValue()));
        Property<?> data = jcrItem.getItemProperty(JcrConstants.JCR_DATA);
        File tmpFile = File.createTempFile("tmp", null);
        OutputStream outputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(((BinaryImpl) data.getValue()).getStream(), outputStream);
        outputStream.close();
        assertEquals(uploadFileYou.length(), tmpFile.length());
        assertEquals("image/jpeg", String.valueOf(jcrItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue()));
    }

    @Test
    public void testPopulateFromReceiverForJcrItemNewNodeAdapterTestFileWrapper() {
        // GIVEN
        // Create an image Node
        Node root = session.getRootNode();

        // Create an Image Item
        JcrNodeAdapter jcrParentItem = new JcrNodeAdapter(root);
        JcrNewNodeAdapter jcrNewItem = new JcrNewNodeAdapter(root, "nt:resource", "photo");
        jcrParentItem.addChild(jcrNewItem);
        basicFileItemWrapper = new BasicFileItemWrapper(jcrNewItem, directory);

        // Create a receiver
        UploadReceiver receiver = new UploadReceiver(directory, i18n);
        receiver.receiveUpload("me.jpg", "image/jpeg");
        receiver.setValue(uploadFileMe);

        // WHEN
        basicFileItemWrapper.populateFromReceiver(receiver);

        // THEN
        assertEquals("me", basicFileItemWrapper.getFileName());
        assertEquals("jpg", basicFileItemWrapper.getExtension());
        assertEquals(uploadFileMe.length(), basicFileItemWrapper.getFileSize());
        assertEquals("image/jpeg", basicFileItemWrapper.getMimeType());
    }

    @Test
    public void testPopulateFromReceiverForJcrItemNewNodeAdapterTestRelatedItem() throws IOException, RepositoryException {
        // GIVEN
        // Create an image Node
        Node root = session.getRootNode();

        // Create an Image Item
        JcrNodeAdapter jcrParentItem = new JcrNodeAdapter(root);
        JcrNewNodeAdapter jcrNewItem = new JcrNewNodeAdapter(root, "nt:resource", "photo");
        jcrParentItem.addChild(jcrNewItem);
        basicFileItemWrapper = new BasicFileItemWrapper(jcrNewItem, directory);

        // Create a receiver
        UploadReceiver receiver = new UploadReceiver(directory, i18n);
        receiver.receiveUpload("me.jpg", "image/jpeg");
        receiver.setValue(uploadFileMe);

        // WHEN
        basicFileItemWrapper.populateFromReceiver(receiver);

        // THEN

        assertEquals("me", String.valueOf(jcrNewItem.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue()));
        assertEquals("jpg", String.valueOf(jcrNewItem.getItemProperty(FileProperties.PROPERTY_EXTENSION).getValue()));
        Property<?> data = jcrNewItem.getItemProperty(JcrConstants.JCR_DATA);
        File tmpFile = File.createTempFile("tmp", null);
        OutputStream outputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(((BinaryImpl) data.getValue()).getStream(), outputStream);
        outputStream.close();
        assertEquals(uploadFileMe.length(), tmpFile.length());
        assertEquals("image/jpeg", String.valueOf(jcrNewItem.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue()));
    }

    @Test
    public void testPopulateFromReceiverWithMissingProperty() throws Exception {
        // GIVEN
        Node photo = session.getRootNode().addNode("photo", "nt:resource");
        photo.setProperty("fileName", "me.jpg");
        photo.setProperty("size", uploadFileMe.length());
        photo.setProperty("extension", "jpg");
        // Voluntarily missing jcr:mimeType property
        photo.setProperty("jcr:lastModified", Calendar.getInstance());
        photo.setProperty("jcr:data", ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadFileMe)));

        JcrNodeAdapter item = new JcrNodeAdapter(photo);
        item.setParent(new JcrNodeAdapter(session.getRootNode()));
        basicFileItemWrapper = new BasicFileItemWrapper(item, directory);

        UploadReceiver receiver = new UploadReceiver(directory, i18n);
        receiver.receiveUpload("you.jpg", "image/jpeg");
        receiver.setValue(uploadFileYou);

        // WHEN
        basicFileItemWrapper.populateFromReceiver(receiver);

        // THEN
        // 1. No exception
        // 2. missing property was successfully added
        assertEquals("image/jpeg", String.valueOf(item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue()));
    }

    @Test
    public void testPopulateFromReceiverWithMistypedProperty() throws Exception {
        // GIVEN
        Node photo = session.getRootNode().addNode("photo", "nt:resource");
        photo.setProperty("fileName", "me.jpg");
        // Voluntarily set size property with String type
        photo.setProperty("size", String.valueOf(uploadFileMe.length()));
        photo.setProperty("extension", "jpg");
        photo.setProperty("jcr:mimeType", "image/jpeg");
        photo.setProperty("jcr:lastModified", Calendar.getInstance());
        photo.setProperty("jcr:data", ValueFactoryImpl.getInstance().createBinary(new FileInputStream(uploadFileMe)));

        JcrNodeAdapter item = new JcrNodeAdapter(photo);
        item.setParent(new JcrNodeAdapter(session.getRootNode()));
        basicFileItemWrapper = new BasicFileItemWrapper(item, directory);

        UploadReceiver receiver = new UploadReceiver(directory, i18n);
        receiver.receiveUpload("you.jpg", "image/jpeg");
        receiver.setValue(uploadFileYou);

        // WHEN
        basicFileItemWrapper.populateFromReceiver(receiver);

        // THEN
        // 1. No conversion exception
        // 2. mistyped property now has correct type
        assertEquals(uploadFileYou.length(), item.getItemProperty(FileProperties.PROPERTY_SIZE).getValue());
    }
}
