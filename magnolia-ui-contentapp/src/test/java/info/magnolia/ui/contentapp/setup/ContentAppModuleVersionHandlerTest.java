/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import static org.junit.Assert.*;

import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
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
import info.magnolia.ui.framework.setup.ChangeJcrDependentAvailabilityRuleClassesFqcnTask;
import info.magnolia.ui.framework.setup.MigrateWorkspaceAndPathToContentConnector;
import info.magnolia.ui.framework.setup.RenameContentConnectorPathPropertyTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
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



    /**
     * testing MigrateWorkspaceAndPathToContentConnector -task-class
     * @throws Exception
     */
    @Test
    public void testUpdateTo53_migrateWorkspaceAndPathToContentConnector() throws Exception{
        // GIVEN
        String workspaceName = "dam";
        String path = "/";
        setUpDummyContentAppWithBrowserAndDetail("/modules/dam/apps/assets/subApps/browser", "/modules/dam/apps/assets/subApps/detail", workspaceName, path, true);
        setUpDummyContentAppWithBrowserAndDetail("/modules/xxx/apps/assets/subApps/browser", null, "someWsName", "/xyz", false);
        setUpDummyContentAppWithBrowserAndDetail(null, null, "/modules/abc/config/detail", "/xyz", false);

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        Session installCtxConfigSession = installContext.getJCRSession("config");

        assertTrue(installCtxConfigSession.nodeExists("/modules/dam/apps/assets/subApps/browser/contentConnector"));
        assertTrue(installCtxConfigSession.nodeExists("/modules/dam/apps/assets/subApps/detail/contentConnector"));
        // node and workspace are moved properly
        Node contentConnectorNode = installCtxConfigSession.getNode("/modules/dam/apps/assets/subApps/browser/contentConnector");
        assertEquals(workspaceName, PropertyUtil.getString(contentConnectorNode, "workspace"));
        // do not move any prop
        assertEquals(null, PropertyUtil.getString(contentConnectorNode, "foo"));
        // do not migrate if it's not the subnode of a subapp
        assertTrue(!installCtxConfigSession.nodeExists("/modules/xxx/apps/assets/subApps/browser/contentConnector"));
        assertTrue(!installCtxConfigSession.nodeExists("/modules/abc/config/detail/contentConnector"));
    }

    /**
     * testing the RenameContentConnectorPathPropertyTask-class
     * @throws Exception
     */
    @Test
    public void testUpdateTo53_renameContentConnectorPathPropertyTask() throws Exception{
        // GIVEN
        Node  contentConnector = NodeUtil.createPath(session.getRootNode(),"/modules/ui-admincentral/apps/websiteJcrBrowser/subApps/browser/contentConnector", NodeTypes.ContentNode.NAME);
        contentConnector.setProperty(RenameContentConnectorPathPropertyTask.PATH_PROPERTY, "/");
        Node  justAnotherNode = NodeUtil.createPath(session.getRootNode(),"/modules/ui-admincentral/apps/xxx/config", NodeTypes.ContentNode.NAME);
        justAnotherNode.setProperty(RenameContentConnectorPathPropertyTask.PATH_PROPERTY, "/");
        session.save();

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        assertTrue(contentConnector.hasProperty(RenameContentConnectorPathPropertyTask.ROOTPATH_PROPERTY));
        assertTrue(!contentConnector.hasProperty(RenameContentConnectorPathPropertyTask.PATH_PROPERTY));
        assertTrue(!justAnotherNode.hasProperty(RenameContentConnectorPathPropertyTask.ROOTPATH_PROPERTY));
        assertTrue(justAnotherNode.hasProperty(RenameContentConnectorPathPropertyTask.PATH_PROPERTY));
    }

    /**
     * testing MigrateRuleClassToAvailabilityRuleDefinitionCollectionTask
     * @throws Exception
     */
    //@Test
    public void testUpdateTo53_migrateRuleClassToAvailabilityRuleDefinitionCollection() throws Exception{
        // GIVEN
        String className = "info.magnolia.ui.api.availability.HasVersionsRule";

        String availabilityContentNodePath = "/modules/groovy/apps/groovy/subApps/browser/actions/showVersions/availability";
        Node availabilityContentNode = NodeUtil.createPath(session.getRootNode(),availabilityContentNodePath, NodeTypes.ContentNode.NAME);
        availabilityContentNode.setProperty("ruleClass", className);

        String availabilityFolderNodePath = "/modules/abc/apps/groovy/subApps/browser/actions/showVersions/availability";
        Node folderNode = NodeUtil.createPath(session.getRootNode(),availabilityContentNodePath, NodeTypes.Folder.NAME);
        folderNode.setProperty("ruleClass", className);

        session.save();

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        String ruleContentNodePath = availabilityContentNodePath+"/rules/"+StringUtils.substringAfterLast(className, ".");
        String ruleFolderNodePath = availabilityFolderNodePath+"/rules/"+StringUtils.substringAfterLast(className, ".");
        assertTrue(installContext.getJCRSession("config").nodeExists(ruleContentNodePath));
        assertNotNull(PropertyUtil.getString(installContext.getJCRSession("config").getNode(ruleContentNodePath),"implementationClass"));
        assertTrue(!installContext.getJCRSession("config").nodeExists(ruleFolderNodePath));
    }


    /**
     * testing ChangeJcrDependentAvailabilityRuleClassesFqcnTask
     */
    @Test
    public void testUpdateTo53_changeJcrDependentAvailabilityRuleClassesFqcn() throws Exception{
        // GIVEN
        Map<String,String> classMappings = ChangeJcrDependentAvailabilityRuleClassesFqcnTask.getClassMapping();
        Map<String, String> newNodePathNewClassMap = new HashMap<String, String>();
        int i=0;
        Iterator<String> classMappingsIterator =  classMappings.keySet().iterator();
        while(classMappingsIterator.hasNext()){
            String oldClass = classMappingsIterator.next();
            String newExceptedClass = classMappings.get(oldClass);
            StringBuilder ruleNodeOldPath =  new StringBuilder("/modules/").append(i).append("/apps/abc/browser/actions/someAction/availability");
            StringBuilder ruleNodeNewPath =  new StringBuilder(ruleNodeOldPath).append("/rules/").append(StringUtils.substringAfterLast(oldClass, "."));
            createNodeWithRuleClassProperty(ruleNodeOldPath.toString(), oldClass);
            newNodePathNewClassMap.put(ruleNodeNewPath.toString(), newExceptedClass);
            i++;
        }
        session.save();

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        //
        Iterator<String> newNodePathsIterator = newNodePathNewClassMap.keySet().iterator();
        while (newNodePathsIterator.hasNext()) {
            String ruleNodeNewPath = newNodePathsIterator.next();
            String ruleNodeExpectedClassName = newNodePathNewClassMap.get(ruleNodeNewPath);
            Node newNode = installContext.getJCRSession("config").getNode(ruleNodeNewPath);
            assertEquals(ruleNodeExpectedClassName, newNode.getProperty("implementationClass").getString());
        }
    }

    private void createNodeWithRuleClassProperty(String nodePath, String ruleClassName) throws Exception{
        Node availabilityContentNode1 = NodeUtil.createPath(session.getRootNode(),nodePath, NodeTypes.ContentNode.NAME);
        availabilityContentNode1.setProperty("ruleClass", ruleClassName);
    }


    private void setUpDummyContentAppWithBrowserAndDetail(String browserSubAppPath, String detailSubAppPath, String workspaceName, String path, boolean setSubappClassProperty) throws Exception{
        if(StringUtils.isNotBlank(browserSubAppPath)){
            String workbenchPath = browserSubAppPath+"/"+MigrateWorkspaceAndPathToContentConnector.WORKBENCH_NODENAME;
            NodeUtil.createPath(session.getRootNode(), workbenchPath, NodeTypes.ContentNode.NAME);
            Node workbench = session.getNode(workbenchPath);
            if(setSubappClassProperty){
                workbench.getParent().setProperty(MigrateWorkspaceAndPathToContentConnector.SUB_APP_CLASS_PROPERTY,"some.class");
            }
            workbench.setProperty("workspace", workspaceName);
            workbench.setProperty("path", path);
            workbench.setProperty("foo", "bar");
        }
        if(StringUtils.isNotBlank(detailSubAppPath)){
            String editorPath = detailSubAppPath+"/"+MigrateWorkspaceAndPathToContentConnector.EDITOR_NODENAME;
            Node editor = NodeUtil.createPath(session.getRootNode(), editorPath, NodeTypes.ContentNode.NAME);
            if(setSubappClassProperty){
                editor.getParent().setProperty(MigrateWorkspaceAndPathToContentConnector.SUB_APP_CLASS_PROPERTY,"some.class");
            }
            editor.setProperty("workspace", workspaceName);
        }
        session.save();
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
    public void testUpdateTo53AddsSaveItemPropertyAction() throws Exception {
        // GIVEN
        setupConfigNode("/modules/ui-admincentral/apps/configuration/subApps/browser/actions");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN
        assertTrue(session.itemExists("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/saveItemProperty"));
        assertEquals(SaveItemPropertyActionDefinition.class.getCanonicalName(), session.getNode("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/saveItemProperty").getProperty("class").getString());
    }
}
