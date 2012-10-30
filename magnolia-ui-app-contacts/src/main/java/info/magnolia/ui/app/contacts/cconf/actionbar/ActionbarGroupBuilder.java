/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.contacts.cconf.actionbar;

import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarItemDefinition;

/**
 * Builder for building an actionbar group definition.
 */
public class ActionbarGroupBuilder {

    private ConfiguredActionbarGroupDefinition definition;

    public ActionbarGroupBuilder() {
        definition = new ConfiguredActionbarGroupDefinition();
    }

    public ActionbarGroupBuilder(ConfiguredActionbarGroupDefinition definition) {
        this.definition = definition;
    }

    public ActionbarGroupBuilder(String name) {
        definition = new ConfiguredActionbarGroupDefinition();
        definition.setName(name);
    }

    public ActionbarGroupDefinition exec() {
        return definition;
    }

    public ActionbarGroupBuilder items(ActionbarItemBuilder... items) {
        for (ActionbarItemBuilder item : items) {
            definition.addItem(item.exec());
        }
        return this;
    }

    public ActionbarItemBuilder item(String name) {
        ConfiguredActionbarItemDefinition itemDefinition = new ConfiguredActionbarItemDefinition();
        itemDefinition.setName(name);
        definition.addItem(itemDefinition);
        return new ActionbarItemBuilder(itemDefinition);
    }
}
