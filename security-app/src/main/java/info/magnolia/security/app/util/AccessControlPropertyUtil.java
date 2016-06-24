/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.security.app.util;

import info.magnolia.cms.security.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for finding a matching permission to the path that want to grant permission when validating.
 */
public class AccessControlPropertyUtil {

    /**
     * Return the best matching permission that matches with path and has equal or greater rights than the ones being granted.
     */
    public static Permission findBestMatchingPermissions(List<Permission> permissions, String path) {
        if (permissions == null) {
            return null;
        }
        Permission bestMatch = null;
        long permission = 0;
        int patternLength = 0;
        List<Permission> temp = new ArrayList<>();
        temp.addAll(permissions);
        for (Permission p : temp) {
            if (p.match(path)) {
                int l = p.getPattern().getLength();
                if (patternLength == l && (permission < p.getPermissions())) {
                    permission = p.getPermissions();
                    bestMatch = p;
                } else if (patternLength < l) {
                    patternLength = l;
                    permission = p.getPermissions();
                    bestMatch = p;
                }
            }
        }
        return bestMatch;
    }
}
