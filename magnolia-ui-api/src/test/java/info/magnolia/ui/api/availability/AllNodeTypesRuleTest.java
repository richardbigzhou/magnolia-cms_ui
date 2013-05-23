/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.api.availability;

import static org.junit.Assert.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the AllNodeTypesRule.
 */
public class AllNodeTypesRuleTest {

    String[] nodeTypes = { NodeTypes.Content.NAME, NodeTypes.Activatable.NAME, NodeTypes.Deleted.NAME };
    AllNodeTypesRule rule;
    Node node;

    @Before
    public void setUp() throws Exception {
        rule = new AllNodeTypesRule();
        rule.setNodeTypes(Arrays.asList(nodeTypes));
        node = new MockNode("test", NodeTypes.Content.NAME);
    }

    @Test
    public void returnFalseOnNullItem() throws Exception {
        // GIVEN
        node = null;

        // THEN
        assertFalse("Rule must return false on empty item.", rule.isAvailable(node));
    }

    @Test
    public void returnFalseOnProperty() throws Exception {
        // GIVEN
        Property prop = node.getProperty("jcr:primaryType");

        // THEN
        assertFalse("Rule must return false on property.", rule.isAvailable(prop));
    }

    @Test
    public void returnFalseOnMissingMixin() throws Exception {
        // GIVEN
        // add just one required mixin
        node.addMixin(NodeTypes.Activatable.NAME);

        // THEN
        assertFalse("Rule must return false when a mixin type is missing.", rule.isAvailable(node));
    }

    @Test
    public void returnTrueOnAllTypesPresent() throws Exception {
        // GIVEN
        // add both required mixins
        node.addMixin(NodeTypes.Activatable.NAME);
        node.addMixin(NodeTypes.Deleted.NAME);

        // THEN
        assertTrue("Rule must return true when all node types are present.", rule.isAvailable(node));
    }

    @Test
    public void returnFalseOnDifferentPrimaryType() throws Exception {
        // GIVEN
        node = new MockNode("fail", NodeTypes.ContentNode.NAME);
        node.addMixin(NodeTypes.Activatable.NAME);
        node.addMixin(NodeTypes.Deleted.NAME);

        // THEN
        assertFalse("Rule must return false if primary node type differs.", rule.isAvailable(node));
    }

    public void returnTrueOnSubtype() throws Exception {
        // GIVEN
        // mgnl:page is a sub-type of mgnl:content
        node = new MockNode("fail", NodeTypes.Page.NAME);
        node.addMixin(NodeTypes.Activatable.NAME);
        node.addMixin(NodeTypes.Deleted.NAME);

        // THEN
        assertTrue("Rule should return true on sub-type of required type.", rule.isAvailable(node));
    }
}
