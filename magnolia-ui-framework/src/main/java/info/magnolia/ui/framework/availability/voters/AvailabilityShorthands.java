/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.ui.framework.availability.voters;

import info.magnolia.ui.api.availability.AvailabilityRule;

import java.util.HashMap;

/**
 * Enumeration of 'shorthand' availability properties that should be converted into
 * {@link AvailabilityRule}'s at least for the case of JCR content apps.
 */
public enum AvailabilityShorthands {

    IS_NODE("nodes", JcrNodesAllowedRule.class, Boolean.TRUE),
    IS_PROPERTY("properties", ActionAvailableForPropertiesVoter.class, Boolean.FALSE),
    IS_MULTIPLE("multiple", MultipleItemsAllowedRule.class, Boolean.FALSE),
    IS_ROOT("root", IsRootItemAllowedRule.class, Boolean.FALSE),
    NODE_TYPE("nodeTypes", JcrItemNodeTypeAllowedRule.class, new HashMap()),
    ACCESS("access", AccessGrantedRule.class, new HashMap());

    private String name;

    private Class<? extends AvailabilityRule> ruleClass;

    private Object defaultValue;

    private AvailabilityShorthands(String name, Class<? extends AvailabilityRule> ruleClass, Object defaultValue) {
        this.name = name;
        this.ruleClass = ruleClass;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return name;
    }

    public static AvailabilityShorthands fromString(String value) throws IllegalArgumentException {
        if(value == null)
            throw new IllegalArgumentException();
        for(AvailabilityShorthands v : values())
            if(value.equalsIgnoreCase(v.toString())) return v;
        throw new IllegalArgumentException();
    }

    public Class<? extends AvailabilityRule> getRuleClass() {
        return ruleClass;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
