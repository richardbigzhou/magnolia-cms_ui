/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.admincentral.availability;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ConfigProtectedNodeRule}.
 */
public class ConfigProtectedNodeRuleTest {

    private ConfigProtectedNodeRule rule;
    private Node rootNode;

    @Before
    public void setUp() throws Exception {
        final Session configSession = new MockSession("config");
        rootNode = configSession.getRootNode();
        rule = new ConfigProtectedNodeRule();

        final MockContext mockContext = new MockContext();
        mockContext.addSession("config", configSession);
        MgnlContext.setInstance(mockContext);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testIsAvailableForItem() throws Exception {
        // GIVEN
        final Node lvl1Node = rootNode.addNode("lvl1", NodeTypes.ContentNode.NAME);
        final Node lvl2Node = rootNode.addNode("lvl1/lvl2", NodeTypes.ContentNode.NAME);
        final Node lvl3Node = rootNode.addNode("lvl1/lvl2/lvl3", NodeTypes.ContentNode.NAME);
        final Node lvl4Node = rootNode.addNode("lvl1/lvl2/lvl3/lvl4", NodeTypes.ContentNode.NAME);

        Object itemId1 = JcrItemUtil.getItemId(lvl1Node);
        Object itemId2 = JcrItemUtil.getItemId(lvl2Node);
        Object itemId3 = JcrItemUtil.getItemId(lvl3Node);
        Object itemId4 = JcrItemUtil.getItemId(lvl4Node);

        // WHEN
        boolean isAvailable1 = rule.isAvailableForItem(itemId1);
        boolean isAvailable2 = rule.isAvailableForItem(itemId2);
        boolean isAvailable3 = rule.isAvailableForItem(itemId3);
        boolean isAvailable4 = rule.isAvailableForItem(itemId4);

        // THEN
        assertFalse(isAvailable1);
        assertFalse(isAvailable2);
        assertTrue(isAvailable3);
        assertTrue(isAvailable4);
    }
}
