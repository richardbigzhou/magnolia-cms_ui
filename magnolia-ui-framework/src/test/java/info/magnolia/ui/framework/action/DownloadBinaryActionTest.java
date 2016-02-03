/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.action;

import static org.mockito.Mockito.*;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.server.DownloadStreamResource;

import java.io.InputStream;

import javax.jcr.Binary;
import javax.jcr.Node;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.ui.UI;

/**
 * Tests for {@link DownloadBinaryAction}.
 */
public class DownloadBinaryActionTest {

    private static String fileName = "char_blume.png";
    private DownloadBinaryAction<DownloadBinaryActionDefinition> action;
    private DownloadBinaryActionDefinition definition = new DownloadBinaryActionDefinition();
    private JcrItemAdapter item;
    private Page page;
    private InputStream inputStream;

    @Before
    public void setUp() throws Exception {
        inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        Node root = new MockNode("root");
        Node node = root.addNode(JcrConstants.JCR_CONTENT);
        node.setProperty("fileName", fileName);
        node.setProperty("extension", "png");
        Binary binary = new BinaryImpl(inputStream);
        node.setProperty(JcrConstants.JCR_DATA, binary);

        UI ui = mock(UI.class);
        page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);
        UI.setCurrent(ui);

        item = mock(JcrNodeAdapter.class);
        when(item.getJcrItem()).thenReturn(root);
    }

    @After
    public void tearDown() {
        IOUtils.closeQuietly(inputStream);
    }

    @Test
    public void testBinaryDownload() throws Exception {
        // GIVEN
        action = new DownloadBinaryAction<DownloadBinaryActionDefinition>(definition, item);

        // WHEN
        action.execute();

        // THEN
        verify(page).open(any(StreamResource.class), (String) isNull(), eq(false));
    }

    @Test
    public void testContentDispositionHeaderIsSetCorrectly() throws Exception {
        // GIVEN
        action = new DownloadBinaryAction<DownloadBinaryActionDefinition>(definition, item);
        DownloadStreamResource downloadStreamResource = action.getStreamResource(inputStream, fileName);
        String expectedContentDispositionHeaderValue = String.format("attachment; filename=\"%s\"", fileName);

        VaadinRequest vaadinRequest = mock(VaadinRequest.class);
        VaadinResponse vaadinResponse = mock(VaadinResponse.class);

        // WHEN
        downloadStreamResource.getStream().writeResponse(vaadinRequest, vaadinResponse);

        // THEN
        verify(vaadinResponse).setHeader("Content-Disposition", expectedContentDispositionHeaderValue);
    }

}
