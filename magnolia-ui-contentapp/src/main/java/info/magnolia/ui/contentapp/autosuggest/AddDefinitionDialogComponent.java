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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * The GUI for the 'Add definition' dialog containing a list of suggested nodes
 * and properties that can be added to a node.
 */
public class AddDefinitionDialogComponent extends VerticalLayout {

    private static Logger log = LoggerFactory.getLogger(AddDefinitionDialogComponent.class);

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_NODE_ICON_NAME = "icon-node-content";
    public static final String DEFAULT_PROPERTY_ICON_NAME = "icon-node-data";

    private Map<String, List<HorizontalLayout>> nodeTypeNameToSuggestedNodeRowsMap = new HashMap<String, List<HorizontalLayout>>();
    private List<HorizontalLayout> suggestedPropertyRows = new ArrayList<HorizontalLayout>();
    private List<CheckBox> suggestedRowCheckboxes = new ArrayList<CheckBox>();
    private Map<String, SortedSet<String>> nodeTypeNameToExistingNodeNamesMap = new HashMap<String, SortedSet<String>>();
    private boolean isHandlingSuggestedRowCheckboxClick = false;
    private boolean isHandlingFirstRowCheckboxClick = false;

    private int checkboxCount = 0;
    private int checkedCheckboxesCount = 0;

