/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.model.workbench.definition;

import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.column.definition.ColumnDefinition;
import info.magnolia.ui.model.thumbnail.ThumbnailProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Default configured implementation for the WorkbenchDefinition.
 */
public class ConfiguredWorkbenchDefinition implements WorkbenchDefinition {

    private String name;

    private String workspace;

    private String path;

    private List<ItemTypeDefinition> itemTypes = new ArrayList<ItemTypeDefinition>();

    private final Map<String, ColumnDefinition> columns = new LinkedHashMap<String, ColumnDefinition>();

    private ActionbarDefinition actionbar;

    private ComponentProviderConfiguration components;
    //Default is always False.
    private boolean dialogWorkbench = false;

    private ThumbnailProvider thumbnailProvider;
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public List<ItemTypeDefinition> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<ItemTypeDefinition> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public boolean addItemType(ItemTypeDefinition itemTypeDefinition) {
        return itemTypes.add(itemTypeDefinition);
    }

    @Override
    public String getItemTypesFilter() {
        String res = " ";
        for (ItemTypeDefinition item : itemTypes) {
            res = res + " " + item.getItemType() + (itemTypes.indexOf(item) < itemTypes.size() - 1 ? " | " : "");
        }
        return res;
    }

    @Override
    public ColumnDefinition getColumn(String columnId) {
        return columns.get(columnId);
    }

    @Override
    public Collection<ColumnDefinition> getColumns() {
        return columns.values();
    }


    public void addColumn(ColumnDefinition treeColumn) {
        columns.put(treeColumn.getLabel(), treeColumn);
    }

    @Override
    public ActionbarDefinition getActionbar() {
        return actionbar;
    }

    public void setActionbar(ActionbarDefinition actionbar) {
        this.actionbar = actionbar;
    }

    @Override
    public ComponentProviderConfiguration getComponents() {
        return components;
    }

    @Override
    public ThumbnailProvider getThumbnailProvider() {
        return thumbnailProvider;
    }

    public void setComponents(ComponentProviderConfiguration components) {
        this.components = components;
    }

    public void setThumbnailProvider(ThumbnailProvider thumbnailProvider) {
        this.thumbnailProvider = thumbnailProvider;
    }


    @Override
    public boolean isDialogWorkbench() {
        return dialogWorkbench;
    }

    public void setDialogWorkbench(boolean dialogWorkbench) {
        this.dialogWorkbench = dialogWorkbench;
    }

}
