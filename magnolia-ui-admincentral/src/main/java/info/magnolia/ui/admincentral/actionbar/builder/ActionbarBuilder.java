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
package info.magnolia.ui.admincentral.actionbar.builder;

import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ThemeResource;

/**
 * Basic builder for an action bar widget based on an action bar definition.
 */
public class ActionbarBuilder {

    private static final Logger log = LoggerFactory.getLogger(ActionbarBuilder.class);

    public static ActionbarView build(ActionbarDefinition definition, Map<String, ActionDefinition> actions) {
        Actionbar actionbar = new Actionbar();
        if (definition == null) {
            log.warn("No actionbar definition found. This will result in an empty action bar. Is that intended?");
            return actionbar;
        } else {

            for (ActionbarSectionDefinition section : definition.getSections()) {
                actionbar.addSection(section.getName(), section.getLabel());

                for (ActionbarGroupDefinition group : section.getGroups()) {
                    // standalone groups make no sense
                    log.info("Group actions: " + group.getActions());
                    for (String item : group.getActions()) {
                        if (!actions.containsKey(item)) {
                            log.warn("Action was not added: an action with name '" + item + "' already exists in section '" + section.getName() + "'.");
                            continue;
                        }
                        addItemFromDefinition(actions.get(item), actionbar, group.getName(), section.getName());
                    }
                }
            }
        }
        return actionbar;
    }

    public static void addItemFromDefinition(ActionDefinition def, Actionbar actionBar, String groupName, String sectionName) {
        ActionbarItem entry = null;
        if (StringUtils.isNotBlank(def.getIcon())) {
            if (def.getIcon().startsWith("icon-")) {
                entry = new ActionbarItem(def.getName(), def.getLabel(), def.getIcon(), groupName);
            } else {
                try {
                    actionBar.registerActionIconResource(def.getName(), new ThemeResource(def.getIcon()));
                } catch (NullPointerException e) {
                    log.warn("Icon resource not found for Actionbar item '" + def.getName() + "'.");
                } finally {
                    entry = new ActionbarItem(def.getName(), def.getLabel(), null, groupName);
                }
            }
        } else {
            entry = new ActionbarItem(def.getName(), def.getLabel(), null, groupName);
        }
        actionBar.addAction(entry, sectionName);
    }
}
