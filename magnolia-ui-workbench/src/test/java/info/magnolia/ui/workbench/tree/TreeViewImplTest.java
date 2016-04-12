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
package info.magnolia.ui.workbench.tree;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.TreeTable;

public class TreeViewImplTest {

    // All first-level nodes are considered roots for Vaadin hierarchical containers, i.e. "visible roots"
    private static final Object ROOT_0 = "ROOT_0";
    private static final Object NODE_1 = "NODE_1";
    private static final Object NODE_11 = "NODE_11";
    private static final Object NODE_12 = "NODE_12";
    private static final Object NODE_121 = "NODE_121";

    private TreeView treeView;
    private TreeTable tree;

    @Before
    public void setUp() throws Exception {
        // testing with a plain Vaadin TreeTable
        TreeView treeView = new TreeViewImpl() {
            @Override
            protected TreeTable createTable(Container container) {
                return new TreeTable(null, container);
            }
        };
        this.treeView = treeView;

        // #setContainer initializes Table and adds event listeners
        Container.Hierarchical container = buildContainer();
        treeView.setContainer(container);

        tree = (TreeTable) treeView.asVaadinComponent();
    }

    private Container.Hierarchical buildContainer() {
        Container.Hierarchical container = new HierarchicalContainer();
        addItem(container, ROOT_0, null);
        addItem(container, NODE_1, ROOT_0);
        addItem(container, NODE_11, NODE_1);
        addItem(container, NODE_12, NODE_1);
        addItem(container, NODE_121, NODE_12);
        return container;
    }

    private static void addItem(Container.Hierarchical container, Object itemId, Object parentItemId) {
        container.addItem(itemId);
        container.setParent(itemId, parentItemId);
    }

    @Test
    public void selectExpandsTreeToNodeButNotNodeItself() throws Exception {
        // GIVEN
        // initial state
        assertThat(tree.isCollapsed(ROOT_0), is(true));
        assertThat(tree.isCollapsed(NODE_1), is(true));
        assertThat(tree.isCollapsed(NODE_12), is(true));
        assertThat(tree.isCollapsed(NODE_121), is(true));

        // WHEN
        treeView.select(Lists.newArrayList(NODE_12));

        // THEN
        assertThat(tree.isCollapsed(ROOT_0), is(false));
        assertThat(tree.isCollapsed(NODE_1), is(false));
        assertThat(tree.isCollapsed(NODE_12), is(true));
    }

    @Test
    public void collapseNodeWithSelectedChildUnselectsChild() throws Exception {
        // GIVEN same initial state
        tree.setCollapsed(ROOT_0, false);
        treeView.select(Lists.newArrayList(ROOT_0, NODE_121));

        // WHEN
        tree.setCollapsed(ROOT_0, true);

        // THEN
        assertThat(tree.isCollapsed(ROOT_0), is(true));
        assertThat(tree.isSelected(ROOT_0), is(true));
        assertThat(tree.isSelected(NODE_121), is(false));
    }

    @Test
    public void isDescendantOf() throws Exception {
        TreeViewImpl treeView = (TreeViewImpl) this.treeView;
        assertThat(treeView.isDescendantOf(NODE_121, ROOT_0), is(true));
        assertThat(treeView.isDescendantOf(NODE_121, NODE_1), is(true));
        assertThat(treeView.isDescendantOf(NODE_121, NODE_12), is(true));
        assertThat(treeView.isDescendantOf(NODE_11, NODE_12), is(false));
    }
}
