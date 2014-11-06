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
import info.magnolia.ui.api.autosuggest.AutoSuggester.AutoSuggesterResult;
import info.magnolia.ui.vaadin.autosuggest.AutoSuggestTextFieldEx;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterForConfigurationApp;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterUtil;
import info.magnolia.ui.workbench.autosuggest.HelperUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
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
    private List<TextField> propertyTextFields = new ArrayList<TextField>();
    private Map<String, String> helpMessages;
    private List<Button> helpButtons = new ArrayList<Button>();
    private int helpOpenCount = 0;
    private Button helpAll;
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
                HorizontalLayout header = new HorizontalLayout();
                Label titleLb = new Label(simpleTranslator.translate("websiteJcrBrowser.browser.views.treeview.name.label"));
                header.setStyleName("header");
                header.addComponent(titleLb);

                helpAll = new Button();
                helpAll.setData("close");
                helpAll.addStyleName("help-icon help-all");
                helpAll.addStyleName("help-icon-close");
                helpAll.addClickListener(new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        String status = (String) helpAll.getData();
                        if (helpButtons != null) {
                            for (Button helpButton : helpButtons) {
                                if (status.equals(helpButton.getData())) {
                                    helpButton.click();
                                }
                            }
                        }
                    }
                });
                header.addComponent(helpAll);
                this.addComponent(header);

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

                Collection<String> allSubNodeNames = new ArrayList<String>();

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
                        allSubNodeNames.addAll(suggestedNodeNames);
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
                        final HorizontalLayout row = new HorizontalLayout();
                        row.addStyleName("definition-item");
                        final CheckBox checkbox = new CheckBox();
                        Label icon = new Label();
                        icon.addStyleName(DEFAULT_PROPERTY_ICON_NAME + " v-table-icon-element");
                        row.addComponent(checkbox);
                        row.addComponent(icon);
                        row.addComponent(new Label(subPropertyName));

                        Property property = selectedNode.setProperty(subPropertyName, "");
                        JcrItemId propertyItemId = JcrItemUtil.getItemId(property);
                        TextField field;
                        if (autoSuggester != null) {
                            AutoSuggesterResult autoSuggesterResult = autoSuggester.getSuggestionsFor(propertyItemId, "value");
                            property.remove();
                            field = new AutoSuggestTextFieldEx(autoSuggesterResult);
                        } else {
                            field = new TextField();
                        }
                        field.setImmediate(true);
                        propertyTextFields.add(field);
                        field.addStyleName("suggestion-field");
                        row.addComponent(field);

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

                helpMessages = getHelpMessage(allSubNodeNames, suggestedPropertyNames, autoSuggester, selectedNode);

                if (helpMessages != null && helpMessages.size() > 0) {
                    for (int i = 1; i < tableRows.getComponentCount(); i++) {
                        HorizontalLayout row = (HorizontalLayout) tableRows.getComponent(i);
                        final Label memberLabel = (Label) row.getComponent(2);
                        if (helpMessages.get(memberLabel.getValue()) != null) {
                            final Button help = new Button();
                            help.setData("close");
                            help.addStyleName("help-icon");
                            help.addStyleName("help-icon-close");
                            help.addClickListener(new ClickListener() {

                                @Override
                                public void buttonClick(ClickEvent event) {
                                    clickHelp(event, memberLabel.getValue());
                                }
                            });
                            row.addComponent(help);
                            helpButtons.add(help);
                        }
                    }
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

                // Add handler to suggested row property fields.
                for (final TextField propertyField : propertyTextFields) {
                    propertyField.addFocusListener(new FocusListener() {

                        @Override
                        public void focus(FocusEvent event) {
                            propertyField.addStyleName("focus");
                        }
                    });
                    propertyField.addBlurListener(new BlurListener() {

                        @Override
                        public void blur(BlurEvent event) {
                            propertyField.removeStyleName("focus");
                        }
                    });
                    propertyField.addTextChangeListener(new TextChangeListener() {

                        @Override
                        public void textChange(TextChangeEvent event) {
                            HorizontalLayout row = (HorizontalLayout) propertyField.getParent();
                            CheckBox checkbox = (CheckBox) row.getComponent(0);
                            checkbox.setValue(true);
                            if (event.getText() == null || event.getText().length() == 0) {
                                row.addStyleName("empty");
                            } else {
                                row.removeStyleName("empty");
                            }
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

    private void clickHelp(ClickEvent event, String itemName) {
        Button help = event.getButton();
        HorizontalLayout hp = (HorizontalLayout) help.getParent();
        VerticalLayout tableRows = (VerticalLayout) hp.getParent();
        int index = tableRows.getComponentIndex(hp);
        if ("close".equals(help.getData())) {
            Label helpLabel = new Label(helpMessages.get(itemName));
            helpLabel.setWidth("100%");
            helpLabel.addStyleName("help-message");
            tableRows.addComponent(helpLabel, index + 1);
            help.removeStyleName("help-icon-close");
            help.addStyleName("help-icon-open");
            help.setData("open");
            helpOpenCount++;
        } else {
            Component comp = tableRows.getComponent(index + 1);
            tableRows.removeComponent(comp);
            help.removeStyleName("help-icon-open");
            help.addStyleName("help-icon-close");
            help.setData("close");
            helpOpenCount--;
        }
        if (helpOpenCount == helpButtons.size()) {
            helpAll.removeStyleName("help-icon-close");
            helpAll.addStyleName("help-icon-open");
            helpAll.setData("open");
        } else {
            helpAll.setData("close");
            helpAll.removeStyleName("help-icon-open");
            helpAll.addStyleName("help-icon-close");
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
                TextField field = (TextField) row.getComponent(3);
                selectedNames.addSelectedProperty(new PropertyItem(label.getValue(), field.getValue()));
            }
        }

        return selectedNames;
    }

    private Map<String, String> getHelpMessage(Collection<String> allSubNodeNames, Collection<String> suggestedPropertyNames, AutoSuggester autoSuggester, Node parentNode) {
        String parentClass = null;
        Set<String> suggestionItems = new HashSet<String>();
        try {
            NodeIterator nodeIterator = parentNode.getNodes();
            while (nodeIterator.hasNext()) {
                suggestionItems.add(nodeIterator.nextNode().getName());
            }
            PropertyIterator propertyIterator = parentNode.getProperties();
            while (propertyIterator.hasNext()) {
                suggestionItems.add(propertyIterator.nextProperty().getName());
            }

            if (parentNode.hasProperty("class")) {
                Property classProperty = parentNode.getProperty("class");
                if (classProperty != null) {
                    parentClass = classProperty.getString();
                }
            }
            else {
                Class<?> mostGeneralNodeClass = ((AutoSuggesterForConfigurationApp) autoSuggester).getMostGeneralNodeClass(parentNode);
                if (mostGeneralNodeClass != null) {
                    parentClass = mostGeneralNodeClass.getName();
                }
            }
            if (parentClass != null) {
                suggestionItems.addAll(allSubNodeNames);
                suggestionItems.addAll(suggestedPropertyNames);
                try {
                    return HelperUtil.getHelper(parentClass, suggestionItems);
                } catch (Exception ex) {
                    log.warn("Couldn't get help message for class " + parentClass, ex);
                }
            }
        } catch (RepositoryException re) {
            log.warn("Couldn't get help message for class " + parentClass, re);
        }
        return null;
    }

    /**
     * Names of selected nodes and properties.
     */
    public static class SelectedNames {
        private Map<String, List<String>> nodeTypeNameToSelectedNodeNamesMap = new HashMap<String, List<String>>();
        private List<PropertyItem> selectedSubProperties = new ArrayList<PropertyItem>();

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

        public List<PropertyItem> getSelectedProperties() {
            return selectedSubProperties;
        }

        private void addSelectedProperty(PropertyItem propertyItem) {
            selectedSubProperties.add(propertyItem);
        }

        public boolean isEmpty() {
            if (!selectedSubProperties.isEmpty()) {
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

    /**
     * Both name and value of property.
     */
    public static class PropertyItem {
        private String name;

        private String value;

        public PropertyItem() {

        }

        public PropertyItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof PropertyItem)) {
                return false;
            }
            PropertyItem another = (PropertyItem) obj;
            return name == null ? name == another.name : name.equals(another.name)
                    && value == null ? value == another.value : value.equals(another.value);
        }
    }

    public List<HorizontalLayout> getSuggestedPropertyRows() {
        return new ArrayList<HorizontalLayout>(suggestedPropertyRows);
    }

    public List<CheckBox> getSuggestedRowCheckboxes() {
        return new ArrayList<CheckBox>(suggestedRowCheckboxes);
    }
}
