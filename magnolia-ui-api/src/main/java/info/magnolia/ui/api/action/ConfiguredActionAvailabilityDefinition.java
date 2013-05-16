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
package info.magnolia.ui.api.action;

import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation for {@link ActionAvailabilityDefinition}.
 */
public class ConfiguredActionAvailabilityDefinition implements ActionAvailabilityDefinition {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean root = false;
    private boolean properties = false;
    private boolean nodes = true;
    private Collection<String> nodeTypes = new ArrayList<String>();
    private AccessDefinition access = Components.newInstance(AccessDefinition.class, new Object[] {});

    @Override
    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    @Override
    public boolean isProperties() {
        return properties;
    }

    public void setProperties(boolean properties) {
        this.properties = properties;
    }

    @Override
    public boolean isNodes() {
        return this.nodes;
    }

    public void setNodes(boolean nodes) {
        this.nodes = nodes;
    }

    @Override
    public Collection<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(Collection<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void addNodeType(String nodeType) {
        nodeTypes.add(nodeType);
    }

    public void setAccess(AccessDefinition access) {
        this.access = access;
    }

    @Override
    public AccessDefinition getAccess() {
        return this.access;
    }

    @Override
    public boolean isAvailable(Item item) {
        // Validate that the user has all required roles
        if (!getAccess().hasAccess(MgnlContext.getUser())) {
            return false;
        }
        // If item is null, we act on root
        if (item == null) {
            return isRoot();
        }
        // If item is not a node, we act on property
        if (!item.isNode()) {
            return isProperties();
        }
        // we act on a node now, so check, whether the action is available for nodes
        if (isNodes()) {
            if (getNodeTypes().isEmpty()) {
                // if no node type specified, then it is available
                return true;
            }
            // else, it must have at least one of the specified node types
            for (String nodeType : getNodeTypes()) {
                try {
                    if (NodeUtil.isNodeType((Node) item, nodeType)) {
                        return true;
                    }
                } catch (RepositoryException e) {
                    log.error("Could not determine node type of node " + NodeUtil.getNodePathIfPossible((Node) item));
                }
            }
        }
        return false;
    }
}
