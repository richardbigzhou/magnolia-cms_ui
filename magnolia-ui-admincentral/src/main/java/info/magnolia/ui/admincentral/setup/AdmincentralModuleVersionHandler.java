/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import static info.magnolia.nodebuilder.Ops.*;

import info.magnolia.cms.core.ItemType;
import info.magnolia.jcr.util.NodeTypeTemplateUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CheckOrCreatePropertyTask;
import info.magnolia.module.delta.ConditionalDelegateTask;
import info.magnolia.module.delta.CreateNodePathTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderNodeAfterTask;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.OrderNodeToFirstPositionTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.RenameNodesTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.NodeBuilderTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.setup.for5_0.AbstractNodeTypeRegistrationTask;
import info.magnolia.ui.admincentral.setup.for5_3.WidgetsetRelocationCondition;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.framework.setup.AddIsPublishedRuleToAllDeactivateActionsTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.jackrabbit.JcrConstants;

/**
 * VersionHandler for the Admincentral module.
 */
public class AdmincentralModuleVersionHandler extends DefaultModuleVersionHandler {

    protected static final String UI_ACTIONS_IMPORT = "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/import";
    protected static final String UI_IMPORT_FIELD = "/modules/ui-admincentral/dialogs/import/form/tabs/import/fields/name";

    /**
     * Check if the activation module is install and correctly configured.
     */
    private class RunConfigureActivationDelegateTask extends ConditionalDelegateTask {

        public RunConfigureActivationDelegateTask(String taskName, String taskDescription, Task ifTrue) {
            super(taskName, taskDescription, ifTrue);
        }

        @Override
        public boolean condition(InstallContext ctx) {
            try {
                return ctx.isModuleRegistered("activation") && ctx.getConfigJCRSession().nodeExists("/modules/activation")
                        && (!ctx.getConfigJCRSession().nodeExists("/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activation")
                        || !ctx.getConfigJCRSession().nodeExists("/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activationMonitor"));
            } catch (RepositoryException e) {
                return false;
            }
        }
    }

    private ArrayDelegateTask createActivationConfig = new ArrayDelegateTask("",
            new NodeExistsDelegateTask("Create node", "Create path.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps", null,
                    new CreateNodePathTask("Create node", "Create path.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps", ItemType.CONTENTNODE)),
            new NodeExistsDelegateTask("Create node", "Create entry in tools group of appLauncher for Activation tools.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activation", null,
                    new CreateNodeTask("Create node", "Create entry in tools group of appLauncher for Activation tools.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps", "activation", NodeTypes.ContentNode.NAME)),
            new NodeExistsDelegateTask("Create node", "Create entry in tools group of appLauncher for Activation monitor.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activationMonitor", null,
                    new CreateNodeTask("Create node", "Create entry in appLauncher for Activation monitor.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps", "activationMonitor", NodeTypes.ContentNode.NAME)));

