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
package info.magnolia.ui.framework.setup;

import static info.magnolia.nodebuilder.Ops.*;

import info.magnolia.i18nsystem.setup.RemoveHardcodedI18nPropertiesFromDialogsTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.ChangeAllPropertiesWithCertainValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RenameNodesTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.NodeBuilderTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.dialog.setup.migration.CheckBoxRadioControlMigrator;
import info.magnolia.ui.dialog.setup.migration.CheckBoxSwitchControlMigrator;
import info.magnolia.ui.dialog.setup.migration.ControlMigratorsRegistry;
import info.magnolia.ui.dialog.setup.migration.DateControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditCodeControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FckEditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FileControlMigrator;
import info.magnolia.ui.dialog.setup.migration.HiddenControlMigrator;
import info.magnolia.ui.dialog.setup.migration.LinkControlMigrator;
import info.magnolia.ui.dialog.setup.migration.MultiSelectControlMigrator;
import info.magnolia.ui.dialog.setup.migration.SelectControlMigrator;
import info.magnolia.ui.dialog.setup.migration.StaticControlMigrator;
import info.magnolia.ui.form.field.definition.CodeFieldDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.factory.CodeFieldFactory;
import info.magnolia.ui.form.field.factory.CompositeFieldFactory;
import info.magnolia.ui.form.field.factory.MultiValueFieldFactory;
import info.magnolia.ui.form.field.factory.SwitchableFieldFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Version handler for the Ui framework module.
 */
