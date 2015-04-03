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
package info.magnolia.ui.framework.availability.shorthandrules;

import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeItemId;

import org.apache.commons.lang3.ObjectUtils;

/**
 * {@link info.magnolia.ui.api.availability.AvailabilityRule AvailabilityRule} implementation which returns true if evaluated item is the default itemId —
 * typically the root node for JCR-based content apps.
 */
public class JcrRootAllowedRule extends AbstractAvailabilityRule {

    private boolean rootAllowed;
    private Object defaultItemId;

    public boolean isRootAllowed() {
        return rootAllowed;
    }

    public void setRootAllowed(boolean rootAllowed) {
        this.rootAllowed = rootAllowed;
    }

    public Object getDefaultItemId() {
        return defaultItemId;
    }

    public void setDefaultItemId(Object defaultItemId) {
        this.defaultItemId = defaultItemId;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        if (itemId instanceof JcrNewNodeItemId) {
            return true;
        }

        if (ObjectUtils.equals(getDefaultItemId(), itemId)) {
            return rootAllowed;
        }
        return true;
    }
}
