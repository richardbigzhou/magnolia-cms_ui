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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

public class JcrNewNodeAdapterTest {


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
    public void testGetItemProperty_Modified() throws Exception {
        // GIVEN
        //Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyName";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);
        Property propertyInitial = adapter.getItemProperty(id);
        propertyInitial.setValue("new");

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
    }

    @Test
    public void testGetNode() throws Exception {
        // GIVEN
        //Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode,nodeType);
        adapter.getItemProperty("notModify");
        adapter.getItemProperty("notModifyRemoved");
        adapter.removeItemProperty("notModifyRemoved");
        Property propertyModified = adapter.getItemProperty("modify");
        propertyModified.setValue("newModify");
        Property propertyModifiedRemoved = adapter.getItemProperty("modifyRemoved");
        propertyModifiedRemoved.setValue("newModifyRemoved");
        adapter.removeItemProperty("modifyRemoved");

        // WHEN
        Node res = adapter.getNode();

        // THEN
        assertNotNull(res);
        assertSame(res, parentNode.getNode(res.getName()));
        assertEquals(false, res.hasProperty("notModify"));
        assertEquals(false, res.hasProperty("notModifyRemoved"));
        assertEquals(true, res.hasProperty("modify"));
        assertEquals("newModify", res.getProperty("modify").getString());
        assertEquals(false, res.hasProperty("modifyRemoved"));
        assertEquals(nodeType, res.getPrimaryNodeType().getName());
    }


    @Test(expected=IllegalAccessError.class)
    public void testGetNode_Twice() throws Exception {
        // GIVEN
        //Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode,nodeType);
        Property propertyModified = adapter.getItemProperty("modify");
        propertyModified.setValue("newModify");
        adapter.getNode();

        // WHEN no duplicate call
        adapter.getNode();

        // THEN

    }

}

