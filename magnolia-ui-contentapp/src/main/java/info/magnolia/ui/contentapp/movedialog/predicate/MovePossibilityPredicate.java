/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.contentapp.movedialog.predicate;


import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Checks whether it is possible to place the collection of nodes relatively to the tested node.
 */
public class MovePossibilityPredicate {

    private Logger log = LoggerFactory.getLogger(getClass());

    private List<? extends Item> candidates;

    protected DropConstraint constraint;

    protected MovePossibilityPredicate(DropConstraint constraint, List<? extends Item> candidates) {
        this.constraint = constraint;
        this.candidates = candidates;
    }

    public boolean isMovePossible(Item hostCandidate) {
        boolean isPossible = true;
        Iterator<? extends Item> it = candidates.iterator();
        while (it.hasNext() && isPossible) {
            Item item = it.next();
            isPossible &= checkItem(item, hostCandidate);
        }
        return isPossible;
    }

    protected boolean checkItem(Item item, Item hostCandidate) {
        if ((item instanceof JcrItemAdapter) && (hostCandidate instanceof JcrItemAdapter)) {
            JcrItemAdapter jcrItem = (JcrItemAdapter)item;
            JcrItemAdapter jcrHost = (JcrItemAdapter)hostCandidate;
            try {
                return basicMoveCheck(jcrItem.getJcrItem(), jcrHost.getJcrItem());
            } catch (RepositoryException e) {
                log.warn("Error occurred during basic move check: ", e);
                return false;
            }
        }
        return true;
    }

    protected boolean hostIsRoot(Item hostCandidate) {
        if (hostCandidate instanceof JcrNodeAdapter) {
            JcrNodeAdapter jcrItem = (JcrNodeAdapter) hostCandidate;
            try {
                if (jcrItem.getJcrItem().getParent() == null) {
                    return true;
                }
            } catch (RepositoryException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform basic check.
     */
    protected boolean basicMoveCheck(javax.jcr.Item source, javax.jcr.Item target) throws RepositoryException {
        if (!target.isNode() || !source.isNode()) {
            return false;
        }
        if (target.getPath().equals(source.getPath())) {
            return false;
        }
        return !NodeUtil.isSame((Node) target, source.getParent());
    }
}
