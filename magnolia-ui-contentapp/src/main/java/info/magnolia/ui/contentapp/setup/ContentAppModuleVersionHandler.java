/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.ChangeAllPropertiesWithCertainValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.contentapp.movedialog.action.MoveNodeActionDefinition;
import info.magnolia.ui.framework.setup.ReplaceMultiLinkFieldDefinitionTask;
import info.magnolia.ui.framework.setup.ReplaceSaveModeTypeFieldDefinitionTask;

/**
 * Handles versioning for {@link info.magnolia.ui.contentapp.ContentAppModule}.
 */
public class ContentAppModuleVersionHandler extends DefaultModuleVersionHandler {

    private final String subAppsQuery = " select * from [nt:base] as t where name(t) = 'subApps' ";

    public ContentAppModuleVersionHandler() {
        register(DeltaBuilder.update("5.1", "")
                .addTask(new RemoveNodeTask("Remove MultiLinkField definition mapping", "", RepositoryConstants.CONFIG, "/modules/ui-framework/fieldTypes/multiLinkField"))
                .addTask((new ReplaceMultiLinkFieldDefinitionTask("Change the MultiLinkFieldDefinition by MultiFieldDefinition ", "", RepositoryConstants.CONFIG, " select * from [nt:base] as t where contains(t.*,'info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition') ")))
                .addTask((new ReplaceSaveModeTypeFieldDefinitionTask("Update field definition sub task from 'saveModeType' to 'transformerClass' ", "", RepositoryConstants.CONFIG, " select * from [nt:base] as t where name(t) = 'saveModeType' ")))
                .addTask((new ContentAppDescriptorMigrationTask("Update descriptor class properties to ConfiguredContentAppDescriptor for Content Apps ", "", RepositoryConstants.CONFIG,
                        subAppsQuery)))
                .addTask(new ChangeAllPropertiesWithCertainValueTask("Change package name of MoveNodeActionDefinition class", "", RepositoryConstants.CONFIG, "info.magnolia.ui.framework.action.MoveNodeActionDefinition", MoveNodeActionDefinition.class.getName()))
        );

        register(DeltaBuilder.update("5.2.2", "")
                .addTask((new ContentAppDescriptorMigrationTask("Remove 'app' properties", "Removes obsolete 'app' properties from Content Apps.", RepositoryConstants.CONFIG,
                        subAppsQuery, new AppPropertyRemoverVisitor()))));
    }
}
