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
package info.magnolia.ui.framework.favorite.bookmark;

import static org.junit.Assert.*;

import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class BookmarkStoreTest extends MgnlTestCase {
    public static final String TEST_USER = "phantomas";
    public static final String BOOKMARK_TITLE = "bookmarkTitle";
    public static final String BOOKMARK_URL = "/myWebapp/something";

    private BookmarkStore store;
    Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = new MockSession(BookmarkStore.WORKSPACE_NAME);
        MockUtil.getSystemMockContext().addSession(BookmarkStore.WORKSPACE_NAME, session);

        store = new BookmarkStore();
    }

    @Test
    public void testFindAllForUser() throws Exception {
        // GIVEN
        final Node bookmarkNode = session.getRootNode().addNode(TEST_USER).addNode(BookmarkStore.FAVORITES_PATH).addNode(BookmarkStore.BOOKMARKS_PATH);

        final Node firstBookmarkNode = bookmarkNode.addNode(BOOKMARK_TITLE);
        firstBookmarkNode.setProperty(BookmarkStore.BOOKMARK_URL_PROPERTY_NAME, BOOKMARK_URL);

        // WHEN
        final List<Node> bookmarks = store.findAllForUser(TEST_USER);

        // THEN
        assertEquals(1, bookmarks.size());
        assertEquals(BOOKMARK_TITLE, bookmarks.get(0).getName());
        assertEquals(BOOKMARK_URL, bookmarks.get(0).getProperty(BookmarkStore.BOOKMARK_URL_PROPERTY_NAME).getString());
    }

    @Test
    public void testCreate() throws Exception {
        // GIVEN
        final String pathToBookmark = "/" + TEST_USER + "/" + BookmarkStore.FAVORITES_PATH + "/" + BookmarkStore.BOOKMARKS_PATH;

        // WHEN
        final boolean success = store.create(TEST_USER, BOOKMARK_TITLE, BOOKMARK_URL);

        // THEN
        assertTrue("Creating user should have succeeded", success);
        assertTrue("Expected item to exist: " + pathToBookmark, session.itemExists(pathToBookmark));
        assertTrue(session.getNode(pathToBookmark).hasNode(BOOKMARK_TITLE));
        assertEquals(BOOKMARK_URL, session.getNode(pathToBookmark).getNode(BOOKMARK_TITLE).getProperty(BookmarkStore.BOOKMARK_URL_PROPERTY_NAME).getString());
    }

}