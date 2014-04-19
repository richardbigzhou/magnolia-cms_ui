/**
 * This file Copyright (c) 2014 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.contentapp.detail.action.EditItemActionDefinition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * An abstract class to test the {@link ContentAppMigrationTask};  extend the class for specific content-apps.<br/>
 * Actually the class is testing<br/><ul>
 * <li>{@link ChangeAvailabilityRuleClassesTask}</li>
 * <li>{@link MigrateAvailabilityRulesTask}</li>
 * <li>{@link MigrateJcrPropertiesToContentConnectorTask}</li>
 * <li>{@link MoveActionNodeTypeRestrictionToAvailabilityTask}</li>
 * </ul>
 */
public abstract class AbstractContentAppMigrationTaskTest extends ModuleVersionHandlerTestCase {

    private Session session;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        ComponentsTestUtil.setImplementation(UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$NonNormalizer");
    }

    /**
     * Returns the path to the module, e.g. "/modules/forum"
     */
    public abstract String getModulePath();

    /**
     * Returns the node-name of app, e.g. "forum", "assets", ... .
     */
    public abstract String getAppName();


    /**
     * Returns the node-name of main content-subApp, e.g. "browser".
     */
    public abstract String getMainSubAppName();

    /**
     * Returns the node-name of the detail-app, usually "detail".
     * Can be null, since not every app has such a content-app.
     */
    public abstract String getDetailSubAppName();

    /**
     * Returns the name of the workspace on which the content-app stores its content.
     */
    public abstract String getWorkspaceName();

    /**
     * Returns the path under which the content-app stores its content (e.g. "/").
     */
    public abstract String getPath();

    /**
     * Returns the version which is installed before the update-process takes place.
     */
    public abstract Version getCurrentlyInstalledVersion();



    /**
     * Testing MoveActionNodeTypeRestrictionToAvailabilityTask.
     */
    @Test
    public void testMoveActionNodeTypeRestrictionToAvailabilityTask() throws Exception {
        // GIVEN
        String actionPath = getModulePath() + "/apps/" + getAppName() + "/subApps/" + getMainSubAppName() + "/actions/editItem";
        String nodeTypeDummyValue = "mgnl:dummyNode";
        Node actionNode = NodeUtil.createPath(session.getRootNode(), actionPath, NodeTypes.ContentNode.NAME);
        actionNode.setProperty("class", EditItemActionDefinition.class.getName());
        actionNode.setProperty(MoveActionNodeTypeRestrictionToAvailabilityTask.NODE_TYPE, nodeTypeDummyValue);
        session.save();

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(getCurrentlyInstalledVersion());
        Session installCtxSession = installContext.getJCRSession(RepositoryConstants.CONFIG);

        // THEN
        //
        // nodeType-property has been removed from the actionNode
        assertTrue(!(installCtxSession.getNode(actionPath).hasProperty(MoveActionNodeTypeRestrictionToAvailabilityTask.NODE_TYPE)));
        // availability- and nodeTypes- node(s) exists
        String expectedNodeTypesNodePath = actionPath + "/availability/" + MoveActionNodeTypeRestrictionToAvailabilityTask.NODE_TYPES;
        assertTrue(installCtxSession.nodeExists(expectedNodeTypesNodePath));
        // new nodeTypesNodePath hast the nodeType as a property
        String nodeTypePropertyName = Path.getValidatedLabel(nodeTypeDummyValue);    // e.g. "dummyNode"
        String nodeTypePropertyValue = nodeTypeDummyValue;   // e.g. "mgnl:dummyNode"
        assertEquals(nodeTypePropertyValue, installCtxSession.getNode(expectedNodeTypesNodePath).getProperty(nodeTypePropertyName).getString());
    }


