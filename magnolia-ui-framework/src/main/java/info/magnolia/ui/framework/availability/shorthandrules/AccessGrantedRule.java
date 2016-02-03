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

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.availability.AvailabilityRule;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;

/**
 * {@link AvailabilityRule} implementation which returns true if current user has any of the specified roles.
 */
public class AccessGrantedRule implements AvailabilityRule {

    public static final String DEFAULT_SUPERUSER_ROLE = "superuser";

    private AccessDefinition accessDefinition;

    public AccessDefinition getAccessDefinition() {
        return accessDefinition;
    }

    public void setAccessDefinition(AccessDefinition accessDefinition) {
        this.accessDefinition = accessDefinition;
    }

    @Override
    public boolean isAvailable(Collection<?> itemIds) {
        User user = MgnlContext.getUser();
        // Validate that the user has all the required roles
        Collection<String> userRoles = user.getAllRoles();
        Collection<String> roles = accessDefinition.getRoles();
        if (roles.isEmpty() || userRoles.contains(DEFAULT_SUPERUSER_ROLE) || CollectionUtils.containsAny(userRoles, roles)) {
            return true;
        }
        return false;
    }
}
