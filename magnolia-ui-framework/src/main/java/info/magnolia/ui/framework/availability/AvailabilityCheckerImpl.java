/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import org.apache.commons.lang3.ObjectUtils;

/**
 * Implements {@link info.magnolia.ui.api.availability.AvailabilityChecker}.
 */
public class AvailabilityCheckerImpl implements AvailabilityChecker {

    private final ComponentProvider componentProvider;

    private final ContentConnector contentConnector;

    private JcrNodesAllowedRule jcrNodesAllowedRule;
    private JcrPropertiesAllowedRule jcrPropertiesAllowedRule;
    private MultipleItemsAllowedRule multipleItemsAllowedRule;
    private JcrRootAllowedRule jcrRootAllowedRule;
    private JcrNodeTypesAllowedRule jcrNodeTypesAllowedRule;
    private AccessGrantedRule accessGrantedRule;
    private WritePermissionRequiredRule writePermissionRequiredRule;

    @Inject
    public AvailabilityCheckerImpl(ComponentProvider componentProvider, ContentConnector contentConnector) {
        this.componentProvider = componentProvider;
        this.contentConnector = contentConnector;
        this.jcrNodesAllowedRule = createJcrNodesAllowedRule();
        this.jcrPropertiesAllowedRule = createJcrPropertiesAllowedRule();
        this.jcrRootAllowedRule = createJcrRootAllowedRule();
        this.jcrNodeTypesAllowedRule = createJcrNodeTypesAllowedRule();
        this.multipleItemsAllowedRule = createMultipleItemsAllowedRule();
        this.accessGrantedRule = createAccessGrantedRule();
        this.writePermissionRequiredRule = createWritePermissionRequiredRule();
    }

    @Override
    public boolean isAvailable(AvailabilityDefinition definition, List<Object> ids) {
        // Prepare rules
        List<AvailabilityRule> rules = prepareRules(definition);

        List<Object> idsToCheck = new ArrayList<Object>(ids);
        Object defaultItemId = contentConnector.getDefaultItemId();
        // In case we have no id's (no selection) add the default itemId defined by the contentConnector.
        if (idsToCheck.isEmpty() && defaultItemId != null) {
            idsToCheck.add(defaultItemId);
        }

        // Since we can't combine rules with AND/OR operands, 'root' availability should keep precedence over nodes/nodeTypes-allowed rules when the default item is selected.
        if (definition.isRoot() && idsToCheck.size() == 1 && ObjectUtils.equals(idsToCheck.get(0), defaultItemId)) {
            rules.remove(jcrNodesAllowedRule);
            rules.remove(jcrNodeTypesAllowedRule);
        }

        boolean isAvailable = true;
        Iterator<AvailabilityRule> ruleIterator = rules.iterator();
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

        // set default itemId to check "root" availability against
        Object defaultItemId = contentConnector.getDefaultItemId();
        jcrRootAllowedRule.setDefaultItemId(defaultItemId);

        List<AvailabilityRule> shorthands = new ArrayList<AvailabilityRule>();
        shorthands.add(jcrRootAllowedRule);
        shorthands.add(jcrNodesAllowedRule);
        shorthands.add(jcrPropertiesAllowedRule);
        shorthands.add(jcrNodeTypesAllowedRule);
        shorthands.add(multipleItemsAllowedRule);
        shorthands.add(accessGrantedRule);
        shorthands.add(writePermissionRequiredRule);

        return shorthands;
    }

    protected JcrNodesAllowedRule createJcrNodesAllowedRule() {
        return new JcrNodesAllowedRule();
    }

    protected JcrPropertiesAllowedRule createJcrPropertiesAllowedRule() {
        return new JcrPropertiesAllowedRule();
    }

    protected MultipleItemsAllowedRule createMultipleItemsAllowedRule() {
        return new MultipleItemsAllowedRule();
    }

    protected JcrRootAllowedRule createJcrRootAllowedRule() {
        return new JcrRootAllowedRule();
    }

    protected JcrNodeTypesAllowedRule createJcrNodeTypesAllowedRule() {
        return new JcrNodeTypesAllowedRule();
    }

    protected AccessGrantedRule createAccessGrantedRule() {
        return new AccessGrantedRule();
    }

    protected WritePermissionRequiredRule createWritePermissionRequiredRule() {
        return new WritePermissionRequiredRule();
    }
}
