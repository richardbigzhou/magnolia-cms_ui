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
package info.magnolia.ui.admincentral.setup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.admincentral.setup.AppLauncherReorderingTask.Order;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * The AppLauncherReorderingTaskTest.
 */
public class AppLauncherReorderingTaskTest {

    private Session session;
    private InstallContext ctx;

    private Node apps;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(RepositoryConstants.CONFIG);
        ctx = mock(InstallContext.class);
        doReturn(session).when(ctx).getJCRSession(RepositoryConstants.CONFIG);
        doReturn(session).when(ctx).getConfigJCRSession();

        apps = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/config/appLauncherLayout/groups/samples/apps", NodeTypes.ContentNode.NAME);
        apps.addNode("foxtrot");
        apps.addNode("uniform");
        apps.addNode("charlie");
        apps.addNode("kilo");
    }

    @Test
    public void reorderAppAsFirstInGroup() throws Exception {
        // GIVEN
        AppLauncherReorderingTask task = new AppLauncherReorderingTask("charlie", "samples", Order.FIRST, null);

        // WHEN
        task.execute(ctx);

        // THEN
        NodeIterator it = apps.getNodes();
        assertEquals("charlie", it.nextNode().getName());
        assertEquals("foxtrot", it.nextNode().getName());
        assertEquals("uniform", it.nextNode().getName());
        assertEquals("kilo", it.nextNode().getName());
    }

    @Test
    public void reorderAppBefore() throws Exception {
        // GIVEN
        AppLauncherReorderingTask task = new AppLauncherReorderingTask("charlie", "samples", Order.BEFORE, "uniform");

        // WHEN
        task.execute(ctx);

        // THEN
        NodeIterator it = apps.getNodes();
        assertEquals("foxtrot", it.nextNode().getName());
        assertEquals("charlie", it.nextNode().getName());
        assertEquals("uniform", it.nextNode().getName());
        assertEquals("kilo", it.nextNode().getName());
    }

    @Test
    public void reorderAppAfter() throws Exception {
        // GIVEN
        AppLauncherReorderingTask task = new AppLauncherReorderingTask("foxtrot", "samples", Order.AFTER, "charlie");

        // WHEN
        task.execute(ctx);

        // THEN
        NodeIterator it = apps.getNodes();
        assertEquals("uniform", it.nextNode().getName());
        assertEquals("charlie", it.nextNode().getName());
        assertEquals("foxtrot", it.nextNode().getName());
        assertEquals("kilo", it.nextNode().getName());
    }

    @Test
    public void reorderAppAsLastInGroup() throws Exception {
        // GIVEN
        AppLauncherReorderingTask task = new AppLauncherReorderingTask("uniform", "samples", Order.LAST, null);

        // WHEN
        task.execute(ctx);

        // THEN
        NodeIterator it = apps.getNodes();
        assertEquals("foxtrot", it.nextNode().getName());
        assertEquals("charlie", it.nextNode().getName());
        assertEquals("kilo", it.nextNode().getName());
        assertEquals("uniform", it.nextNode().getName());
    }

}
