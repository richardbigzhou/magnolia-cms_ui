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
package info.magnolia.messages.setup;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link MessagesModuleVersionHandler}.
 */
public class MessagesModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/messages-app.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new MessagesModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/core.xml");
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testUpdateTo52ReordersMessagesAppInDev() throws Exception {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node devApps = NodeUtil.createPath(session.getRootNode(), "modules/ui-admincentral/config/appLauncherLayout/groups/dev/apps", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(devApps, "dummyApp", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(devApps, "messages", NodeTypes.ContentNode.NAME);
        Node messages = NodeUtil.createPath(session.getRootNode(), "modules/messages-app/apps/messages", NodeTypes.ContentNode.NAME);
        messages.setProperty("icon", "icon");
        messages.setProperty("label", "label");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1.2"));

        // THEN
        NodeIterator it = devApps.getNodes();
        assertTrue(devApps.hasNode("messages"));
        assertEquals("messages", it.nextNode().getName());
        assertEquals("dummyApp", it.nextNode().getName());
        assertFalse(messages.hasProperty("icon"));
        assertFalse(messages.hasProperty("label"));
    }

    @Test
    public void testFreshInstallReordersMessagesAppInDev() throws Exception {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node devApps = NodeUtil.createPath(session.getRootNode(), "modules/ui-admincentral/config/appLauncherLayout/groups/dev/apps", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(devApps, "dummyApp", NodeTypes.ContentNode.NAME);
        // we have to create the messages node artificially before bootstrapping, otherwise test would fail in maven
        NodeUtil.createPath(devApps, "messages", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        NodeIterator it = devApps.getNodes();
        assertTrue(devApps.hasNode("messages"));
        assertEquals("messages", it.nextNode().getName());
        assertEquals("dummyApp", it.nextNode().getName());
    }

}
