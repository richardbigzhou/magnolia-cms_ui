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
import info.magnolia.ui.framework.availability.shorthandrules.JcrNodeTypesAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.JcrNodesAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.JcrPropertiesAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.JcrRootAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.MultipleItemsAllowedRule;
import info.magnolia.ui.framework.availability.shorthandrules.WritePermissionRequiredRule;
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

    private JcrNodesAllowedRule jcrNodesAllowedRule = new JcrNodesAllowedRule();
    private JcrPropertiesAllowedRule jcrPropertiesAllowedRule = new JcrPropertiesAllowedRule();
    private MultipleItemsAllowedRule multipleItemsAllowedRule = new MultipleItemsAllowedRule();
    private JcrRootAllowedRule jcrRootAllowedRule = new JcrRootAllowedRule();
    private JcrNodeTypesAllowedRule jcrNodeTypesAllowedRule = new JcrNodeTypesAllowedRule();
    private AccessGrantedRule accessGrantedRule = new AccessGrantedRule();
    private WritePermissionRequiredRule writePermissionRequiredRule = new WritePermissionRequiredRule();

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
        if (idsToCheck.contains(defaultId) || (idsToCheck.isEmpty() && defaultId == null)) {
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
        jcrNodesAllowedRule.setNodesAllowed(definition.isNodes());
        jcrPropertiesAllowedRule.setPropertiesAllowed(definition.isProperties());
        jcrRootAllowedRule.setRootAllowed(definition.isRoot());
        jcrNodeTypesAllowedRule.setNodeTypes(definition.getNodeTypes());
        multipleItemsAllowedRule.setMultipleItemsAllowed(definition.isMultiple());
        accessGrantedRule.setAccessDefinition(definition.getAccess());
        writePermissionRequiredRule.setWritePermissionRequired(definition.isWritePermissionRequired());

        List<AvailabilityRule> shorthands = new ArrayList<AvailabilityRule>(6);
        shorthands.add(jcrNodesAllowedRule);
        shorthands.add(jcrPropertiesAllowedRule);
        shorthands.add(jcrRootAllowedRule);
        shorthands.add(jcrNodeTypesAllowedRule);
        shorthands.add(multipleItemsAllowedRule);
        shorthands.add(accessGrantedRule);
        shorthands.add(writePermissionRequiredRule);

        return shorthands;
    }
}
