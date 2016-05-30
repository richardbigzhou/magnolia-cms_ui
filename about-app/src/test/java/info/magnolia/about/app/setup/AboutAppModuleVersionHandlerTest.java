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
package info.magnolia.about.app.setup;

import com.google.common.collect.Lists;
import info.magnolia.about.app.AboutAppBaseApp;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.List;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class AboutAppModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session configSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
    }

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/about-app.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new AboutAppModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Lists.newArrayList("/META-INF/magnolia/core.xml");
    }

    @Test
    public void updateTo547RemovesOldConfigInfoFromAppLauncher() throws Exception {
        removeOldConfigInfoFromAppLauncher(Version.parseVersion("5.4.6"));
    }

    @Test
    public void updateTo547ConfigureAboutApp() throws Exception {
        // GIVEN
        setupConfigNode("/modules/about-app/apps/about");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4.6"));

        // THEN
        Node subApps = configSession.getNode("/modules/about-app/apps/about");
        assertThat(subApps, hasNode("subApps/main"));
        assertThat(subApps, hasNode("subApps/config"));
        assertThat(subApps, hasNode("subApps/mapping"));
        assertThat(subApps, hasProperty("appClass", AboutAppBaseApp.class.getName()));
    }

    @Test
    public void installRemovesOldConfigInfoFromAppLauncher() throws Exception {
        removeOldConfigInfoFromAppLauncher(null);
    }

    private void removeOldConfigInfoFromAppLauncher(Version installedVersion) throws Exception{
        // GIVEN
        setupConfigNode(AboutAppModuleVersionHandler.APP_LAUNCHER_APPS_PATH + AboutAppModuleVersionHandler.CONFIG_INFO);
        Node apps = configSession.getNode(AboutAppModuleVersionHandler.APP_LAUNCHER_APPS_PATH);
        assertThat(apps, hasNode(AboutAppModuleVersionHandler.CONFIG_INFO));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(installedVersion);

        // THEN
        assertThat(apps, not(hasNode(AboutAppModuleVersionHandler.CONFIG_INFO)));
    }
}
