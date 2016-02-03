/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.workbench.tree.drop;

import static org.junit.Assert.assertFalse;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BaseDropConstraint}.
 *
 * We're using {@link NodeTypes.Content#NAME} to demonstrate the {@link BaseDropConstraint}.
 * Normally, the NodeType would be specific to a ContentApp, e.g. contacts: <code>mgnl:contact</code>.
 */
public class BaseDropConstraintTest {

    private static final String WORKSPACE_NAME = "workspace";
    private static final String TESTED_NODE_TYPE = NodeTypes.Content.NAME;

    private final BaseDropConstraint baseDropConstraint = new BaseDropConstraint(TESTED_NODE_TYPE);

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(WORKSPACE_NAME);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE_NAME, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    /**
     * Test moving a node into a folder with same name sibling.
     */
    @Test
    public void testCreationOfSameNameSibling() throws Exception {
        // GIVEN
        final String sourceNodeName = "nodeName1";
        final String tragetNodeName = "nodeName1";
        final String tragetFolderNodeName = "folder1";

        Node sourceNode = session.getRootNode().addNode(sourceNodeName, TESTED_NODE_TYPE);
        Node targetFolderNode = session.getRootNode().addNode(tragetFolderNodeName, NodeTypes.Folder.NAME);
        targetFolderNode.addNode(tragetNodeName, TESTED_NODE_TYPE);

        JcrNodeAdapter sourceJcrNodeAdapter = new JcrNodeAdapter(sourceNode);
        JcrNodeAdapter targetJcrNodeAdapter = new JcrNodeAdapter(targetFolderNode);

        // WHEN
        boolean isDroppable = baseDropConstraint.allowedAsChild(sourceJcrNodeAdapter, targetJcrNodeAdapter);

        // THEN
        assertFalse("We expect to not be able to create a same name sibling", isDroppable);
    }

    /**
     * Test moving a node into a node of same node type.
     */
    @Test
    public void testDroppingNodeIntoNodeOfSameNodeType() throws Exception {
        // GIVEN
        final String sourceNodeName = "nodeName2";
        final String tragetNodeName = "nodeName3";

        Node sourceNode = session.getRootNode().addNode(sourceNodeName, TESTED_NODE_TYPE);
        Node targetNode = session.getRootNode().addNode(tragetNodeName, TESTED_NODE_TYPE);

        JcrNodeAdapter sourceJcrNodeAdapter = new JcrNodeAdapter(sourceNode);
        JcrNodeAdapter targetJcrNodeAdapter = new JcrNodeAdapter(targetNode);

        // WHEN
        boolean isDroppable = baseDropConstraint.allowedAsChild(sourceJcrNodeAdapter, targetJcrNodeAdapter);

        // THEN
        assertFalse("We expect to not be able to drop a node into a node of same type", isDroppable);
    }

    /**
     * Test moving a folder into a node of content type.
     */
    @Test
    public void testDroppingFolderToContentNode() throws Exception {
        // GIVEN
        final String sourceFolderNodeName = "folder2";
        final String tragetNodeName = "nodeName4";

        Node sourceNode = session.getRootNode().addNode(sourceFolderNodeName, NodeTypes.Folder.NAME);
        Node targetNode = session.getRootNode().addNode(tragetNodeName, TESTED_NODE_TYPE);

        JcrNodeAdapter sourceJcrNodeAdapter = new JcrNodeAdapter(sourceNode);
        JcrNodeAdapter targetJcrNodeAdapter = new JcrNodeAdapter(targetNode);

        // WHEN
        boolean isDroppable = baseDropConstraint.allowedAsChild(sourceJcrNodeAdapter, targetJcrNodeAdapter);

        // THEN
        assertFalse("We expect to not be able to drop a folder into a content node", isDroppable);
    }

}
