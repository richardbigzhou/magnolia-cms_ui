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
package info.magnolia.security.app.container;

import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.tree.MoveLocation;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;
import info.magnolia.ui.workbench.tree.drop.TreeViewDropHandler;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.TreeTable;

/**
 * Custom drop hander ensuring update of acls for security principals upon moving their location.
 */
public class SecurityDropHandler extends TreeViewDropHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityDropHandler.class);

    public SecurityDropHandler() {
        super();
    }

    public SecurityDropHandler(TreeTable tree, DropConstraint constraint) {
        super(tree, constraint);
    }

    @Override
    public boolean moveItem(com.vaadin.data.Item source, com.vaadin.data.Item target, MoveLocation location) {
        Item sourceItem = ((JcrItemAdapter) source).getJcrItem();
        try {
            String pathBefore = sourceItem.getPath();
            boolean moved = super.moveItem(source, target, location);
            if (moved && sourceItem.isNode()) {
                UsersWorkspaceUtil.updateAcls((Node) sourceItem, pathBefore);
                sourceItem.getSession().save();
            }
            return moved;
        } catch (RepositoryException e) {
            log.debug("Failed to move security principal (user,group,role) with {}", e.getMessage(), e);
            return false;
        }
    }

}
