/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.workbench.list;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import info.magnolia.ui.workbench.ContentView;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

public class ListViewImplTest {

    private static final Object A = "a";
    private static final Object B = "b";
    private static final Object C = "c";

    private ListView listView;
    private ListView.Listener listener;
    private Table table;

    @Before
    public void setUp() throws Exception {
        // testing with a plain Vaadin Table
        listView = new ListViewImpl() {
            @Override
            protected Table createTable(Container container) {
                Table table = new Table(null, container);
                table.setSelectable(true);
                table.setMultiSelect(true);
                // use B as default selected value here
                table.setValue(Lists.newArrayList(B));
                return table;
            }
        };

        // #setContainer initializes Table and adds event listeners
        Container container = new IndexedContainer(Lists.newArrayList(A, B, C));
        listView.setContainer(container);

        // mock listener (i.e. ListPresenter) for interactions
        listener = mock(ContentView.Listener.class);
        listView.setListener(listener);

        table = (Table) listView.asVaadinComponent();
    }

    @Test
    public void selectReplacesSelectionOnlyOnce() throws Exception {
        // Vaadin table is multiselect, however when we select programmatically through the ListView, we intend to act
        // in single selection fashion (e.g. as a consequence of a location change).
        // GIVEN
        ArgumentCaptor<Set> itemsCaptor = ArgumentCaptor.forClass(Set.class);

        // WHEN
        listView.select(Lists.newArrayList(A));
        verify(listener, only()).onItemSelection(itemsCaptor.capture());

        // THEN
        Set<?> items = itemsCaptor.getValue();
        assertThat(items, allOf(hasSize(1), contains(A)));
    }

    @Test
    public void reselectDoesntTriggerAnything() throws Exception {
        listView.select(Lists.newArrayList(B)); // B is already selected in #setUp
        verifyZeroInteractions(listener);
    }

    @Test
    public void supportsNullSelection() throws Exception {
        // content-connectors may use null as default itemId
        listView.select(null);
        verify(listener, only()).onItemSelection((Set) argThat(empty()));
    }

    @Test
    public void supportsEmptySelection() throws Exception {
        listView.select(Collections.emptyList());
        verify(listener, only()).onItemSelection((Set) argThat(empty()));
    }

    @Test
    public void unselectGoesBackToEmptySelection() throws Exception {
        table.unselect(B); // B is already selected in #setUp
        verify(listener, only()).onItemSelection((Set) argThat(empty()));
    }
}