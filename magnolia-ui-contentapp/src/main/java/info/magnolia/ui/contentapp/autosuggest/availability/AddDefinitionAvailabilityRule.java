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

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterUtil;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if there are suggestions for names for sub-nodes and sub-properties for a node
 * using the {@link AutoSuggester} configured for the rule.
 */
public class AddDefinitionAvailabilityRule extends AbstractAvailabilityRule {

    private static Logger log = LoggerFactory.getLogger(AddDefinitionAvailabilityRule.class);

    private AddDefinitionAvailabilityRuleDefinition ruleDefinition;
    private ContentConnector contentConnector;
    private AutoSuggester autoSuggester;

    @Inject
    public AddDefinitionAvailabilityRule(AddDefinitionAvailabilityRuleDefinition ruleDefinition, ContentConnector contentConnector) {
        this.ruleDefinition = ruleDefinition;
        this.contentConnector = contentConnector;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        if (autoSuggester == null && ruleDefinition != null) {
            try {
                Class<? extends AutoSuggester> autoSuggesterClass = ruleDefinition.getAutoSuggesterClass();
                if (autoSuggesterClass != null) {
                    autoSuggester = autoSuggesterClass.newInstance();
                }
            } catch (InstantiationException e) {
                autoSuggester = null;
                log.warn("Could not instantiate AutoSuggester class " + ruleDefinition.getAutoSuggesterClass() + ": " + e);
            } catch (IllegalAccessException e) {
                autoSuggester = null;
                log.warn("Could not instantiate AutoSuggester class " + ruleDefinition.getAutoSuggesterClass() + ": " + e);
            }
        }

        if (autoSuggester != null) {

            if (itemId instanceof JcrNodeItemId) {
                JcrNodeItemId jcrNodeItemId = (JcrNodeItemId) itemId;
                Node node = SessionUtil.getNodeByIdentifier(jcrNodeItemId.getWorkspace(), jcrNodeItemId.getUuid());

                if (node != null) {
                    Collection<String> subPropertyNameSuggestions = AutoSuggesterUtil.getSuggestedSubPropertyNames(autoSuggester, node);

                    if (subPropertyNameSuggestions != null && !subPropertyNameSuggestions.isEmpty()) {
                        return true;
                    }
                    else {
                        List<String> availableNodeTypeNames = AutoSuggesterUtil.getJcrNodeTypeNamesFromContentConnector(contentConnector);

                        if (availableNodeTypeNames != null) {

                            for (String nodeTypeName : availableNodeTypeNames) {
                                Collection<String> subNodeNameSuggestions = AutoSuggesterUtil.getSuggestedSubNodeNames(autoSuggester, node, nodeTypeName);

                                if (subNodeNameSuggestions != null && !subNodeNameSuggestions.isEmpty()) {
                                    return true;
                                }
                            }

                            return false;
                        }
                        else {
                            return false;
                        }
                    }
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

}
