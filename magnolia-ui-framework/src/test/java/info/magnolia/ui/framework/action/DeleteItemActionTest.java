/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.DummyUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.RecordingEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link info.magnolia.ui.framework.action.DeleteItemAction}.
 */
public class DeleteItemActionTest extends MgnlTestCase {

    private RecordingEventBus eventBus;
    private Session session;
    private SimpleTranslator i18n;
    private AtomicReference<ConfirmationCallback> callback;
    private SubAppContext subAppContext;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        session = new MockSession("workspace");
        MockContext ctx = new MockContext();
        ctx.addSession("workspace", session);
        ctx.setUser(new DummyUser());
        MgnlContext.setInstance(ctx);

        eventBus = new RecordingEventBus();
        i18n = mock(SimpleTranslator.class);
        when(i18n.translate(anyString())).thenReturn("some translated message");

        callback = new AtomicReference<ConfirmationCallback>();
        subAppContext = mock(SubAppContext.class);
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                callback.set((ConfirmationCallback) arguments[6]);
                return null;
            }
        }).when(subAppContext).openConfirmation(any(MessageStyleTypeEnum.class), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(ConfirmationCallback.class));
    }

    @Test
    public void testCanDeleteNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node");
        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), nodeAdapter, eventBus, subAppContext, i18n);

        // WHEN
        action.execute();

        assertTrue(root.hasNode("node"));

        callback.get().onSuccess();

        // THEN
        assertFalse(root.hasNode("node"));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(root)));
    }

    @Test
    public void testCanDeleteProperty() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Property property = root.setProperty("property", "value");
        JcrPropertyAdapter propertyAdapter = new JcrPropertyAdapter(property);

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), propertyAdapter, eventBus, subAppContext, i18n);

        // WHEN
        action.execute();

        assertTrue(root.hasProperty("property"));

        callback.get().onSuccess();

        // THEN
        assertFalse(root.hasProperty("property"));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(root)));
    }

    @Test
    public void testDeleteMultipleItems() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node toKeep = root.addNode("keepMe");
        Node toDelete = root.addNode("deleteMe");
        Property propToDelete = toKeep.setProperty("propToDelete", "value");

        List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>(2);
        items.add(new JcrNodeAdapter(toDelete));
        items.add(new JcrPropertyAdapter(propToDelete));
        UiContext uiContext = mock(UiContext.class);

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), items, eventBus, subAppContext, i18n);

        // WHEN
        action.execute();

        assertTrue(root.hasNode("deleteMe"));
        assertTrue(root.hasNode("keepMe"));
        assertTrue(root.getNode("keepMe").hasProperty("propToDelete"));

        callback.get().onSuccess();

        // THEN
        assertFalse(root.hasNode("deleteMe"));
        assertTrue(root.hasNode("keepMe"));
        assertFalse(root.getNode("keepMe").hasProperty("propToDelete"));
    }

    @Test
    public void testRaiseErrorOnRootNodeDeletion() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node toKeep = root.addNode("keepMe");
        Node toDelete = root.addNode("deleteMe");
        Property propToDelete = toKeep.setProperty("propToDelete", "value");

        List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>(2);
        items.add(new JcrNodeAdapter(toDelete));
        items.add(new JcrNodeAdapter(root));
        items.add(new JcrPropertyAdapter(propToDelete));
        UiContext uiContext = mock(UiContext.class);

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), items, eventBus, subAppContext, i18n);

        // WHEN
        action.execute();

        assertTrue(root.hasNode("deleteMe"));
        assertTrue(root.hasNode("keepMe"));
        assertTrue(root.getNode("keepMe").hasProperty("propToDelete"));

        callback.get().onSuccess();

        // THEN
        assertFalse(root.hasNode("deleteMe"));
        assertTrue(root.hasNode("keepMe"));
        assertFalse(root.getNode("keepMe").hasProperty("propToDelete"));
    }
}