public class UiFrameworkModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final class RenameLegacyI18nNodeIfExistingTask extends IsModuleInstalledOrRegistered {
        public RenameLegacyI18nNodeIfExistingTask() {
            super("Rename legacy i18n node", "Renames /server/i18n/authoring as authoringLegacy. Only run if adminInterface legacy module is installed.", "adminInterface", new RenameNodesTask("", "", RepositoryConstants.CONFIG, "/server/i18n", "authoring", "authoringLegacy", NodeTypes.ContentNode.NAME));
        }
    }

    @Inject
    public UiFrameworkModuleVersionHandler(ControlMigratorsRegistry controlMigratorsRegistry) {
        // Register control migration task.
        controlMigratorsRegistry.register("edit", new EditControlMigrator());
        controlMigratorsRegistry.register("fckEdit", new FckEditControlMigrator());
        controlMigratorsRegistry.register("date", new DateControlMigrator());
        controlMigratorsRegistry.register("select", new SelectControlMigrator());
        controlMigratorsRegistry.register("checkbox", new CheckBoxRadioControlMigrator(true));
        controlMigratorsRegistry.register("checkboxSwitch", new CheckBoxSwitchControlMigrator());
        controlMigratorsRegistry.register("radio", new CheckBoxRadioControlMigrator(false));
        controlMigratorsRegistry.register("uuidLink", new LinkControlMigrator());
        controlMigratorsRegistry.register("link", new LinkControlMigrator());
        controlMigratorsRegistry.register("multiselect", new MultiSelectControlMigrator(false));
        controlMigratorsRegistry.register("file", new FileControlMigrator());
        controlMigratorsRegistry.register("static", new StaticControlMigrator());
        controlMigratorsRegistry.register("hidden", new HiddenControlMigrator());
        controlMigratorsRegistry.register("editCode", new EditCodeControlMigrator());

        register(DeltaBuilder.update("5.0.1", "")
                .addTask(new RenameLegacyI18nNodeIfExistingTask())
                .addTask(new RenameNodesTask("Rename 5.0 i18n node", "Renames /server/i18n/authoring50 as authoring.", RepositoryConstants.CONFIG, "/server/i18n", "authoring50", "authoring", NodeTypes.ContentNode.NAME))
                .addTask(new BootstrapSingleModuleResource("Add dialogs to ui-framework", "", "config.modules.ui-framework.dialogs.xml")));

        register(DeltaBuilder.update("5.1", "")
                .addTask(new BootstrapSingleModuleResource("Register WorkbenchFieldDefinition", "", "config.modules.ui-framework.fieldTypes.workbenchField.xml"))
                .addTask(new RemoveNodeTask("Remove MultiLinkField definition mapping", "", RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes/multiLinkField"))
                .addTask(createNewFieldDefinition("code", CodeFieldDefinition.class.getName(), CodeFieldFactory.class.getName()))
                .addTask(createNewFieldDefinition("switchableField", SwitchableFieldDefinition.class.getName(), SwitchableFieldFactory.class.getName()))
                .addTask(createNewFieldDefinition("multiField", MultiValueFieldDefinition.class.getName(), MultiValueFieldFactory.class.getName()))
                .addTask(createNewFieldDefinition("compositeField", CompositeFieldDefinition.class.getName(), CompositeFieldFactory.class.getName()))
                .addTask((new ReplaceMultiLinkFieldDefinitionTask("Change the MultiLinkFieldDefinition by MultiValueFieldDefinition ", "", RepositoryConstants.CONFIG, " select * from [nt:base] as t where contains(t.*,'info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition') ")))
                .addTask((new ReplaceSaveModeTypeFieldDefinitionTask("Update field definition sub task from 'saveModeType' to 'transformerClass' ", "", RepositoryConstants.CONFIG, " select * from [nt:base] as t where name(t) = 'saveModeType' ")))
                .addTask(new ChangeAllPropertiesWithCertainValueTask("Change package name of CallbackDialogActionDefinition class", "", RepositoryConstants.CONFIG, "info.magnolia.ui.admincentral.dialog.action.CallbackDialogActionDefinition", CallbackDialogActionDefinition.class.getName()))
                .addTask((new RemoveHardcodedI18nPropertiesFromDialogsTask("ui-framework"))));

        register(DeltaBuilder.update("5.2.2", "")
                .addTask(new NodeExistsDelegateTask("Register WorkbenchFieldDefinition if not yet done", "", RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes/workbenchField", null, new BootstrapSingleModuleResource("Register WorkbenchFieldDefinition", "", "config.modules.ui-framework.fieldTypes.workbenchField.xml")))
                .addTask(new NodeExistsDelegateTask("Rename fieldType if it's misspelled", "/modules/ui-framework/fieldTypes/compositField",
                        new MoveNodeTask("Rename misspelled fieldType", "/modules/ui-framework/fieldTypes/compositField", "/modules/ui-framework/fieldTypes/compositeField", false)
                        ))
                .addTask(new NodeExistsDelegateTask("Rename command catalog if it's incorrect", "Rename command catalog to 'default' if it's incorrect", RepositoryConstants.CONFIG, "/modules/ui-framework/commands/deafult",
                        new MoveNodeTask("Rename command catalog", "Rename command catalog to 'default'", RepositoryConstants.CONFIG, "/modules/ui-framework/commands/deafult",
                                "/modules/ui-framework/commands/default", false)
                        ))
                .addTask(new NodeExistsDelegateTask("Bootstrap 'importZip' command it doesn't exists yet", "Bootstrap 'importZip' command it doesn't exists yet", RepositoryConstants.CONFIG, "/modules/ui-framework/commands/default/importZip", null, new BootstrapSingleModuleResource("Bootstrap 'importZip' command", "Bootstrap 'importZip' command", "config.modules.ui-framework.commands.xml"
                        )))
                .addTask(new NodeExistsDelegateTask("Bootstrap 'importZip' dialog it doesn't exists yet", "Bootstrap 'importZip' dialog it doesn't exists yet", RepositoryConstants.CONFIG, "/modules/ui-framework/dialog/importZip", null,
                        new PartialBootstrapTask("Bootstrap 'importZip' dialog", "Bootstraps 'importZip' dialog.", "/mgnl-bootstrap/ui-framework/config.modules.ui-framework.dialogs.xml", "dialogs/importZip")))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/encoding/options/utf-8", "label", "UTF-8"))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/encoding/options/windows", "label", "CP437")));

        register(DeltaBuilder.update("5.3", "")
                .addTask(new ArrayDelegateTask("Make dialogs light", "Turns edit node name and edit folder name dialogs into light dialogs.",
                        new NodeExistsDelegateTask("", "", RepositoryConstants.CONFIG, "/modules/ui-framework/dialogs/rename",
                                new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/modules/ui-framework/dialogs/rename", "modalityLevel", "light")),
                        new NodeExistsDelegateTask("", "", RepositoryConstants.CONFIG, "/modules/ui-framework/dialogs/folder",
                                new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/modules/ui-framework/dialogs/folder", "modalityLevel", "light"))
                        )));

        register(DeltaBuilder.update("5.4", "")
                .addTask(new NodeExistsDelegateTask("Update code field type", "", RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes/basicTextCodeField",
                        new ArrayDelegateTask("", "",
                                new RenameNodesTask("", "", RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes", "basicTextCodeField", "code", NodeTypes.ContentNode.NAME),
                                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes/code", "definitionClass", "info.magnolia.ui.form.field.definition.CodeFieldDefinition"),
                                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes/code", "factoryClass", "info.magnolia.ui.form.field.factory.CodeFieldFactory")))));
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        return tasks;
    }

    /**
     * Create a new Field Definition.
     */
    private NodeBuilderTask createNewFieldDefinition(String fieldName, String definitionClass, String factoryClass) {
        return new NodeBuilderTask("Add definition for the newly introduce field: " + fieldName, "", ErrorHandling.logging, RepositoryConstants.CONFIG, "/modules/ui-framework",
                getNode("fieldTypes").then(
                        addNode(fieldName, NodeTypes.ContentNode.NAME),
                        getNode(fieldName).then(
                                addProperty("definitionClass", definitionClass),
                                addProperty("factoryClass", factoryClass)
                                )
                        ));
    }
}
