/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.workbench.ContentView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link info.magnolia.ui.contentapp.browser.BrowserLocation}.
 */
public class BrowserLocationTest {

    private MockWebContext ctx;

    @Before
    public void setUp() throws Exception {
        ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testToString() {
        assertEquals("app:someContentApp:browser;/some/node:treeview", new BrowserLocation("someContentApp", "browser", "/some/node:treeview").toString());
        assertEquals("app:someContentApp;/some/node:searchview:hideandseek", new BrowserLocation("someContentApp", "", "/some/node:searchview:hideandseek").toString());
        assertEquals("app:someContentApp;/some/node:treeview", new BrowserLocation("someContentApp", "", "/some/node:treeview").toString());
        assertEquals("app:someContentApp;/some/node:treeview", new BrowserLocation("someContentApp", "", "/some/node:treeview").toString());
        assertEquals("app;/some/node:treeview", new BrowserLocation("", "", "/some/node:treeview").toString());
        assertEquals("app:mainSubApp;treeview", new BrowserLocation("mainSubApp", "", "treeview").toString());
        assertEquals("app:someContentApp;:searchview:qux*", new BrowserLocation("someContentApp", "", ":searchview:qux*").toString());
        assertEquals("app;:searchview:qux*", new BrowserLocation("", "", ":searchview:qux*").toString());
        assertEquals("app;:searchview:qux*", new BrowserLocation("", "", ":searchview:qux*").toString());
    }

    @Test
    public void testEqualsWithSameParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:searchview:hideandseek");

        // WHEN
        DefaultLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/node:searchview:hideandseek");

        // TEST
        assertTrue(defaultLocation.equals(contentLocation));

    }

    @Test
    public void testEqualsWithDifferentParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:searchview:hideandseek");

        // WHEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/node:searchview:found*");

        // TEST
        assertFalse(defaultLocation.equals(contentLocation));

    }

    @Test
    public void testGetNodePath() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:searchview:found*");

        // TEST
        assertEquals("/some/other/node/00", contentLocation.getNodePath());

    }

    @Test
    public void testGetViewType() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:searchview:found*");

        // TEST
        assertEquals(ContentView.ViewType.SEARCH, contentLocation.getViewType());

    }

    @Test
    public void testGetDefaultViewType() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00");

        // TEST
        assertEquals(null, contentLocation.getViewType());

    }

    @Test
    public void testGetQuery() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:searchview:found*");

        // TEST
        assertEquals("found*", contentLocation.getQuery());

    }

    @Test
    public void testUpdateNodePath() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:searchview:found*");

        // WHEN
        contentLocation.updateNodePath("/node/has/changes/01");

        // TEST
        assertEquals("/node/has/changes/01", contentLocation.getNodePath());

    }

    @Test
    public void testUpdateViewType() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:treeview");

        // WHEN
        contentLocation.updateViewType(ContentView.ViewType.LIST);

        // TEST
        assertEquals(ContentView.ViewType.LIST, contentLocation.getViewType());
    }

    @Test
    public void testUpdateNodePathParameter() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:treeview");

        // WHEN
        contentLocation.updateNodePath("/some/other/node/01");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/01:treeview:", contentLocation.toString());
    }

    @Test
    public void testUpdateViewTypeParameter() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:treeview");

        // WHEN
        contentLocation.updateViewType(ContentView.ViewType.LIST);

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:listview:", contentLocation.toString());
    }

    @Test
    public void testUpdateQueryParameter() {
        // GIVEN
        BrowserLocation contentLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:searchview:*blablu");

        // WHEN
        contentLocation.updateQuery("*blablu");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:searchview:*blablu", contentLocation.toString());
    }

    @Test
         public void testGetNullViewType() {
        // GIVEN
        BrowserLocation browserLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:");

        // TEST
        assertNull(browserLocation.getViewType());
    }

    @Test
    public void testGetNullViewTypeWhenUnknown() {
        // GIVEN
        BrowserLocation browserLocation = new BrowserLocation("someApp", "someContentApp", "/some/other/node/00:someUnknownViewType");

        // TEST
        assertNull(browserLocation.getViewType());
    }
}
