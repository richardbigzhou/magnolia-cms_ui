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
package info.magnolia.ui.model.workbench.builder;

import info.magnolia.ui.model.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.model.column.builder.ColumnBuilder;
import info.magnolia.ui.model.form.builder.FormBuilder;
import info.magnolia.ui.model.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;

/**
 * Builder for building a workbench definition.
 */
public class WorkbenchBuilder {

    private ConfiguredWorkbenchDefinition definition = new ConfiguredWorkbenchDefinition();

    public WorkbenchBuilder workspace(String workspace) {
        definition.setWorkspace(workspace);
        return this;
    }

    public WorkbenchBuilder root(String path) {
        definition.setPath(path);
        return this;
    }

    public WorkbenchBuilder defaultOrder(String defaultOrder) {
        definition.setDefaultOrder(defaultOrder);
        return this;
    }

    public WorkbenchBuilder columns(ColumnBuilder... columns) {
        for (ColumnBuilder column : columns) {
            definition.addColumn(column.exec());
        }
        return this;
    }

    public ConfiguredWorkbenchDefinition exec() {
        return definition;
    }

    public WorkbenchBuilder actionbar(ActionbarBuilder builder) {
        this.definition.setActionbar(builder.exec());
        return this;
    }

    public WorkbenchBuilder form(FormBuilder builder) {
        this.definition.setFormDefinition(builder.exec());
        return this;
    }

    public WorkbenchBuilder itemType(ItemTypeBuilder itemType) {
        definition.addItemType(itemType.exec());
        return this;
    }

    public WorkbenchBuilder imageProvider(ImageProviderDefinition imageProvider) {
        definition.setImageProvider(imageProvider);
        return this;
    }
}
