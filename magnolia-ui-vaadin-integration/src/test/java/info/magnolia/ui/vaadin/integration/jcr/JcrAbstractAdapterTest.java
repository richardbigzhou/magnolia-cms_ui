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
package info.magnolia.ui.vaadin.integration.jcr;

import static org.junit.Assert.assertEquals;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class JcrAbstractAdapterTest {

    private String worksapceName = "workspace";
    private MockSession session;


    @Before
    public void setUp() {
        session = new MockSession(worksapceName);
        MockContext ctx = new MockContext();
        ctx.addSession(worksapceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }




    @Test
    public void testSetCommonAttributes_Node() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);

        // WHEN
        AbstractAdapterTest adapter = new AbstractAdapterTest(testNode);

        // THEN
        assertEquals(true, adapter.isNode());
        assertEquals(worksapceName, adapter.getWorkspace());
        assertEquals(testNode.getIdentifier(), adapter.getNodeIdentifier());
        assertEquals(testNode.getPath(), adapter.getItemId());
        assertEquals(testNode.getPath(), ((Node)adapter.getJcrItem()).getPath());
    }


    @Test
    public void testSetCommonAttributes_Property() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        Property testProperty = testNode.setProperty(propertyName, propertyValue);

        // WHEN
        AbstractAdapterTest adapter = new AbstractAdapterTest(testProperty);

        // THEN
        assertEquals(false, adapter.isNode());
        assertEquals(worksapceName, adapter.getWorkspace());
        assertEquals(testNode.getIdentifier(), adapter.getNodeIdentifier());
        assertEquals(testNode.getPath()+"/propertyName", adapter.getItemId());
        assertEquals(testNode.getPath()+"/propertyName", ((Property)adapter.getJcrItem()).getPath());
    }


    /**
     * Dummy implementation of the Abstract class.
     */
    public class AbstractAdapterTest extends JcrAbstractAdapter {
        public AbstractAdapterTest(Item jcrItem) {
            super(jcrItem);
        }
        @Override
        public com.vaadin.data.Property getItemProperty(Object id) {
            return null;
        }
        @Override
        public Collection< ? > getItemPropertyIds() {
            return null;
        }
        @Override
        public boolean addItemProperty(Object id, com.vaadin.data.Property property) throws UnsupportedOperationException {
            return false;
        }
        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            return false;
        }
    }

}
