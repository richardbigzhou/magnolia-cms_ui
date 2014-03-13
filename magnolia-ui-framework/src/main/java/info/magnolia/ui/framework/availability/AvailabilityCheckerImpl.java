/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.framework.availability;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;
import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;
import info.magnolia.ui.framework.availability.shorthandrules.AccessGrantedRule;
import info.magnolia.ui.framework.availability.shorthandrules.IsRootItemAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.JcrItemNodeTypeAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.JcrNodesAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.JcrPropertiesAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.MultipleItemsAllowedRule;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

/**
 * Implements {@link info.magnolia.ui.api.availability.AvailabilityChecker}.
 */
public class AvailabilityCheckerImpl implements AvailabilityChecker {

    private ComponentProvider componentProvider;

    private ContentConnector contentConnector;

    private JcrNodesAllowedRule nodesAllowedShorthandRule = new JcrNodesAllowedRule();
    private JcrPropertiesAllowedRule propertiesAllowedShorthandRule = new JcrPropertiesAllowedRule();
    private MultipleItemsAllowedRule multipleItemsAllowedShorthandRule = new MultipleItemsAllowedRule();
    private IsRootItemAllowedRule isRootItemAllowedShorthandRule = new IsRootItemAllowedRule();
    private JcrItemNodeTypeAllowedRule jcrItemNodeTypeAllowedShorthandRule = new JcrItemNodeTypeAllowedRule();
    private AccessGrantedRule accessGrantedShorthandRule = new AccessGrantedRule();

    @Inject
    public AvailabilityCheckerImpl(ComponentProvider componentProvider, ContentConnector contentConnector) {
        this.componentProvider = componentProvider;
        this.contentConnector = contentConnector;
    }

    @Override
    public boolean isAvailable(AvailabilityDefinition definition, List<Object> ids) {
        boolean isAvailable = true;
        Iterator<AvailabilityRule> ruleIterator = prepareRules(definition).iterator();
        List<Object> idsToCheck = new ArrayList<Object>(ids);
        Object defaultId = contentConnector.getDefaultItemId();
        // In order to be compatible with the old logic and to let shorthand criteria work - we substitute a default item id with null
        // TODO - this should be done in a nicer way!
        if (idsToCheck.contains(defaultId)) {
            idsToCheck.remove(defaultId);
            idsToCheck.add(null);
        }
        while (isAvailable && ruleIterator.hasNext()) {
            AvailabilityRule rule = ruleIterator.next();
            boolean ruleHolds = rule.isAvailable(idsToCheck);
            isAvailable &= ruleHolds;
        }
        return isAvailable;
    }

    private List<AvailabilityRule> prepareRules(AvailabilityDefinition definition) {
        List<AvailabilityRule> rules = new ArrayList<AvailabilityRule>();
        rules.addAll(prepareShorthandRules(definition));
        for (AvailabilityRuleDefinition ruleDefinition : definition.getRules()) {
            rules.add(componentProvider.newInstance(ruleDefinition.getImplementationClass(), ruleDefinition));
        }
        return rules;
    }

    private Collection<? extends AvailabilityRule> prepareShorthandRules(AvailabilityDefinition definition) {
        nodesAllowedShorthandRule.setNodeAllowed(definition.isNodes());
        propertiesAllowedShorthandRule.setPropertiesAllowed(definition.isProperties());
        isRootItemAllowedShorthandRule.setRoot(definition.isRoot());
        jcrItemNodeTypeAllowedShorthandRule.setNodeTypes(definition.getNodeTypes());
        multipleItemsAllowedShorthandRule.setMultipleAllowed(definition.isMultiple());
        accessGrantedShorthandRule.setAccessDefinition(definition.getAccess());

        List<AvailabilityRule> shorthands = new ArrayList<AvailabilityRule>(6);
        shorthands.add(nodesAllowedShorthandRule);
        shorthands.add(propertiesAllowedShorthandRule);
        shorthands.add(isRootItemAllowedShorthandRule);
        shorthands.add(jcrItemNodeTypeAllowedShorthandRule);
        shorthands.add(multipleItemsAllowedShorthandRule);
        shorthands.add(accessGrantedShorthandRule);

        return shorthands;
    }
}
