/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.ui.model.thumbnail;


import static org.junit.Assert.assertEquals;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class AbstractThumbnailProviderTest {
    protected String workspaceName = "test";
    protected MockSession session;

    protected static final class DummyThumbnailProvider extends AbstractThumbnailProvider {
        private static final BufferedImage dummyImage = new BufferedImage(2, 2,  BufferedImage.TYPE_INT_RGB);

        @Override
        protected BufferedImage createThumbnail(Image image, String format, int width, int height, float quality) throws IOException {
            return dummyImage;
        }

    }

    @Before
    public void setUp() throws Exception {
        MockWebContext webCtx = new MockWebContext();
        session = new MockSession(workspaceName);
        webCtx.addSession(workspaceName, session);
        webCtx.setContextPath("/foo");
        MgnlContext.setInstance(webCtx);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        session = null;
    }

    @Test
    public void testGetThumbnailWhenNotYetExisting() throws Exception {
        // GIVEN
        Components.setComponentProvider(new MockComponentProvider());
        ComponentsTestUtil.setImplementation(LinkTransformerManager.class, LinkTransformerManager.class);
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);

        final PropertiesImportExport pie = new PropertiesImportExport();

        final Node root = session.getRootNode();

        final String content =
                "/myNode/originalImage.@type=mgnl:resource\n" +
                        "/myNode/originalImage.fileName=MaxMustermann\n" +
                        "/myNode/originalImage.extension=gif\n" +
                        "/myNode/originalImage.jcr\\:data=binary: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\n" +
                        "/myNode/originalImage.jcr\\:mimeType=image/gif\n" +
                        "/myNode/originalImage.size=1234";

        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));

        AbstractThumbnailProvider provider = new DummyThumbnailProvider();
        provider.setFormat("jpg");
        provider.setQuality(0.75f);

        // WHEN
        final Node contactNode = root.getNode("myNode");
        final String result = provider.getPath(contactNode.getIdentifier(), workspaceName, 30, 30);

        // THEN
        assertEquals("/myNode/thumbnail/MaxMustermann.jpg", result.toString());
    }

    @Test(expected=PathNotFoundException.class)
    public void testGetThumbnailFailsIfNoSizeIsSet() throws Exception {
        // GIVEN
        Components.setComponentProvider(new MockComponentProvider());
        ComponentsTestUtil.setImplementation(LinkTransformerManager.class, LinkTransformerManager.class);
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);

        final PropertiesImportExport pie = new PropertiesImportExport();
        final MockSession session  = new MockSession("test");
        final Node root = session.getRootNode();

        //notice the missing size property here
        final String content =
                "/myNode/thumbnail.@type=mgnl:resource\n" +
                        "/myNode/thumbnail.fileName=MaxMustermann\n" +
                        "/myNode/thumbnail.extension=gif\n" +
                        "/myNode/thumbnail.jcr\\:data=binary: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\n" +
                        "/myNode/thumbnail.jcr\\:mimeType=image/gif";

        pie.createNodes(root, new ByteArrayInputStream(content.getBytes()));

        AbstractThumbnailProvider provider = new DummyThumbnailProvider();
        provider.setFormat("jpg");
        provider.setQuality(0.75f);

        // WHEN
        final Node contactNode = root.getNode("myNode");

        // THEN
        contactNode.getProperty("size");
    }
}
