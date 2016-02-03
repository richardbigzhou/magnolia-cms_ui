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

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.RecordingEventBus;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class DuplicateNodeActionTest extends RepositoryTestCase {

    private DuplicateNodeActionDefinition definition;

    private Node nodeToCopy;
    private RecordingEventBus eventBus;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
        definition = new DuplicateNodeActionDefinition();

        session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        nodeToCopy = session.getRootNode().addNode("nodeToCopy", NodeTypes.Page.NAME);
        NodeTypes.Created.set(nodeToCopy);
        nodeToCopy.setProperty("property", "property");
        nodeToCopy.addNode("subNode", NodeTypes.Page.NAME);
        nodeToCopy.getNode("subNode").setProperty("property_subNode", "property_subNode");
        session.save();

        eventBus = new RecordingEventBus();
    }

    @Test
    public void testDuplicatesNode() throws Exception {
        // GIVEN
        DuplicateNodeAction action = new DuplicateNodeAction(definition, new JcrNodeAdapter(nodeToCopy), eventBus);

        // WHEN
        action.execute();

        // THEN
        Node parent = nodeToCopy.getParent();
        assertEquals(2, parent.getNodes("nodeToCopy*").getSize());
        assertTrue(parent.hasNode("nodeToCopy"));
        assertTrue(parent.hasNode("nodeToCopy/subNode"));
        assertTrue(parent.getNode("nodeToCopy/subNode").hasProperty("property_subNode"));
        assertTrue(parent.hasNode("nodeToCopy0"));
        assertTrue(parent.hasNode("nodeToCopy0/subNode"));
        assertTrue(parent.getNode("nodeToCopy0/subNode").hasProperty("property_subNode"));
        assertEquals(NodeTypes.Page.NAME, parent.getNode("nodeToCopy0").getPrimaryNodeType().getName());
        assertTrue(parent.getNode("nodeToCopy0").hasProperty(NodeTypes.LastModified.LAST_MODIFIED));
        assertTrue(parent.getNode("nodeToCopy0").hasProperty(NodeTypes.LastModified.LAST_MODIFIED_BY));
        assertTrue(parent.getNode("nodeToCopy0").hasProperty(NodeTypes.Created.CREATED));
        assertTrue(parent.getNode("nodeToCopy0").hasProperty(NodeTypes.Created.CREATED_BY));
        assertFalse(NodeTypes.Activatable.isActivated(parent.getNode("nodeToCopy0")));

        Calendar init = parent.getNode("nodeToCopy").getProperty(NodeTypes.LastModified.LAST_MODIFIED).getDate();
        Calendar duplicate = parent.getNode("nodeToCopy0").getProperty(NodeTypes.LastModified.LAST_MODIFIED).getDate();
        assertTrue(init.before(duplicate));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(parent.getNode("nodeToCopy0"))));
    }

    @Test
    public void testDoesNothingGivenProperty() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("nodeName");
        node.setProperty("propertyName", "propertyValue");
        long nodeCountBefore = node.getNodes().getSize();
        DuplicateNodeAction action = new DuplicateNodeAction(definition, new JcrPropertyAdapter(node.getProperty("propertyName")), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertEquals(nodeCountBefore, node.getNodes().getSize());
        assertFalse(node.hasNode("nodeName0"));
        assertTrue(eventBus.isEmpty());
    }
}
