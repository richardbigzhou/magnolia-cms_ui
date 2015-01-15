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
package info.magnolia.ui.vaadin.grid;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Container.Hierarchical;

/**
 * MagnoliaTreeTableTest.
 */
public class MagnoliaTreeTableTest {

    protected MagnoliaTreeTable treeTable;
    protected Object root = "root";
    protected Object sub1 = "sub1";
    protected Object sub1Item = "sub1Item";
    protected Object sub2 = "sub2";
    protected Object sub2Item = "sub2Item";

    @Before
    public void setUp() {
        treeTable = new MagnoliaTreeTable();

        Hierarchical containerDataSource = treeTable.getContainerDataSource();

        containerDataSource.addItem(root);

        containerDataSource.addItem(sub1);
        containerDataSource.setParent(sub1, root);

        containerDataSource.addItem(sub1Item);
        containerDataSource.setParent(sub1Item, sub1);

        containerDataSource.addItem(sub2);
        containerDataSource.setParent(sub2, sub1);

        containerDataSource.addItem(sub2Item);
        containerDataSource.setParent(sub2Item, sub2);
    }

    @Test
    public void isDescendantOf() throws Exception {
        // GIVEN

        // WHEN
        boolean result = treeTable.isDescendantOf(sub2Item, root);

        // THEN
        assertTrue(result);

        // WHEN
        result = treeTable.isDescendantOf(sub2Item, sub1);

        // THEN
        assertTrue(result);

        // WHEN
        result = treeTable.isDescendantOf(sub2Item, sub2);

        // THEN
        assertTrue(result);

        // WHEN
        result = treeTable.isDescendantOf(sub1Item, sub2);

        // THEN
        assertFalse(result);
    }
}
