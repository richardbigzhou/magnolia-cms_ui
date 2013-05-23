/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.api.availability;

import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This rule returns true only if item is node and has all the specified node types.
 */
public class AllNodeTypesRule implements AvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(AllNodeTypesRule.class);

    Collection<String> nodeTypes = new ArrayList<String>();

    @Override
    public boolean isAvailable(Item item) {
        // is it node?
        if (item == null || !item.isNode()) {
            return false;
        }
        Node node = (Node) item;
        // evaluate all node types
        for (String nodeType : nodeTypes) {
            try {
                // if the node is of the reguired node type, or has the required mixin
                if (NodeUtil.isNodeType(node, nodeType) || NodeUtil.hasMixin(node, nodeType)) {
                    continue;
                }
                // else return false
                return false;
            } catch (RepositoryException e) {
                String path = "unknown";
                try {
                    path = node.getPath();
                } catch (RepositoryException e1) {
                    // nothing to do
                }
                log.warn("Error evaluating availability for node [{}], returning false: {}", path, e.getMessage());
                return false;
            }
        }
        // all node is of all node types
        return true;
    }

    public void setNodeTypes(Collection<String> rules) {
        this.nodeTypes = rules;
    }

    public Collection<String> getNodeTypes() {
        return this.nodeTypes;
    }

}
