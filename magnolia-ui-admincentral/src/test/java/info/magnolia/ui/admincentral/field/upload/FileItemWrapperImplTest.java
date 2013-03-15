/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.field.upload;

import static org.junit.Assert.assertEquals;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Date;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class FileItemWrapperImplTest {
    private Session session;

    @Before
    public void setup() {
        MockContext mockContext = new MockContext();
        MgnlContext.setInstance(mockContext);
        session = new MockSession("test");
        mockContext.addSession("test", session);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testPopulateJcrItemProperty() throws Exception{
        // GIVEN
        long size = 12;
        long width = 20;
        long height = 39;
        Node node = session.getRootNode().addNode("mock");

        JcrItemNodeAdapter parentAdapter = new JcrNodeAdapter((node.getParent()));
        JcrItemNodeAdapter adapter = new JcrNodeAdapter(node);
        parentAdapter.addChild(adapter);

        adapter.addItemProperty(FileProperties.PROPERTY_FILENAME, new DefaultProperty(FileProperties.PROPERTY_FILENAME, String.class, "myFile.git"));
        adapter.addItemProperty(FileProperties.PROPERTY_CONTENTTYPE, new DefaultProperty(FileProperties.PROPERTY_CONTENTTYPE, String.class, "image.gif"));

        adapter.addItemProperty(FileProperties.PROPERTY_LASTMODIFIED, new DefaultProperty(FileProperties.PROPERTY_LASTMODIFIED, Date.class, new Date()));
        adapter.addItemProperty(JcrConstants.JCR_DATA, new DefaultProperty(JcrConstants.JCR_DATA, Binary.class, "Does not matter".getBytes()));
        adapter.addItemProperty(FileProperties.PROPERTY_SIZE, new DefaultProperty(FileProperties.PROPERTY_SIZE, Long.class, size));
        adapter.addItemProperty(FileProperties.PROPERTY_HEIGHT, new DefaultProperty(FileProperties.PROPERTY_HEIGHT, Long.class, height));
        adapter.addItemProperty(FileProperties.PROPERTY_WIDTH, new DefaultProperty(FileProperties.PROPERTY_WIDTH, Long.class, width));
        adapter.addItemProperty(FileProperties.PROPERTY_EXTENSION, new DefaultProperty(FileProperties.PROPERTY_EXTENSION, String.class, "gif"));

        FileItemWrapperImpl wrapper = new FileItemWrapperImpl(adapter);

        // WHEN
        wrapper.populateJcrItemProperty();

        // THEN
        assertEquals(size, wrapper.getJcrItem().getItemProperty(FileProperties.PROPERTY_SIZE).getValue());
        assertEquals(width, wrapper.getJcrItem().getItemProperty(FileProperties.PROPERTY_WIDTH).getValue());
        assertEquals(height, wrapper.getJcrItem().getItemProperty(FileProperties.PROPERTY_HEIGHT).getValue());
    }
}
