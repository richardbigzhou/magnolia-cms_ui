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
package info.magnolia.security.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.CheckAndModifyPartOfPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.repository.RepositoryConstants;

/**
 * Version handler for Security app module.
 */
public class SecurityModuleVersionHandler extends DefaultModuleVersionHandler {


    public SecurityModuleVersionHandler() {
        register(DeltaBuilder.update("5.0.1", "")

                .addTask(new NodeExistsDelegateTask("Change label of folder creation action to 'Add folder'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addFolder",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of foldercreation action to 'Add folder'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addFolder", "label", "New folder", "Add folder")))

                .addTask(new NodeExistsDelegateTask("Change label of user creation action to 'Add user'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addUser",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of user creation action to 'Add user'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addUser", "label", "New user", "Add user")))

                .addTask(new NodeExistsDelegateTask("Change label of user creation action to 'Add group'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/addGroup",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of user creation action to 'Add group'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/addGroup", "label", "New group", "Add group")))


                .addTask(new NodeExistsDelegateTask("Change label of role creation action to 'Add role'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of role creation action to 'Add roll'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole", "label", "New role", "Add role")))

                );
    }


}
