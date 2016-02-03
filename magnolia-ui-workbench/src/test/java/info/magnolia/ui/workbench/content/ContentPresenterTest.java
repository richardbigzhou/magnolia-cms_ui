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
package info.magnolia.ui.workbench.content;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.AbstractContentPresenter;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.vaadin.data.Container;

/**
 * Tests for ContentPresenter.
 */
public class ContentPresenterTest {

    protected EventBus eventBus;

    protected JcrItemAdapter item;
    protected Set<String> items;

    protected static final String TEST_WORKSPACE_NAME = "test";

    private static final String TEST_WORKBENCHDEF_PATH = "/path/to/somewhere";

    private WorkbenchDefinition workbench;
    private Node workbenchRoot;
    private Node testNode;

    @Before
    public void setUp() throws Exception {
        MockSession session = new MockSession(TEST_WORKSPACE_NAME);
        workbenchRoot = NodeUtil.createPath(session.getRootNode(), TEST_WORKBENCHDEF_PATH.substring(1), NodeTypes.Content.NAME);
        testNode = NodeUtil.createPath(workbenchRoot, "testNode", NodeTypes.Content.NAME);

        this.workbench = mock(WorkbenchDefinition.class);
        when(workbench.getWorkspace()).thenReturn(TEST_WORKSPACE_NAME);
        when(workbench.getPath()).thenReturn(TEST_WORKBENCHDEF_PATH);
        eventBus = mock(EventBus.class);
        item = mock(JcrItemAdapter.class);
        when(item.getItemId()).thenReturn(testNode.getIdentifier());
        items = new HashSet<String>();
        items.add(item.getItemId());

        MockContext ctx = new MockContext();
        ctx.addSession(TEST_WORKSPACE_NAME, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testOnItemSelectionFiresOnEventBus() throws Exception {
        // GIVEN
        final AbstractContentPresenter presenter = new DummyContentPresenter();
        presenter.start(workbench, eventBus, "", null);
        // WHEN
        presenter.onItemSelection(items);

        // THEN
        ArgumentCaptor<SelectionChangedEvent> argument = ArgumentCaptor.forClass(SelectionChangedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(TEST_WORKSPACE_NAME, argument.getValue().getWorkspace());
        assertEquals(items.size(), argument.getValue().getItemIds().size());
        assertEquals(testNode.getIdentifier(), argument.getValue().getFirstItemId());
    }

    @Test
    public void testOnDoubleClickFiresOnEventBus() throws Exception {
        // GIVEN
        final AbstractContentPresenter presenter = new DummyContentPresenter();
        presenter.start(workbench, eventBus, "", null);

        // WHEN
        presenter.onDoubleClick(item);

        // THEN
        ArgumentCaptor<ItemDoubleClickedEvent> argument = ArgumentCaptor.forClass(ItemDoubleClickedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(TEST_WORKSPACE_NAME, argument.getValue().getWorkspace());
        assertEquals(testNode.getIdentifier(), argument.getValue().getPath());
    }

    @Test
    public void testOnItemSelectionWithNullItemSetSelectedPath() {
        // GIVEN
        AbstractContentPresenter presenter = new DummyContentPresenter();
        presenter.start(workbench, eventBus, "", null);
        items = new HashSet<String>();
        items.add(null);

        // WHEN
        presenter.onItemSelection(items);

        // THEN
        assertEquals(null, presenter.getSelectedItemId());
    }

    private static class DummyContentPresenter extends AbstractContentPresenter {
        private DummyContentPresenter() {
            super(null);
        }

        @Override
        public void refresh() {
        }

        @Override
        public Container getContainer() {
            return mock(Container.class);
        }

    }
}
