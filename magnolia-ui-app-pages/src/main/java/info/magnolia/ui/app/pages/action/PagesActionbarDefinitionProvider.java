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
package info.magnolia.ui.app.pages.action;

import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarSectionDefinition;


/**
 * Temporary hard coded definition provider for the page editor's actionbar.
 */
public class PagesActionbarDefinitionProvider {

    private static final String ADD_ICON = "img/actionbar-icons/icon-action-add-tablet.png";

    private static final String DELETE_ICON = "img/actionbar-icons/icon-action-delete-tablet.png";

    private static final String PREVIEW_ICON = "img/actionbar-icons/icon-action-preview-tablet.png";

    private static final String EDIT_ICON = "img/actionbar-icons/icon-action-edit-tablet.png";

    /**
     * Builds and returns an actionbar definition for the page editor.
     * 
     * @return an actionbar definition
     */
    public static ActionbarDefinition getPageEditorActionbarDefinition() {

        ConfiguredActionbarDefinition actionbar = new ConfiguredActionbarDefinition();
        actionbar.setName("pageEditorActionbar");

        // common group
        ActionbarGroupDefinition previewGroup = buildGroup("0", buildItem("previewPage", "Preview", PREVIEW_ICON));

        // sections
        ActionbarSectionDefinition pageSection = buildSection("Pages", "Pages",
            previewGroup,
            buildGroup("1",
                buildItem("addPage", "Add subpage", ADD_ICON),
                buildItem("deletePage", "Delete page", DELETE_ICON)));
        ActionbarSectionDefinition areaSection = buildSection("Areas", "Areas",
            previewGroup,
            buildGroup("1",
                buildItem("editArea", "Edit properties", EDIT_ICON),
                buildItem("deleteArea", "Delete content", DELETE_ICON)));
        ActionbarSectionDefinition componentSection = buildSection("Components", "Components",
            previewGroup,
            buildGroup("1",
                buildItem("addComponent", "Add", ADD_ICON),
                buildItem("deleteComponent", "Delete", DELETE_ICON)),
            buildGroup("2",
                buildItem("editComponent", "Edit", EDIT_ICON)));

        actionbar.addSection(pageSection);
        actionbar.addSection(areaSection);
        actionbar.addSection(componentSection);

        return actionbar;
    }

    private static ActionbarSectionDefinition buildSection(String name, String label, ActionbarGroupDefinition... groups) {
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        section.setName(name);
        section.setLabel(label);
        for (ActionbarGroupDefinition group : groups) {
            section.addGroup(group);
        }
        return section;
    }

    private static ActionbarGroupDefinition buildGroup(String name, ActionbarItemDefinition... items) {
        ConfiguredActionbarGroupDefinition group = new ConfiguredActionbarGroupDefinition();
        group.setName(name);
        for (ActionbarItemDefinition item : items) {
            group.addItem(item);
        }
        return group;
    }

    private static ActionbarItemDefinition buildItem(String name, String label, String icon) {
        ConfiguredActionbarItemDefinition item = new ConfiguredActionbarItemDefinition();
        item.setName(name);
        item.setLabel(label);
        item.setIcon(icon);
        // item.setActionDefinition(actionDefinition)
        return item;
    }
}
