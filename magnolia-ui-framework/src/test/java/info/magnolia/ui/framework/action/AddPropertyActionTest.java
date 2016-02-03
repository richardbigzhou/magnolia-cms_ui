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
import info.magnolia.cms.security.DummyUser;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.event.RecordingEventBus;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
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
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests covering execution of {@link info.magnolia.ui.framework.action.AddPropertyAction}.
 */
public class AddPropertyActionTest extends MgnlTestCase {

    private final static String WORKSPACE = "workspace";

    private final static String NODE_NAME = "johnNode";

    private final static String UNTITLED_PROPERTY_NAME = AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME;

    private final static String UNTITLED_PROPERTY_VALUE = "preset";

    private AddPropertyActionDefinition definition;

    private RecordingEventBus eventBus;

    private Session session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);

        definition = new AddPropertyActionDefinition();

        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(new DummyUser());
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);

        eventBus = new RecordingEventBus();
    }

    @Override
    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testCanAddPropertyToRootNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        long propertyCountBefore = root.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(definition, new JcrNodeAdapter(root), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertAddedNewProperty(root, propertyCountBefore, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME);
    }

    @Test
    public void testCanAddPropertyOnNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        long propertyCountBefore = node.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(definition, new JcrNodeAdapter(node), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertAddedNewProperty(node, propertyCountBefore, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME);
    }

    @Test
    public void testGivesPropertyUniqueName() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        node.setProperty(UNTITLED_PROPERTY_NAME, UNTITLED_PROPERTY_VALUE);
        long propertyCountBefore = node.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(definition, new JcrNodeAdapter(node), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertTrue(node.hasProperty(AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME));
        assertAddedNewProperty(node, propertyCountBefore, AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME + "0");
    }

    @Test
    public void testDoesNothingGivenProperty() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        node.setProperty(UNTITLED_PROPERTY_NAME, UNTITLED_PROPERTY_VALUE);
        long propertyCountBefore = node.getProperties().getSize();
        AddPropertyAction action = new AddPropertyAction(definition, new JcrPropertyAdapter(node.getProperty(UNTITLED_PROPERTY_NAME)), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertEquals(propertyCountBefore, node.getProperties().getSize());
        assertEquals(node.getProperty(UNTITLED_PROPERTY_NAME).getString(), UNTITLED_PROPERTY_VALUE);
        assertTrue(eventBus.isEmpty());
    }

    private void assertAddedNewProperty(Node root, long propertyCountBefore, String newPropertyName) throws RepositoryException {
        // no LUD wrapping on test
        assertEquals(propertyCountBefore + 1, root.getProperties().getSize());
        assertTrue(root.hasProperty(newPropertyName));
        // assertTrue(root.hasProperty(NodeTypes.LastModified.LAST_MODIFIED));
        // assertTrue(root.hasProperty(NodeTypes.LastModified.LAST_MODIFIED_BY));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(root)));
    }
}
