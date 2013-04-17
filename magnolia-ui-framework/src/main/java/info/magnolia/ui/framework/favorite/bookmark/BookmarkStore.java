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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.framework.AdmincentralNodeTypes;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores and retrieves bookmarks.
 */
@Singleton
public class BookmarkStore {

    public static final String BOOKMARK_URL_PROPERTY_NAME = "url";
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String WORKSPACE_NAME = "profiles";
    public static final String FAVORITES_PATH = "favorites";
    public static final String BOOKMARKS_PATH = "bookmarks";
    public static final String BOOKMARK_NODETYPE = JcrConstants.NT_BASE;


    protected Session getSession() throws RepositoryException{
        return MgnlContext.getJCRSession(WORKSPACE_NAME);
    }

    /**
     * @return the root node of all bookmarks for the current user
     */
    public Node getBookmarkRoot() throws RepositoryException {
        return getOrCreateBookmarkNode(MgnlContext.getInstance().getUser().getName());
    }

    protected Node getOrCreateBookmarkNode(String userName) throws RepositoryException {
        String favoritesUserNode = "/" + userName;
        Session session = getSession();
        if (session.nodeExists(favoritesUserNode)) {
            return NodeUtil.createPath(session.getNode(favoritesUserNode), FAVORITES_PATH + "/" + BOOKMARKS_PATH, AdmincentralNodeTypes.Favorite.NAME);
        } else {
            return NodeUtil.createPath(session.getRootNode(), favoritesUserNode + "/" + FAVORITES_PATH + "/" + BOOKMARKS_PATH, AdmincentralNodeTypes.Favorite.NAME);
        }
    }
}
