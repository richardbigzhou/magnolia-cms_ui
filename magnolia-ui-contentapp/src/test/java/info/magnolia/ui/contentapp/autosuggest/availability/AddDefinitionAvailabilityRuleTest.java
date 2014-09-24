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
package info.magnolia.ui.contentapp.autosuggest.availability;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.contentapp.autosuggest.MockBeanForNameOfPropertyAndNameOfNode;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterForConfigurationApp;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AddDefinitionAvailabilityRule}.
 */
public class AddDefinitionAvailabilityRuleTest extends RepositoryTestCase {

    private Session session;
    private Node rootNode;
    private AddDefinitionAvailabilityRule availabilityRule;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        rootNode = session.getRootNode();
        ComponentsTestUtil.setImplementation(EventBus.class, SimpleEventBus.class);
        AddDefinitionAvailabilityRuleDefinition definition = new AddDefinitionAvailabilityRuleDefinition();
        definition.setAutoSuggesterClass(AutoSuggesterForConfigurationApp.class);
        ConfiguredJcrContentConnectorDefinition jcrContentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        ConfiguredNodeTypeDefinition contentNodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        contentNodeTypeDefinition.setName(NodeTypes.ContentNode.NAME);
        jcrContentConnectorDefinition.addNodeType(contentNodeTypeDefinition);
        ConfiguredNodeTypeDefinition folderNodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        contentNodeTypeDefinition.setName(NodeTypes.Content.NAME);
        jcrContentConnectorDefinition.addNodeType(folderNodeTypeDefinition);
        JcrContentConnector jcrContentConnector = Mockito.mock(JcrContentConnector.class);
        Mockito.when(jcrContentConnector.getContentConnectorDefinition()).thenReturn(jcrContentConnectorDefinition);
        availabilityRule = new AddDefinitionAvailabilityRule(definition, jcrContentConnector);
    }

    @Test
    public void testAddDefinitionAvailabilityRuleForProperty() throws RepositoryException {
        // GIVEN
        Property testProperty = rootNode.setProperty("testProperty", "");

        // WHEN
        boolean isAvailable = availabilityRule.isAvailableForItem(JcrItemUtil.getItemId(testProperty));

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testAddDefinitionAvailabilityRuleForContentNode() throws RepositoryException {
        // GIVEN
        Node testNode = rootNode.addNode("testNode", NodeTypes.ContentNode.NAME);
        testNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());

        // WHEN
        boolean isAvailable = availabilityRule.isAvailableForItem(JcrItemUtil.getItemId(testNode));

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testAddDefinitionAvailabilityRuleForContentNodeWhenAllSubNodesAndSubPropertiesExist() throws RepositoryException {
        // GIVEN
        Node testNode = rootNode.addNode("testNode", NodeTypes.ContentNode.NAME);
        testNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        testNode.setProperty("extends", "");
        testNode.setProperty("booleanProperty", "");
        testNode.setProperty("stringProperty", "");
        testNode.addNode("objectProperty", NodeTypes.ContentNode.NAME);
        testNode.addNode("mapProperty", NodeTypes.ContentNode.NAME);

        // WHEN
        boolean isAvailable = availabilityRule.isAvailableForItem(JcrItemUtil.getItemId(testNode));

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testAddDefinitionAvailabilityRuleForModuleFolderNode() throws RepositoryException {
        // GIVEN
        Node testModule = NodeUtil.createPath(rootNode, "modules/testmodule", NodeTypes.Content.NAME, true);
        testModule.setProperty("class", "");
        testModule.setProperty("extends", "");
        testModule.setProperty("version", "");

        // WHEN
        boolean isAvailable = availabilityRule.isAvailableForItem(JcrItemUtil.getItemId(testModule));

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testAddDefinitionAvailabilityRuleForModuleFolderNodeAndAllSubfoldersExist() throws RepositoryException {
        // GIVEN
        Node testModule = NodeUtil.createPath(rootNode, "modules/testmodule", NodeTypes.Content.NAME, true);
        testModule.setProperty("class", "");
        testModule.setProperty("extends", "");
        testModule.setProperty("version", "");
        testModule.addNode("apps", NodeTypes.ContentNode.NAME);
        testModule.addNode("templates", NodeTypes.ContentNode.NAME);
        testModule.addNode("dialogs", NodeTypes.ContentNode.NAME);
        testModule.addNode("commands", NodeTypes.ContentNode.NAME);
        testModule.addNode("fieldTypes", NodeTypes.ContentNode.NAME);
        testModule.addNode("virtualURIMapping", NodeTypes.ContentNode.NAME);
        testModule.addNode("renderers", NodeTypes.ContentNode.NAME);
        testModule.addNode("config", NodeTypes.ContentNode.NAME);

        // WHEN
        boolean isAvailable = availabilityRule.isAvailableForItem(JcrItemUtil.getItemId(testModule));

        // THEN
        assertFalse(isAvailable);
    }
}
