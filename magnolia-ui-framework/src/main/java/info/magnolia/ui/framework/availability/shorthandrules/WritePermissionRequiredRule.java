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

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link info.magnolia.ui.api.availability.AvailabilityRule AvailabilityRule} implementation which returns true if current user has write permissions for the evaluated items.
 */
public class WritePermissionRequiredRule extends AbstractAvailabilityRule {

    private static Logger log = LoggerFactory.getLogger(WritePermissionRequiredRule.class);

    private boolean writePermissionRequired;

    public boolean isWritePermissionRequired() {
        return writePermissionRequired;
    }

    public void setWritePermissionRequired(boolean writePermissionRequired) {
        this.writePermissionRequired = writePermissionRequired;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        if (writePermissionRequired && itemId instanceof JcrItemId) {
            Item jcrItem = null;
            try {
                jcrItem = JcrItemUtil.getJcrItem((JcrItemId)itemId);
                Node node = jcrItem instanceof Property ? jcrItem.getParent() : (Node) jcrItem;
                return PermissionUtil.isGranted(node, Permission.WRITE);
            } catch (RepositoryException e) {
                log.warn("Could not evaluate write permission for {}.", jcrItem);
            }
        }
        return true;
    }
}