    /**
     * Testing MigrateAvailabilityRulesTask.
     */
    @Test
    public void testMigrateAvailabilityRulesTask() throws Exception {
        // GIVEN
        //
        String className = "info.magnolia.ui.api.availability.HasVersionsRule";
        // a content-node
        String availabilityContentNodePath = getModulePath() + "/apps/" + getAppName() + "/subApps/" + getMainSubAppName() + "/actions/showVersions/availability";
        Node availabilityContentNode = NodeUtil.createPath(session.getRootNode(), availabilityContentNodePath, NodeTypes.ContentNode.NAME);
        availabilityContentNode.setProperty("ruleClass", className);
        // a folder-node
        String availabilityFolderNodePath = getModulePath() + "/apps/" + getAppName() + "2/subApps/" + getMainSubAppName() + "/actions/showVersions/availability";
        Node folderNode = NodeUtil.createPath(session.getRootNode(), availabilityContentNodePath, NodeTypes.Folder.NAME);
        folderNode.setProperty("ruleClass", className);
        //
        session.save();

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(getCurrentlyInstalledVersion());

        // THEN
        Session installCtxSession = installContext.getJCRSession("config");
        String ruleContentNodePath = availabilityContentNodePath + "/rules/" + StringUtils.substringAfterLast(className, ".");
        String ruleFolderNodePath = availabilityFolderNodePath + "/rules/" + StringUtils.substringAfterLast(className, ".");

        assertTrue(installCtxSession.nodeExists(ruleContentNodePath));
        assertNotNull(PropertyUtil.getString(installCtxSession.getNode(ruleContentNodePath), "implementationClass"));
        assertTrue(!installCtxSession.nodeExists(ruleFolderNodePath));

    }


