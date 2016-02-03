/**
 * This file Copyright (c) 2003-2016 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.imageprovider.definition.ConfiguredImageProviderDefinition;

import java.io.ByteArrayInputStream;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ExternalResource;

/**
 * Tests.
 */
public class DefaultImageProviderTest {
    protected String workspaceName = "test";
    protected MockSession session;
    private DefaultImageProvider imageProvider;

    private final String IMAGE_NODE_NAME = "originalImage";

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
        imageProvider = new DefaultImageProvider(cipd);
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
        final String result = imageProvider.getThumbnailPath(workspaceName, null);

        // THEN
        assertNull(result);
    }

    @Test
    public void testGetThumbnailPath() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();

        // WHEN
        final String result = imageProvider.getThumbnailPath(workspaceName, contactNode.getPath());

        // THEN
        assertEquals("/foo/.imaging/thumbnail/test/" + imageNodeUuid + "/MaxMustermann.png", result);
    }

    @Test
    public void testGetPortraitPathByUuid() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();

        // WHEN
        final String result = imageProvider.getPortraitPathByIdentifier(workspaceName, contactNode.getIdentifier());

        // THEN
        assertEquals("/foo/.imaging/portrait/test/" + imageNodeUuid + "/MaxMustermann.png", result);
    }

    @Test
    public void testGetThumbnailPathWithoutFileName() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME);
        contactNode.getNode(IMAGE_NODE_NAME).getProperty("fileName").remove();
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();

        // WHEN
        final String result = imageProvider.getThumbnailPath(workspaceName, contactNode.getPath());

        // THEN
        assertEquals("/foo/.imaging/thumbnail/test/" + imageNodeUuid + "/myNode.png", result);
    }

    @Test
    public void testGetPortraitFromNonDefaultOriginalImageNodeName() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("contact1", IMAGE_NODE_NAME);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();

        // WHEN
        final String result = imageProvider.getPortraitPath(workspaceName, contactNode.getPath());

        // THEN
        assertEquals("/foo/.imaging/portrait/test/" + imageNodeUuid + "/MaxMustermann.png", result);
    }

    @Test
    public void testGetThumbnailResourceById() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();

        // WHEN
        Object resource = imageProvider.getThumbnailResourceById(workspaceName, contactNode.getIdentifier(), ImageProvider.PORTRAIT_GENERATOR);

        // THEN
        assertNotNull(resource);
        assertEquals(true, resource instanceof ExternalResource);
        assertEquals("/foo/.imaging/portrait/test/" + imageNodeUuid + "/MaxMustermann.png", ((ExternalResource) resource).getURL());
    }

    @Test
    public void testGetThumbnailResourceByPath() throws Exception {
        // GIVEN
        final Node contactNode = createMainImageNode("myNode", IMAGE_NODE_NAME);
        final String imageNodeUuid = contactNode.getNode(IMAGE_NODE_NAME).getIdentifier();

        // WHEN
        Object resource = imageProvider.getThumbnailResourceByPath(workspaceName, contactNode.getPath(), ImageProvider.THUMBNAIL_GENERATOR);

        // THEN
        assertNotNull(resource);
        assertEquals(true, resource instanceof ExternalResource);
        assertEquals("/foo/.imaging/thumbnail/test/" + imageNodeUuid + "/MaxMustermann.png", ((ExternalResource) resource).getURL());
    }


    private Node createMainImageNode(String mainNodeName, String imageNodeName) throws Exception {
        String rootPath = "/" + mainNodeName + "/" + imageNodeName;
        final PropertiesImportExport pie = new PropertiesImportExport();

        final Node root = session.getRootNode();

        final String content = rootPath
                + ".@type=mgnl:resource\n"
                + rootPath
                + ".fileName=MaxMustermann\n"
                + rootPath
                + ".extension=gif\n"
                + rootPath
                + ".jcr\\:data=binary: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\n"
                + rootPath + ".jcr\\:mimeType=image/gif\n" + rootPath + ".size=1234";

        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));
        return root.getNode(mainNodeName);
    }

}
