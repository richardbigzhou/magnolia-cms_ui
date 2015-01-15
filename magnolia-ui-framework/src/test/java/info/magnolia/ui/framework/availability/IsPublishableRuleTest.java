/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.framework.availability;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsPublishableRule}.
 */
public class IsPublishableRuleTest {

    private IsPublishableRule rule;
    private Node rootNode;

    @Before
    public void setUp() throws Exception {
        final Session webSiteSession = new MockSession("website");
        rootNode = webSiteSession.getRootNode();
        rule = new IsPublishableRule();

        final MockContext mockContext = new MockContext();
        mockContext.addSession("website", webSiteSession);
        MgnlContext.setInstance(mockContext);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void isAvailableForPublishing() throws RepositoryException {
        // GIVEN
        final Node publishedNode = rootNode.addNode("foo", NodeTypes.Page.NAME);
        // activation status is true also for modified pages
        publishedNode.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, true);

        final Node unpublishedNode = publishedNode.addNode("bar");

        Object itemId = JcrItemUtil.getItemId(unpublishedNode);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void itemUnderRootIsAvailableForPublishing() throws RepositoryException {
        // GIVEN
        final Node unpublishedNode = rootNode.addNode("foo", NodeTypes.Page.NAME);

        Object itemId = JcrItemUtil.getItemId(unpublishedNode);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void isNotAvailableForPublishing() throws RepositoryException {
        // GIVEN
        final Node unpublishedNode = rootNode.addNode("foo", NodeTypes.Page.NAME);
        unpublishedNode.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, false);

        final Node child = unpublishedNode.addNode("bar");

        Object itemId = JcrItemUtil.getItemId(child);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(isAvailable);
    }
}