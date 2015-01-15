/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.applauncher.connector;

import static org.junit.Assert.assertEquals;

import info.magnolia.ui.vaadin.gwt.client.applauncher.connector.AppLauncherConnector.GroupComparator;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the {@link GroupComparator}.
 */
public class GroupComparatorTest {

    private GroupComparator comparator;

    @Test
    public void testGroupOrdering() {
        // GIVEN
        List<AppGroup> groupList = new ArrayList<AppGroup>();
        groupList.add(new AppGroup("A", "group A", null, true, false));
        groupList.add(new AppGroup("B", "group B", null, true, false));
        groupList.add(new AppGroup("C", "group C", null, true, false));

        // configured order
        List<String> order = Arrays.asList("C", "A", "B");

        comparator = new GroupComparator(order);

        // WHEN
        Collections.sort(groupList, comparator);

        // THEN - expecting order C A B, i.e. as per ordering list.
        Iterator<AppGroup> it = groupList.iterator();
        assertEquals("C", it.next().getName());
        assertEquals("A", it.next().getName());
        assertEquals("B", it.next().getName());
    }

    @Test
    public void testGroupOrderingEnsuresAllCollapsibleGroupsGoLast() {
        // GIVEN
        List<AppGroup> groupList = new ArrayList<AppGroup>();
        groupList.add(new AppGroup("A", "Permanent group A", null, true, false));
        groupList.add(new AppGroup("B", "Permanent group B", null, true, false));
        groupList.add(new AppGroup("C", "Permanent group C", null, true, false));
        groupList.add(new AppGroup("D", "Temporary group D", null, false, false));
        groupList.add(new AppGroup("E", "Temporary group E", null, false, false));
        groupList.add(new AppGroup("F", "Temporary group F", null, false, false));

        // configured order
        List<String> order = Arrays.asList("C", "A", "D", "F", "E", "B");

        comparator = new GroupComparator(order);

        // WHEN
        Collections.sort(groupList, comparator);

        // THEN - expecting order C A B D F E, i.e. all fixed groups before all collapsible groups, otherwise as per ordering list.
        Iterator<AppGroup> it = groupList.iterator();
        assertEquals("C", it.next().getName());
        assertEquals("A", it.next().getName());
        assertEquals("B", it.next().getName());
        assertEquals("D", it.next().getName());
        assertEquals("F", it.next().getName());
        assertEquals("E", it.next().getName());
    }

}
