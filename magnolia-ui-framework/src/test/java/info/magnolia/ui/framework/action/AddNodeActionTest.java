/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.cms.security.DummyUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.RecordingEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests covering execution of {@link info.magnolia.ui.framework.action.AddNodeAction}.
 */
public class AddNodeActionTest extends MgnlTestCase {

    private final static String WORKSPACE = "workspace";

    private final static String NODE_NAME = "johnNode";

    private final static String NEW_NODE_NAME = AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME;

    private static AddNodeActionDefinition definition;

    private RecordingEventBus eventBus;

    private MockSession session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        definition = new AddNodeActionDefinition();
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(new DummyUser());
        MgnlContext.setInstance(ctx);

        eventBus = new RecordingEventBus();
    }

    @Override
    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testCanAddNodeToRootNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        long nodeCountBefore = root.getNodes().getSize();
        AddNodeAction action = new AddNodeAction(definition, new JcrNodeAdapter(root), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertAddedNewNode(root, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME, NodeTypes.Content.NAME, nodeCountBefore + 1);
    }

    @Test
    public void testCanAddChildNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        long nodeCountBefore = node.getNodes().getSize();
        AddNodeAction action = new AddNodeAction(definition, new JcrNodeAdapter(node), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertAddedNewNode(node, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME, NodeTypes.Content.NAME, nodeCountBefore + 1);
    }

    @Test
    public void testUsesConfiguredNodeType() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        long nodeCountBefore = node.getNodes().getSize();
        AddNodeActionDefinition definition = new AddNodeActionDefinition();
        definition.setNodeType(NodeTypes.ContentNode.NAME);
        AddNodeAction action = new AddNodeAction(definition, new JcrNodeAdapter(node), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertAddedNewNode(node, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME, NodeTypes.ContentNode.NAME, nodeCountBefore + 1);
    }

    @Test
    public void testGivesNodeUniqueName() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        node.addNode(NEW_NODE_NAME);
        long nodeCountBefore = node.getNodes().getSize();
        AddNodeAction action = new AddNodeAction(definition, new JcrNodeAdapter(node), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertTrue(node.hasNode(AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME));
        assertAddedNewNode(node, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME + "0", NodeTypes.Content.NAME, nodeCountBefore + 1);
    }

    @Test
    public void testDoesNothingGivenProperty() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        node.setProperty("propertyName", "propertyValue");
        long nodeCountBefore = node.getNodes().getSize();
        AddNodeAction action = new AddNodeAction(definition, new JcrPropertyAdapter(node.getProperty("propertyName")), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertEquals(nodeCountBefore, node.getNodes().getSize());
        assertFalse(node.hasNode(NEW_NODE_NAME));
        assertTrue(eventBus.isEmpty());
    }

    private void assertAddedNewNode(Node parent, String nodeName, String nodeType, long expectedNumberOfChildNodes) throws RepositoryException {
        assertEquals(expectedNumberOfChildNodes, parent.getNodes().getSize());
        assertTrue(parent.hasNode(nodeName));
        Node newNode = parent.getNode(nodeName);
        assertEquals(nodeType, newNode.getPrimaryNodeType().getName());
        assertTrue(newNode.hasProperty(NodeTypes.Created.CREATED));
        assertTrue(newNode.hasProperty(NodeTypes.Created.CREATED_BY));
        assertTrue(newNode.hasProperty(NodeTypes.LastModified.LAST_MODIFIED));
        assertTrue(newNode.hasProperty(NodeTypes.LastModified.LAST_MODIFIED_BY));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(newNode)));
    }
}
