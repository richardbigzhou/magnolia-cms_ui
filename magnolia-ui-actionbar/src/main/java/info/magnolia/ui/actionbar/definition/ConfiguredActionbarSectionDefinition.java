/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.actionbar.definition;

import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple implementation for {@link ActionbarSectionDefinition}.
 *
 * @deprecated since 5.4.3, use {@link DefaultActionbarSectionDefinition} instead.
 */
@Deprecated
public class ConfiguredActionbarSectionDefinition implements ActionbarSectionDefinition {

    private String name;

    private String label;

    private String i18nBasename;

    private List<ActionbarGroupDefinition> groups = new ArrayList<>();

    private AvailabilityDefinition availability = new ConfiguredAvailabilityDefinition();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    @Override
    public List<ActionbarGroupDefinition> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public void setGroups(List<ActionbarGroupDefinition> groups) {
        this.groups = groups;
    }

    /**
     * Adds a group to this section.
     *
     * @param groupDefinition the group definition
     */
    public void addGroup(ActionbarGroupDefinition groupDefinition) {
        groups.add(groupDefinition);
    }

    @Override
    public AvailabilityDefinition getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityDefinition availability) {
        // FIXME This is plain wrong, availability for multiple selection matches sections for which it is not configured (i.e. set to false)
        // But we have to keep it for the time being because some content apps rely on this incorrect behavior.
        ((ConfiguredAvailabilityDefinition) availability).setMultiple(true);
        this.availability = availability;
    }
}
