/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the {@link DelegateByNodeTypeAction}.
 */
public class DelegateByNodeTypeActionTest {

    private static final String SOME_ACTION = "someAction";

    private DelegateByNodeTypeAction delegateByNodeTypeAction;
    private ActionExecutor actionExecutor;
    private AvailabilityChecker availabilityChecker;
    private ActionDefinition actionDefinition;

    @Before
    public void setUp() throws Exception {
        List<DelegateByNodeTypeActionDefinition.NodeTypeToActionMapping> nodeTypeToActionMappingList = new ArrayList<>();
        DelegateByNodeTypeActionDefinition.NodeTypeToActionMapping nodeTypeToActionMapping = new DelegateByNodeTypeActionDefinition.NodeTypeToActionMapping();
        nodeTypeToActionMapping.setAction(SOME_ACTION);
        nodeTypeToActionMapping.setNodeType(NodeTypes.Folder.NAME);
        nodeTypeToActionMappingList.add(nodeTypeToActionMapping);
        DelegateByNodeTypeActionDefinition delegateByNodeTypeActionDefinition = new DelegateByNodeTypeActionDefinition();
        delegateByNodeTypeActionDefinition.setNodeTypeToActionMappings(nodeTypeToActionMappingList);

        actionDefinition = mock(ActionDefinition.class);
        actionExecutor = mock(ActionExecutor.class);

        NodeType nodeType = mock(NodeType.class);
        when(nodeType.getName()).thenReturn(NodeTypes.Folder.NAME);

        Node node = mock(Node.class);
        when(node.isNode()).thenReturn(true);
        when(node.getPrimaryNodeType()).thenReturn(nodeType);

        JcrItemAdapter itemAdapter = mock(JcrItemAdapter.class);
        when(itemAdapter.getJcrItem()).thenReturn(node);

        availabilityChecker = mock(AvailabilityChecker.class);

        ContentConnector contentConnector = mock(ContentConnector.class);
        delegateByNodeTypeAction = new DelegateByNodeTypeAction(delegateByNodeTypeActionDefinition, actionExecutor, itemAdapter, availabilityChecker, contentConnector);
    }

    @Test
    public void testDelegatedActionIsExecuted() throws Exception {
        // GIVEN
        when(actionExecutor.getActionDefinition(SOME_ACTION)).thenReturn(actionDefinition);
        when(availabilityChecker.isAvailable(any(AvailabilityDefinition.class), anyListOf(Object.class))).thenReturn(true);
        // WHEN
        delegateByNodeTypeAction.execute();
        // THEN
        verify(actionExecutor).execute(eq(SOME_ACTION), any());
    }

    @Test
    public void testDelegatedActionIsNotAvailable() throws Exception {
        // GIVEN
        when(actionExecutor.getActionDefinition(SOME_ACTION)).thenReturn(actionDefinition);
        when(availabilityChecker.isAvailable(any(AvailabilityDefinition.class), anyListOf(Object.class))).thenReturn(false);
        // WHEN
        delegateByNodeTypeAction.execute();
        // THEN
        verify(actionExecutor, never()).execute(anyString(), any());
    }

    @Test
    public void testDefinitionForDelegatedActionIsNotFound() throws Exception {
        // GIVEN
        when(actionExecutor.getActionDefinition(SOME_ACTION)).thenReturn(null);
        when(availabilityChecker.isAvailable(any(AvailabilityDefinition.class), anyListOf(Object.class))).thenReturn(true);
        // WHEN
        delegateByNodeTypeAction.execute();
        // THEN
        verify(actionExecutor, never()).execute(anyString(), any());
    }
}