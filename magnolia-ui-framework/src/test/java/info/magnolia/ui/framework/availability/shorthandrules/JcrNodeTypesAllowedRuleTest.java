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
package info.magnolia.ui.framework.availability.shorthandrules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeItemId;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link JcrNodeTypesAllowedRule}.
 */
public class JcrNodeTypesAllowedRuleTest {

    private JcrNodeTypesAllowedRule rule;

    @Before
    public void setUp() throws Exception {
        this.rule = new JcrNodeTypesAllowedRule();
    }

    @Test
    public void testNodeTypeForNewItemId() throws Exception {
        // GIVEN
        final String nodeType = NodeTypes.Area.NAME;
        rule.setNodeTypes(new LinkedList() {{
            add(nodeType);
        }});

        JcrNewNodeItemId itemId = mock(JcrNewNodeItemId.class);
        when(itemId.getPrimaryNodeType()).thenReturn(nodeType);

        // WHEN
        boolean result = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(String.format("Expected new itemId to be of type '%s', but was '%s'.", NodeTypes.Area.NAME, itemId.getPrimaryNodeType()), result);
    }

    @Test
    public void testWrongNodeTypeForNewItemId() throws Exception {
        // GIVEN
        final String nodeType = NodeTypes.Area.NAME;
        rule.setNodeTypes(new LinkedList() {{
            add(NodeTypes.Component.NAME);
        }});

        JcrNewNodeItemId itemId = mock(JcrNewNodeItemId.class);
        when(itemId.getPrimaryNodeType()).thenReturn(nodeType);

        // WHEN
        boolean result = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(String.format("Expected new itemId to be of type '%s', but was '%s'.", NodeTypes.Component.NAME, itemId.getPrimaryNodeType()), result);
    }

    @Test
    public void testItemIdIsNull() throws Exception {
        // GIVEN
        rule.setNodeTypes(new LinkedList() {{
            add(NodeTypes.ContentNode.NAME);
        }});

        JcrNewNodeItemId itemId = null;

        // WHEN
        boolean result = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse("Rule should return false in case the itemId is null.", result);
    }

    @Test
    public void testItemIdIsNullAndNodeTypeEmpty() throws Exception {
        // GIVEN
        rule.setNodeTypes(new LinkedList());

        JcrNewNodeItemId itemId = null;

        // WHEN
        boolean result = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse("Rule should return false in case the itemId is null.", result);
    }
}
