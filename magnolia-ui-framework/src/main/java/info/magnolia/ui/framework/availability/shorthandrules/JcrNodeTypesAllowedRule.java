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
package info.magnolia.ui.framework.availability.shorthandrules;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * {@link info.magnolia.ui.api.availability.AvailabilityRule AvailabilityRule} implementation which returns true if evaluated items are of the specified JCR node types.
 */
public class JcrNodeTypesAllowedRule extends AbstractAvailabilityRule {

    private Collection<String> nodeTypes;

    public Collection<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(Collection<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        // if no node type defined, then valid for all node types
        if (nodeTypes.isEmpty() && itemId != null) {
            return true;
        }
        if (itemId instanceof JcrNewNodeItemId) {
            for (String nodeType : nodeTypes) {
                if (((JcrNewNodeItemId) itemId).getPrimaryNodeType().equals(nodeType)) {
                    return true;
                }
            }
        }
        else if (itemId instanceof JcrItemId && !(itemId instanceof JcrPropertyItemId)) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            Node node = SessionUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
            // else the node must match at least one of the configured node types
            for (String nodeType : nodeTypes) {
                try {
                    if (NodeUtil.isNodeType(node, nodeType)) {
                        return true;
                    }
                } catch (RepositoryException e) {
                    continue;
                }
            }
        }
        return false;
    }
}
