/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.contacts;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.Security;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for Contacts app module.
 */
public class ContactsAppModuleVersionHandler extends DefaultModuleVersionHandler {

    final Task grantReadPermissionToAnonymousUser = new AbstractTask("Anonymous permissions for contact App", "Grants the anonymous user the read permission to the contact App workspace.") {
        @Override
        public void execute(InstallContext installContext) throws TaskExecutionException {

            RoleManager roleManager = Security.getRoleManager();
            Role anonymous = roleManager.getRole("anonymous");

            roleManager.addPermission(anonymous, "contacts", "/", Permission.READ);
            roleManager.addPermission(anonymous, "contacts", "/*", Permission.READ);
        }
    };

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        tasks.add(grantReadPermissionToAnonymousUser);
        return tasks;
    }
}
