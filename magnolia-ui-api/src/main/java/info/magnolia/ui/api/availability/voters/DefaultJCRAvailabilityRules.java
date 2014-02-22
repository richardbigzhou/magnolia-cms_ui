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
package info.magnolia.ui.api.availability.voters;

import info.magnolia.voting.Voter;

import java.util.HashMap;

/**
 * Enumeration of 'shorthand' availability properties that should be converted into
 * availability {@link Voter}'s at least for the case of JCR content apps.
 */
public enum DefaultJCRAvailabilityRules {

    IS_NODE("nodes", ActionAvailableForNodesVoter.class, Boolean.TRUE),
    IS_PROPERTY("properties", ActionAvailableForPropertiesVoter.class, Boolean.FALSE),
    IS_MULTIPLE("multiple", ActionAvailableForMultipleItemsVoter.class, Boolean.FALSE),
    IS_ROOT("root", ActionAvailableForRootItemVoter.class, Boolean.FALSE),
    RULE("ruleClass", ActionAvailableForRuleVoter.class, AlwaysTrueAvailabilityRule.class.getName()),
    NODE_TYPE("nodeTypes", ActionAvailableForNodeTypesVoter.class, new HashMap()),
    ACCESS("access", AccessGrantedToActionVoter.class, new HashMap());

    private String name;

    private Class<? extends Voter> voterClass;

    private Object defaultValue;

    private DefaultJCRAvailabilityRules(String name, Class<? extends Voter> voterClass, Object defaultValue) {
        this.name = name;
        this.voterClass = voterClass;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DefaultJCRAvailabilityRules getEnum(String value) {
        if(value == null)
            throw new IllegalArgumentException();
        for(DefaultJCRAvailabilityRules v : values())
            if(value.equalsIgnoreCase(v.toString())) return v;
        throw new IllegalArgumentException();
    }

    public Class<? extends Voter> getVoterClass() {
        return voterClass;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
