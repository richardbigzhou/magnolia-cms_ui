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
package info.magnolia.security.setup;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.dialog.field.SystemLanguagesFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;

import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Security App.
 */
public class SecurityModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/security-app.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
        );
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new SecurityModuleVersionHandler();
    }



    @Test
    public void testUpdateTo51DeleteUserActionAvailability() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability", NodeTypes.ContentNode.NAME);
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(action.hasProperty("ruleClass"));
        assertEquals("info.magnolia.security.app.action.availability.IsNotCurrentUserRule", action.getProperty("ruleClass").getString());
    }

    @Test
    public void testUpdateTo51DeleteGroupActionClass() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/groups/actions/deleteGroup", NodeTypes.ContentNode.NAME);
        action.setProperty("class", "oldValue");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(action.hasProperty("class"));
        assertEquals("info.magnolia.security.app.action.DeleteGroupActionDefinition", action.getProperty("class").getString());
    }

    @Test
    public void testUpdateTo51DeleteRoleActionClass() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/actions/deleteRole", NodeTypes.ContentNode.NAME);
        action.setProperty("class", "oldValue");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(action.hasProperty("class"));
        assertEquals("info.magnolia.security.app.action.DeleteRoleActionDefinition", action.getProperty("class").getString());
    }

    @Test
    public void testUpdateTo51DeleteItemsAction() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node actions = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions", NodeTypes.ContentNode.NAME);
        assertFalse(actions.hasNode("deleteItems"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(actions.hasNode("deleteItems"));
    }

    @Test
    public void testUpdateTo51NewActionbarSectionInUsers() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node sections = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actionbar/sections", NodeTypes.ContentNode.NAME);
        assertFalse(sections.hasNode("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(sections.hasNode("multiple"));
    }

    @Test
    public void testTo51EnsureSecurityAppLauncherConfigOrderAfterExtraInstallTask() throws RepositoryException, ModuleManagementException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        String applauncherManageGroupParentPath = "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps";
        Node appsNode = NodeUtil.createPath(session.getRootNode(), applauncherManageGroupParentPath, NodeTypes.ContentNode.NAME);
        appsNode.addNode("configuration", NodeTypes.ContentNode.NAME);
        // we have to create the security node artificially before bootstrapping, otherwise test would fail in maven
        appsNode.addNode("security", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        Node node1st = appsNode.getNodes().nextNode();
        assertEquals("security", node1st.getName());
    }

    @Test
    public void emptyLabelsAreRemovedOnUpdateTo51() throws RepositoryException, ModuleManagementException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        String static1 = "/modules/security-app/dialogs/role/form/tabs/acls/fields/static1";
        String static2 = "/modules/security-app/dialogs/role/form/tabs/acls/fields/static2";
        Node static1Node = NodeUtil.createPath(session.getRootNode(), static1, NodeTypes.ContentNode.NAME);
        Node static2Node = NodeUtil.createPath(session.getRootNode(), static2, NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(static1Node, "label", "");
        PropertyUtil.setProperty(static2Node, "label", "");
        assertTrue(static1Node.hasProperty("label"));
        assertTrue(static2Node.hasProperty("label"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.4"));

        // THEN
        assertFalse(static1Node.hasProperty("label"));
        assertFalse(static2Node.hasProperty("label"));
    }

    @Test
    public void systemLanguagesFieldAddedOnUpdateTo511() throws Exception {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        String fieldTypes = "/modules/security-app/fieldTypes";
        Node fieldTypesNode = NodeUtil.createPath(session.getRootNode(), fieldTypes, NodeTypes.ContentNode.NAME);
        assertFalse(fieldTypesNode.hasNode("systemLanguagesField"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(fieldTypesNode.hasNode("systemLanguagesField"));
        assertEquals(SystemLanguagesFieldDefinition.class.getName(), fieldTypesNode.getNode("systemLanguagesField").getProperty("definitionClass").getString());
        assertEquals(SelectFieldFactory.class.getName(), fieldTypesNode.getNode("systemLanguagesField").getProperty("factoryClass").getString());
    }

    @Test
    public void userDialogLanguageFieldHasNewDefinitionOnUpdateTo511() throws Exception {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        String languageField = "/modules/security-app/dialogs/user/form/tabs/user/fields/language";
        Node languageNode = NodeUtil.createPath(session.getRootNode(), languageField, NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(languageNode, "class", SelectFieldDefinition.class.getName());

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertEquals(SystemLanguagesFieldDefinition.class.getName(), languageNode.getProperty("class").getString());
    }

    @Test
    public void userDialogLanguageFieldOptionsHaveBeenRemovedOnUpdateTo511() throws Exception {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        String languageField = "/modules/security-app/dialogs/user/form/tabs/user/fields/language";
        Node languageNode = NodeUtil.createPath(session.getRootNode(), languageField, NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(languageNode, "options", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertFalse(languageNode.hasNode("options"));
    }
}
