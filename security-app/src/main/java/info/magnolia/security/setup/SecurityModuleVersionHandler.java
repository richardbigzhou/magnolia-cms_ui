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

import info.magnolia.i18nsystem.setup.RemoveHardcodedI18nPropertiesFromSubappsTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.CheckAndModifyPartOfPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderNodeTo1stPosTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.dialog.field.SystemLanguagesFieldDefinition;

import java.util.ArrayList;
import java.util.List;

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
                        new CheckAndModifyPartOfPropertyValueTask("Change label of role creation action to 'Add role'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole", "label", "New role", "Add role")))

                );

        register(DeltaBuilder.update("5.1", "")
                .addTask(new PartialBootstrapTask("Bootstrap Delete Items action in Security app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actions/deleteItems"))
                .addTask(new PartialBootstrapTask("Bootstrap new actionbar section in Security app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actionbar/sections/multiple"))
                .addTask(new NodeExistsDelegateTask("Set ruleClass for deleteUser action to IsNotCurrentUserRule", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability",
                        new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability", "ruleClass", "info.magnolia.security.app.action.availability.IsNotCurrentUserRule")))
                .addTask(new NodeExistsDelegateTask("Update class for deleteGroup action", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/deleteGroup",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/deleteGroup", "class", "info.magnolia.security.app.action.DeleteGroupActionDefinition")))
                .addTask(new NodeExistsDelegateTask("Update class for deleteRole action", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/deleteRole",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/deleteRole", "class", "info.magnolia.security.app.action.DeleteRoleActionDefinition")))
                .addTask(new RemovePropertyTask("Remove label from form field", "Remove label property from the static1 field of the ACL tab of the Role dialog", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/acls/fields/static1", "label"))
                .addTask(new RemovePropertyTask("Remove label from form field", "Remove label property from the static1 field of the ACL tab of the Role dialog", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/acls/fields/static2", "label"))
                // Remove hardcoded i18n properties, e.g. label, description, etc.
                .addTask(new RemoveHardcodedI18nPropertiesFromSubappsTask("security-app"))
        );

        register(DeltaBuilder.update("5.1.1", "")
                .addTask(new PartialBootstrapTask("Bootstrap SystemLanguages field type.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.fieldTypes.xml", "/fieldTypes/systemLanguagesField"))
                .addTask(new NodeExistsDelegateTask("Update definition class for language field in user dialog.", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language", "class", SystemLanguagesFieldDefinition.class.getName())))
                .addTask(new NodeExistsDelegateTask("Remove now unnecessary options from language field in user dialog.", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language/options",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language/options")))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        Task orderNodeTo1stPosTask  = new OrderNodeTo1stPosTask("Security app ordering", "Moves the security app before the configuration app",RepositoryConstants.CONFIG,"modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/security");
        NodeExistsDelegateTask delegateTask = new NodeExistsDelegateTask("Security app ordering delegate task", "Moves the security app before the configuration app if the node exists", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/security", orderNodeTo1stPosTask);
        tasks.add(delegateTask);
        return tasks;
    }


}
