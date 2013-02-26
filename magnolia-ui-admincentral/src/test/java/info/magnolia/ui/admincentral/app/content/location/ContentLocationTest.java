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
package info.magnolia.ui.admincentral.app.content.location;

import static org.junit.Assert.*;

import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.workbench.content.view.ContentView;

import org.junit.Test;

/**
 * Test case for {@link info.magnolia.ui.admincentral.app.content.location.ContentLocation}.
 */
public class ContentLocationTest {
    @Test
    public void testToString() {
        assertEquals("app:someContentApp:main;/some/node:tree", new ContentLocation("someContentApp", "main", "/some/node:tree").toString());
        assertEquals("app:someContentApp;/some/node:search:hideandseek", new ContentLocation("someContentApp", "", "/some/node:search:hideandseek").toString());
        assertEquals("app:someContentApp;/some/node:tree", new ContentLocation("someContentApp", "", "/some/node:tree").toString());
        assertEquals("app:someContentApp;/some/node:tree", new ContentLocation("someContentApp", "", "/some/node:tree").toString());
        assertEquals("app;/some/node:tree", new ContentLocation("", "", "/some/node:tree").toString());
        assertEquals("app:mainSubApp;tree", new ContentLocation("mainSubApp", "", "tree").toString());
        assertEquals("app:someContentApp;:search:qux*", new ContentLocation("someContentApp", "", ":search:qux*").toString());
        assertEquals("app;:search:qux*", new ContentLocation("", "", ":search:qux*").toString());
        assertEquals("app;:search:qux*", new ContentLocation("", "", ":search:qux*").toString());
    }

    @Test
    public void testEqualsWithSameParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:search:hideandseek");

        // WHEN
        DefaultLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/node:search:hideandseek");

        // TEST
        assertTrue(defaultLocation.equals(contentLocation));

    }

    @Test
    public void testEqualsWithDifferentParameters() {
        // GIVEN
        DefaultLocation defaultLocation = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:search:hideandseek");

        // WHEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/node:search:found*");

        // TEST
        assertFalse(defaultLocation.equals(contentLocation));

    }

    @Test
    public void testGetNodePath() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:search:found*");

        // TEST
        assertEquals("/some/other/node/00", contentLocation.getNodePath());

    }

    @Test
    public void testGetViewType() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:search:found*");

        // TEST
        assertEquals(ContentView.ViewType.SEARCH, contentLocation.getViewType());

    }

    @Test
    public void testGetDefaultViewType() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00");

        // TEST
        assertEquals(ContentView.ViewType.TREE, contentLocation.getViewType());

    }

    @Test
    public void testGetQuery() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:search:found*");

        // TEST
        assertEquals("found*", contentLocation.getQuery());

    }

    @Test
    public void testUpdateNodePath() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:search:found*");

        // WHEN
        contentLocation.updateNodePath("/node/has/changes/01");

        // TEST
        assertEquals("/node/has/changes/01", contentLocation.getNodePath());

    }

    @Test
    public void testUpdateViewType() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:tree");

        // WHEN
        contentLocation.updateViewType(ContentView.ViewType.LIST);

        // TEST
        assertEquals(ContentView.ViewType.LIST, contentLocation.getViewType());
    }

    @Test
    public void testUpdateNodePathParameter() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:tree");

        // WHEN
        contentLocation.updateNodePath("/some/other/node/01");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/01:tree:", contentLocation.toString());
    }

    @Test
    public void testUpdateViewTypeParameter() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:tree");

        // WHEN
        contentLocation.updateViewType(ContentView.ViewType.LIST);

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:list:", contentLocation.toString());
    }

    @Test
    public void testUpdateQueryParameter() {
        // GIVEN
        ContentLocation contentLocation = new ContentLocation("someApp", "someContentApp", "/some/other/node/00:search:*blablu");

        // WHEN
        contentLocation.updateQuery("*blablu");

        // TEST
        assertEquals("app:someApp:someContentApp;/some/other/node/00:search:*blablu", contentLocation.toString());
    }
}
