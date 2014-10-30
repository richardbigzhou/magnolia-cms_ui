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
package info.magnolia.ui.contentapp.autosuggest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogComponent.SelectedNames;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterForConfigurationApp;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Tests for {@link AddDefinitionDialogComponent}.
 */
public class AddDefinitionDialogComponentTest extends RepositoryTestCase {

    private Session session;
    private Node rootNode;
    private AutoSuggesterForConfigurationApp autoSuggester;
    private ContentConnector contentConnector;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        rootNode = session.getRootNode();

        ComponentsTestUtil.setImplementation(EventBus.class, SimpleEventBus.class);
        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);

        ContextLocaleProvider provider = mock(ContextLocaleProvider.class);
        when(provider.getLocale()).thenReturn(Locale.ENGLISH);
        ComponentsTestUtil.setInstance(LocaleProvider.class, provider);

        autoSuggester = new AutoSuggesterForConfigurationApp();

        ConfiguredJcrContentConnectorDefinition contentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        contentConnectorDefinition.setWorkspace(RepositoryConstants.CONFIG);
        ConfiguredNodeTypeDefinition mainNodeType = new ConfiguredNodeTypeDefinition();
        mainNodeType.setName("mgnl:contentNode");
        mainNodeType.setIcon("icon-node-content");
        ConfiguredNodeTypeDefinition folderNodeType = new ConfiguredNodeTypeDefinition();
        folderNodeType.setName("mgnl:content");
        folderNodeType.setIcon("icon-folder-l");
        contentConnectorDefinition.addNodeType(mainNodeType);
        contentConnectorDefinition.addNodeType(folderNodeType);
        ComponentProvider componentProvider = new MockComponentProvider();
        contentConnector = new JcrContentConnector(null, contentConnectorDefinition, componentProvider);
    }

    @Test
    public void testAddDefinitionDialogComponentWhenNoSubItemsExist() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);

        // WHEN
        firstRowCheckbox.setValue(Boolean.TRUE);

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        assertTrue(addDefinitionDialogComponent.getSuggestedPropertyRows().size() == 3);
    }

    @Test
    public void testAddDefinitionDialogComponentWhenSubPropertyExists() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        parentNode.setProperty("extends", "");
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);

        // WHEN
        firstRowCheckbox.setValue(Boolean.TRUE);

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        assertTrue(addDefinitionDialogComponent.getSuggestedPropertyRows().size() == 2);
    }

    @Test
    public void testAddDefinitionDialogComponentWhenSubNodeExists() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        parentNode.addNode("mapProperty", NodeTypes.ContentNode.NAME);
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);

        // WHEN
        firstRowCheckbox.setValue(Boolean.TRUE);

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 1);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        assertTrue(addDefinitionDialogComponent.getSuggestedPropertyRows().size() == 3);
    }

    @Test
    public void testGetSelectedNamesWhenNothingSelected() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);

        // WHEN

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertTrue(selectedNames.isEmpty());
    }

    @Test
    public void testGetSelectedNamesWhenNodesSelected() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        List<CheckBox> suggestedRowCheckboxes = addDefinitionDialogComponent.getSuggestedRowCheckboxes();

        // WHEN
        suggestedRowCheckboxes.get(0).setValue(Boolean.TRUE);
        suggestedRowCheckboxes.get(1).setValue(Boolean.TRUE);

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertFalse(selectedNames.isEmpty());
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("objectProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("mapProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        assertTrue(selectedNames.getSelectedPropertyNames().size() == 0);
    }

    @Test
    public void testGetSelectedNamesWhenPropertiesSelected() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        List<HorizontalLayout> suggestedPropertyRows = addDefinitionDialogComponent.getSuggestedPropertyRows();

        // WHEN
        for (HorizontalLayout row : suggestedPropertyRows) {
            CheckBox checkbox = (CheckBox) row.getComponent(0);
            checkbox.setValue(Boolean.TRUE);
        }

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertFalse(selectedNames.isEmpty());
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 0);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        List<String> selectedSubPropertyNames = selectedNames.getSelectedPropertyNames();
        assertTrue(selectedSubPropertyNames.size() == 3);
        assertTrue(selectedSubPropertyNames.contains("extends"));
        assertTrue(selectedSubPropertyNames.contains("booleanProperty"));
        assertTrue(selectedSubPropertyNames.contains("stringProperty"));
    }

    @Test
    public void testSelectAllCheckboxWhenChecked() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);

        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);

        // WHEN
        firstRowCheckbox.setValue(Boolean.TRUE);

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertFalse(selectedNames.isEmpty());
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("objectProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("mapProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        List<String> selectedSubPropertyNames = selectedNames.getSelectedPropertyNames();
        assertTrue(selectedSubPropertyNames.size() == 3);
        assertTrue(selectedSubPropertyNames.contains("extends"));
        assertTrue(selectedSubPropertyNames.contains("booleanProperty"));
        assertTrue(selectedSubPropertyNames.contains("stringProperty"));
    }

    @Test
    public void testSelectAllCheckboxWhenUnchecked() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);

        // WHEN
        firstRowCheckbox.setValue(Boolean.TRUE);
        firstRowCheckbox.setValue(Boolean.FALSE);

        // THEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertTrue(selectedNames.isEmpty());
    }

    @Test
    public void testSelectAllCheckboxWhenCheckedAndUncheckSubNode() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);
        List<CheckBox> suggestedRowCheckboxes = addDefinitionDialogComponent.getSuggestedRowCheckboxes();

        // WHEN
        firstRowCheckbox.setValue(Boolean.TRUE);
        suggestedRowCheckboxes.get(0).setValue(Boolean.FALSE);

        // THEN
        assertFalse(firstRowCheckbox.getValue());
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertFalse(selectedNames.isEmpty());
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 1);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("objectProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        List<String> selectedSubPropertyNames = selectedNames.getSelectedPropertyNames();
        assertTrue(selectedSubPropertyNames.size() == 3);
        assertTrue(selectedSubPropertyNames.contains("extends"));
        assertTrue(selectedSubPropertyNames.contains("booleanProperty"));
        assertTrue(selectedSubPropertyNames.contains("stringProperty"));
    }

    @Test
    public void testSelectAllCheckboxWhenAllSubItemsSelected() throws Exception {
        // GIVEN
        Node parentNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parentNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        JcrNodeAdapter selectedItem = new JcrNodeAdapter(parentNode);
        AddDefinitionDialogComponent addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        List<CheckBox> suggestedRowCheckboxes = addDefinitionDialogComponent.getSuggestedRowCheckboxes();

        // WHEN
        for (CheckBox checkbox : suggestedRowCheckboxes) {
            checkbox.setValue(Boolean.TRUE);
        }

        // THEN
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);
        assertTrue(firstRowCheckbox.getValue());

        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        assertFalse(selectedNames.isEmpty());
        Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = selectedNames.getNodeTypeNameToSelectedNodeNamesMap();
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.ContentNode.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.containsKey(NodeTypes.Content.NAME));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).size() == 2);
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("objectProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.ContentNode.NAME).contains("mapProperty"));
        assertTrue(nodeTypeNameToSelectedNodeNamesMap.get(NodeTypes.Content.NAME).size() == 0);
        List<String> selectedSubPropertyNames = selectedNames.getSelectedPropertyNames();
        assertTrue(selectedSubPropertyNames.size() == 3);
        assertTrue(selectedSubPropertyNames.contains("extends"));
        assertTrue(selectedSubPropertyNames.contains("booleanProperty"));
        assertTrue(selectedSubPropertyNames.contains("stringProperty"));
    }
}