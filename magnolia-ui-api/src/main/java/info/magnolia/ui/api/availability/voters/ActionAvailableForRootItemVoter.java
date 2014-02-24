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
package info.magnolia.ui.api.availability.voters;

import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;

/**
 * Action availability voter which returns positive result in case action is capable
 * of operating over root JCR items.
 */
public class ActionAvailableForRootItemVoter extends AbstractActionAvailabilityVoter {

    private boolean root;

    public ActionAvailableForRootItemVoter(Boolean isRoot) {
        root = isRoot;
    }

    public ActionAvailableForRootItemVoter(String isRoot) {
        this(Boolean.parseBoolean(isRoot));
    }

    private boolean isRoot(Object value) {
        if (value instanceof JcrNodeAdapter) {
            JcrNodeAdapter nodeAdapter = (JcrNodeAdapter)value;
            Node node = nodeAdapter.getJcrItem();
            try {
                node.getParent();
                return false;
            } catch (RepositoryException e) {
                return true;
            }

        }
        return false;
    }

    @Override
    protected boolean isAvailableForItem(Item item) {
        if (item == null || isRoot(item)) {
            return root;
        }
        return true;
    }
}
