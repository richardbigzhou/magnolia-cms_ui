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
package info.magnolia.ui.admincentral.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Task converts list of old 45 Admincentral URLs ACLs to new apps permissions.
 */
public class ConvertListAclToAppPermissionsTask extends AbstractRepositoryTask {

    private final Map<String, String[]> urlsToAppsPathsMap;
    private final boolean removeOldPermissions;

    public ConvertListAclToAppPermissionsTask(String name, String description, Map<String, String[]> urlsToAppsPathsMap, boolean removeOldPermissions) {
        super(name, description);
        this.urlsToAppsPathsMap = urlsToAppsPathsMap;
        this.removeOldPermissions = removeOldPermissions;
    }

    @Override
    public void doExecute(InstallContext ctx) throws TaskExecutionException {
        for (Entry<String, String[]> entry : urlsToAppsPathsMap.entrySet()) {
            new ConvertAclToAppPermissionTask(this.getName(), this.getDescription(), entry.getKey(), entry.getValue(), removeOldPermissions).execute(ctx);
        }
    }
}
