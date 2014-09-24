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

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.api.autosuggest.AutoSuggester.AutoSuggesterResult;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionActionCallback;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogComponent.SelectedNames;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that adds items selected in a dialog to a node.
 */
public class AddDefinitionAction extends AbstractAction<ActionDefinition> {

    private static Logger log = LoggerFactory.getLogger(AddDefinitionAction.class);

    private AutoSuggester autoSuggester;

    private JcrItemAdapter selectedItem;

    private SelectedNames selectedNames;

    private final EventBus admincentralEventBus;

    private AddDefinitionActionCallback callback;

    @Inject
    protected AddDefinitionAction(
            ActionDefinition definition,
            AutoSuggester autoSuggester,
            JcrItemAdapter selectedItem,
            SelectedNames selectedNames,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
            AddDefinitionActionCallback callback) {
        super(definition);
        this.autoSuggester = autoSuggester;
        this.selectedItem = selectedItem;
        this.selectedNames = selectedNames;
        this.admincentralEventBus = admincentralEventBus;
        this.callback = callback;
    }

    @Override
    public void execute() throws ActionExecutionException {

        if (selectedNames != null) {

            JcrItemId selectedItemId = selectedItem.getItemId();
            Node selectedNode = SessionUtil.getNodeByIdentifier(selectedItemId.getWorkspace(), selectedItemId.getUuid());

            if (selectedNode != null) {

                // Add selected sub-nodes
                for (Map.Entry<String, List<String>> nodeTypeNameToSelectedNodeNamesMapEntry : selectedNames.getNodeTypeNameToSelectedNodeNamesMap().entrySet()) {
                    String nodeTypeName = nodeTypeNameToSelectedNodeNamesMapEntry.getKey();

                    for (String selectedNodeName : nodeTypeNameToSelectedNodeNamesMapEntry.getValue()) {
                        try {
                            if (!selectedNode.hasNode(selectedNodeName)) {
                                selectedNode.addNode(selectedNodeName, nodeTypeName);
                            }
                        } catch (RepositoryException ex) {
                            log.warn("Could not add sub-node: " + ex);
                        }
                    }
                }

                // Add selected sub-properties
                for (String propertyName : selectedNames.getSelectedPropertyNames()) {
                    try {
                        Property newProperty = selectedNode.setProperty(propertyName, "");

                        // Try to set property to appropriate type
                        JcrItemId propertyItemId = JcrItemUtil.getItemId(newProperty);
                        AutoSuggesterResult suggesterResult = autoSuggester.getSuggestionsFor(propertyItemId, "type");
                        if (suggesterResult != null) {
                            Collection<String> suggestedTypes = suggesterResult.getSuggestions();

                            if (suggestedTypes != null && suggestedTypes.size() == 1) {
                                String typeName = suggestedTypes.iterator().next();

                                if ("Boolean".equals(typeName)) {
                                    newProperty.setValue(false);
                                } else if ("Long".equals(typeName)) {
                                    newProperty.setValue(0L);
                                } else if ("Double".equals(typeName)) {
                                    newProperty.setValue(0D);
                                } else {
                                    newProperty.setValue("");
                                }
                            } else {
                                newProperty.setValue("");
                            }
                        }
                        else {
                            newProperty.setValue("");
                        }

                    } catch (RepositoryException ex) {
                        log.warn("Could not add sub-property: " + ex);
                    }
                }

                try {
                    selectedNode.getSession().save();

                    if (!selectedNames.isEmpty()) {
                        admincentralEventBus.fireEvent(new ContentChangedEvent(selectedItemId, true));
                        callback.onAddDefinitionPerformed();
                        return;
                    }
                    else {
                        callback.onAddDefinitionCancelled();
                        return;
                    }
                } catch (RepositoryException ex) {
                    log.warn("Could not save added sub-nodes and sub-properties: " + ex);
                    callback.onAddDefinitionCancelled();
                    return;
                }
            }
            else {
                log.warn("Could not get the selected node to add to.");
                callback.onAddDefinitionCancelled();
                return;
            }
        }
        else {
            callback.onAddDefinitionCancelled();
            return;
        }
    }
}
