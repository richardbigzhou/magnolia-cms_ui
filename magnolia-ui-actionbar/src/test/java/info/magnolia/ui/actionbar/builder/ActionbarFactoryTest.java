/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.actionbar.builder;

import static org.junit.Assert.*;

import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarSection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the ActionbarFactory.
 */
public class ActionbarFactoryTest extends MgnlTestCase {

    private static final String SECTION_A = "sectionA";

    private static final String SECTION_B = "sectionB";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
    }

    @Test
    public void testBuildingActionbar() {

        // GIVEN
        Map<String, ActionDefinition> actionDefs = new HashMap<String, ActionDefinition>();
        actionDefs.put("0.0", new TestActionDefinition("0.0"));
        actionDefs.put("1.0", new TestActionDefinition("1.0"));
        actionDefs.put("1.1", new TestActionDefinition("1.1"));
        actionDefs.put("2.0", new TestActionDefinition("2.0"));

        final ConfiguredActionbarDefinition def = new ConfiguredActionbarDefinition();

        // common group
        ActionbarGroupDefinition previewGroup = buildGroup("0",
                    "0.0");

        // sections
        ActionbarSectionDefinition aSection = buildSection(SECTION_A,
                previewGroup,
                buildGroup("1",
                            "1.0",
                            "1.1"));
        ActionbarSectionDefinition bSection = buildSection(SECTION_B,
                previewGroup,
                buildGroup("1",
                            "1.0",
                            "1.1"),
                buildGroup("2",
                            "2.0"));

        def.addSection(aSection);
        def.addSection(bSection);

        ActionbarListener listener = new ActionbarListener(actionDefs);

        // test variables
        int aActionCount = getActionsCount(aSection);
        int bActionCount = getActionsCount(bSection);

        // WHEN
        ActionbarView actionbar = ActionbarFactory.build(def, listener);

        // THEN
        Map<String, ActionbarSection> sections = ((Actionbar) actionbar).getSections();
        assertEquals(def.getSections().size(), sections.size());
        assertEquals(aSection.getName(), sections.get(SECTION_A).getName());
        assertEquals(bSection.getName(), sections.get(SECTION_B).getName());

        Map<String, ActionbarItem> aActions = sections.get(SECTION_A).getActions();
        assertEquals(aActionCount, aActions.size());
        assertTrue(aActions.containsKey("0.0"));
        assertTrue(aActions.containsKey("1.1"));

        Map<String, ActionbarItem> bActions = sections.get(SECTION_B).getActions();
        assertEquals(bActionCount, bActions.size());
        assertTrue(bActions.containsKey("0.0"));
        assertTrue(bActions.containsKey("1.0"));
        assertTrue(bActions.containsKey("2.0"));
    }

    @Test
    public void testBuildingActionbarWithEmptyGroup() {
        // GIVEN
        Map<String, ActionDefinition> actionDefs = new HashMap<String, ActionDefinition>();
        actionDefs.put("1.0", new TestActionDefinition("1.0"));
        actionDefs.put("1.1", new TestActionDefinition("1.1"));
        ConfiguredActionbarDefinition def = new ConfiguredActionbarDefinition();
        ActionbarSectionDefinition sectionDef = buildSection(SECTION_A,
                buildGroup("0"),
                buildGroup("1",
                            "1.0",
                            "1.1"));
        def.addSection(sectionDef);
        int actionCount = getActionsCount(sectionDef);

        ActionbarListener listener = new ActionbarListener(actionDefs);

        // WHEN
        ActionbarView actionbar = ActionbarFactory.build(def, listener);

        // THEN
        Map<String, ActionbarItem> actions = ((Actionbar) actionbar).getSections().get(SECTION_A).getActions();
        assertEquals(actionCount, actions.size());
        for (ActionbarItem action : actions.values()) {
            assertTrue(action.getGroupName() != "0");
        }
    }

    private static class TestActionDefinition extends ConfiguredActionDefinition {

        public TestActionDefinition(String id) {
            setName(id);
            setIcon("test");
            setDescription("");
            setI18nBasename("");
            setImplementationClass(null);
            setLabel("label");
        }
    }

    @Test
    public void testBuildingActionbarWithDuplicateAction() {
        // GIVEN
        Map<String, ActionDefinition> actionDefs = new HashMap<String, ActionDefinition>();
        actionDefs.put("0.0", new TestActionDefinition("0.0"));
        actionDefs.put("0.1", new TestActionDefinition("0.1"));
        actionDefs.put("0.2", new TestActionDefinition("0.2"));
        actionDefs.put("0.3", new TestActionDefinition("0.3"));
        actionDefs.put("1.0", new TestActionDefinition("1.0"));

        ConfiguredActionbarDefinition def = new ConfiguredActionbarDefinition();
        ActionbarSectionDefinition aSection = buildSection(SECTION_A,
                buildGroup("0",
                            "0.0",
                            "0.1",
                            "0.1",
                            "0.2",
                            "0.3"),
                buildGroup("1",
                            "1.0",
                            "0.2"));
        ActionbarSectionDefinition bSection = buildSection(SECTION_B,
                buildGroup("0",
                            "0.3"));
        def.addSection(aSection);
        def.addSection(bSection);

        ActionbarListener listener = new ActionbarListener(actionDefs);

        // test variables
        int aActionCount = getActionsCount(aSection);
        int bActionCount = getActionsCount(bSection);


        // WHEN
        ActionbarView actionbar = ActionbarFactory.build(def, listener);

        // THEN
        Map<String, ActionbarItem> aActions = ((Actionbar) actionbar).getSections().get(SECTION_A).getActions();
        Map<String, ActionbarItem> bActions = ((Actionbar) actionbar).getSections().get(SECTION_B).getActions();
        // duplicates 0.1 and 0.2 shouldn't be added, but duplicate 0.3 should
        assertEquals(aActionCount - 2, aActions.size());
        assertEquals(bActionCount, bActions.size());
        assertTrue(aActions.containsKey("0.1"));
        assertTrue(aActions.containsKey("0.2"));
        assertTrue(aActions.containsKey("0.3"));
        assertTrue(bActions.containsKey("0.3"));

    }

    private ActionbarSectionDefinition buildSection(String name, ActionbarGroupDefinition... groups) {
        ConfiguredActionbarSectionDefinition section = new ConfiguredActionbarSectionDefinition();
        section.setName(name);
        for (ActionbarGroupDefinition group : groups) {
            section.addGroup(group);
        }
        return section;
    }

    private ActionbarGroupDefinition buildGroup(String name, String...actions) {
        ConfiguredActionbarGroupDefinition group = new ConfiguredActionbarGroupDefinition();
        group.setName(name);
        for (String action : actions) {
            ConfiguredActionbarItemDefinition def = new ConfiguredActionbarItemDefinition();
            def.setName(action);
            group.addItem(def);
        }
        return group;
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

    private class ActionbarListener implements ActionbarPresenter.Listener {

        private Map<String, ActionDefinition> actionDefinitions;

        private ActionbarListener(Map<String, ActionDefinition> actionDefinitions) {
            this.actionDefinitions = actionDefinitions;
        }

        @Override
        public void onActionbarItemClicked(String actionName) {

        }

        @Override
        public String getLabel(String actionName) {
            return actionDefinitions.get(actionName).getLabel();
        }

        @Override
        public String getIcon(String actionName) {
            return actionDefinitions.get(actionName).getLabel();
        }

    }

}