    /**
     * Testing ChangeAvailabilityRuleClassesTask.
     */
    @Test
    public void testChangeAvailabilityRuleClassesTask() throws Exception {

        // GIVEN
        Map<String, String> classMappings = ChangeAvailabilityRuleClassesTask.getClassMapping();
        Map<String, String> newNodePathNewClassMap = new HashMap<String, String>();
        int i = 0;
        Iterator<String> classMappingsIterator = classMappings.keySet().iterator();
        while (classMappingsIterator.hasNext()) {
            String oldClass = classMappingsIterator.next();
            String newExceptedClass = classMappings.get(oldClass);
            StringBuilder ruleNodeOldPath = new StringBuilder(getModulePath()).append("/apps/myApp").append(i).append("/browser/actions/someAction/availability");
            StringBuilder ruleNodeNewPath = new StringBuilder(ruleNodeOldPath).append("/rules/").append(StringUtils.substringAfterLast(oldClass, "."));
            createNodeWithOldRuleClassProperty(ruleNodeOldPath.toString(), oldClass);
            newNodePathNewClassMap.put(ruleNodeNewPath.toString(), newExceptedClass);
            i++;
        }
        session.save();

        // WHEN
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(getCurrentlyInstalledVersion());

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


    /**
     * Testing MigrateJcrPropertiesToContentConnectorTask -task-class.
     * Tests the 'default-content-app' (e.g. "browser")
     * and the 'detail-subApp' (if existing).
     */
    @Test
    public void testMigrateJcrPropertiesToContentConnectorTask() throws Exception {
        if (StringUtils.isBlank(getMainSubAppName())) {
            throw new Exception("You must specify mainSubAppName ; #getMainSubAppName must return a value which is neither null nor blank.");
        }

        // GIVEN
        //
        String workspaceName = getWorkspaceName();
        String path = getPath();
        String contentSubAppPath = null;
        String detailSubAppPath = null;
        boolean withDetailSubApp = false;
        if (StringUtils.isNotBlank(getMainSubAppName())) {
            contentSubAppPath = getModulePath() + "/apps/" + getAppName() + "/subApps/" + getMainSubAppName();
        }
        if (StringUtils.isNotBlank(getDetailSubAppName())) {
            detailSubAppPath = getModulePath() + "/apps/" + getAppName() + "/subApps/" + getDetailSubAppName();
            withDetailSubApp = true;
        }
        String noSubAppPath = getModulePath() + "/apps/" + getAppName() + "1/" + getMainSubAppName();

        setUpDummyContentAppWithBrowserAndDetail(contentSubAppPath, detailSubAppPath, getWorkspaceName(), getPath(), true);
        setUpDummyContentAppWithBrowserAndDetail(noSubAppPath, null, "someWorkspaceName", "/xyz", false);


        // WHEN
        //
        InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(getCurrentlyInstalledVersion());


        // THEN
        //
        Session installCtxSession = installContext.getJCRSession("config");

        // contentConnector-nodes created
        String contentSubAppContentConnectorPath = contentSubAppPath + "/contentConnector";
        String detailSubAppContentConnectorPath = null;
        assertTrue(installCtxSession.nodeExists(contentSubAppContentConnectorPath));
        if (withDetailSubApp) {
            detailSubAppContentConnectorPath = detailSubAppPath + "/contentConnector";
            assertTrue(installCtxSession.nodeExists(detailSubAppContentConnectorPath));
        }
        // but not if the parent is not a subApp
        String noSubAppAppContentConnectorPath = noSubAppPath + "/contentConnector";
        assertTrue(!(installCtxSession.nodeExists(noSubAppAppContentConnectorPath)));


        // path and workspace are moved properly; path was renamed to rootPath
        Node contentSubAppContentConnectorNode = installCtxSession.getNode(contentSubAppContentConnectorPath);
        assertTrue(contentSubAppContentConnectorNode.hasProperty("rootPath"));
        assertEquals(path, PropertyUtil.getString(contentSubAppContentConnectorNode, "rootPath"));
        // workspace-property-value properly moved
        assertEquals(workspaceName, PropertyUtil.getString(contentSubAppContentConnectorNode, "workspace"));
        if (withDetailSubApp) {
            Node detailSubAppContentConnectorNode = installCtxSession.getNode(detailSubAppContentConnectorPath);
            assertTrue(detailSubAppContentConnectorNode.hasProperty("workspace"));
        }
        // nodeTypes-node has been moved
        assertTrue(installCtxSession.nodeExists(contentSubAppContentConnectorPath + "/nodeTypes"));

        // didn't move "any" prop
        assertEquals(null, PropertyUtil.getString(contentSubAppContentConnectorNode, "foo"));
    }


    private void setUpDummyContentAppWithBrowserAndDetail(String browserSubAppPath, String detailSubAppPath, String workspaceName, String path, boolean subAppClass) throws Exception {
        if (StringUtils.isNotBlank(browserSubAppPath)) {
            String workbenchPath = browserSubAppPath + "/workbench";
            NodeUtil.createPath(session.getRootNode(), workbenchPath, NodeTypes.ContentNode.NAME);
            Node workbench = session.getNode(workbenchPath);
            if (subAppClass) {
                workbench.getParent().setProperty(MigrateJcrPropertiesToContentConnectorTask.SUB_APP_CLASS_PROPERTY, "some.class");
            }
            workbench.setProperty("workspace", workspaceName);
            workbench.setProperty("path", path);
            workbench.setProperty("foo", "bar");
            Node nodeTypesNode = workbench.addNode("nodeTypes", NodeTypes.ContentNode.NAME);
            Node nodeType = nodeTypesNode.addNode("mainNodeType", NodeTypes.ContentNode.NAME);
            nodeType.setProperty("icon", "anIcon");
            nodeType.setProperty("name", "mgnl:someNodeName");

        }
        if (StringUtils.isNotBlank(detailSubAppPath)) {
            String editorPath = detailSubAppPath + "/editor";
            Node editor = NodeUtil.createPath(session.getRootNode(), editorPath, NodeTypes.ContentNode.NAME, true);
            if (subAppClass) {
                editor.getParent().setProperty(MigrateJcrPropertiesToContentConnectorTask.SUB_APP_CLASS_PROPERTY, "some.class");
            }
            editor.setProperty("workspace", workspaceName);
        }
        session.save();
    }

    private void createNodeWithOldRuleClassProperty(String nodePath, String ruleClassName) throws Exception {
        Node availabilityContentNode1 = NodeUtil.createPath(session.getRootNode(), nodePath, NodeTypes.ContentNode.NAME);
        availabilityContentNode1.setProperty("ruleClass", ruleClassName);
    }


}
