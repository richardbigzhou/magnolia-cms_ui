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
package info.magnolia.ui.form.config;

import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TabDefinition;

/**
 * FormBuilder that builds form containing tabs and actions.
 * Returns a {@link info.magnolia.ui.form.definition.FormDefinition}.
 */
public class FormBuilder {

    private final ConfiguredFormDefinition definition;

    public FormBuilder() {
        definition = new ConfiguredFormDefinition();
    }

    public FormBuilder(ConfiguredFormDefinition definition) {
        this.definition = definition;
    }

    public ConfiguredFormDefinition definition() {
        return definition;
    }

    public FormBuilder label(String label) {
        definition().setLabel(label);
        return this;
    }

    public FormBuilder i18nBasename(String i18nBasename) {
        definition().setI18nBasename(i18nBasename);
        return this;
    }

    public FormBuilder description(String description) {
        definition().setDescription(description);
        return this;
    }

    public FormBuilder tabs(TabBuilder... builders) {
        for (TabBuilder builder : builders) {
            definition().addTab(builder.definition());
        }
        return this;
    }

    public TabBuilder tab(String name) {
        for (TabDefinition tab : definition().getTabs()) {
            if (tab.getName().equals(name)) {
                return new TabBuilder((ConfiguredTabDefinition) tab);
            }
        }

        TabBuilder tabBuilder = new TabBuilder(name);
        definition.addTab(tabBuilder.definition());
        return tabBuilder;
    }
}