    public AdmincentralModuleVersionHandler() {

        register(DeltaBuilder.update("5.0", "")
                .addTask(new ConvertListAclToAppPermissionsTask("Convert permissions for 'ui-admincentral' apps", "Convert ACL permissions to old menus to new admincentral apps permission", this.getAclsToAppsPermissionsMap(), true)));

        register(DeltaBuilder.update("5.0.1", "")
                .addTask(new NodeExistsDelegateTask("Remove dialog links Node", "Remove dialog definition in ui-admincentral/dialogs/links", RepositoryConstants.CONFIG, "/modules/ui-admincentral/dialogs/link",
                        new RemoveNodeTask("Remove dialog links Node", "Remove dialog definition in ui-admincentral/dialogs/links", RepositoryConstants.CONFIG, "/modules/ui-admincentral/dialogs/link")))

                .addTask(new PartialBootstrapTask("Add editProperty dialog", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.dialogs.xml", "/dialogs/editProperty"))
                .addTask(new PartialBootstrapTask("Add editProperty action to Configuration app", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/editProperty"))

                .addTask(new PartialBootstrapTask("Add renameItem dialog", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.dialogs.xml", "/dialogs/renameItem"))
                .addTask(new PartialBootstrapTask("Add rename action to Configuration app", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/rename"))

                // Update actionbars
                .addTask(new NodeExistsDelegateTask("Remove duplicateActions section from Configuration app.", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/duplicateActions",
                        new RemoveNodeTask("Remove duplicateActions section from Configuration app", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/duplicateActions")))
                .addTask(new PartialBootstrapTask("Add editActions section to Configuration app", "Adds editProperty, rename, and duplicate actions.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actionbar/sections/folders/groups/editActions"))
                .addTask(new OrderNodeAfterTask("Move editActions section after addingActions", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/editActions", "addingActions"))

                // JCR App should extend Configuration App.
                .addTask(new NodeExistsDelegateTask("Remove websiteJcrBrowser App subApps.", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/websiteJcrBrowser/subApps",
                        new RemoveNodeTask("Remove websiteJcrBrowser App subApps.", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/websiteJcrBrowser/subApps")))
                .addTask(new PartialBootstrapTask("Add updated websiteJcrBrowser App subApps.", "It now extends Configuration app.", "/mgnl-bootstrap-prior-5_3/config.modules.ui-admincentral.apps.websiteJcrBrowser.xml", "/websiteJcrBrowser/subApps"))

        );

        register(DeltaBuilder.update("5.0.2", "")
                // new action for confirmation
                .addTask(new PartialBootstrapTask("Add new confirmation action definition", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/confirmDeletion"))

                .addTask(new NodeExistsDelegateTask("Remove action availability from delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete/availability",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete/availability")))
                .addTask(new PropertyExistsDelegateTask("Remove label for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "label",
                        new RemovePropertyTask("Remove label for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "label")))
                .addTask(new PropertyExistsDelegateTask("Remove icon for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "icon",
                        new RemovePropertyTask("Remove icon for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "icon")))

                // update actionbar for confirmation
                .addTask(new NodeExistsDelegateTask("Update actionbar configuration", "Rename action mapping to new confirmation action", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar",
                        new RenameNodesTask("Rename action bar items", "Rename delete to confirmDeletion", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar", "delete", "confirmDeletion", NodeTypes.ContentNode.NAME))));

        register(DeltaBuilder.update("5.1", "")
                .addTask(new PartialBootstrapTask("Bootstrap new actionbar section in Configuration app", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actionbar/sections/multiple"))
                .addTask(new PartialBootstrapTask("JCR browser app node types", "Bootstraps the new node types configuration for the JCR browser app", "/mgnl-bootstrap-prior-5_3/config.modules.ui-admincentral.apps.websiteJcrBrowser.xml", "/websiteJcrBrowser/subApps/browser/workbench/nodeTypes"))
                .addTask(new NewPropertyTask("Set multiple=true in confirmDeletion action's availability.", "Sets multiple=true in confirmDeletion action's availability., i.e. the Delete action now supports multiple items.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/confirmDeletion/availability", "multiple", true))
                .addTask(new NewPropertyTask("Set main node type in configuration app as strict", "Sets main node type as strict, i.e. its substypes won't be included in list and search views.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/workbench/nodeTypes/mainNodeType", "strict", true))
                .addTask(new NewPropertyTask("Set folder node type in configuration app as strict", "Sets folder node type as strict, i.e. its substypes won't be included in list and search views.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/workbench/nodeTypes/folderNodeType", "strict", true))
                .addTask(new NodeExistsDelegateTask("Conditional move of configuration of the appLauncherLayout from the ui-framework to the ui-admincentral", "Moves the the conf. of the appLauncherLayout from the ui-framework to the ui-admincentral (if the node exists)", RepositoryConstants.CONFIG, "/modules/ui-framework/config/appLauncherLayout", new MoveNodeTask("Move configuration of the appLauncherLayout from the ui-framework to the ui-admincentral", "Moves the the conf. of the appLauncherLayout from the ui-framework to the ui-admincentral", RepositoryConstants.CONFIG, "/modules/ui-framework/config/appLauncherLayout", "/modules/ui-admincentral/config/appLauncherLayout", false)))
                .addTask(new NodeExistsDelegateTask("Conditional removal of the node /modules/ui-framework/config", "Removes the node /modules/ui-framework/config if it exists (it should empty)", RepositoryConstants.CONFIG, "/modules/ui-framework/config", new RemoveNodeTask("Removing the empty node /modules/ui-framework/config", "Removes the empty node /modules/ui-framework/config", RepositoryConstants.CONFIG, "/modules/ui-framework/config")))
                .addTask(new RemoveHardcodedI18nPropertiesFromAdmincentralTask())

                // update vaadin servlet params (we inject a custom UIProvider instead)
                .addTask(new PropertyExistsDelegateTask("Check widgetset servlet param", "Checks if widgetset is configured as servlet parameter", RepositoryConstants.CONFIG, "/server/filters/servlets/AdminCentral/parameters", "widgetset",
                        new RemovePropertyTask("Remove widgetset servlet param", "Removes the widgetset property from AdminCentral servlet parameters", RepositoryConstants.CONFIG, "/server/filters/servlets/AdminCentral/parameters", "widgetset")))

                .addTask(new PartialBootstrapTask("Bootstrap move action in Configuration app", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/move"))
                .addTask(new PartialBootstrapTask("Bootstrap move action to Configuration app actionbar", "Adds action move to folder/editActions actionbar.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actionbar/sections/folders/groups/editActions/items/move")));

        register(DeltaBuilder.update("5.1.1", "")

                // update favorite node type
                .addTask(new AbstractNodeTypeRegistrationTask("Update favorite node type", "This task ensures the mgnl:favorite node type is updated with its appropriate mixin supertypes.", FavoriteStore.WORKSPACE_NAME) {

                    @Override
                    public List<String> getNodeTypesToUnregister(NodeTypeManager nodeTypeManager) {
                        return null;
                    }

                    @Override
                    public List<NodeTypeDefinition> getNodeTypesToRegister(NodeTypeManager nodeTypeManager) throws RepositoryException {
                        List<NodeTypeDefinition> types = new ArrayList<NodeTypeDefinition>();

                        types.add(NodeTypeTemplateUtil.createSimpleNodeType(nodeTypeManager,
                                AdmincentralNodeTypes.Favorite.NAME,
                                Arrays.asList(
                                        JcrConstants.NT_BASE,
                                        NodeTypes.Created.NAME,
                                        NodeTypes.LastModified.NAME))
                                );

                        return types;
                    }
                }));

        register(DeltaBuilder.update("5.1.2", "")
                .addTask(new PartialBootstrapTask("Configuration app", "Change availability of add folder action", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/addFolder/availability/nodeTypes"))
                .addTask(new RemovePropertyTask("Remove hardcoded icon", "Remove hardcoded icon of Configuration app", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration", "icon"))
                .addTask(new RemovePropertyTask("Remove hardcoded icon", "Remove hardcoded icon of JCR app", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/websiteJcrBrowser", "icon")));

        register(DeltaBuilder.update("5.2", "")
                .addTask(new BootstrapSingleModuleResource("Bootstrap virtualURIMapping", "Bootstrap virtual URI mappings which have moved from adminInterface module.", "config.modules.ui-admincentral.virtualURIMapping.xml")));

        register(DeltaBuilder.update("5.2.1", "")
                .addTask(new IsModuleInstalledOrRegistered("Create node", "Create entry in tools group of appLauncher for Activation tools.", "activation",
                        createActivationConfig))
                .addTask(new NodeExistsDelegateTask("Reorder JCR in TOOLS group", "This reorders the JCR app before Activation in the Tools group of the applauncher.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activation",
                        new OrderNodeBeforeTask("", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/websiteJcrBrowser", "activation"))));

        register(DeltaBuilder.update("5.2.2", "")
                .addTask(new RunConfigureActivationDelegateTask("Correct miss configuration of the activation module", "", createActivationConfig))
                .addTask(new PropertyExistsDelegateTask("Remove obsolete property if exists", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/stkSiteApp", "icon",
                        new RemovePropertyTask("Remove obsolete property", "Remove obsolete '/modules/ui-admincentral/apps/stkSiteApp/icon'",
                                RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/stkSiteApp", "icon")))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/commands/default/delete/deactivate", "enabled", "true"))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/templates/deleted", "i18nBasename", "info.magnolia.module.admininterface.messages"))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration", "class", ConfiguredAppDescriptor.class.getName()))
                .addTask(new RemoveNodeTask("Delete ICEPush MIME mapping", "ICEPush is no longer used an thus its MIME-mapping should be removed", RepositoryConstants.CONFIG, "/server/MIMEMapping/icepush"))
                .addTask(new NodeExistsDelegateTask("Reconfigure activate action of configuration app", "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activate",
                        new PartialBootstrapTask("Reconfigure activate action of configuration app", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/activate/params"))));

        register(DeltaBuilder.update("5.2.4", "")
                .addTask(new ArrayDelegateTask("Update user menu", "This task updates the user menu with icons and a new action to edit user profile.",
                        new NodeExistsDelegateTask("Logout icon", "This task adds an icon to logout user menu entry.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/userMenu/actions/logout",
                                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/userMenu/actions/logout", "icon", "icon-redo")),
                        new PartialBootstrapTask("Edit user profile dialog", "This task adds dialog to edit user profile.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.dialogs.xml", "/dialogs/editUserProfile"),
                        new PartialBootstrapTask("Edit user profile action", "This taks adds the edit user profile action to user menu.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.config.userMenu.xml", "/userMenu/actions/editUserProfile"),
                        new OrderNodeToFirstPositionTask("Order edit user profile action to first position in user menu", "modules/ui-admincentral/config/userMenu/actions/editUserProfile")))
                .addTask(new CheckAndModifyPropertyValueTask("/modules/ui-admincentral/apps/configuration/", "class", "info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor", "info.magnolia.ui.contentapp.ContentAppDescriptor")));

        register(DeltaBuilder.update("5.2.5", "")
                .addTask(new IsModuleInstalledOrRegistered("Configure recursive activation and deletion as asynchronous", "scheduler", new ArrayDelegateTask("",
                        new NodeExistsDelegateTask("Configure recursive activation as asynchronous", "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activateRecursive",
                                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activateRecursive", "asynchronous", "true")),
                        new NodeExistsDelegateTask("Configure deletion as asynchronous", "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete",
                                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "asynchronous", "true")),
                        new BootstrapSingleModuleResource("config.modules.ui-admincentral.messageViews.longRunning.xml")
                        ))));

        register(DeltaBuilder.update("5.2.7", "")
                .addTask(new NodeExistsDelegateTask("Allow import action at root level in configuration and STK apps", UI_ACTIONS_IMPORT,
                        new NodeBuilderTask("Allow import action at root level in configuration and STK apps", "", ErrorHandling.logging, RepositoryConstants.CONFIG, UI_ACTIONS_IMPORT,
                                addNode("availability", NodeTypes.ContentNode.NAME).then(
                                        addProperty("root", true))))));

        register(DeltaBuilder.update("5.3", "")
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/workbench", "dropConstraintClass", "info.magnolia.ui.workbench.tree.drop.NodesAndPropsDropConstraint"))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/move/availability", "properties", "true"))
                .addCondition(new WidgetsetRelocationCondition()));

        register(DeltaBuilder.update("5.3.2", "")
                .addTask(new NodeExistsDelegateTask("Create a new property required in '/modules/ui-admincentral/dialogs/import/form/tabs/import/fields/name' with true value", UI_IMPORT_FIELD,
                        new CheckOrCreatePropertyTask("Create a new property required in '/modules/ui-admincentral/dialogs/import/form/tabs/import/fields/name' with true value", UI_IMPORT_FIELD, "required", "true"))));
        register(DeltaBuilder.update("5.3.4", "")
                .addTask(new PartialBootstrapTask("Add restorePreviousVersion command", "Adds restorePreviousVersion command.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.commands.xml", "/commands/default/restorePreviousVersion")));
        register(DeltaBuilder.update("5.3.6", "")
                .addTask(new AddIsPublishedRuleToAllDeactivateActionsTask("", "/modules/ui-admincentral/apps/")));
        register(DeltaBuilder.update("5.4", "")
                .addTask(new BootstrapSingleResource("Bootstrap Pulse Presenters", "Bootstrap the new configuration for tasks and messages in Pulse.",
                        "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.config.pulse.xml")));
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> list = new ArrayList<Task>();

        list.add(new IsModuleInstalledOrRegistered(
                "Replace login security pattern",
                "Replaces old login security pattern '/.resources/loginForm' (if present) with the new one '/.resources/defaultLoginForm'.",
                "adminInterface",
                new CheckAndModifyPropertyValueTask(
                        "",
                        "",
                        RepositoryConstants.CONFIG,
                        "/server/filters/uriSecurity/bypasses/login",
                        "pattern",
                        "/.resources/loginForm",
                        "/.resources/defaultLoginForm")));
        list.add(new NodeExistsDelegateTask("Remove PageEditorServlet from configuration", "Remove 4.5.x PageEditorServlet from the servlet registration. ", RepositoryConstants.CONFIG, "/server/filters/servlets/PageEditorServlet",
                new RemoveNodeTask("Remove PageEditorServlet from configuration", "Remove 4.5.x PageEditorServlet from the servlet registration. ", RepositoryConstants.CONFIG, "/server/filters/servlets/PageEditorServlet")));
        list.add(new NodeExistsDelegateTask("Reorder JCR in TOOLS group", "This reorders the JCR app before Activation in the Tools group of the applauncher.", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activation",
                new OrderNodeBeforeTask("", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/websiteJcrBrowser", "activation")));
        list.add(new IsModuleInstalledOrRegistered("Configure recursive activation and deletion as asynchronous", "scheduler", new ArrayDelegateTask("",
                new NodeExistsDelegateTask("Configure recursive activation as asynchronous", "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activateRecursive",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activateRecursive", "asynchronous", "true")),
                new NodeExistsDelegateTask("Configure deletion as asynchronous", "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "asynchronous", "true")),
                new BootstrapSingleModuleResource("config.modules.ui-admincentral.messageViews.longRunning.xml"))));
        return list;
    }

    private Map<String, String[]> getAclsToAppsPermissionsMap() {
        Map<String, String[]> permissionsMap = new HashMap<String, String[]>();

        // DEV and TOOLS menu
        permissionsMap.put("/modules/adminInterface/config/menu/tools", new String[] { // old 'Tools' menu is in 5 is split into two
                "/modules/ui-admincentral/config/appLauncherLayout/groups/tools", "/modules/ui-admincentral/config/appLauncherLayout/groups/dev"
        });

        permissionsMap.put("/modules/adminInterface/config/menu/tools/websiteJCR", new String[] { "/modules/ui-admincentral/apps/websiteJcrBrowser" });
        permissionsMap.put("/.magnolia/pages/configuration", new String[] { "/modules/ui-admincentral/apps/configuration" });

        // DATA menu
        permissionsMap.put("/modules/adminInterface/config/menu/templating-kit/data", new String[] { "/modules/ui-admincentral/config/appLauncherLayout/groups/data" });

        return permissionsMap;
    }

}
