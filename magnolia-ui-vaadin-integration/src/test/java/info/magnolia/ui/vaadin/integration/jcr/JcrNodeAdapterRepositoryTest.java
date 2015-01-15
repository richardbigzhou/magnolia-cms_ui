/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Test {@link JcrNodeAdapter} function who needs deeper Jcr functionality (like move).
 */
public class JcrNodeAdapterRepositoryTest extends RepositoryTestCase {

    private Node node;
    private final String nodeName = "parent";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:content\n" +
                        "/parent.propertyString=hello\n" +
                        "/parent/child.@type=mgnl:content\n" +
                        "/parent/child.propertyString=chield1\n";

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();

        node = session.getRootNode().getNode(nodeName);
    }

    @Test
    public void testGetNodeUpdatesNodeWithNewName() throws Exception {
        // GIVEN
        String id = ModelConstants.JCR_NAME;
        String value = "newParent";

        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        // Get the node name as property
        Property property = adapter.getItemProperty(id);
        assertEquals(nodeName, property.getValue().toString());
        // Change the property node name
        property.setValue(value);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        // should have a new node name and still all its properties
        assertEquals(value, res.getName());
        assertTrue(res.hasNode("child"));
        assertTrue(res.hasProperty("propertyString"));
        assertEquals("hello", res.getProperty("propertyString").getString());
    }

    @Test
    public void testGetNodeUpdatesChildNodeOrderExistingNode() throws Exception {
        // GIVEN
        // Create a rootItem with two child
        node.getNode("child").remove();
        node.addNode("child_2", node.getPrimaryNodeType().getName());
        JcrNodeAdapter adapterChild_2 = new JcrNodeAdapter(node.getNode("child_2"));
        adapterChild_2.addItemProperty("p1", DefaultPropertyUtil.newDefaultProperty("String", "1"));
        node.addNode("child_1", node.getPrimaryNodeType().getName());
        JcrNodeAdapter adapterChild_1 = new JcrNodeAdapter(node.getNode("child_1"));
        adapterChild_1.addItemProperty("p2", DefaultPropertyUtil.newDefaultProperty("String", "2"));
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        adapter.addChild(adapterChild_1);
        adapter.addChild(adapterChild_2);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        //
        NodeIterator iterator = res.getNodes();
        assertEquals(2, iterator.getSize());
        assertEquals("child_2", iterator.nextNode().getName());
        assertEquals("child_1", iterator.nextNode().getName());
    }

    @Test
    public void testGetNodeUpdatesChildNodeOrderExistingNodeAndNew() throws Exception {
        // GIVEN
        // Create a rootItem with one child and add a newJcrNodeAdapter
        node.getNode("child").remove();
        node.addNode("child_1", node.getPrimaryNodeType().getName());
        JcrNodeAdapter adapterChild_1 = new JcrNodeAdapter(node.getNode("child_1"));
        adapterChild_1.addItemProperty("p1", DefaultPropertyUtil.newDefaultProperty("String", "1"));

        JcrNodeAdapter adapterChild_2 = new JcrNewNodeAdapter(node, node.getPrimaryNodeType().getName(), "child_2");
        adapterChild_2.addItemProperty("p2", DefaultPropertyUtil.newDefaultProperty("String", "2"));
        JcrNodeAdapter adapterChild_3 = new JcrNewNodeAdapter(node, node.getPrimaryNodeType().getName(), "child_3");
        adapterChild_3.addItemProperty("p3", DefaultPropertyUtil.newDefaultProperty("String", "3"));

        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        adapter.addChild(adapterChild_1);
        adapter.addChild(adapterChild_2);
        adapter.addChild(adapterChild_3);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        NodeIterator iterator = res.getNodes();
        assertEquals(3, iterator.getSize());
        assertEquals("child_1", iterator.nextNode().getName());
        assertEquals("child_2", iterator.nextNode().getName());
        assertEquals("child_3", iterator.nextNode().getName());
    }

    @Test
    public void testGetNodeUpdatesChildNodeOrderNewNode() throws Exception {
        // GIVEN
        node.getNode("child").remove();
        JcrNodeAdapter adapterChild_1 = new JcrNewNodeAdapter(node, node.getPrimaryNodeType().getName(), "child_1");
        adapterChild_1.addItemProperty("p1", DefaultPropertyUtil.newDefaultProperty("String", "1"));
        JcrNodeAdapter adapterChild_2 = new JcrNewNodeAdapter(node, node.getPrimaryNodeType().getName(), "child_2");
        adapterChild_2.addItemProperty("p2", DefaultPropertyUtil.newDefaultProperty("String", "2"));
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        adapter.addChild(adapterChild_1);
        adapter.addChild(adapterChild_2);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        NodeIterator iterator = res.getNodes();
        assertEquals(2, iterator.getSize());
        assertEquals("child_1", iterator.nextNode().getName());
        assertEquals("child_2", iterator.nextNode().getName());
    }

    @Test
    public void testGetNode_NewProperty() throws Exception {
        // GIVEN
        String id = ModelConstants.JCR_NAME;
        String value = "new Parent { % !";

        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        // Get the node name as property
        Property property = adapter.getItemProperty(id);
        assertEquals(nodeName, property.getValue().toString());
        // Change the property node name
        property.setValue(value);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        // should have a new NodeName
        assertEquals(Path.getValidatedLabel(value), res.getName());
        assertEquals(true, res.hasProperty("propertyString"));
        assertEquals(true, res.hasNode("child"));
    }

    @Test
    public void testGetMultiValueProperty() throws Exception {
        // GIVEN
        String[] values = { "Art", "Dan", "Jen" };
        node.setProperty("multiple", values);

        // WHEN
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);

        // THEN
        Property property = adapter.getItemProperty("multiple");
        assertTrue(property.getValue() instanceof LinkedList);
    }

    @Test
    public void testSetMultiValueProperty() throws Exception {
        // GIVEN
        String[] values = { "Art", "Dan", "Jen" };
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        DefaultProperty<HashSet> property = new DefaultProperty<HashSet>(new HashSet<String>(Arrays.asList(values)));
        adapter.addItemProperty("multiple", property);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        assertTrue(res.getProperty("multiple").isMultiple());
    }

    @Test
    public void testChangeMultiValueProperty() throws Exception {
        // GIVEN
        String[] values = { "Art", "Dan", "Jen" };
        node.setProperty("multiple", values);
        JcrNodeAdapter adapter = new JcrNodeAdapter(node);
        Property property = adapter.getItemProperty("multiple");
        ((List) property.getValue()).add("Sun");
        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        assertTrue(res.getProperty("multiple").isMultiple());
        assertEquals(4, res.getProperty("multiple").getValues().length);
    }

}
