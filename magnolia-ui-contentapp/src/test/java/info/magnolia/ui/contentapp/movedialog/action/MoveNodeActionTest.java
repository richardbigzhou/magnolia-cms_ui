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

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;

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
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.framework.action.MoveLocation;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Arrays;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Test for {@link MoveNodeAction}.
 */
public class MoveNodeActionTest extends RepositoryTestCase {

    private static final String MOVE_ACTION_NAME = "move";

    private MoveNodeActionDefinition definition = new MoveNodeActionDefinition();

    private ComponentProvider provider = mock(ComponentProvider.class);

    private UiContext uiContext = mock(UiContext.class);

    private EventBus adminCentralEventBusMock = mock(EventBus.class);

    private final String nodeName1 = "node1";

    private final String nodeName2 = "node2";

    private final String nodeName3 = "node3";

    private JcrNodeAdapter node3;

    private JcrNodeAdapter node1;

    private JcrNodeAdapter node2;

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

    @Test
    public void testOrdinaryInside() throws Exception {
        //GIVEN
        initNodes();
        MoveNodeAction action = createAction();
        definition.setMoveLocation(MoveLocation.INSIDE);

        //WHEN
        when(provider.newInstance(any(Class.class), anyVararg())).thenReturn(action);
        executor.execute(MOVE_ACTION_NAME);

        //THEN
        assert(node3.getJcrItem().hasNode(node1.getJcrItem().getName()));
        assert(node3.getJcrItem().hasNode(node2.getJcrItem().getName()));

    }

    @Test
    public void testMoveWithParentTryingToBeMovedToChild() throws Exception {
        //GIVEN
        initNodes();
        NodeUtil.moveNode(node3.getJcrItem(), node2.getJcrItem());
        MoveNodeAction action = createAction();

        //WHEN
        definition.setMoveLocation(MoveLocation.INSIDE);
        action.execute();

        //THEN
        assert(node3.getJcrItem().hasNode(node1.getJcrItem().getName()));
        assert(!node3.getJcrItem().hasNode(node2.getJcrItem().getName()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteOnItemWithBasicBasicCheckFail() throws Exception {
        //GIVEN
        initNodes();
        MoveNodeAction action = createAction();

        //WHEN
        definition.setMoveLocation(MoveLocation.INSIDE);

        //THEN
        action.executeOnItem(node3);
    }

    private MoveNodeAction createAction() {
        return new MoveNodeAction(definition, Arrays.asList((JcrItemAdapter)node1, node2), node3, adminCentralEventBusMock, uiContext, callback);
    }

    private void initNodes() {
        try {
            Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
            node1 = new JcrNodeAdapter(session.getRootNode().addNode(nodeName1));
            node2 = new JcrNodeAdapter(session.getRootNode().addNode(nodeName2));
            node3 = new JcrNodeAdapter(session.getRootNode().addNode(nodeName3));
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
}
