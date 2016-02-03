/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.contentapp.setup.for5_3;

import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.ui.api.action.ActionDefinition;

/**
 * Invokes JCR-liberated content app migration tasks. The list of such tasks includes:
 * <ul>
 *     <li>{@link MigrateAvailabilityRulesTask} - migrates availability rules from ui-api</li>
 *     <li>{@link MigrateJcrPropertiesToContentConnectorTask} - moves respective properties from workbench definition to content connector</li>
 *     <li>{@link MoveActionNodeTypeRestrictionToAvailabilityTask} - fixes AbstractItemActionDefinition and its descendants configuration</li>
 *     <li>{@link ChangeAvailabilityRuleClassesTask} - updates availability definition structure</li>
 * </ul>
 */
public class ContentAppMigrationTask extends ArrayDelegateTask {

    public ContentAppMigrationTask(String path) {
        this(path, new Class[]{});
    }

    /**
     * Constructs {@link ContentAppMigrationTask}.
     * 
     * @param path path to the module/app to be updated.
     * @param additionalActionsToMigrate if an app to be updated utilizes some descendants of {@link ActionDefinition} they should be specified so that their configuration gets also updated.
     */
    public ContentAppMigrationTask(String path, Class<? extends ActionDefinition>... additionalActionsToMigrate) {
        super("Execute JCR-liberated content app migration tasks",
                "Migrate availability rules from ui-api, move respective properties from workbench to content connector, update availability definition structure, fix AbstractItemActionDefinition and its descendants configuration.");

        addTask(new ChangeAvailabilityRuleClassesTask(path));
        addTask(new MigrateAvailabilityRulesTask(path));
        addTask(new MigrateJcrPropertiesToContentConnectorTask(path));
        addTask(new MoveActionNodeTypeRestrictionToAvailabilityTask(path, additionalActionsToMigrate));
    }
}
