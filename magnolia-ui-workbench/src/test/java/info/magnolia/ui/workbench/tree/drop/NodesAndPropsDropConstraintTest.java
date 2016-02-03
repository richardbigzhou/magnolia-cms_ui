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

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link NodesAndPropsDropConstraint}.
 * Should allow drop of props or nodes in nodes, but not the vice-versa.
 */
public class NodesAndPropsDropConstraintTest extends RepositoryTestCase {

    private static final String TESTED_NODE_TYPE = NodeTypes.Content.NAME;

    private final NodesAndPropsDropConstraint dropConstraint = new NodesAndPropsDropConstraint();

    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession("config");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
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
        boolean isDroppable = dropConstraint.allowedAsChild(sourceJcrNodeAdapter, targetJcrNodeAdapter);

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
        boolean isDroppable = dropConstraint.allowedAsChild(sourceJcrNodeAdapter, targetJcrNodeAdapter);

        // THEN
        assertTrue("We expect to be able to drop a node into a node of same type", isDroppable);
    }

    /**
     * Test moving a folder into a node of content type.
     */
    @Test
    public void testDroppingFolderToProperty() throws Exception {
        // GIVEN
        final String sourceFolderNodeName = "folder2";
        final String tragetNodeName = "prop1";

        Node sourceNode = session.getRootNode().addNode(sourceFolderNodeName, NodeTypes.Folder.NAME);
        Node targetNode = session.getRootNode().addNode(tragetNodeName, TESTED_NODE_TYPE);
        Property prop1 = targetNode.setProperty("prop1", "bla");

        JcrNodeAdapter sourceJcrNodeAdapter = new JcrNodeAdapter(sourceNode);
        JcrPropertyAdapter targetJcrPropAdapter = new JcrPropertyAdapter(prop1);

        // WHEN
        boolean isDroppable = dropConstraint.allowedAsChild(sourceJcrNodeAdapter, targetJcrPropAdapter);

        // THEN
        assertFalse("We expect to not be able to drop a folder into a property", isDroppable);
    }

}
