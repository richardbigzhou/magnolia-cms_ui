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
package info.magnolia.ui.contentapp.autosuggest.action;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
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
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionActionCallback;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogComponent;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogComponent.SelectedNames;
import info.magnolia.ui.contentapp.autosuggest.MockBeanForNameOfPropertyAndNameOfNode;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterForConfigurationApp;

import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Tests for {@link AddDefinitionAction}.
 */
public class AddDefinitionActionTest extends RepositoryTestCase {

    private Session session;
    private Node selectedNode;
    private AutoSuggesterForConfigurationApp autoSuggester;
    private JcrItemAdapter selectedItem;
    private AddDefinitionDialogComponent addDefinitionDialogComponent;
    private EventBus admincentralEventBus;
    private AddDefinitionActionCallback callback;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node rootNode = session.getRootNode();

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
        ContentConnector contentConnector = new JcrContentConnector(null, contentConnectorDefinition, componentProvider);

        selectedNode = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        selectedNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        selectedItem = new JcrNodeAdapter(selectedNode);

        addDefinitionDialogComponent = new AddDefinitionDialogComponent(selectedItem, autoSuggester, contentConnector);

        admincentralEventBus = mock(EventBus.class);
        callback = mock(AddDefinitionActionCallback.class);
    }

    @Test
    public void testExecuteWhenNothingSelected() throws Exception {
        // GIVEN
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertFalse(selectedNode.hasProperty("extends"));
        assertFalse(selectedNode.hasProperty("booleanProperty"));
        assertFalse(selectedNode.hasProperty("stringProperty"));
        assertFalse(selectedNode.hasNodes());
    }

    @Test
    public void testExecuteWhenSubPropertiesSelected() throws Exception {
        // GIVEN
        List<HorizontalLayout> suggestedPropertyRows = addDefinitionDialogComponent.getSuggestedPropertyRows();
        for (HorizontalLayout row : suggestedPropertyRows) {
            CheckBox checkbox = (CheckBox) row.getComponent(0);
            checkbox.setValue(Boolean.TRUE);
        }
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertTrue(selectedNode.hasProperty("extends"));
        assertTrue(selectedNode.hasProperty("booleanProperty"));
        assertTrue(selectedNode.hasProperty("stringProperty"));
        assertFalse(selectedNode.hasNodes());
    }

    @Test
    public void testExecuteWhenSubNodesSelected() throws Exception {
        // GIVEN
        List<CheckBox> suggestedRowCheckboxes = addDefinitionDialogComponent.getSuggestedRowCheckboxes();
        suggestedRowCheckboxes.get(0).setValue(Boolean.TRUE);
        suggestedRowCheckboxes.get(1).setValue(Boolean.TRUE);
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertFalse(selectedNode.hasProperty("extends"));
        assertFalse(selectedNode.hasProperty("booleanProperty"));
        assertFalse(selectedNode.hasProperty("stringProperty"));
        assertTrue(selectedNode.hasNodes());
        assertTrue(selectedNode.hasNode("objectProperty"));
        assertTrue(selectedNode.hasNode("mapProperty"));
        assertTrue(selectedNode.getNodes().getSize() == 2);
    }

    @Test
    public void testExecuteWhenAllSelected() throws Exception {
        // GIVEN
        VerticalLayout table = (VerticalLayout) addDefinitionDialogComponent.getComponent(1);
        HorizontalLayout firstRow = (HorizontalLayout) table.getComponent(0);
        CheckBox firstRowCheckbox = (CheckBox) firstRow.getComponent(0);
        firstRowCheckbox.setValue(Boolean.TRUE);
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertTrue(selectedNode.hasProperty("extends"));
        assertTrue(selectedNode.hasProperty("booleanProperty"));
        assertTrue(selectedNode.hasProperty("stringProperty"));
        assertTrue(selectedNode.hasNodes());
        assertTrue(selectedNode.hasNode("objectProperty"));
        assertTrue(selectedNode.hasNode("mapProperty"));
        assertTrue(selectedNode.getNodes().getSize() == 2);
    }

    @Test
    public void testExecuteWhenSomeSelectedSubNodesExist() throws Exception {
        // GIVEN
        List<CheckBox> suggestedRowCheckboxes = addDefinitionDialogComponent.getSuggestedRowCheckboxes();
        suggestedRowCheckboxes.get(0).setValue(Boolean.TRUE);
        suggestedRowCheckboxes.get(1).setValue(Boolean.TRUE);
        selectedNode.addNode("objectProperty", NodeTypes.ContentNode.NAME);
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertFalse(selectedNode.hasProperty("extends"));
        assertFalse(selectedNode.hasProperty("booleanProperty"));
        assertFalse(selectedNode.hasProperty("stringProperty"));
        assertTrue(selectedNode.hasNodes());
        assertTrue(selectedNode.hasNode("objectProperty"));
        assertTrue(selectedNode.hasNode("mapProperty"));
        assertTrue(selectedNode.getNodes().getSize() == 2);

        verify(admincentralEventBus).fireEvent(any(ContentChangedEvent.class));
        verify(callback).onAddDefinitionPerformed();
    }

    @Test
    public void testExecuteWhenAllSelectedSubNodesExist() throws Exception {
        // GIVEN
        List<CheckBox> suggestedRowCheckboxes = addDefinitionDialogComponent.getSuggestedRowCheckboxes();
        suggestedRowCheckboxes.get(0).setValue(Boolean.TRUE);
        suggestedRowCheckboxes.get(1).setValue(Boolean.TRUE);
        selectedNode.addNode("objectProperty", NodeTypes.ContentNode.NAME);
        selectedNode.addNode("mapProperty", NodeTypes.ContentNode.NAME);
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertFalse(selectedNode.hasProperty("extends"));
        assertFalse(selectedNode.hasProperty("booleanProperty"));
        assertFalse(selectedNode.hasProperty("stringProperty"));
        assertTrue(selectedNode.hasNodes());
        assertTrue(selectedNode.hasNode("objectProperty"));
        assertTrue(selectedNode.hasNode("mapProperty"));
        assertTrue(selectedNode.getNodes().getSize() == 2);

        verify(admincentralEventBus).fireEvent(any(ContentChangedEvent.class));
        verify(callback).onAddDefinitionPerformed();
    }

    @Test
    public void testExecuteWhenSomeSelectedSubPropertiesExist() throws Exception {
        // GIVEN
        List<HorizontalLayout> suggestedPropertyRows = addDefinitionDialogComponent.getSuggestedPropertyRows();
        for (HorizontalLayout row : suggestedPropertyRows) {
            CheckBox checkbox = (CheckBox) row.getComponent(0);
            checkbox.setValue(Boolean.TRUE);
        }
        selectedNode.setProperty("extends", "");
        selectedNode.setProperty("booleanProperty", true);
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertTrue(selectedNode.hasProperty("extends"));
        assertTrue(selectedNode.hasProperty("booleanProperty"));
        assertTrue(selectedNode.hasProperty("stringProperty"));
        assertFalse(selectedNode.hasNodes());

        verify(admincentralEventBus).fireEvent(any(ContentChangedEvent.class));
        verify(callback).onAddDefinitionPerformed();
    }

    @Test
    public void testExecuteWhenAllSelectedSubPropertiesExist() throws Exception {
        // GIVEN
        List<HorizontalLayout> suggestedPropertyRows = addDefinitionDialogComponent.getSuggestedPropertyRows();
        for (HorizontalLayout row : suggestedPropertyRows) {
            CheckBox checkbox = (CheckBox) row.getComponent(0);
            checkbox.setValue(Boolean.TRUE);
        }
        selectedNode.setProperty("extends", "");
        selectedNode.setProperty("booleanProperty", true);
        selectedNode.setProperty("stringProperty", "");
        SelectedNames selectedNames = addDefinitionDialogComponent.getSelectedNames();
        AddDefinitionAction action = new AddDefinitionAction(null, autoSuggester, selectedItem, selectedNames, admincentralEventBus, callback);

        // WHEN
        action.execute();

        // THEN
        assertTrue(selectedNode.hasProperty("class"));
        assertTrue(selectedNode.hasProperty("extends"));
        assertTrue(selectedNode.hasProperty("booleanProperty"));
        assertTrue(selectedNode.hasProperty("stringProperty"));
        assertFalse(selectedNode.hasNodes());

        verify(admincentralEventBus).fireEvent(any(ContentChangedEvent.class));
        verify(callback).onAddDefinitionPerformed();
    }
}
