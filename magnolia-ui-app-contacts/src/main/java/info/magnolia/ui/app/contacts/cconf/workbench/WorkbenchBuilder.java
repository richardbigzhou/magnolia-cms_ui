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
package info.magnolia.ui.app.contacts.cconf.workbench;

import info.magnolia.ui.app.contacts.cconf.actionbar.ActionbarBuilder;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.thumbnail.ImageProvider;
import info.magnolia.ui.model.workbench.definition.ConfiguredItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;

/**
 * Builder for building a workbench definition.
 */
public class WorkbenchBuilder {

    private ConfiguredWorkbenchDefinition definition;

    public WorkbenchBuilder() {
        this.definition = new ConfiguredWorkbenchDefinition();
    }

    public WorkbenchBuilder(ConfiguredWorkbenchDefinition definition) {
        this.definition = definition;
    }

    public WorkbenchBuilder workspace(String workspace) {
        definition.setWorkspace(workspace);
        return this;
    }

    public WorkbenchBuilder root(String path) {
        definition.setPath(path);
        return this;
    }

    public WorkbenchBuilder columns(ColumnBuilder... columns) {
        for (ColumnBuilder column : columns) {
            definition.addColumn(column.exec());
        }
        return this;
    }

    public ActionbarBuilder actionbar() {
        ConfiguredActionbarDefinition actionbarDefinition = new ConfiguredActionbarDefinition();
        definition.setActionbar(actionbarDefinition);
        return new ActionbarBuilder(actionbarDefinition);
    }

    public ConfiguredWorkbenchDefinition exec() {
        return definition;
    }

    public ItemTypeBuilder groupingItemType(String itemType) {
        ConfiguredItemTypeDefinition itemTypeDefinition = new ConfiguredItemTypeDefinition();
        itemTypeDefinition.setItemType(itemType);
        definition.setGroupingItemType(itemTypeDefinition);
        return new ItemTypeBuilder(itemTypeDefinition);
    }

    public ItemTypeBuilder mainItemType(String itemType) {
        ConfiguredItemTypeDefinition itemTypeDefinition = new ConfiguredItemTypeDefinition();
        itemTypeDefinition.setItemType(itemType);
        definition.setMainItemType(itemTypeDefinition);
        return new ItemTypeBuilder(itemTypeDefinition);
    }

    public ColumnBuilder column(AbstractColumnDefinition columnDefinition) {
        definition.addColumn(columnDefinition);
        return new ColumnBuilder<AbstractColumnDefinition>(columnDefinition);
    }

    public WorkbenchBuilder defaultOrder(String defaultOrder) {
        definition.setDefaultOrder(defaultOrder);
        return this;
    }

    public WorkbenchBuilder imageProvider(ImageProvider imageProvider) {
        definition.setImageProvider(imageProvider);
        return this;
    }
}
