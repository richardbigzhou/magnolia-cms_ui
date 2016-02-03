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
package info.magnolia.pages.app.setup;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.pages.setup.PagesModuleVersionHandler;
import info.magnolia.repository.RepositoryConstants;

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
public class PagesModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Node dialog;
    private Node actions;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-admincentral.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
                );
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new PagesModuleVersionHandler();
    }


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        dialog = NodeUtil.createPath(session.getRootNode(), "/modules/pages/dialogs", NodeTypes.ContentNode.NAME);
        dialog.getSession().save();

        actions = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions", NodeTypes.ContentNode.NAME);

    }

    @Test
    public void testUpdateTo501WithExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN
        dialog.addNode("link", NodeTypes.ContentNode.NAME);
        assertTrue(dialog.hasNode("link"));
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialog.hasNode("link"));

    }

    @Test
    public void testUpdateTo501WithNonExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialog.hasNode("link"));
    }

    @Test
    public void testUpdateTo501CreatePageDialogHasLabel() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node createPage = NodeUtil.createPath(session.getRootNode(), "/modules/pages/dialogs/createPage/form", NodeTypes.ContentNode.NAME);
        createPage.getSession().save();
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("2.0"));

        // THEN
        assertTrue(createPage.hasProperty("label"));
    }

    @Test
    public void testUpdateTo502HasNewActions() throws ModuleManagementException, RepositoryException {

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(actions.hasNode("confirmDeletion"));
    }


    @Test
    public void testUpdateTo502CleanupDeleteAction() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/delete", NodeTypes.ContentNode.NAME);
        action.setProperty("label", "Delete item");
        action.setProperty("icon", "icon-delete");
        action.getSession().save();

        // WHEN
        NodeUtil.createPath(action, "availability", NodeTypes.ContentNode.NAME);

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        Node delete = actions.getNode("delete");
        assertFalse(delete.hasNode("availability"));
        assertFalse(delete.hasProperty("icon"));
        assertFalse(delete.hasProperty("label"));
    }

    @Test
    public void testUpdateTo502ActionbarNodesUpdated() throws ModuleManagementException, RepositoryException {

        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node actionbarItems = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actionbar/sections/pageActions/groups/addingActions/items", NodeTypes.ContentNode.NAME);

        NodeUtil.createPath(actionbarItems, "delete", NodeTypes.ContentNode.NAME);


        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertFalse(actionbarItems.hasNode("delete"));
        assertTrue(actionbarItems.hasNode("confirmDeletion"));
    }


    @Test
    public void testUpdateTo502ReplacesDeactivateCommand() throws ModuleManagementException, RepositoryException {

        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node commands = NodeUtil.createPath(session.getRootNode(), "/modules/pages/commands/website", NodeTypes.ContentNode.NAME);
        Node deactivate = commands.addNode("deactivate", NodeTypes.ContentNode.NAME);
        deactivate.setProperty("actionName", "default-deactivate");
        deactivate.getSession().save();

        String identifier = deactivate.getIdentifier();


        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        Node deactivateChain = commands.getNode("deactivate");
        assertNotEquals(identifier, deactivateChain.getIdentifier());

    }

    @Test
    public void testUpdateTo502DeactivationCommandBootstrapped() throws ModuleManagementException, RepositoryException {

        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node commands = NodeUtil.createPath(session.getRootNode(), "/modules/pages/commands/website", NodeTypes.ContentNode.NAME);
        Node deactivate = commands.addNode("deactivate", NodeTypes.ContentNode.NAME);
        deactivate.setProperty("actionName", "default-deactivate");
        deactivate.getSession().save();


        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        Node deactivateChain = commands.getNode("deactivate");

        assertTrue(deactivateChain.hasNode("version"));
        assertTrue(deactivateChain.hasNode("deactivate"));
    }
}
