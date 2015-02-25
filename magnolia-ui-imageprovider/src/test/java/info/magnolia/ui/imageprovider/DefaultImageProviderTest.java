/**
 * This file Copyright (c) 2003-2015 Magnolia International
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
package info.magnolia.ui.imageprovider;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.ByteArrayInputStream;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.net.MediaType;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.StreamResource;

/**
 * Tests.
 */
public class DefaultImageProviderTest {
    protected String workspaceName = "test";
    protected MockSession session;
    private DefaultImageProvider imageProvider;

    private final String IMAGE_NODE_NAME = "originalImage";
    private ContentConnector contentConnector;

    @Before
    public void setUp() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        MockWebContext webCtx = new MockWebContext();
        session = new MockSession(workspaceName);
        webCtx.addSession(workspaceName, session);
        webCtx.setContextPath("/foo");
        MgnlContext.setInstance(webCtx);

        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName(IMAGE_NODE_NAME);

        contentConnector = mock(ContentConnector.class);
        imageProvider = new DefaultImageProvider(cipd, contentConnector);
    }


    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        session = null;
        imageProvider = null;
    }

    @Test
    public void testGetNonExistingParentNodeImagePath() throws Exception {
        // GIVEN - see setUp

        // WHEN
        final String result = imageProvider.getThumbnailPath(null);

        // THEN
        assertNull(result);
    }

    @Test
    public void testGetThumbnailPath() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME, MediaType.GIF);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();
        final JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(contactNode);
        Object itemId = JcrItemUtil.getItemId(contactNode);
        doReturn(nodeAdapter).when(contentConnector).getItem(itemId);

        // WHEN
        final String result = imageProvider.getThumbnailPath(itemId);

        // THEN
        assertEquals("/foo/.imaging/thumbnail/test/" + imageNodeUuid + "/MaxMustermann.png", result);
    }

    @Test
    public void testGetThumbnailPathWithoutFileName() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME, MediaType.GIF);
        contactNode.getNode(IMAGE_NODE_NAME).getProperty("fileName").remove();
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();
        final JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(contactNode);
        Object itemId = JcrItemUtil.getItemId(contactNode);
        doReturn(nodeAdapter).when(contentConnector).getItem(itemId);

        // WHEN
        final String result = imageProvider.getThumbnailPath(itemId);

        // THEN
        assertEquals("/foo/.imaging/thumbnail/test/" + imageNodeUuid + "/myNode.png", result);
    }

    @Test
    public void testGetPortraitFromNonDefaultOriginalImageNodeName() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("contact1", IMAGE_NODE_NAME, MediaType.GIF);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();
        final JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(contactNode);
        Object itemId = JcrItemUtil.getItemId(contactNode);
        doReturn(nodeAdapter).when(contentConnector).getItem(itemId);

        // WHEN
        final String result = imageProvider.getPortraitPath(itemId);

        // THEN
        assertEquals("/foo/.imaging/portrait/test/" + imageNodeUuid + "/MaxMustermann.png", result);
    }

    @Test
    public void testGetThumbnailResourceByPath() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME, MediaType.GIF);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();
        final JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(contactNode);
        Object itemId = JcrItemUtil.getItemId(contactNode);
        doReturn(nodeAdapter).when(contentConnector).getItem(itemId);

        // WHEN
        Object resource = imageProvider.getThumbnailResource(itemId, ImageProvider.THUMBNAIL_GENERATOR);

        // THEN
        assertNotNull(resource);
        assertTrue(resource instanceof ExternalResource);
        assertEquals("/foo/.imaging/thumbnail/test/" + imageNodeUuid + "/MaxMustermann.png", ((ExternalResource) resource).getURL());
    }

    @Test
    public void testGetThumbnailSVGResource() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME, MediaType.SVG_UTF_8);
        final JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(contactNode);
        Object itemId = JcrItemUtil.getItemId(contactNode);
        doReturn(nodeAdapter).when(contentConnector).getItem(itemId);

        // WHEN
        Object resource = imageProvider.getThumbnailResource(itemId, ImageProvider.THUMBNAIL_GENERATOR);

        // THEN
        assertNotNull(resource);
        assertTrue(resource instanceof StreamResource);
    }


    private Node createMainImageNode(String mainNodeName, String imageNodeName, MediaType type) throws Exception {
        String rootPath = "/" + mainNodeName + "/" + imageNodeName;
        final PropertiesImportExport pie = new PropertiesImportExport();

        final Node root = session.getRootNode();

        final String content = rootPath
                + ".@type=mgnl:resource\n"
                + rootPath
                + ".fileName=MaxMustermann\n"
                + rootPath
                + ".extension="+ type.subtype() +"\n"
                + rootPath
                + ".jcr\\:data=binary:R0lGODlhUABrAPc\n"
                + rootPath + ".jcr\\:mimeType=" + type.toString() + "\n"
                + rootPath + ".size=1234\n"
                + rootPath + ".mgnl\\:lastModified=2009-04-07T21:54:15.910+01:00\n";

        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));
        return root.getNode(mainNodeName);
    }

}
