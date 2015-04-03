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
package info.magnolia.ui.actionbar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
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
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarSection;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the ActionbarPresenter.
 */
public class ActionbarPresenterTest {

    private static final String SECTION_ASSETS = "assets";
    private static final String SECTION_BOOKS = "books";

    private static final String ACTION_ASSET_ADD = "addAsset";
    private static final String ACTION_ASSET_DELETE = "deleteAsset";
    private static final String ACTION_ASSET_EDIT = "editAsset";

    private static final String ACTION_BOOK_ADD = "addBook";
    private static final String ACTION_BOOK_DELETE = "deleteBook";
    private static final String ACTION_BOOK_EDIT = "editBook";


    private ActionbarPresenter presenter;

    private ActionbarDefinition definition;
    private Map<String, ActionDefinition> actions = new HashMap<String, ActionDefinition>();

    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        ComponentsTestUtil.setImplementation(ActionbarView.class, ActionbarViewImpl.class);
        ComponentsTestUtil.setInstance(SimpleTranslator.class, mock(SimpleTranslator.class));
        presenter = Components.newInstance(ActionbarPresenter.class);
        definition = initDefinitions();
    }

    @Test
    public void testBuildingActionbar() {

        // GIVEN
        ActionbarSectionDefinition sectionA = definition.getSections().get(0);
        ActionbarSectionDefinition sectionB = definition.getSections().get(1);
        int countA = getActionsCount(sectionA);
        int countB = getActionsCount(sectionB);

        // WHEN
        ActionbarView view = presenter.start(definition, actions);

        // THEN
        Actionbar actionbar = ((ActionbarViewImpl) view).asVaadinComponent();
        Map<String, ActionbarSection> sections = actionbar.getSections();
        assertEquals(definition.getSections().size(), sections.size());
        assertEquals(sectionA.getName(), sections.get(SECTION_ASSETS).getName());
        assertEquals(sectionB.getName(), sections.get(SECTION_BOOKS).getName());

        Map<String, ActionbarItem> aActions = sections.get(SECTION_ASSETS).getActions();
        assertEquals(countA, aActions.size());
        assertTrue(aActions.containsKey(ACTION_ASSET_ADD));
        assertTrue(aActions.containsKey(ACTION_ASSET_DELETE));
        assertTrue(aActions.containsKey(ACTION_ASSET_EDIT));

        Map<String, ActionbarItem> actionsB = sections.get(SECTION_BOOKS).getActions();
        assertEquals(countB, actionsB.size());
        assertTrue(actionsB.containsKey(ACTION_BOOK_ADD));
        assertTrue(actionsB.containsKey(ACTION_BOOK_DELETE));
        assertTrue(actionsB.containsKey(ACTION_BOOK_EDIT));
    }

    @Test
    public void testBuildingActionbarWithEmptyGroup() {
        // GIVEN
        final String emptyGroupName = "2";
        ActionbarSectionDefinition section = definition.getSections().get(0);
        ((ConfiguredActionbarSectionDefinition) section).addGroup(new ConfiguredActionbarGroupDefinition() {
            {
                setName(emptyGroupName);
            }
        });

        int count = getActionsCount(section);

        // WHEN
        ActionbarView view = presenter.start(definition, actions);

        // THEN
        Actionbar actionbar = ((ActionbarViewImpl) view).asVaadinComponent();
        Map<String, ActionbarItem> actions = actionbar.getSections().get(SECTION_ASSETS).getActions();
        assertEquals(count, actions.size());
        for (ActionbarItem action : actions.values()) {
            assertNotEquals(emptyGroupName, action.getGroupName());
        }
    }

    @Test
    public void testBuildingActionbarWithDuplicateAction() {
        // GIVEN
        final String duplicateActionName = ACTION_ASSET_ADD;

        // duplicate in same group
        ActionbarSectionDefinition sectionA = definition.getSections().get(0);
        ActionbarGroupDefinition groupA0 = sectionA.getGroups().get(0);
        ((ConfiguredActionbarGroupDefinition) groupA0).addItem(new ConfiguredActionbarItemDefinition() {
            {
                setName(duplicateActionName);
            }
        });

        // duplicate in same section but different group
        ActionbarGroupDefinition groupA1 = sectionA.getGroups().get(1);
        ((ConfiguredActionbarGroupDefinition) groupA1).addItem(new ConfiguredActionbarItemDefinition() {
            {
                setName(duplicateActionName);
            }
        });

        // duplicate in different section
        ActionbarSectionDefinition sectionB = definition.getSections().get(1);
        ActionbarGroupDefinition groupB0 = sectionB.getGroups().get(0);
        ((ConfiguredActionbarGroupDefinition) groupB0).addItem(new ConfiguredActionbarItemDefinition() {
            {
                setName(duplicateActionName);
            }
        });

        int countA = getActionsCount(sectionA);
        int countB = getActionsCount(sectionB);

        // WHEN
        ActionbarView view = presenter.start(definition, actions);

        // THEN
        Actionbar actionbar = ((ActionbarViewImpl) view).asVaadinComponent();
        Map<String, ActionbarItem> actionsA = actionbar.getSections().get(SECTION_ASSETS).getActions();
        Map<String, ActionbarItem> actionsB = actionbar.getSections().get(SECTION_BOOKS).getActions();

        // same section duplicates shouldn't be added, but duplicate in different section should
        assertEquals(countA - 2, actionsA.size());
        assertEquals(countB, actionsB.size());
        assertTrue(actionsA.containsKey(ACTION_ASSET_ADD));
        assertTrue(actionsA.containsKey(ACTION_ASSET_DELETE));
        assertTrue(actionsA.containsKey(ACTION_ASSET_EDIT));
        assertTrue(actionsB.containsKey(duplicateActionName));

    }

    /**
     * Builds an actionbar definition consisiting of two sections with two groups each, one for adding/deleting actions, one for editing actions.
     */
    private ActionbarDefinition initDefinitions() {
        return new ConfiguredActionbarDefinition() {
            {
                addSection(new ConfiguredActionbarSectionDefinition() {
                    {
                        setName(SECTION_ASSETS);
                        addGroup(new ConfiguredActionbarGroupDefinition() {
                            {
                                setName("0");
                                addItem(initAction(ACTION_ASSET_ADD));
                                addItem(initAction(ACTION_ASSET_DELETE));
                            }
                        });
                        addGroup(new ConfiguredActionbarGroupDefinition() {
                            {
                                setName("1");
                                addItem(initAction(ACTION_ASSET_EDIT));
                            }
                        });
                    }
                });
                addSection(new ConfiguredActionbarSectionDefinition() {
                    {
                        setName(SECTION_BOOKS);
                        addGroup(new ConfiguredActionbarGroupDefinition() {
                            {
                                setName("0");
                                addItem(initAction(ACTION_BOOK_ADD));
                                addItem(initAction(ACTION_BOOK_DELETE));
                            }
                        });
                        addGroup(new ConfiguredActionbarGroupDefinition() {
                            {
                                setName("1");
                                addItem(initAction(ACTION_BOOK_EDIT));
                            }
                        });
                    }
                });
            }
        };
    }

    /**
     * Builds an action bar item and registers an action definition with the same name.
     */
    private ActionbarItemDefinition initAction(final String actionName) {
        actions.put(actionName, new ConfiguredActionDefinition() {
            {
                setName(actionName);
            }
        });
        return new ConfiguredActionbarItemDefinition() {
            {
                setName(actionName);
            }
        };
    }

    private int getActionsCount(ActionbarSectionDefinition section) {
        int count = 0;
        for (ActionbarGroupDefinition group : section.getGroups()) {
            count += group.getItems().size();
        }
        return count;
    }

}
