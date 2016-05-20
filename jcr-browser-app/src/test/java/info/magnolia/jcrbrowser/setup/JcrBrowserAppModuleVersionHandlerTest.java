/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.jcrbrowser.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

public class JcrBrowserAppModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
    }

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/jcr-browser-app.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Collections.singletonList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new JcrBrowserAppModuleVersionHandler();
    }

    @Test
    public void jcrBrowserAppComesFirstInAppLauncherToolGroup() throws Exception {
        // GIVEN
        setupConfigNode("/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/foo");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        final NodeIterator toolApps = session.getRootNode().getNode("modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps").getNodes();
        assertThat(toolApps.nextNode().getName(), is("jcr-browser"));
    }

    @Test
    public void updateFrom546UpdatesWorkspaceToWebsite() throws Exception {
        // GIVEN
        setupConfigProperty("/modules/jcr-browser-app/apps/jcr-browser/subApps/browser/contentConnector", "workspace", "config");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4.6"));

        // THEN
        Node contentConnector = session.getRootNode().getNode("modules/jcr-browser-app/apps/jcr-browser/subApps/browser/contentConnector");
        assertThat(contentConnector, hasProperty("workspace", "website"));
    }
}