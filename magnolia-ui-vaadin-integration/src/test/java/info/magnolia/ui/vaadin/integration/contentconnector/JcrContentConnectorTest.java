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
package info.magnolia.ui.vaadin.integration.contentconnector;

import static org.junit.Assert.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link JcrContentConnector}.
 */
public class JcrContentConnectorTest extends MgnlTestCase {

    private static final String WORKSPACE = "testWorkspace";

    private ConfiguredJcrContentConnectorDefinition configuredJcrContentConnectorDefinition;
    private MockSession session;
    private JcrContentConnector jcrContentConnector;
    private Node rootNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = new MockSession(WORKSPACE);
        MockUtil.setSessionAndHierarchyManager(session);
        rootNode = session.getRootNode();
        jcrContentConnector = createContentConnector(null);
    }

    /**
     * Is testing JcrContentConnector#getItemUrlFragment.
     */
    @Test
    public void testGetItemUrlFragment() throws Exception {
        // GIVEN
        String nodePath = "/a/b/c";
        Node testNode = NodeUtil.createPath(rootNode, nodePath, NodeTypes.Content.NAME);
        JcrItemId itemId = new JcrItemId(testNode.getIdentifier(), WORKSPACE);
        String newNodeName = "test";
        JcrNewNodeItemId newNodeItemId = new JcrNewNodeItemId(itemId.getUuid(), itemId.getWorkspace(), NodeTypes.ContentNode.NAME, newNodeName);

        // THEN
        String urlFragment = jcrContentConnector.getItemUrlFragment(itemId);
        assertEquals(nodePath, urlFragment);

        String newNodeUrlFragment = jcrContentConnector.getItemUrlFragment(newNodeItemId);
        assertEquals(newNodeUrlFragment, String.format("%s/%s", urlFragment, newNodeName));
    }

    /**
     * Is testing JcrContentConnector#getItemUrlFragment, too;
     * this time testing a jcrConnector which has a path (not "/").
     */
    @Test
    public void testGetItemUrlFragmentWithPath() throws Exception {
        // GIVEN
        String basePath = "/chms-path";
        Node baseNode = NodeUtil.createPath(rootNode, basePath, NodeTypes.Content.NAME);
        jcrContentConnector = createContentConnector(basePath);

        String nodePath = "/a/b/c";
        Node testNode = NodeUtil.createPath(baseNode, nodePath, NodeTypes.Content.NAME);
        JcrItemId itemId = new JcrItemId(testNode.getIdentifier(), WORKSPACE);

        // THEN
        String urlFragment = jcrContentConnector.getItemUrlFragment(itemId);
        assertEquals(nodePath, urlFragment);
    }


    /**
     * This method tests JcrContentConnector#getItemIdByUrlFragment.
     */
    @Test
    public void testGetItemIdByUrlFragment() throws Exception {
        // GIVEN
        String urlFragment = "/x/y/z";
        // node should exist for the url-fragment
        Node node = NodeUtil.createPath(session.getRootNode(), urlFragment, NodeTypes.Content.NAME);

        // THEN
        JcrItemId itemId = jcrContentConnector.getItemIdByUrlFragment(urlFragment);
        assertEquals(itemId.getUuid(), node.getIdentifier());
    }

    /**
     * This method tests JcrContentConnector#getItemIdByUrlFragment, too;
     * this time testing an urlFragment for a path where no nod exists.
     */
    @Test
    public void testGetItemIdByUrlFragmentWithMissingNode() throws Exception {
        // GIVEN
        String urlFragment = "/x/y/z";

        // THEN
        JcrItemId itemId = jcrContentConnector.getItemIdByUrlFragment(urlFragment);
        assertNull(itemId);
    }

    /**
     * This method tests JcrContentConnector#getItemIdByUrlFragment, too;
     * this time testing an urlFragment for a jcrConnector which has a path (not "/").
     */
    @Test
    public void testGetItemIdByUrlFragmentWithPath() throws Exception {
        // GIVEN
        //
        String basePath = "/chms-path";
        Node baseNode = NodeUtil.createPath(rootNode, basePath, NodeTypes.Content.NAME);
        jcrContentConnector = createContentConnector(basePath);

        String urlFragment = "/x/y/z";
        // node should exist for the url-fragment
        Node node = NodeUtil.createPath(baseNode, urlFragment, NodeTypes.Content.NAME);

        // THEN
        JcrItemId itemId = jcrContentConnector.getItemIdByUrlFragment(urlFragment);
        assertEquals(itemId.getUuid(), node.getIdentifier());
    }

    @Test
    public void getItemIdByUrlFragmentDoesNotFailOnNPEWhenNodeIsMissing() {
        // GIVEN
        String urlFragment = "/x/y/z@property";

        // WHEN
        JcrItemId itemId = jcrContentConnector.getItemIdByUrlFragment(urlFragment);

        // THEN - do not fail on NPE
    }

    /**
     * This method tests JcrContentConnector#getItem.
     */
    @Test
    public void testGetItem() throws Exception {
        // GIVEN
        String nodePath = "/this/is-the/nodename";
        NodeUtil.createPath(session.getRootNode(), nodePath, NodeTypes.Content.NAME);
        JcrItemId itemId = JcrItemUtil.getItemId(WORKSPACE, nodePath);

        // THEN
        JcrItemAdapter itemAdapter = jcrContentConnector.getItem(itemId);
        assertNotNull(itemAdapter);
        assertEquals("nodename", itemAdapter.getJcrItem().getName());
        assertEquals(WORKSPACE, itemAdapter.getWorkspace());
    }

    /**
     * This method tests JcrContentConnector#getItemId.
     */
    @Test
    public void testGetItemId() throws Exception {
        // GIVEN
        Node node = NodeUtil.createPath(session.getRootNode(), "/r/s/t", NodeTypes.Content.NAME);
        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        // THEN
        Object itemId = jcrContentConnector.getItemId(nodeAdapter);
        assertNotNull(itemId);
        assertTrue(itemId instanceof JcrItemId);
        assertEquals(node.getIdentifier(), ((JcrItemId) itemId).getUuid());
        assertEquals(WORKSPACE, ((JcrItemId) itemId).getWorkspace());
    }

    /**
     * This method tests JcrContentConnector#getDefaultItemId.
     */
    @Test
    public void testGetDefaultItemId() throws Exception {
        // GIVEN
        Node rootNode = session.getRootNode();

        // THEN
        Object defaultItemId = jcrContentConnector.getDefaultItemId();
        assertNotNull(defaultItemId);
        assertEquals(rootNode.getIdentifier(), ((JcrItemId) defaultItemId).getUuid());
    }

    /**
     * This method tests JcrContentConnector#getDefaultItemId;
     * this time testing a jcrConnector which has a path (not "/").
     */
    @Test
    public void testGetDefaultItemIdWithPath() throws Exception {
        // GIVEN
        String basePath = "/chms-path";
        Node baseNode = NodeUtil.createPath(rootNode, basePath, NodeTypes.Content.NAME);
        jcrContentConnector = createContentConnector(basePath);

        // THEN
        Object defaultItemId = jcrContentConnector.getDefaultItemId();
        assertNotNull(defaultItemId);
        assertEquals(baseNode.getIdentifier(), ((JcrItemId) defaultItemId).getUuid());
    }

    /**
     * This method tests JcrContentConnector#canHandleItem.
     */
    @Test
    public void testCanHandleItem() throws Exception {
        // GIVEN
        String anotherWorkspace = "anotherWorkspace";
        MockSession anotherSession = new MockSession(anotherWorkspace);
        MockUtil.setSessionAndHierarchyManager(anotherSession);

        String basePath = "/chms-path";
        String nodePath = "/a/b/c";
        Node baseNode = NodeUtil.createPath(rootNode, basePath, NodeTypes.Content.NAME);
        jcrContentConnector = createContentConnector(basePath);

        // node within the path of the jcrContentConnector
        Node node = NodeUtil.createPath(baseNode, nodePath, NodeTypes.Content.NAME);
        JcrItemId itemId = JcrItemUtil.getItemId(node);

        // node within the same session like the jcrContentConnector but outside its base-path
        String anotherNodePath = "/yet/another/node";
        Node nodeOutsidePath = NodeUtil.createPath(rootNode, anotherNodePath, NodeTypes.Content.NAME);
        JcrItemId itemIdOutsidePath = JcrItemUtil.getItemId(nodeOutsidePath);

        // node from another session
        Node nodeFromAnotherSession = NodeUtil.createPath(anotherSession.getRootNode(), nodePath, NodeTypes.Content.NAME);
        JcrItemId itemIdFromAnotherSession = JcrItemUtil.getItemId(nodeFromAnotherSession);


        // THEN
        assertTrue(jcrContentConnector.canHandleItem(itemId));
        /*
         * one may expect that itemIdOutsidePath cannot be handled, but it can.
         * JcrContentConnector#canHandleItem is kind of light-weight-implementation which doesn't need jcr-querying,
         */
        assertTrue(jcrContentConnector.canHandleItem(itemIdOutsidePath));
        assertFalse(jcrContentConnector.canHandleItem(itemIdFromAnotherSession));

    }


    private JcrContentConnector createContentConnector(String path) {
        configuredJcrContentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        if (StringUtils.isNotBlank(path)) {
            configuredJcrContentConnectorDefinition.setRootPath(path);
        }
        configuredJcrContentConnectorDefinition.setWorkspace(WORKSPACE);

        ComponentProvider componentProvider = new MockComponentProvider();
        return new JcrContentConnector(null, configuredJcrContentConnectorDefinition, componentProvider);
    }


}
