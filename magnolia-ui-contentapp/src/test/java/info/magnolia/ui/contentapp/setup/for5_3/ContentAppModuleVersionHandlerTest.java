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
package info.magnolia.ui.contentapp.setup.for5_3;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.contentapp.ConfiguredContentAppDescriptor;
import info.magnolia.ui.contentapp.ContentApp;
import info.magnolia.ui.contentapp.browser.action.SaveItemPropertyActionDefinition;
import info.magnolia.ui.contentapp.movedialog.action.MoveNodeActionDefinition;
import info.magnolia.ui.contentapp.setup.ContentAppModuleVersionHandler;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class ContentAppModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Node i18n;
    private Node contentapp;
    private Session session;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-contentapp.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml",
                "/META-INF/magnolia/ui-framework.xml"
                );
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new ContentAppModuleVersionHandler();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        i18n = NodeUtil.createPath(session.getRootNode(), "/server/i18n", NodeTypes.ContentNode.NAME);
        i18n.addNode("authoring", NodeTypes.ContentNode.NAME);
        i18n.addNode("authoring50", NodeTypes.ContentNode.NAME);
        i18n.getSession().save();

        contentapp = NodeUtil.createPath(session.getRootNode(), "/modules/ui-contentapp", NodeTypes.ContentNode.NAME);

        ComponentsTestUtil.setImplementation(UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$NonNormalizer");
    }

    @Test
    public void testUpdateTo5_15_1ChangePackageName() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node path = contentapp.addNode("path", NodeTypes.ContentNode.NAME);
        path.setProperty("moveNodeActionDefinition", "info.magnolia.ui.framework.action.MoveNodeActionDefinition");
        contentapp.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(path.hasProperty("moveNodeActionDefinition"));
        assertEquals(MoveNodeActionDefinition.class.getName(), path.getProperty("moveNodeActionDefinition").getString());
    }

    @Test
    public void testUpdateFrom50() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/ui-admincentral/apps/configuration/subApps");
        this.setupConfigProperty("/modules/ui-admincentral/apps/configuration", "appClass", ContentApp.class.getCanonicalName());
        // obsolete 'app' property:
        this.setupConfigNode("/modules/ui-admincentral/apps/stkSiteApp/subApps");
        this.setupConfigProperty("/modules/ui-admincentral/apps/stkSiteApp", "app", "info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(session.propertyExists("/modules/ui-admincentral/apps/configuration/class"));
        assertEquals(ConfiguredContentAppDescriptor.class.getCanonicalName(), session.getProperty("/modules/ui-admincentral/apps/configuration/class").getString());

        assertFalse(session.propertyExists("/modules/ui-admincentral/apps/stkSiteApp/app"));
        assertTrue(session.propertyExists("/modules/ui-admincentral/apps/stkSiteApp/class"));
        assertEquals(ConfiguredContentAppDescriptor.class.getCanonicalName(), session.getProperty("/modules/ui-admincentral/apps/stkSiteApp/class").getString());
    }

    @Test
    public void testUpdateTo53MigratesAdmincentralContentApps() throws Exception {
        // GIVEN
        Node browser = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser", NodeTypes.ContentNode.NAME);
        browser.addNode("workbench", NodeTypes.ContentNode.NAME);
        session.save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN
        assertTrue(browser.hasNode("contentConnector"));
    }

    @Test
    public void testUpdateTo53AddsSaveItemPropertyAction() throws Exception {
        // GIVEN
        setupConfigNode("/modules/ui-admincentral/apps/configuration/subApps/browser/actions");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN
        assertTrue(session.itemExists("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/saveItemProperty"));
        assertEquals(SaveItemPropertyActionDefinition.class.getCanonicalName(), session.getNode("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/saveItemProperty").getProperty("class").getString());
    }

    @Test
    public void testUpdateTo532AddsIncludeSystemNodesPropertyToJCRBrowser() throws Exception {
        // GIVEN
        Node websiteJCRBrowserContentConnector = NodeUtil.createPath(session.getRootNode(), ContentAppModuleVersionHandler.UI_ADMINCENTRAL_CONTENTCONNECTOR, NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3"));

        // THEN
        assertThat(websiteJCRBrowserContentConnector, hasProperty("includeSystemNodes"));
        assertThat(websiteJCRBrowserContentConnector.getProperty("includeSystemNodes").getBoolean(), is(false));
    }
}
