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
package info.magnolia.ui.workbench;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.workbench.column.definition.LabelColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.list.ListContentViewDefinition;
import info.magnolia.ui.workbench.list.ListView;
import info.magnolia.ui.workbench.search.SearchContentViewDefinition;
import info.magnolia.ui.workbench.search.SearchView;
import info.magnolia.ui.workbench.thumbnail.ThumbnailContentViewDefinition;
import info.magnolia.ui.workbench.thumbnail.ThumbnailView;
import info.magnolia.ui.workbench.tree.TreeContentViewDefinition;
import info.magnolia.ui.workbench.tree.TreeView;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for ConfiguredContentViewBuilder.
 */
public class ContentViewBuilderImplTest {

    protected final MockComponentProvider componentProvider = new MockComponentProvider();
    protected final ConfiguredWorkbenchDefinition workbenchDef = new ConfiguredWorkbenchDefinition();
    private ImageProviderDefinition imageProvider;

    @Before
    public void setUp() throws Exception {

        MockUtil.initMockContext();
        final String workspace = "website";
        final MockSession session = new MockSession(workspace);
        MockUtil.setSessionAndHierarchyManager(session);

        this.imageProvider = mock(ImageProviderDefinition.class);
        componentProvider.setInstance(ImageProviderDefinition.class, imageProvider);

        workbenchDef.setWorkspace(workspace);
        workbenchDef.setPath("/");

        final ConfiguredNodeTypeDefinition nodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        nodeTypeDefinition.setName("qux");
        workbenchDef.addNodeType(nodeTypeDefinition);
        final LabelColumnDefinition def = new LabelColumnDefinition();
        def.setName("foo");
        workbenchDef.addColumn(def);
    }

    @Test
    @Ignore
    public void testBuildingListView() {
        // GIVEN all conditions in setUp

        // WHEN
        final ContentViewBuilderImpl builder = new ContentViewBuilderImpl(componentProvider);
        final ContentView result = builder.build(workbenchDef, imageProvider, new ListContentViewDefinition());

        // THEN
        assertTrue(result instanceof ListView);
    }

    @Test
    @Ignore
    public void testBuildingTreeView() {
        // GIVEN all conditions in setUp

        // WHEN
        final ContentViewBuilderImpl builder = new ContentViewBuilderImpl(componentProvider);
        final ContentView result = builder.build(workbenchDef, imageProvider, new TreeContentViewDefinition());

        // THEN
        assertTrue(result instanceof TreeView);
    }

    @Test
    @Ignore
    public void testBuildingThumbnailView() {
        // GIVEN all conditions in setUp

        // WHEN
        final ContentViewBuilderImpl builder = new ContentViewBuilderImpl(componentProvider);
        final ContentView result = builder.build(workbenchDef, imageProvider, new ThumbnailContentViewDefinition());

        // THEN
        assertTrue(result instanceof ThumbnailView);
    }

    @Test
    @Ignore
    public void testBuildingSearchView() {
        // GIVEN all conditions in setUp

        // WHEN
        final ContentViewBuilderImpl builder = new ContentViewBuilderImpl(componentProvider);
        final ContentView result = builder.build(workbenchDef, imageProvider, new SearchContentViewDefinition());

        // THEN
        assertTrue(result instanceof SearchView);
    }

    @Test
    public void testGetPathReturnsRootIfNotSet() throws Exception {
        // GIVEN all conditions in setUp

        // WHEN
        String path = workbenchDef.getPath();

        // THEN
        assertEquals("/", path);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

}
