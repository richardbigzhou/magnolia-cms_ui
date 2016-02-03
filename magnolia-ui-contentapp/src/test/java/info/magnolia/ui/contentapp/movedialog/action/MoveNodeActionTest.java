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
package info.magnolia.ui.contentapp.movedialog.action;

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.not;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.AbstractActionExecutor;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.tree.MoveLocation;
import info.magnolia.ui.workbench.tree.drop.TreeViewDropHandler;

import java.util.Arrays;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Test for {@link MoveNodeAction}.
 */
public class MoveNodeActionTest extends RepositoryTestCase {

    private static final String MOVE_ACTION_NAME = "move";
    private static final String NODE_NAME_1 = "node1";
    private static final String NODE_NAME_2 = "node2";
    private static final String NODE_NAME_3 = "node3";

    private ComponentProvider provider = mock(ComponentProvider.class);
    private EventBus adminCentralEventBusMock = mock(EventBus.class);

    private MoveNodeActionDefinition definition = new MoveNodeActionDefinition();
    private JcrNodeAdapter node1;
    private JcrNodeAdapter node2;
    private JcrNodeAdapter node3;

    private MoveNodeAction action;

    private MoveActionCallback callback = new MoveActionCallback() {
        @Override
        public void onMoveCancelled() {
        }

        @Override
        public void onMovePerformed(Item newHost, MoveLocation moveLocation) {
        }
    };

    private ActionExecutor executor = new AbstractActionExecutor(provider) {
        @Override
        public ActionDefinition getActionDefinition(String actionName) {
            return definition;
        }
    };

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        node1 = new JcrNodeAdapter(session.getRootNode().addNode(NODE_NAME_1));
        node2 = new JcrNodeAdapter(session.getRootNode().addNode(NODE_NAME_2));
        node3 = new JcrNodeAdapter(session.getRootNode().addNode(NODE_NAME_3));
        action = new MoveNodeAction(definition, Arrays.asList((JcrItemAdapter) node1, node2), node3, adminCentralEventBusMock, mock(UiContext.class), callback, new TreeViewDropHandler(null, null));
    }

    @Test
    public void testOrdinaryInside() throws Exception {
        //GIVEN
        definition.setMoveLocation(MoveLocation.INSIDE);
        when(provider.newInstance(any(Class.class), anyVararg())).thenReturn(action);

        //WHEN
        executor.execute(MOVE_ACTION_NAME);

        //THEN
        assertThat(node3.getJcrItem(), hasNode(NODE_NAME_1));
        assertThat(node3.getJcrItem(), hasNode(NODE_NAME_2));
        // verify actions are fired - once for source, once for target
        verify(adminCentralEventBusMock, times(2)).fireEvent(any(ContentChangedEvent.class));
    }

    @Test
    public void testMoveWithParentTryingToBeMovedToChild() throws Exception {
        //GIVEN
        NodeUtil.moveNode(node3.getJcrItem(), node2.getJcrItem());
        definition.setMoveLocation(MoveLocation.INSIDE);

        //WHEN
        action.execute();

        //THEN
        assertThat(node3.getJcrItem(), hasNode(NODE_NAME_1));
        assertThat(node3.getJcrItem(), not(hasNode(NODE_NAME_2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteOnItemWithBasicBasicCheckFail() throws Exception {
        //GIVEN
        definition.setMoveLocation(MoveLocation.INSIDE);

        //WHEN
        action.executeOnItem(node3);
    }

    @Test
    public void moveAfterPreservesOrder() throws Exception {
        // GIVEN
        definition.setMoveLocation(MoveLocation.AFTER);

        // WHEN
        action.execute();

        // THEN
        final Iterator<Node> iterator = NodeUtil.getNodes(MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getRootNode(), JcrConstants.NT_UNSTRUCTURED).iterator();
        assertThat(iterator.next(), nodeName(NODE_NAME_3));
        assertThat(iterator.next(), nodeName(NODE_NAME_1));
        assertThat(iterator.next(), nodeName(NODE_NAME_2));
    }
}
