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

import static info.magnolia.test.hamcrest.ExecutionMatcher.throwsNothing;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.test.hamcrest.Execution;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.TreeTable;

public class TreeViewImplTest {

    // All first-level nodes are considered roots for Vaadin hierarchical containers, i.e. "visible roots"
    private static final Object ROOT_0 = "ROOT_0";
    private static final Object NODE_1 = "NODE_1";
    private static final Object NODE_11 = "NODE_11";
    private static final Object NODE_12 = "NODE_12";
    private static final Object NODE_121 = "NODE_121";

    private TreeView treeView;
    private TreeView.Listener listener;
    private TreeTable tree;

    @Before
    public void setUp() throws Exception {
        // testing with a plain Vaadin TreeTable
        setUp(new TreeViewImpl() {
            @Override
            protected TreeTable createTable(Container container) {
                return new TreeTable(null, container);
            }
        });
    }

    private void setUp(TreeView treeView) throws Exception {
        this.treeView = treeView;

        // #setContainer initializes Table and adds event listeners
        Container.Hierarchical container = buildContainer();
        treeView.setContainer(container);

        // mock listener (i.e. TreePresenter) for interactions
        listener = mock(TreeView.Listener.class);
        treeView.setListener(listener);

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

    /**
     * Should this test start to fail right after focusParent, returning null selection instead of a Set containing null,
     * then we'd consider it a Vaadin fix and remove the attached workaround in ListViewImpl.
     */
    @Test
    public void proveVaadinTableValueCanBeASetContainingNull() throws Exception {
        // when an itemId is selected and we process e.g. an itemClick, we want to unselect it
        // make sure this doesn't fail if value ends up being a set containing null

        // GIVEN
        TreeTable tree = new TreeTable(null, buildContainer());
        tree.setSelectable(true);
        tree.setMultiSelect(true);
        assertThat(tree.getValue(), anyOf(nullValue(), instanceOf(Set.class)));

        // WHEN
        produceTableValueContainingNull(tree);

        // THEN
        assertThat((Set<?>) tree.getValue(), contains((Object) null));
    }

    private void produceTableValueContainingNull(TreeTable tree) {
        tree.select(ROOT_0);
        assertThat((Set<?>) tree.getValue(), contains(ROOT_0));
        focusParent(tree, ROOT_0); // using a random rowKey, doesn't matter here
    }

    @Test
    public void clickListenerDoesntFailWhenValueContainsNull() throws Exception {
        // GIVEN
        final TreeTable tree = mock(TreeTable.class);
        final ArgumentCaptor<ItemClickEvent.ItemClickListener> itemClickListener = ArgumentCaptor.forClass(ItemClickEvent.ItemClickListener.class);
        setUp(new TreeViewImpl() {
            @Override
            protected TreeTable createTable(Container container) {
                return tree;
            }
        });
        verify(tree).addItemClickListener(itemClickListener.capture());

        // we already proved above a null-only set could occur as value before
        Set<Object> nullOnlySelection = new HashSet<>();
        nullOnlySelection.add(null);
        doReturn(nullOnlySelection).when(tree).getValue();

        // WHEN/THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                // manually clicking sth else again (i.e. where our NPE originated from)
                itemClickListener.getValue().itemClick(new ItemClickEvent(tree, null, "foo", null, mock(MouseEventDetails.class)));
            }
        }, throwsNothing());
    }

    /**
     * Simulates receiving a focusParent event from the client, via #changeVariables
     */
    private static void focusParent(TreeTable treeTable, Object rowKey) {
        treeTable.changeVariables(treeTable, ImmutableMap.of("focusParent", rowKey));
    }
}
