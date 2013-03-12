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
package info.magnolia.ui.workbench.definition;

import info.magnolia.ui.workbench.column.definition.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Default configured implementation of {@link WorkbenchDefinition}.
 */
public class ConfiguredWorkbenchDefinition implements WorkbenchDefinition {

    private String name;

    private String workspace;

    private String path;

    private String defaultOrder;

    private List<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();

    private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

    private boolean dialogWorkbench = false;

    private boolean includeProperties = false;

    private boolean editable;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<NodeTypeDefinition> getNodeTypes() {
        return nodeTypes;
    }

    @Override
    public boolean includeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
    }

    public void addNodeType(NodeTypeDefinition nodeTypeDefinition) {
        nodeTypes.add(nodeTypeDefinition);
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
    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void addColumn(ColumnDefinition columnDefinition) {
        columns.add(columnDefinition);
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    @Override
    public boolean isDialogWorkbench() {
        return dialogWorkbench;
    }

    public void setDialogWorkbench(boolean dialogWorkbench) {
        this.dialogWorkbench = dialogWorkbench;
    }

    @Override
    public String getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(String defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
