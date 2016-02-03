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
package info.magnolia.ui.framework.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.DummyUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.RecordingEventBus;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

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

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        session = new MockSession("workspace");
        MockContext ctx = new MockContext();
        ctx.addSession("workspace", session);
        ctx.setUser(new DummyUser());
        MgnlContext.setInstance(ctx);

        eventBus = new RecordingEventBus();
    }

    @Test
    public void testRefusesToDeleteRootNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(root);

        SubAppContext subAppContext = mock(SubAppContext.class);

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), nodeAdapter, eventBus, subAppContext);

        // WHEN
        action.execute();

        // THEN
        verify(subAppContext, never()).openConfirmation(any(MessageStyleTypeEnum.class), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(ConfirmationCallback.class));
    }

    @Test
    public void testCanDeleteNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node");
        JcrNodeAdapter nodeAdapter = new JcrNodeAdapter(node);

        final AtomicReference<ConfirmationCallback> callback = new AtomicReference<ConfirmationCallback>();

        SubAppContext subAppContext = mock(SubAppContext.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                assertSame(MessageStyleTypeEnum.WARNING, arguments[0]);
                assertEquals("Do you really want to delete this item?", arguments[1]);
                assertEquals("This action can't be undone.", arguments[2]);
                assertEquals("Yes, Delete", arguments[3]);
                assertEquals("No", arguments[4]);
                assertTrue((Boolean) arguments[5]);
                callback.set((ConfirmationCallback) arguments[6]);
                return null;
            }
        }).when(subAppContext).openConfirmation(any(MessageStyleTypeEnum.class), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(ConfirmationCallback.class));

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), nodeAdapter, eventBus, subAppContext);

        // WHEN
        action.execute();

        assertTrue(root.hasNode("node"));

        callback.get().onSuccess();

        // THEN
        assertFalse(root.hasNode("node"));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(root)));
        verify(subAppContext, times(1)).openNotification(MessageStyleTypeEnum.INFO, true, "Item deleted.");
    }

    @Test
    public void testCanDeleteProperty() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Property property = root.setProperty("property", "value");
        JcrPropertyAdapter propertyAdapter = new JcrPropertyAdapter(property);

        final AtomicReference<ConfirmationCallback> callback = new AtomicReference<ConfirmationCallback>();

        SubAppContext subAppContext = mock(SubAppContext.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                assertSame(MessageStyleTypeEnum.WARNING, arguments[0]);
                assertEquals("Do you really want to delete this item?", arguments[1]);
                assertEquals("This action can't be undone.", arguments[2]);
                assertEquals("Yes, Delete", arguments[3]);
                assertEquals("No", arguments[4]);
                assertTrue((Boolean) arguments[5]);
                callback.set((ConfirmationCallback) arguments[6]);
                return null;
            }
        }).when(subAppContext).openConfirmation(any(MessageStyleTypeEnum.class), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(ConfirmationCallback.class));

        DeleteItemAction action = new DeleteItemAction(new DeleteItemActionDefinition(), propertyAdapter, eventBus, subAppContext);

        // WHEN
        action.execute();

        assertTrue(root.hasProperty("property"));

        callback.get().onSuccess();

        // THEN
        assertFalse(root.hasProperty("property"));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(root)));
        verify(subAppContext, times(1)).openNotification(MessageStyleTypeEnum.INFO, true, "Item deleted.");
    }
}
