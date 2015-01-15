/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Availability rule for non-versioned items.
 */
public class IsNotVersionedRule extends AbstractAvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(IsDeletedRule.class);

    @Override
    protected boolean isAvailableForItem(Item item) {
        if (item != null && item.isNode()) {
            Node node = (Node) item;

            if (node instanceof Version) {
                return false;
            }

            try {
                String workspace = node.getSession().getWorkspace().getName();
                if (RepositoryConstants.VERSION_STORE.equals(workspace)) {
                    return false;
                }
            } catch (RepositoryException e) {
                log.warn("Error evaluating availability for node [{}], returning false: {}", NodeUtil.getPathIfPossible(node), e);
            }
        }

        return true;
    }

}
