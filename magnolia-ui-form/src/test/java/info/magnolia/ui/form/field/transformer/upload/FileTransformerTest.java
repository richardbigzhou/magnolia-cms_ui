/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.form.field.transformer.upload;

import static info.magnolia.cms.beans.runtime.FileProperties.*;
import static info.magnolia.cms.core.SystemProperty.*;
import static info.magnolia.test.ComponentsTestUtil.*;
import static info.magnolia.test.mock.MockUtil.initMockContext;
import static info.magnolia.ui.form.field.upload.UploadReceiver.INVALID_FILE_NAME;
import static org.apache.commons.io.IOUtils.*;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.jcr.util.NodeTypes.ContentNode;
import info.magnolia.jcr.util.NodeTypes.Resource;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.transformer.item.FileTransformer;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.value.BinaryValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Main test class for {@link FileTransformer}.
 */
public class FileTransformerTest {

    private BasicUploadFieldDefinition definition = new BasicUploadFieldDefinition();
    private Item rootItem;
    private Node fileNode;
    private I18nContentSupport i18nContentSupport;
    private Locale defaultLocal = Locale.ENGLISH;

    @Before
    public void setUp() throws Exception {
        initMockContext();
        setProperty(MAGNOLIA_APP_ROOTDIR, "./src/test/resources");
        setProperty(MAGNOLIA_UPLOAD_TMPDIR, System.getProperty("java.io.tmpdir"));
        // Init rootItem
        MockSession session = new MockSession("test");
        ((MockContext) MgnlContext.getInstance()).addSession("test", session);
        MockNode rootNode = new MockNode(session);
        rootNode.setName("root");
        rootNode.setPrimaryType(ContentNode.NAME);
        rootNode.setProperty("text", "some text");
        rootNode.addNode(new MockNode(definition.getBinaryNodeName(), Resource.NAME));
        fileNode = rootNode.getNode(definition.getBinaryNodeName());
        rootItem = new JcrNodeAdapter(rootNode);

        // Init I18n
        i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getDefaultLocale()).thenReturn(defaultLocal);
        when(i18nContentSupport.isEnabled()).thenReturn(false);
        definition.setI18n(false);

        setInstance(SimpleTranslator.class, mock(SimpleTranslator.class));

        setImplementation(TypeMapping.class, TypeMappingImpl.class);
        setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void readFromItemWithoutExistingFile() throws RepositoryException {
        // GIVEN
        fileNode.remove();
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);

        // WHEN
        UploadReceiver property = transformer.readFromItem();

        // THEN
        assertNotNull(property);
        assertEquals(INVALID_FILE_NAME, property.getFileName());
        assertEquals(0l, property.getFileSize());
    }

    @Test
    public void readFromItemWithExistingFile() throws RepositoryException {
        // GIVEN
        fileNode.setProperty(JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);

        // WHEN
        UploadReceiver property = transformer.readFromItem();

        // THEN
        assertNotNull(property);
        assertEquals("test.jpg", property.getFileName());
        assertEquals("image/jpg", property.getMimeType());
        assertEquals(4l, property.getFileSize());
        assertEquals("jpg", property.getExtension());
    }

    @Test
    public void i18nReadFromItem() throws IOException, RepositoryException {
        // GIVEN
        when(i18nContentSupport.isEnabled()).thenReturn(true);
        definition.setI18n(true);
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);
        transformer.setI18NPropertyName(definition.getBinaryNodeName() + "_de");
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("test.jpg", "image/jpg");
        copy(new BinaryValue("test").getStream(), out);
        closeQuietly(out);

        // WHEN
        transformer.writeToItem(property);

        // THEN
        assertNotNull(((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName() + "_de"));
    }

    @Test
    public void writeToItemWithoutExistingFile() throws RepositoryException, IOException {
        // GIVEN
        fileNode.remove();
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("test.jpg", "image/jpg");
        copy(new BinaryValue("test").getStream(), out);
        closeQuietly(out);

        // WHEN
        transformer.writeToItem(property);

        // THEN
        Item item = ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName());
        assertEquals("test.jpg", item.getItemProperty(PROPERTY_FILENAME).getValue());
        assertEquals("image/jpg", item.getItemProperty(PROPERTY_CONTENTTYPE).getValue());
        assertEquals(4l, item.getItemProperty(SIZE).getValue());
        assertEquals("jpg", item.getItemProperty(EXTENSION).getValue());
    }

    @Test
    public void writeToItemWithExistingFile() throws RepositoryException, IOException {
        // GIVEN
        // Init a stored file
        fileNode.setProperty(JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);
        // Simulate a new upload
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("newTest.pgn", "image/pgn");
        copy(new BinaryValue("new test").getStream(), out);
        closeQuietly(out);

        // WHEN
        transformer.writeToItem(property);

        // THEN
        Item item = ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName());
        assertEquals("newTest.pgn", item.getItemProperty(PROPERTY_FILENAME).getValue());
        assertEquals("image/pgn", item.getItemProperty(PROPERTY_CONTENTTYPE).getValue());
        assertEquals(8l, item.getItemProperty(SIZE).getValue());
        assertEquals("pgn", item.getItemProperty(EXTENSION).getValue());
    }

    @Test
    public void createPropertyFromItem() throws RepositoryException {
        // GIVEN
        fileNode.setProperty(JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);
        transformer.readFromItem();

        // WHEN
        UploadReceiver property = transformer.createPropertyFromItem(((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName()));

        // THEN
        assertNotNull(property);
        assertEquals("test.jpg", property.getFileName());
        assertEquals("image/jpg", property.getMimeType());
        assertEquals(4l, property.getFileSize());
        assertEquals("jpg", property.getExtension());
    }

    @Test
    public void populateExistingItem() throws RepositoryException, IOException {
        // GIVEN
        // Init a stored file
        fileNode.setProperty(JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);
        // Simulate a new upload
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("newTest.pgn", "image/pgn");
        copy(new BinaryValue("new test").getStream(), out);
        closeQuietly(out);

        // WHEN
        Item item = transformer.populateItem(property, ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName()));

        // THEN
        assertEquals("newTest.pgn", item.getItemProperty(PROPERTY_FILENAME).getValue());
        assertEquals("image/pgn", item.getItemProperty(PROPERTY_CONTENTTYPE).getValue());
        assertEquals(8l, item.getItemProperty(SIZE).getValue());
        assertEquals("pgn", item.getItemProperty(EXTENSION).getValue());
    }

    @Test
    public void populateNewItem() throws RepositoryException, IOException {
        // GIVEN
        fileNode.remove();
        FileTransformer<UploadReceiver> transformer = new FileTransformer<>(rootItem, definition, UploadReceiver.class);
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("test.jpg", "image/jpg");
        copy(new BinaryValue("test").getStream(), out);
        closeQuietly(out);
        transformer.writeToItem(property);

        // WHEN
        Item item = transformer.populateItem(property, ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName()));

        // THEN
        assertEquals("test.jpg", item.getItemProperty(PROPERTY_FILENAME).getValue());
        assertEquals("image/jpg", item.getItemProperty(PROPERTY_CONTENTTYPE).getValue());
        assertEquals(4l, item.getItemProperty(SIZE).getValue());
        assertEquals("jpg", item.getItemProperty(EXTENSION).getValue());
    }
}
