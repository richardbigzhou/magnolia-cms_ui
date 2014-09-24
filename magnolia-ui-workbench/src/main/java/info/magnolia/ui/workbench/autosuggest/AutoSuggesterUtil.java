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
package info.magnolia.ui.workbench.autosuggest;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.api.autosuggest.AutoSuggester.AutoSuggesterResult;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods that use {@link AutoSuggester}.
 */
public class AutoSuggesterUtil {

    private static Logger log = LoggerFactory.getLogger(AutoSuggesterUtil.class);

    /**
     * Get suggestions for names of sub-properties of a node.
     */
    public static Collection<String> getSuggestedSubPropertyNames(AutoSuggester autoSuggester, Node node) {
        if (autoSuggester == null || node == null) {
            return new LinkedList<String>();
        }

        String uniqueName = null;

        try {
            uniqueName = Path.getUniqueLabel(node.getSession(), node.getPath(), "untitled");

            if (uniqueName != null) {
                Property uniqueSubProperty = node.setProperty(uniqueName, "");

                if (uniqueSubProperty != null) {
                    JcrItemId uniqueSubPropertyId = JcrItemUtil.getItemId(uniqueSubProperty);
                    AutoSuggesterResult subPropertyNameSuggestions = autoSuggester.getSuggestionsFor(uniqueSubPropertyId, "jcrName");
                    uniqueSubProperty.remove();

                    if (subPropertyNameSuggestions != null && subPropertyNameSuggestions.suggestionsAvailable()) {
                        return subPropertyNameSuggestions.getSuggestions();
                    }
                    else {
                        return new LinkedList<String>();
                    }
                }
                else {
                    return new LinkedList<String>();
                }
            }
            else {
                return new LinkedList<String>();
            }
        } catch (RepositoryException e) {
            log.warn("Could not get suggested sub-property names of node + " + node + ": " + e);
            return new LinkedList<String>();
        } finally {
            try {
                Property subProperty = null;
                if (node != null && uniqueName != null && (subProperty = node.getProperty(uniqueName)) != null) {
                    subProperty.remove();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Get suggestions for names of content sub-nodes of a node.
     */
    public static Collection<String> getSuggestedSubContentNodeNames(AutoSuggester autoSuggester, Node node) {
        return getSuggestedSubNodeNames(autoSuggester, node, NodeTypes.ContentNode.NAME);
    }

    /**
     * Get suggestions for names of folder sub-nodes of a node.
     */
    public static Collection<String> getSuggestedSubContentNames(AutoSuggester autoSuggester, Node node) {
        return getSuggestedSubNodeNames(autoSuggester, node, NodeTypes.Content.NAME);
    }

    /**
     * Get suggestions for names of sub-nodes of the specified type.
     */
    public static Collection<String> getSuggestedSubNodeNames(AutoSuggester autoSuggester, Node node, String subNodeTypeName) {
        if (autoSuggester == null || node == null || subNodeTypeName == null) {
            return new LinkedList<String>();
        }

        String uniqueName = null;

        try {
            uniqueName = Path.getUniqueLabel(node.getSession(), node.getPath(), "untitled");

            if (uniqueName != null) {
                Node uniqueSubNode = node.addNode(uniqueName, subNodeTypeName);

                if (uniqueSubNode != null) {
                    JcrItemId subNodeId = JcrItemUtil.getItemId(uniqueSubNode);
                    AutoSuggesterResult subNodeNameSuggestions = autoSuggester.getSuggestionsFor(subNodeId, "jcrName");
                    uniqueSubNode.remove();

                    if (subNodeNameSuggestions != null && subNodeNameSuggestions.suggestionsAvailable()) {
                        return subNodeNameSuggestions.getSuggestions();
                    }
                    else {
                        return new LinkedList<String>();
                    }
                }
                else {
                    return new LinkedList<String>();
                }
            }
            else {
                return new LinkedList<String>();
            }
        } catch (RepositoryException e) {
            log.warn("Could not get suggested sub-node of type " + subNodeTypeName + " names of node + " + node + ": " + e);
            return new LinkedList<String>();
        } finally {
            try {
                Node subNode = null;
                if (node != null && uniqueName != null && (subNode = node.getNode(uniqueName)) != null) {
                    subNode.remove();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Get all type names from a {@link JcrContentConnector}.
     */
    public static List<String> getJcrNodeTypeNamesFromContentConnector(ContentConnector contentConnector) {
        List<String> availableNodeTypes = new LinkedList<String>();

        if (contentConnector instanceof JcrContentConnector) {
            JcrContentConnectorDefinition jcrContentConnectorDefinition = ((JcrContentConnector) contentConnector).getContentConnectorDefinition();

            if (jcrContentConnectorDefinition != null) {
                List<NodeTypeDefinition> nodeTypes = jcrContentConnectorDefinition.getNodeTypes();

                if (nodeTypes != null) {

                    for (NodeTypeDefinition nodeTypeDefinition : nodeTypes) {

                        String nodeTypeName = nodeTypeDefinition.getName();

                        if (nodeTypeName != null) {
                            availableNodeTypes.add(nodeTypeName);
                        }
                    }

                    return availableNodeTypes;
                }
                else {
                    return availableNodeTypes;
                }
            }
            else {
                return availableNodeTypes;
            }
        }
        else {
            return availableNodeTypes;
        }
    }
}
