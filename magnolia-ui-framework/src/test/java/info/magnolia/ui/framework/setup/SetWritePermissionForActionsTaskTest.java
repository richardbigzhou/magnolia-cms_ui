/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.setup;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SetWritePermissionForActionsTask}.
 */
public class SetWritePermissionForActionsTaskTest {

    private Session session;
    private InstallContext ctx;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(RepositoryConstants.CONFIG);
        ctx = mock(InstallContext.class);
        doReturn(session).when(ctx).getJCRSession(RepositoryConstants.CONFIG);
        doReturn(session).when(ctx).getConfigJCRSession();
    }

    @Test
    public void executeSetsWritePermissionOnGivenActions() throws Exception {
        // GIVEN
        Node actions = NodeUtil.createPath(session.getRootNode(), "/modules/dummy/apps/dummy/subApps/dummy/actions", NodeTypes.ContentNode.NAME);
        Node createAction = actions.addNode("create", NodeTypes.ContentNode.NAME);
        Node readAction = actions.addNode("read", NodeTypes.ContentNode.NAME);
        Node updateAction = actions.addNode("update", NodeTypes.ContentNode.NAME);
        Node updateAvailability = updateAction.addNode("availability");
        updateAvailability.setProperty("multiple", true);


        // WHEN
        SetWritePermissionForActionsTask task = new SetWritePermissionForActionsTask("/modules/dummy/apps/dummy/subApps/dummy/actions",
                new String[] { "create", "update" });
        task.execute(ctx);

        // THEN
        assertTrue(createAction.hasNode("availability"));
        assertTrue(createAction.getNode("availability").hasProperty("writePermissionRequired"));
        assertTrue(createAction.getNode("availability").getProperty("writePermissionRequired").getBoolean());
        assertFalse(readAction.hasNode("availability"));
        assertTrue(updateAction.hasNode("availability"));
        assertTrue(updateAction.getNode("availability").hasProperty("writePermissionRequired"));
        assertTrue(updateAction.getNode("availability").getProperty("writePermissionRequired").getBoolean());
    }

    @Test
    public void executePassesIfWritePermissionIsAlreadySet() throws Exception {
        // GIVEN
        Node actions = NodeUtil.createPath(session.getRootNode(), "/modules/dummy/apps/dummy/subApps/dummy/actions", NodeTypes.ContentNode.NAME);
        Node createAction = actions.addNode("create", NodeTypes.ContentNode.NAME);
        Node readAction = actions.addNode("read", NodeTypes.ContentNode.NAME);
        Node updateAction = actions.addNode("update", NodeTypes.ContentNode.NAME);
        updateAction.addNode("availability");

        // WHEN
        SetWritePermissionForActionsTask task = new SetWritePermissionForActionsTask("/modules/dummy/apps/dummy/subApps/dummy/actions",
                new String[] { "create", "update" });
        task.execute(ctx);
        task.execute(ctx); // intentionally executing twice!

        // THEN
        assertTrue(createAction.hasNode("availability"));
        assertTrue(createAction.getNode("availability").hasProperty("writePermissionRequired"));
        assertTrue(createAction.getNode("availability").getProperty("writePermissionRequired").getBoolean());
        assertFalse(readAction.hasNode("availability"));
        assertTrue(updateAction.hasNode("availability"));
        assertTrue(updateAction.getNode("availability").hasProperty("writePermissionRequired"));
        assertTrue(updateAction.getNode("availability").getProperty("writePermissionRequired").getBoolean());
    }

}
