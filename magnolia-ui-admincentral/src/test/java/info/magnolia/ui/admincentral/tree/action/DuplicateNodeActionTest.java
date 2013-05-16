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
package info.magnolia.ui.admincentral.tree.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ActionAvailabilityDefinition;
import info.magnolia.ui.api.action.ConfiguredActionAvailabilityDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests covering execution of the {@link DuplicateNodeAction}.
 */
public class DuplicateNodeActionTest extends RepositoryTestCase {

    private Node nodeToCopy;

    private DuplicateNodeActionDefinition definition;

    private EventBus eventBus;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(ActionAvailabilityDefinition.class, ConfiguredActionAvailabilityDefinition.class);
        definition = new DuplicateNodeActionDefinition();

        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        nodeToCopy = webSiteSession.getRootNode().addNode("nodeToCopy", NodeTypes.Page.NAME);
        NodeTypes.Created.set(nodeToCopy);
        nodeToCopy.setProperty("property", "property");
        nodeToCopy.addNode("subNode", NodeTypes.Page.NAME);
        nodeToCopy.getNode("subNode").setProperty("property_subNode", "property_subNode");
        webSiteSession.save();
        eventBus = mock(EventBus.class);
    }


    @Test
    public void testExecute() throws Exception {
        // GIVEN
        DuplicateNodeAction action = new DuplicateNodeAction(definition, new JcrNodeAdapter(nodeToCopy), eventBus);

        // WHEN
        action.execute();

        // THEN
        Node rootNode = nodeToCopy.getParent();
        assertEquals(2, rootNode.getNodes("nodeToCopy*").getSize());
        assertTrue(rootNode.hasNode("nodeToCopy"));
        assertTrue(rootNode.hasNode("nodeToCopy/subNode"));
        assertTrue(rootNode.getNode("nodeToCopy/subNode").hasProperty("property_subNode"));
        assertTrue(rootNode.hasNode("nodeToCopy0"));
        assertEquals(NodeTypes.Page.NAME, rootNode.getNode("nodeToCopy0").getPrimaryNodeType().getName());
        assertTrue(rootNode.hasNode("nodeToCopy0/subNode"));
        assertTrue(rootNode.getNode("nodeToCopy0/subNode").hasProperty("property_subNode"));
    }

    @Test
    public void testExecuteUpdateDate() throws Exception {
        // GIVEN
        DuplicateNodeAction action = new DuplicateNodeAction(definition, new JcrNodeAdapter(nodeToCopy), eventBus);

        // WHEN
        action.execute();

        // THEN
        Node rootNode = nodeToCopy.getParent();
        Calendar init = rootNode.getNode("nodeToCopy").getProperty(NodeTypes.LastModified.LAST_MODIFIED).getDate();
        Calendar duplicate = rootNode.getNode("nodeToCopy0").getProperty(NodeTypes.LastModified.LAST_MODIFIED).getDate();
        assertTrue(init.before(duplicate));
    }

    @Test
    public void testExecuteUpdateMultipleCall() throws Exception {
        // GIVEN
        DuplicateNodeAction action = new DuplicateNodeAction(definition, new JcrNodeAdapter(nodeToCopy), eventBus);

        // WHEN
        action.execute();
        action.execute();
        action.execute();

        // THEN
        Node rootNode = nodeToCopy.getParent();
        assertEquals(4, rootNode.getNodes("nodeToCopy*").getSize());
        assertTrue(rootNode.hasNode("nodeToCopy"));
        assertTrue(rootNode.hasNode("nodeToCopy0"));
        assertTrue(rootNode.hasNode("nodeToCopy1"));
        assertTrue(rootNode.hasNode("nodeToCopy2"));
        assertTrue(rootNode.getNode("nodeToCopy2/subNode").hasProperty("property_subNode"));
    }
}
