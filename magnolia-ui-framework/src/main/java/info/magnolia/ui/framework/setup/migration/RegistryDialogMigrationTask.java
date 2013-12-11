/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.framework.setup.migration;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ClassFactory;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.dialog.setup.DialogMigrationTask;
import info.magnolia.ui.dialog.setup.migration.ActionCreator;
import info.magnolia.ui.dialog.setup.migration.ControlMigrator;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This dialog migration task uses module definition to collect all {@link ControlMigrators}.<br>
 * These {@link ControlMigrators} defined all individual {@link ControlMigrator} to use. <br>
 * The main advantage of this migration task is to be able to register {@link ControlMigrator} defined in {@link ControlMigrators} and those <br>
 * for <b>all modules</b> defined in the Magnolia instance.<br>
 * <br>
 * Register a {@link ControlMigrators}<br>
 * In your module: <br>
 * - Add a property to the module definition xml file. This property will point to the local implementation of {@ControlMigrators} <br>
 * 
 * <pre>
 * {@code
 * 'properties'
 * 'property'
 *     'name'info.magnolia.ui.framework.setup.migration.ControlMigrators'/name'
 *     'value'info.magnolia.ui.framework.setup.migration.BasicUIControlMigrators'/value'
 *   '/property'
 * '/properties'
 * }
 * </pre>
 * 
 * - Create a local implementation of {@ControlMigrators} <br>
 * 
 * <pre>
 * {@code
 *  public class BasicUIControlMigrators implements ControlMigrators {
 *   @Override
 *   public void register(Map<String, ControlMigrator> map, InstallContext installContext) {
 *       map.put("edit", new EditControlMigrator());
 *       ...
 * }
 * </pre>
 * 
 * <br>
 * Doing so, your {@link ControlMigrator} are visible for all modules that uses this DialogMigrationRegisteryTask.
 */
public class RegistryDialogMigrationTask extends DialogMigrationTask {

    private final ModuleRegistry moduleRegistry;
    /**
     * @param moduleName
     */
    public RegistryDialogMigrationTask(String moduleName) {
        this(moduleName, null);
    }

    public RegistryDialogMigrationTask(String moduleName, HashMap<String, List<ActionCreator>> customDialogActionsToMigrate) {
        super("Dialog Migration for 5.x", "Migrate dialog for the following module: " + moduleName, moduleName, null, customDialogActionsToMigrate);
        moduleRegistry = Components.getComponent(ModuleRegistry.class);
    }


    @Override
    protected void addCustomControlsToMigrate(final HashMap<String, ControlMigrator> controlsToMigrate) {
        try {
            final ClassFactory classFactory = Classes.getClassFactory();

            for (ModuleDefinition moduleDefinition : moduleRegistry.getModuleDefinitions()) {
                // for each module, we get this property
                String migratorsClassName = moduleDefinition.getProperty(ControlMigrators.class.getName());
                if (StringUtils.isNotBlank(migratorsClassName)) {
                    // which should be an implementation of ControlMigrators
                    final Class<ControlMigrators> migratorsClass = classFactory.forName(migratorsClassName);
                    final ControlMigrators m = classFactory.newInstance(migratorsClass);
                    // then we call it and collect
                    m.register(controlsToMigrate, installContext);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
