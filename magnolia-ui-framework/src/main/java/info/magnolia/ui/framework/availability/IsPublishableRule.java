/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.framework.availability;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns true if all ancestors of this item are activated, false otherwise.
 */
public class IsPublishableRule extends AbstractAvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(IsPublishableRule.class);

    @Override
    public boolean isAvailableForItem(Object itemId) {
        if (itemId instanceof JcrItemId && !(itemId instanceof JcrPropertyItemId)) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            Node node = SessionUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
            if (node != null) {
                try {
                    return canBePublished(node);
                } catch (RepositoryException e) {
                    log.warn("Error evaluating availability for node [{}], returning false: {}", NodeUtil.getPathIfPossible(node), e.getMessage());
                }
            }
        }
        return false;
    }

    private boolean canBePublished(final Node node) throws RepositoryException {
        int level = node.getDepth();

        while (level != 0) {
            try {
                Node ancestor = (Node) node.getAncestor(--level);
                // this is root
                if (ancestor.getDepth() == 0) {
                    return true;
                }
                if (!NodeTypes.Activatable.isActivated(ancestor)) {
                    return false;
                }
            } catch (AccessDeniedException e) {
                log.debug("Node {} didn't allow access to ancestors ", node.getIdentifier());
            }
        }
        return true;
    }
}
