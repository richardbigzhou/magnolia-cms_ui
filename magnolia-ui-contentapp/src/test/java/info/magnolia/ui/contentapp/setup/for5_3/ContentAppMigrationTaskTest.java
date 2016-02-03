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
package info.magnolia.ui.contentapp.setup.for5_3;

import static info.magnolia.jcr.nodebuilder.Ops.*;
import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static info.magnolia.ui.contentapp.setup.for5_3.MoveActionNodeTypeRestrictionToAvailabilityTask.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.jcr.nodebuilder.NodeBuilder;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.contentapp.detail.DetailSubApp;
import info.magnolia.ui.contentapp.detail.action.EditItemActionDefinition;
import info.magnolia.ui.framework.availability.IsNotDeletedRule;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.hamcrest.Matchers;
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
public class ContentAppMigrationTaskTest extends RepositoryTestCase {

    private static final String TEST_RULE_CLASS = "info.magnolia.test.availability.AnyRule";

    private Session session;
    private InstallContext installContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        installContext = new InstallContextImpl(mock(ModuleRegistry.class));
        session = installContext.getConfigJCRSession();
    }

    /**
     * Test for the {@link MoveActionNodeTypeRestrictionToAvailabilityTask}.
     */
    @Test
    public void testMoveActionNodeTypeRestrictionToAvailabilityTask() throws Exception {
        // GIVEN
        Node actionNode = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/subApps/browser/actions/editItem", NodeTypes.ContentNode.NAME);
        actionNode.setProperty("class", EditItemActionDefinition.class.getName());
        actionNode.setProperty(NODE_TYPE, "mgnl:dummyNode");
        session.save();

        ContentAppMigrationTask task = new ContentAppMigrationTask("/modules/test");

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(actionNode.hasProperty(NODE_TYPE)); // nodeType property has been removed from the action node
        assertTrue(actionNode.hasNode("availability")); // availability and nodeTypes nodes exist
        assertTrue(actionNode.getNode("availability").hasNode(NODE_TYPES));
        Node nodeTypes = actionNode.getNode("availability/nodeTypes");
        // It's a pity we have to help the either().or() matcher with generics here; 1.7 doesn't need this.
        assertThat(nodeTypes, everyProperty(Matchers.<Property>
                either(propertyName(startsWith("jcr:"))).
                or(propertyName(startsWith("mgnl:"))).
                or(propertyValue(equalTo("mgnl:dummyNode")))));
    }

    /**
     * Test for the {@link MigrateAvailabilityRulesTask}.
     */
    @Test
    public void testMigrateAvailabilityRulesTask() throws Exception {
        // GIVEN
        Node availability = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/subApps/browser/actions/testEdit/availability", NodeTypes.ContentNode.NAME);
        availability.setProperty("ruleClass", TEST_RULE_CLASS);
        session.save();

        ContentAppMigrationTask task = new ContentAppMigrationTask("/modules/test");

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(availability.hasProperty("ruleClass"));
        assertTrue(availability.hasNode("rules"));
        assertTrue(availability.getNode("rules").hasNodes());
        assertTrue(availability.getNode("rules").getNodes().nextNode().hasProperty("implementationClass"));
        assertEquals(TEST_RULE_CLASS, availability.getNode("rules").getNodes().nextNode().getProperty("implementationClass").getString());
    }

    /**
     * Test for the {@link ChangeAvailabilityRuleClassesTask}.
     */
    @Test
    public void testChangeAvailabilityRuleClassesTask() throws Exception {
        // GIVEN
        Node availability = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/subApps/browser/actions/someAction/availability", NodeTypes.ContentNode.NAME);
        availability.setProperty("ruleClass", "info.magnolia.ui.api.availability.IsNotDeletedRule");
        session.save();

        ContentAppMigrationTask task = new ContentAppMigrationTask("/modules/test");

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(availability.hasProperty("ruleClass"));
        assertTrue(availability.hasNode("rules"));
        assertTrue(availability.getNode("rules").hasNodes());
        assertTrue(availability.getNode("rules").getNodes().nextNode().hasProperty("implementationClass"));
        assertEquals(IsNotDeletedRule.class.getName(), availability.getNode("rules").getNodes().nextNode().getProperty("implementationClass").getString());
    }

    /**
     * Test for the {@link MigrateJcrPropertiesToContentConnectorTask}.
     * Tests the default subApp (browser) and the detail subApp if existing.
     */
    @Test
    public void testMigrateJcrPropertiesToContentConnectorTask() throws Exception {
        // GIVEN
        String workspace = "workspace";
        String rootPath = "/rootPath";

        Node browser = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/subApps/browser", NodeTypes.ContentNode.NAME);
        new NodeBuilder(browser, addNode("workbench", NodeTypes.ContentNode.NAME).then(
                addProperty("workspace", workspace),
                addProperty("path", rootPath),
                addProperty("foo", "bar"),
                addNode("nodeTypes", NodeTypes.ContentNode.NAME).then(
                        addNode("mainNodeType", NodeTypes.ContentNode.NAME).then(
                                addProperty("name", "mgnl:someNodeType"),
                                addProperty("icon", "icon-dummy"))))).exec();

        Node detail = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/subApps/detail", NodeTypes.ContentNode.NAME);
        new NodeBuilder(detail, addNode("editor", NodeTypes.ContentNode.NAME).then(
                addProperty("workspace", workspace),
                addNode("form", NodeTypes.ContentNode.NAME))).exec();

        Node nested = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/subApps/generic/nested", NodeTypes.ContentNode.NAME);
        new NodeBuilder(nested,
                addProperty("subAppClass", DetailSubApp.class.getName()), // since nested subApp is not a direct child of the 'subApps' node, the task expects to find a subAppClass to know it can operate on it anyhow.
                addNode("editor", NodeTypes.ContentNode.NAME).then(
                        addProperty("workspace", workspace),
                        addNode("form", NodeTypes.ContentNode.NAME))).exec();

        Node dialogWorkbench = NodeUtil.createPath(session.getRootNode(), "/modules/test/apps/myApp/chooseDialog/field/workbench", NodeTypes.ContentNode.NAME);
        dialogWorkbench.setProperty("extends", "../../../subApps/browser/workbench");

        session.save();

        ContentAppMigrationTask task = new ContentAppMigrationTask("/modules/test");

        // WHEN
        task.execute(installContext);

        // THEN
        // // BROWSER SUBAPP
        assertTrue(browser.hasNode("contentConnector")); // contentConnector node created
        assertTrue(browser.getNode("contentConnector").hasProperty("workspace")); // workspace property properly moved
        assertTrue(browser.getNode("contentConnector").hasProperty("rootPath")); // path property properly moved and renamed to rootPath
        assertEquals(workspace, browser.getNode("contentConnector").getProperty("workspace").getString());
        assertEquals(rootPath, browser.getNode("contentConnector").getProperty("rootPath").getString());
        assertTrue(browser.getNode("contentConnector").hasNode("nodeTypes")); // nodeTypes have been moved
        assertTrue(browser.getNode("contentConnector").getNode("nodeTypes").hasNode("mainNodeType"));
        assertEquals("mgnl:someNodeType", browser.getNode("contentConnector").getNode("nodeTypes").getNode("mainNodeType").getProperty("name").getString());

        assertFalse(browser.getNode("workbench").hasProperty("workspace"));
        assertFalse(browser.getNode("workbench").hasProperty("path"));
        assertFalse(browser.getNode("workbench").hasNode("nodeTypes"));
        assertTrue(browser.getNode("workbench").hasProperty("foo")); // didn't move other properties

        // // DETAIL SUBAPP
        assertTrue(detail.hasNode("contentConnector"));
        assertTrue(detail.getNode("contentConnector").hasProperty("workspace"));
        assertEquals(workspace, detail.getNode("contentConnector").getProperty("workspace").getString());
        assertFalse(detail.getNode("editor").hasProperty("workspace"));

        // // NESTED SUBAPP
        assertTrue(nested.hasNode("contentConnector"));
        assertTrue(nested.getNode("contentConnector").hasProperty("workspace"));
        assertEquals(workspace, nested.getNode("contentConnector").getProperty("workspace").getString());
        assertFalse(nested.getNode("editor").hasProperty("workspace"));

        // // NOT A SUBAPP
        assertFalse(dialogWorkbench.hasNode("contentConnector"));
    }

}
