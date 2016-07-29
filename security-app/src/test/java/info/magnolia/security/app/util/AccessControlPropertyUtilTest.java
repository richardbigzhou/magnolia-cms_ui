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

import static info.magnolia.security.app.util.AccessControlPropertyUtil.findBestMatchingPermissions;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.SimpleUrlPattern;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class AccessControlPropertyUtilTest {

    @Test
    public void testFindBestMatchingPermissions() throws Exception {
        // GIVEN
        Permission rootReadOnly             = newPermission("/", Permission.READ);
        Permission rootSubNodesReadWrite    = newPermission("/*", Permission.ALL);
        Permission pathReadWrite            = newPermission("/path", Permission.ALL);
        Permission pathSubNodesReadWrite    = newPermission("/path/*", Permission.ALL);
        Permission pathTestReadWrite        = newPermission("/path/testA", Permission.ALL);
        Permission pathTestSubNodesReadOnly = newPermission("/path/testA/*", Permission.READ);

        List<Permission> permissionList = Arrays.asList(
                rootReadOnly,
                rootSubNodesReadWrite,
                pathReadWrite,
                pathSubNodesReadWrite,
                pathTestReadWrite,
                pathTestSubNodesReadOnly);

        // WHEN-THEN
        assertThat(findBestMatchingPermissions(permissionList, "/"), is(rootSubNodesReadWrite));
        assertThat(findBestMatchingPermissions(permissionList, "/test"), is(rootSubNodesReadWrite));
        assertThat(findBestMatchingPermissions(permissionList, "/path"), is(pathReadWrite));
        assertThat(findBestMatchingPermissions(permissionList, "/path/test"), is(pathSubNodesReadWrite));
        assertThat(findBestMatchingPermissions(permissionList, "/path/testA"), is(pathTestReadWrite));
        assertThat(findBestMatchingPermissions(permissionList, "/path/testA/test"), is(pathTestSubNodesReadOnly));

        assertThat(findBestMatchingPermissions(permissionList, ""), is(nullValue()));
    }

    private Permission newPermission(String pattern, long permission) {
        Permission perm = new PermissionImpl();
        perm.setPattern(new SimpleUrlPattern(pattern));
        perm.setPermissions(permission);
        return perm;
    }
}