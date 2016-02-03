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
package info.magnolia.ui.framework.favorite;

import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.context.MgnlContext;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;

/**
 * Retrieves root node for favorites or bookmarks. Creates those nodes if not yet around.
 */
@Singleton
public class FavoriteStore {

    public static final String WORKSPACE_NAME = "profiles";
    public static final String FAVORITES_PATH = "favorites";
    public static final String BOOKMARKS_PATH = "bookmarks";

    /**
     * Get the bookmark root - create it if it's not yet around.
     *
     * @return the bookmark node for all favorites
     */
    public Node getBookmarkRoot() throws RepositoryException {
        final Node bookmarkRoot = JcrUtils.getOrAddNode(getFavoriteRoot(), BOOKMARKS_PATH, JcrConstants.NT_UNSTRUCTURED);
        bookmarkRoot.getSession().save();
        return bookmarkRoot;
    }

    /**
     * Get the favorites root - create it if it's not yet around.
     *
     * @return the root node for all favorites
     */
    public Node getFavoriteRoot() throws RepositoryException {
        final String userName = MgnlContext.getUser().getName();
        // Only system user has grants to add a node under the root of this workspace
        final Node favoriteRoot = MgnlContext.doInSystemContext(new JCRSessionOp<Node>(WORKSPACE_NAME) {
            @Override
            public Node exec(Session session) throws RepositoryException {

                final Node userNode = JcrUtils.getOrAddNode(session.getRootNode(), userName, JcrConstants.NT_UNSTRUCTURED);
                final Node favoriteNode = JcrUtils.getOrAddNode(userNode, FAVORITES_PATH, JcrConstants.NT_UNSTRUCTURED);
                session.save();

                return favoriteNode;
            }
        });
        return favoriteRoot;

    }
}
