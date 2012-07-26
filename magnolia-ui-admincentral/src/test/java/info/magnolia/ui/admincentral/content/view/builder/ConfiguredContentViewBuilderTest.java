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
package info.magnolia.ui.admincentral.content.view.builder;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.list.view.ListView;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailProvider;
import info.magnolia.ui.admincentral.thumbnail.view.ThumbnailView;
import info.magnolia.ui.admincentral.tree.view.TreeView;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.model.column.definition.LabelColumnDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.model.workbench.definition.ItemTypeDefinition;

import org.junit.Test;

/**
 * Tests for ConfiguredContentViewBuilder.
 */
public class ConfiguredContentViewBuilderTest {

    @Test
    public void testBuildingListView() {
        // GIVEN
        final MockComponentProvider componentProvider = new MockComponentProvider();
        componentProvider.setInstance(WorkbenchActionFactory.class, new WorkbenchActionFactoryImpl());
        final ConfiguredWorkbenchDefinition workbenchDef = new ConfiguredWorkbenchDefinition();
        LabelColumnDefinition def = new LabelColumnDefinition();
        def.setName("foo");
        workbenchDef.addColumn(def);

        // WHEN
        final ConfiguredContentViewBuilder builder = new ConfiguredContentViewBuilder(componentProvider);
        final ContentView result = builder.build(workbenchDef, ViewType.LIST);

        // THEN
        assertTrue(result instanceof ListView);
    }

    @Test
    public void testBuildingTreeView() {
        // GIVEN
        final MockComponentProvider componentProvider = new MockComponentProvider();
        componentProvider.setInstance(WorkbenchActionFactory.class, new WorkbenchActionFactoryImpl());
        final ConfiguredWorkbenchDefinition workbenchDef = new ConfiguredWorkbenchDefinition();

        // WHEN
        final ConfiguredContentViewBuilder builder = new ConfiguredContentViewBuilder(componentProvider);
        final ContentView result = builder.build(workbenchDef, ViewType.TREE);

        // THEN
        assertTrue(result instanceof TreeView);
    }

    @Test
    public void testBuildingThumbnailView() {
        // GIVEN
        MockUtil.initMockContext();
        final String workspace = "website";
        final MockSession session = new MockSession(workspace);
        MockUtil.setSessionAndHierarchyManager(session);

        final MockComponentProvider componentProvider = new MockComponentProvider();
        componentProvider.setInstance(WorkbenchActionFactory.class, new WorkbenchActionFactoryImpl());
        componentProvider.setInstance(ThumbnailProvider.class, mock(ThumbnailProvider.class));

        final ConfiguredWorkbenchDefinition workbenchDef = new ConfiguredWorkbenchDefinition();
        workbenchDef.setWorkspace(workspace);
        workbenchDef.setPath("/");

        final ConfiguredItemTypeDefinition itemTypeDefinition = new ConfiguredItemTypeDefinition();
        itemTypeDefinition.setItemType("qux");
        final List<ItemTypeDefinition> itemTypeDefs = new ArrayList<ItemTypeDefinition>();
        itemTypeDefs.add(itemTypeDefinition);
        workbenchDef.setItemTypes(itemTypeDefs);
        // WHEN
        final ConfiguredContentViewBuilder builder = new ConfiguredContentViewBuilder(componentProvider);
        final ContentView result = builder.build(workbenchDef, ViewType.THUMBNAIL);

        // THEN
        assertTrue(result instanceof ThumbnailView);
    }

}
