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
package info.magnolia.ui.contentapp.location;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.api.location.DefaultLocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * ItemLocationTest.
 */
public class DetailLocationTest {

    @Before
    public void setUp() throws Exception {
        MockWebContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testToString() {
        assertEquals("app:someContentApp:browser;/some/node:edit:1.1", new DetailLocation("someContentApp", "browser", "/some/node:edit:1.1").toString());
        assertEquals("app:someContentApp;/some/node:view:1.0", new DetailLocation("someContentApp", "", "/some/node:view:1.0").toString());
        assertEquals("app:someContentApp", new DetailLocation("someContentApp", "", "").toString());
    }

    @Test
    public void testEqualsWithSameParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:edit:1.1");

        // WHEN
        DefaultLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/node:edit:1.1");

        // TEST
        assertTrue(defaultLocation.equals(itemLocation));

    }

    @Test
    public void testEqualsWithDifferentParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:view:1.1");

        // WHEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/node:view:1.0");

        // TEST
        assertFalse(defaultLocation.equals(itemLocation));

    }

    @Test
    public void testGetNodePath() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view:1.0");

        // TEST
        assertEquals("/some/other/node/00", itemLocation.getNodePath());
    }

    @Test
    public void testGetVersion() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view:1.0");

        // TEST
        assertEquals("1.0", itemLocation.getVersion());
    }

    @Test
    public void testGetAction() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit:1.0");

        // TEST
        assertEquals(DetailView.ViewType.EDIT, itemLocation.getViewType());
    }

    @Test
    public void testGetDefaultViewType() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00");

        // TEST
        assertEquals(DetailView.ViewType.EDIT, itemLocation.getViewType());

    }

    @Test
    public void testGetDefaultViewTypeWhenUnknown() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:someUnknownViewType");

        // TEST
        assertEquals(DetailView.ViewType.EDIT, itemLocation.getViewType());

    }

    @Test
    public void testUpdateNodePath() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit:1.1");

        // WHEN
        itemLocation.updateNodePath("/node/has/changes/01");

        // TEST
        assertEquals("/node/has/changes/01", itemLocation.getNodePath());

    }

    @Test
    public void testUpdateAction() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit:1.1");

        // WHEN
        itemLocation.updateViewtype(DetailView.ViewType.VIEW);

        // TEST
        assertEquals(DetailView.ViewType.VIEW, itemLocation.getViewType());
    }

    @Test
    public void testUpdateVersion() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:edit:1.1");

        // WHEN
        itemLocation.updateVersion("2.0");

        // TEST
        assertEquals("2.0", itemLocation.getVersion());
    }

    @Test
    public void testUpdateNodePathParameter() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view:1.1");

        // WHEN
        itemLocation.updateNodePath("/some/other/node/01");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/01:view:1.1", itemLocation.toString());
    }

    @Test
    public void testUpdateViewTypeParameter() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view:1.1");

        // WHEN
        itemLocation.updateViewtype(DetailView.ViewType.EDIT);

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:edit:1.1", itemLocation.toString());
    }

    @Test
    public void testUpdateVersionParameter() {
        // GIVEN
        DetailLocation itemLocation = new DetailLocation("someApp", "someContentApp", "/some/other/node/00:view:1.1");

        // WHEN
        itemLocation.updateVersion("2.0");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:view:2.0", itemLocation.toString());
    }

}
