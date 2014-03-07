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
import info.magnolia.ui.framework.availability.voters.AvailabilityShorthands;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link info.magnolia.ui.api.availability.AvailabilityChecker}.
 */
public class AvailabilityCheckerImpl implements AvailabilityChecker {

    private Logger log = LoggerFactory.getLogger(getClass());

    private ComponentProvider componentProvider;

    private ContentConnector contentConnector;

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
        List<AvailabilityRule> rules = new ArrayList<AvailabilityRule>();
        try {
            BeanInfo info = Introspector.getBeanInfo(definition.getClass());
            PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                try {
                    AvailabilityShorthands shorthandRule = AvailabilityShorthands.fromString(propertyDescriptor.getName());
                    Object argument = propertyDescriptor.getReadMethod().invoke(definition);
                    rules.add(componentProvider.newInstance(shorthandRule.getRuleClass(), argument == null ? shorthandRule.getDefaultValue() : argument));
                } catch (IllegalArgumentException ignore){}
            }
        } catch (IntrospectionException e) {
            log.error("Error occurred while trying to retrieve object properties with introspection: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            log.error("Error occurred while trying to prepare ctor argument for a rule: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error("Access violation while trying to instantiate rule object: " + e.getMessage(), e);
        }
        return rules;
    }
}
