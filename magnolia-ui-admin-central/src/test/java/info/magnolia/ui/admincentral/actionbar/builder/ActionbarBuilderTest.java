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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarSectionDefinition;
import info.magnolia.ui.widget.actionbar.Actionbar;
import info.magnolia.ui.widget.actionbar.Actionbar.ActionbarItem;
import info.magnolia.ui.widget.actionbar.Actionbar.ActionbarSection;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;


public class ActionbarBuilderTest {

    private static final String SECTION_A = "sectionA";

    private static final String SECTION_B = "sectionB";

    @Test
    public void testBuildingActionbar() {

        // GIVEN
        ConfiguredActionbarDefinition def = new ConfiguredActionbarDefinition();
        def.setName("myActionbar");

        // common group
        ActionbarGroupDefinition previewGroup = buildGroup("0",
            buildItem("0.0"));

        // sections
        ActionbarSectionDefinition aSection = buildSection(SECTION_A,
            previewGroup,
            buildGroup("1",
                buildItem("1.0"),
                buildItem("1.1")));
        ActionbarSectionDefinition bSection = buildSection(SECTION_B,
            previewGroup,
            buildGroup("1",
                buildItem("1.0"),
                buildItem("1.1")),
            buildGroup("2",
                buildItem("2.0")));

        def.addSection(aSection);
        def.addSection(bSection);

        // test variables
        int aActionCount = getActionsCount(aSection);
        int bActionCount = getActionsCount(bSection);

        // WHEN
        ActionbarView actionbar = ActionbarBuilder.build(def);

        // THEN
        Map<String, ActionbarSection> sections = ((Actionbar) actionbar).getSections();
        assertEquals(def.getSections().size(), sections.size());
        assertEquals(aSection.getName(), sections.get(SECTION_A).getName());
        assertEquals(bSection.getName(), sections.get(SECTION_B).getName());

        List<ActionbarItem> aActions = sections.get(SECTION_A).getActions();
        assertEquals(aActionCount, aActions.size());
        assertTrue(containsAction(aActions, "0.0"));
        assertTrue(containsAction(aActions, "1.1"));

        List<ActionbarItem> bActions = sections.get(SECTION_B).getActions();
        assertEquals(bActionCount, bActions.size());
        assertTrue(containsAction(bActions, "0.0"));
        assertTrue(containsAction(bActions, "1.0"));
        assertTrue(containsAction(bActions, "2.0"));
    }

    @Test
    public void testBuildingActionbarWithEmptyGroup() {
        // GIVEN
        ConfiguredActionbarDefinition def = new ConfiguredActionbarDefinition();
        def.setName("myActionbar");
        ActionbarSectionDefinition sectionDef = buildSection(SECTION_A,
            buildGroup("0"),
            buildGroup("1",
                buildItem("1.0"),
                buildItem("1.1")));
        def.addSection(sectionDef);
        int actionCount = getActionsCount(sectionDef);

        // WHEN
        ActionbarView actionbar = ActionbarBuilder.build(def);

        // THEN
        List<ActionbarItem> actions = ((Actionbar) actionbar).getSections().get(SECTION_A).getActions();
        assertEquals(actionCount, actions.size());
        for (ActionbarItem action : actions) {
            assertTrue(action.getGroupName() != "0");
        }
    }

    @Test
    public void testBuildingActionbarWithDuplicateAction() {
        // GIVEN
        ConfiguredActionbarDefinition def = new ConfiguredActionbarDefinition();
        def.setName("myActionbar");
        ActionbarSectionDefinition aSection = buildSection(SECTION_A,
            buildGroup("0",
                buildItem("0.0"),
                buildItem("0.1"),
                buildItem("0.1"),
                buildItem("0.2"),
                buildItem("0.3")),
            buildGroup("1",
                buildItem("1.0"),
                buildItem("0.2")));
        ActionbarSectionDefinition bSection = buildSection(SECTION_B,
            buildGroup("0",
                buildItem("0.3")));
        def.addSection(aSection);
        def.addSection(bSection);

        // test variables
        int aActionCount = getActionsCount(aSection);
        int bActionCount = getActionsCount(bSection);

        // WHEN
        ActionbarView actionbar = ActionbarBuilder.build(def);

        // THEN
        List<ActionbarItem> aActions = ((Actionbar) actionbar).getSections().get(SECTION_A).getActions();
        List<ActionbarItem> bActions = ((Actionbar) actionbar).getSections().get(SECTION_B).getActions();
        // duplicates 0.1 and 0.2 shouldn't be added, but duplicate 0.3 should
        assertEquals(aActionCount - 2, aActions.size());
        assertEquals(bActionCount, bActions.size());
        assertTrue(containsAction(aActions, "0.1"));
        assertTrue(containsAction(aActions, "0.2"));
        assertTrue(containsAction(aActions, "0.3"));
        assertTrue(containsAction(bActions, "0.3"));

    }

    private ActionbarSectionDefinition buildSection(String name, ActionbarGroupDefinition... groups) {
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        section.setName(name);
        for (ActionbarGroupDefinition group : groups) {
            section.addGroup(group);
        }
        return section;
    }

    private ActionbarGroupDefinition buildGroup(String name, ActionbarItemDefinition... items) {
        ConfiguredActionbarGroupDefinition group = new ConfiguredActionbarGroupDefinition();
        group.setName(name);
        for (ActionbarItemDefinition item : items) {
            group.addItem(item);
        }
        return group;
    }

    private ActionbarItemDefinition buildItem(String name) {
        ConfiguredActionbarItemDefinition item = new ConfiguredActionbarItemDefinition();
        item.setName(name);
        return item;
    }

    private int getActionsCount(ActionbarSectionDefinition section) {
        int count = 0;
        for (ActionbarGroupDefinition group : section.getGroups()) {
            count += group.getItems().size();
        }
        return count;
    }

    private boolean containsAction(List<ActionbarItem> actions, String actionName) {
        Iterator<ActionbarItem> iterator = actions.iterator();
        while (iterator.hasNext()) {
            ActionbarItem action = iterator.next();
            if (StringUtils.equals(action.getName(), actionName)) {
                return true;
            }
        }
        return false;
    }

}
