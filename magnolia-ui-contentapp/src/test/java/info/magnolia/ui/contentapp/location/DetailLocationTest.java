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
package info.magnolia.ui.contentapp.location;

import static org.junit.Assert.*;

import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.framework.location.DefaultLocation;

import org.junit.Test;

/**
 * ItemLocationTest.
 */
public class DetailLocationTest {

    @Test
    public void testToString() {
        assertEquals("app:someContentApp:browser;/some/node:edit", new DetailLocation("someContentApp", "browser", "/some/node:edit").toString());
        assertEquals("app:someContentApp;/some/node:view", new DetailLocation("someContentApp", "", "/some/node:view").toString());
        assertEquals("app:someContentApp", new DetailLocation("someContentApp", "", "").toString());

    }

    @Test
    public void testEqualsWithSameParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:edit");

        // WHEN
        DefaultLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/node:edit");

        // TEST
        assertTrue(defaultLocation.equals(itemLocation));

    }

    @Test
    public void testEqualsWithDifferentParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:view");

        // WHEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/node:edit");

        // TEST
        assertFalse(defaultLocation.equals(itemLocation));

    }

    @Test
    public void testGetNodePath() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view");

        // TEST
        assertEquals("/some/other/node/00", itemLocation.getNodePath());

    }

    @Test
    public void testGetAction() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit");

        // TEST
        assertEquals(DetailView.ViewType.EDIT, itemLocation.getViewType());

    }

    @Test
    public void testGetDefaultAction() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00");

        // TEST
        assertEquals(DetailView.ViewType.VIEW, itemLocation.getViewType());

    }

    @Test
    public void testUpdateNodePath() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit");

        // WHEN
        itemLocation.updateNodePath("/node/has/changes/01");

        // TEST
        assertEquals("/node/has/changes/01", itemLocation.getNodePath());

    }

    @Test
    public void testUpdateAction() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit");

        // WHEN
        itemLocation.updateViewtype(DetailView.ViewType.VIEW);

        // TEST
        assertEquals(DetailView.ViewType.VIEW, itemLocation.getViewType());
    }

    @Test
    public void testUpdateNodePathParameter() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view");

        // WHEN
        itemLocation.updateNodePath("/some/other/node/01");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/01:view", itemLocation.toString());
    }

    @Test
    public void testUpdateViewTypeParameter() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view");

        // WHEN
        itemLocation.updateViewtype(DetailView.ViewType.EDIT);

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:edit", itemLocation.toString());
    }

}
