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
package info.magnolia.security.app.util;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for updating ACLs when moving or renaming users and folders in the users workspace.
 */
public class UsersWorkspaceUtil {

    /**
     * Update ACLs on a user or role node or recursively for all contained users or roles when given a folder.
     */
    public static void updateAcls(Node node, String previousPath) throws RepositoryException {
        if (NodeUtil.isNodeType(node, NodeTypes.Folder.NAME)) {
            Iterable<Node> children = NodeUtil.getNodes(node);
            for (Node child : children) {
                updateAcls(child, previousPath + "/" + child.getName());
            }
        }

        if (NodeUtil.isNodeType(node, NodeTypes.User.NAME) && node.hasNode("acl_users")) {
            updateAclEntries(node, previousPath, node.getNode("acl_users"));
        }
        if (NodeUtil.isNodeType(node, NodeTypes.Role.NAME) && node.hasNode("acl_userroles")) {
            updateAclEntries(node, previousPath, node.getNode("acl_userroles"));
        }
    }

    private static void updateAclEntries(Node parentNode, String previousPath, Node aclNode) throws RepositoryException {
        for (Node entryNode : NodeUtil.getNodes(aclNode)) {
            Property path = entryNode.getProperty("path");
            String aclPath = path.getString();
            if (aclPath.startsWith(previousPath + "/")) {
                path.setValue(parentNode.getPath() + "/" + StringUtils.substringAfter(aclPath, previousPath + "/"));
            }
            if (aclPath.equals(previousPath)) {
                path.setValue(parentNode.getPath());
            }
        }
    }
}
