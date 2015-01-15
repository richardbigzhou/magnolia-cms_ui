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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
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

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
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
    private MockNode rootNode;
    private Node fileNode;
    private I18nContentSupport i18nContentSupport;
    private Locale defaultLocal = Locale.ENGLISH;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, "./src/test/resources");
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, System.getProperty("java.io.tmpdir"));
        // Init rootItem
        MockSession session = new MockSession("test");
        ((MockContext) MgnlContext.getInstance()).addSession("test", session);
        rootNode = new MockNode(session);
        rootNode.setName("root");
        rootNode.setPrimaryType(NodeTypes.ContentNode.NAME);
        rootNode.setProperty("text", "some text");
        rootNode.addNode(new MockNode(definition.getBinaryNodeName(), NodeTypes.Resource.NAME));
        fileNode = rootNode.getNode(definition.getBinaryNodeName());
        rootItem = new JcrNodeAdapter(rootNode);

        // Init I18n
        i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getDefaultLocale()).thenReturn(defaultLocal);
        when(i18nContentSupport.isEnabled()).thenReturn(false);
        definition.setI18n(false);


        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);

        ComponentsTestUtil.setInstance(TranslationService.class, new TranslationServiceImpl());

        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
        ComponentsTestUtil.setImplementation(SimpleTranslator.class, SimpleTranslator.class);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void readFromItemWithoutExisitngFile() throws RepositoryException {
        // GIVEN
        fileNode.remove();
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);

        // WHEN
        UploadReceiver property = transformer.readFromItem();

        // THEN
        assertNotNull(property);
        assertEquals(UploadReceiver.INVALID_FILE_NAME, property.getFileName());
        assertEquals(0l, property.getFileSize());
    }

    @Test
    public void readFromItemWithExisitngFile() throws RepositoryException {
        // GIVEN
        fileNode.setProperty(JcrConstants.JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(FileProperties.PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(FileProperties.PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);

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
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);
        transformer.setI18NPropertyName(definition.getBinaryNodeName() + "_de");
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("test.jpg", "image/jpg");
        IOUtils.copy(new BinaryValue("test").getStream(), out);
        IOUtils.closeQuietly(out);

        // WHEN
        transformer.writeToItem(property);

        // THEN
        assertNotNull(((JcrNodeAdapter)rootItem).getChild(definition.getBinaryNodeName()+"_de"));
    }

    @Test
    public void writeToItemWithoutExisitngFile() throws RepositoryException, IOException {
        // GIVEN
        fileNode.remove();
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("test.jpg", "image/jpg");
        IOUtils.copy(new BinaryValue("test").getStream(), out);
        IOUtils.closeQuietly(out);

        // WHEN
        transformer.writeToItem(property);

        // THEN
        Item item = ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName());
        assertEquals("test.jpg", item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());
        assertEquals("image/jpg", item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue());
        assertEquals(4l, item.getItemProperty(FileProperties.SIZE).getValue());
        assertEquals("jpg", item.getItemProperty(FileProperties.EXTENSION).getValue());
    }

    @Test
    public void writeToItemWithExisitngFile() throws RepositoryException, IOException {
        // GIVEN
        // Init a stored file
        fileNode.setProperty(JcrConstants.JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(FileProperties.PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(FileProperties.PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);
        // Simulate a new upload
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("newTest.pgn", "image/pgn");
        IOUtils.copy(new BinaryValue("new test").getStream(), out);
        IOUtils.closeQuietly(out);

        // WHEN
        transformer.writeToItem(property);

        // THEN
        Item item = ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName());
        assertEquals("newTest.pgn", item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());
        assertEquals("image/pgn", item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue());
        assertEquals(8l, item.getItemProperty(FileProperties.SIZE).getValue());
        assertEquals("pgn", item.getItemProperty(FileProperties.EXTENSION).getValue());
    }

    @Test
    public void createPropertyFromItem() throws RepositoryException {
        // GIVEN
        fileNode.setProperty(JcrConstants.JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(FileProperties.PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(FileProperties.PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);
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
        fileNode.setProperty(JcrConstants.JCR_DATA, new BinaryValue("test"));
        fileNode.setProperty(FileProperties.PROPERTY_FILENAME, "test.jpg");
        fileNode.setProperty(FileProperties.PROPERTY_CONTENTTYPE, "image/jpg");
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);
        // Simulate a new upload
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("newTest.pgn", "image/pgn");
        IOUtils.copy(new BinaryValue("new test").getStream(), out);
        IOUtils.closeQuietly(out);

        // WHEN
        Item item = transformer.populateItem(property, ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName()));

        // THEN
        assertEquals("newTest.pgn", item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());
        assertEquals("image/pgn", item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue());
        assertEquals(8l, item.getItemProperty(FileProperties.SIZE).getValue());
        assertEquals("pgn", item.getItemProperty(FileProperties.EXTENSION).getValue());
    }

    @Test
    public void populateNewItem() throws RepositoryException, IOException {
        // GIVEN
        fileNode.remove();
        FileTransformer<UploadReceiver> transformer = new FileTransformer<UploadReceiver>(rootItem, definition, UploadReceiver.class);
        UploadReceiver property = transformer.readFromItem();
        OutputStream out = property.receiveUpload("test.jpg", "image/jpg");
        IOUtils.copy(new BinaryValue("test").getStream(), out);
        IOUtils.closeQuietly(out);
        transformer.writeToItem(property);
        // WHEN
        Item item = transformer.populateItem(property, ((JcrNodeAdapter) rootItem).getChild(definition.getBinaryNodeName()));

        // THEN
        assertEquals("test.jpg", item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue());
        assertEquals("image/jpg", item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue());
        assertEquals(4l, item.getItemProperty(FileProperties.SIZE).getValue());
        assertEquals("jpg", item.getItemProperty(FileProperties.EXTENSION).getValue());
    }
}