    public AddDefinitionDialogComponent(JcrItemAdapter selectedItem, AutoSuggester autoSuggester, ContentConnector contentConnector) {
        // Get the selected node
        JcrItemId itemId = selectedItem.getItemId();
        Node selectedNode = SessionUtil.getNodeByIdentifier(itemId.getWorkspace(), itemId.getUuid());

        if (selectedNode != null) {

            try {
                this.addStyleName("add-definition-dialog");

                // Create header row of table, containing column name
                SimpleTranslator simpleTranslator = Components.getComponent(SimpleTranslator.class);
                Label nodeNameColumnLabel = new Label(simpleTranslator.translate("websiteJcrBrowser.browser.views.treeview.name.label"));
                nodeNameColumnLabel.setStyleName("node-name-header");
                this.addComponent(nodeNameColumnLabel);

                // Create vertical layout containing rows of table
                VerticalLayout tableRows = new VerticalLayout();
                tableRows.addStyleName("node-list-box");
                this.addComponent(tableRows);

                // Create first table row containing the node being added to
                final HorizontalLayout firstRow = new HorizontalLayout();
                firstRow.addStyleName("definition-item");
                final CheckBox firstRowCheckbox = new CheckBox();
                Label firstRowIcon = new Label();
                firstRowIcon.addStyleName(getIconNameForNode(contentConnector, selectedNode, DEFAULT_NODE_ICON_NAME) + " v-table-icon-element root");
                firstRow.addComponent(firstRowCheckbox);
                firstRow.addComponent(firstRowIcon);
                firstRow.addComponent(new Label(selectedNode.getName()));
                tableRows.addComponent(firstRow);

                // Create rows for suggested nodes of each node type
                List<String> jcrNodeTypeNames = AutoSuggesterUtil.getJcrNodeTypeNamesFromContentConnector(contentConnector);
                for (String jcrNodeTypeName : jcrNodeTypeNames) {
                    List<HorizontalLayout> suggestedNodeRows = nodeTypeNameToSuggestedNodeRowsMap.get(jcrNodeTypeName);
                    if (suggestedNodeRows == null) {
                        suggestedNodeRows = new ArrayList<HorizontalLayout>();
                        nodeTypeNameToSuggestedNodeRowsMap.put(jcrNodeTypeName, suggestedNodeRows);
                    }

                    Collection<String> suggestedNodeNames = AutoSuggesterUtil.getSuggestedSubNodeNames(autoSuggester, selectedNode, jcrNodeTypeName);
                    if (suggestedNodeNames != null && !suggestedNodeNames.isEmpty()) {
                        SortedSet<String> sortedSuggestedNodeNames = new TreeSet<String>(suggestedNodeNames);
                        String iconName = getIconNameForNodeType(contentConnector, jcrNodeTypeName, DEFAULT_NODE_ICON_NAME);

                        for (String suggestedNodeName : sortedSuggestedNodeNames) {
                            HorizontalLayout row = new HorizontalLayout();
                            row.addStyleName("definition-item");
                            CheckBox checkbox = new CheckBox();
                            Label icon = new Label();
                            icon.addStyleName(iconName + " v-table-icon-element");
                            row.addComponent(checkbox);
                            row.addComponent(icon);
                            row.addComponent(new Label(suggestedNodeName));
                            tableRows.addComponent(row);
                            suggestedNodeRows.add(row);
                            suggestedRowCheckboxes.add(checkbox);
                            ++checkboxCount;
                        }
                    }
                }

                // Create rows containing suggested property names to add
                Collection<String> suggestedPropertyNames = AutoSuggesterUtil.getSuggestedSubPropertyNames(autoSuggester, selectedNode);
                if (suggestedPropertyNames != null && !suggestedPropertyNames.isEmpty()) {
                    SortedSet<String> sortedSuggestedPropertyNames = new TreeSet<String>(suggestedPropertyNames);

                    for (String subPropertyName : sortedSuggestedPropertyNames) {
                        HorizontalLayout row = new HorizontalLayout();
                        row.addStyleName("definition-item");
                        CheckBox checkbox = new CheckBox();
                        Label icon = new Label();
                        icon.addStyleName(DEFAULT_PROPERTY_ICON_NAME + " v-table-icon-element");
                        row.addComponent(checkbox);
                        row.addComponent(icon);
                        row.addComponent(new Label(subPropertyName));
                        tableRows.addComponent(row);
                        suggestedPropertyRows.add(row);
                        suggestedRowCheckboxes.add(checkbox);
                        ++checkboxCount;
                    }
                }

                // Get existing node names and group them by node type
                NodeIterator existingNodes = selectedNode.getNodes();
                while (existingNodes.hasNext()) {
                    Node existingNode = existingNodes.nextNode();
                    String existingNodeName = existingNode.getName();
                    NodeType existingNodeType = existingNode.getPrimaryNodeType();

                    if (existingNodeType != null) {
                        String existingNodeTypeName = existingNodeType.getName();
                        SortedSet<String> existingNodeNames = nodeTypeNameToExistingNodeNamesMap.get(existingNodeTypeName);

                        if (existingNodeNames == null) {
                            existingNodeNames = new TreeSet<String>();
                            nodeTypeNameToExistingNodeNamesMap.put(existingNodeTypeName, existingNodeNames);
                        }
                        existingNodeNames.add(existingNodeName);
                    }
                }

                // Create rows containing existing node names grouped by node type
                for (String jcrNodeTypeName : jcrNodeTypeNames) {

                    if (nodeTypeNameToExistingNodeNamesMap.containsKey(jcrNodeTypeName)) {
                        SortedSet<String> existingNodeNames = nodeTypeNameToExistingNodeNamesMap.get(jcrNodeTypeName);

                        if (existingNodeNames != null && !existingNodeNames.isEmpty()) {
                            String iconName = getIconNameForNodeType(contentConnector, jcrNodeTypeName, DEFAULT_NODE_ICON_NAME);

                            for (String existingNodeName : existingNodeNames) {
                                HorizontalLayout row = new HorizontalLayout();
                                row.addStyleName("definition-item disabled");
                                CheckBox checkbox = new CheckBox();
                                checkbox.setValue(true);
                                checkbox.setEnabled(false);
                                Label icon = new Label();
                                icon.addStyleName(iconName + " v-table-icon-element");
                                row.addComponent(checkbox);
                                row.addComponent(icon);
                                row.addComponent(new Label(existingNodeName));
                                tableRows.addComponent(row);
                            }
                        }
                    }
                }

                // Get existing property names
                SortedSet<String> existingPropertyNames = new TreeSet<String>();
                PropertyIterator properties = selectedNode.getProperties();
                while (properties.hasNext()) {
                    Property property = properties.nextProperty();
                    String propertyName = property.getName();

                    if (!propertyName.startsWith(NodeTypes.JCR_PREFIX) && !propertyName.startsWith(NodeTypes.MGNL_PREFIX)) {
                        existingPropertyNames.add(propertyName);
                    }
                }

                // Create rows containing existing property names
                for (String propertyName : existingPropertyNames) {
                    HorizontalLayout row = new HorizontalLayout();
                    row.addStyleName("definition-item disabled");
                    CheckBox checkbox = new CheckBox();
                    checkbox.setValue(true);
                    checkbox.setEnabled(false);
                    Label icon = new Label();
                    icon.addStyleName(DEFAULT_PROPERTY_ICON_NAME + " v-table-icon-element");
                    row.addComponent(checkbox);
                    row.addComponent(icon);
                    row.addComponent(new Label(propertyName));
                    tableRows.addComponent(row);
                }

                // Add handler to first row checkbox
                firstRowCheckbox.addValueChangeListener(new ValueChangeListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        if (!isHandlingSuggestedRowCheckboxClick) {
                            isHandlingFirstRowCheckboxClick = true;
                        }

                        boolean firstRowCheckboxChecked = firstRowCheckbox.getValue();

                        if (firstRowCheckboxChecked) {
                            firstRow.addStyleName("selected");
                        } else {
                            firstRow.removeStyleName("selected");
                        }

                        if (isHandlingFirstRowCheckboxClick) {
                            for (CheckBox checkbox : suggestedRowCheckboxes) {
                                checkbox.setValue(firstRowCheckboxChecked);
                            }

                            if (firstRowCheckboxChecked) {
                                checkedCheckboxesCount = checkboxCount;
                            }
                            else {
                                checkedCheckboxesCount = 0;
                            }
                        }

                        isHandlingFirstRowCheckboxClick = false;
                    }
                });

                // Add handler to suggested row checkboxes
                for (final CheckBox suggestedRowCheckbox : suggestedRowCheckboxes) {
                    suggestedRowCheckbox.addValueChangeListener(new ValueChangeListener() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            if (!isHandlingFirstRowCheckboxClick) {
                                isHandlingSuggestedRowCheckboxClick = true;
                            }

                            HorizontalLayout row = (HorizontalLayout) suggestedRowCheckbox.getParent();
                            if (!suggestedRowCheckbox.getValue()) {
                                row.removeStyleName("selected");
                                --checkedCheckboxesCount;

                                if (isHandlingSuggestedRowCheckboxClick && firstRowCheckbox.getValue()) {
                                    firstRowCheckbox.setValue(false);
                                }
                            } else {
                                row.addStyleName("selected");
                                ++checkedCheckboxesCount;

                                if (isHandlingSuggestedRowCheckboxClick && checkedCheckboxesCount == checkboxCount) {
                                    firstRowCheckbox.setValue(true);
                                }
                            }

                            isHandlingSuggestedRowCheckboxClick = false;
                        }
                    });
                }
            } catch (RepositoryException e) {
                log.warn("Could not create Add definition dialog component: " + e);
            }
        }
        else {
            log.warn("Could not get the selected node for Add definition dialog.");
        }
    }

    private String getIconNameForNode(ContentConnector contentConnector, Node node, String defaultIconName) {
        if (node != null) {
            try {
                NodeType nodeType = node.getPrimaryNodeType();

                if (nodeType != null) {
                    return getIconNameForNodeType(contentConnector, nodeType.getName(), defaultIconName);
                }
                else {
                    return defaultIconName;
                }
            } catch (RepositoryException e) {
                return defaultIconName;
            }
        }
        else {
            return defaultIconName;
        }
    }

    private String getIconNameForNodeType(ContentConnector contentConnector, String nodeTypeName, String defaultIconName) {
        if (nodeTypeName != null) {

            if (contentConnector instanceof JcrContentConnector) {
                JcrContentConnectorDefinition jcrContentConnectorDefinition = ((JcrContentConnector) contentConnector).getContentConnectorDefinition();

                if (jcrContentConnectorDefinition != null) {
                    List<NodeTypeDefinition> nodeTypes = jcrContentConnectorDefinition.getNodeTypes();

                    if (nodeTypes != null) {

                        for (NodeTypeDefinition nodeTypeDefinition : nodeTypes) {

                            if (StringUtils.equals(nodeTypeDefinition.getName(), nodeTypeName)) {
                                return nodeTypeDefinition.getIcon();
                            }
                        }

                        return defaultIconName;
                    }
                    else {
                        return defaultIconName;
                    }
                }
                else {
                    return defaultIconName;
                }
            }
            else {
                return defaultIconName;
            }
        }
        else {
            return defaultIconName;
        }
    }

    public SelectedNames getSelectedNames() {
        SelectedNames selectedNames = new SelectedNames();

        for (Map.Entry<String, List<HorizontalLayout>> nodeTypeNameToSuggestedNodeRowsMapEntry : nodeTypeNameToSuggestedNodeRowsMap.entrySet()) {
            List<String> selectedNodeNames = new ArrayList<String>();

            for (HorizontalLayout row : nodeTypeNameToSuggestedNodeRowsMapEntry.getValue()) {
                CheckBox checkbox = (CheckBox) row.getComponent(0);

                if (checkbox.getValue()) {
                    Label label = (Label) row.getComponent(2);
                    selectedNodeNames.add(label.getValue());
                }
            }

            selectedNames.addSelectedNodeNames(nodeTypeNameToSuggestedNodeRowsMapEntry.getKey(), selectedNodeNames);
        }

        for (HorizontalLayout row : suggestedPropertyRows) {
            CheckBox checkbox = (CheckBox) row.getComponent(0);

            if (checkbox.getValue()) {
                Label label = (Label) row.getComponent(2);
                selectedNames.addSelectedPropertyName(label.getValue());
            }
        }

        return selectedNames;
    }

    /**
     * Names of selected nodes and properties.
     */
    public static class SelectedNames {
        private Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = new HashMap<String, List<String>>();
        private List<String> selectedSubPropertyNames = new ArrayList<String>();

        public Map<String, List<String>> getNodeTypeNameToSelectedNodeNamesMap() {
            return nodeTypeNameToSelectedNodeNamesMap;
        }

        private void addSelectedNodeName(String nodeTypeName, String nodeName) {
            List<String> nodeNames = nodeTypeNameToSelectedNodeNamesMap.get(nodeTypeName);
            if (nodeNames == null) {
                nodeNames = new ArrayList<String>();
                nodeTypeNameToSelectedNodeNamesMap.put(nodeTypeName, nodeNames);
            }
            nodeNames.add(nodeName);
        }

        private void addSelectedNodeNames(String nodeTypeName, Collection<String> newNodeNames) {
            List<String> nodeNames = nodeTypeNameToSelectedNodeNamesMap.get(nodeTypeName);
            if (nodeNames == null) {
                nodeNames = new ArrayList<String>();
                nodeTypeNameToSelectedNodeNamesMap.put(nodeTypeName, nodeNames);
            }
            nodeNames.addAll(newNodeNames);
        }

        public List<String> getSelectedPropertyNames() {
            return selectedSubPropertyNames;
        }

        private void addSelectedPropertyName(String name) {
            selectedSubPropertyNames.add(name);
        }

        public boolean isEmpty() {
            if (!selectedSubPropertyNames.isEmpty()) {
                return false;
            }
            else {
                for (Collection<String> selectedNodeNames : nodeTypeNameToSelectedNodeNamesMap.values()) {
                    if (!selectedNodeNames.isEmpty()) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    public List<HorizontalLayout> getSuggestedPropertyRows() {
        return new ArrayList<HorizontalLayout>(suggestedPropertyRows);
    }

    public List<CheckBox> getSuggestedRowCheckboxes() {
        return new ArrayList<CheckBox>(suggestedRowCheckboxes);
    }
}
