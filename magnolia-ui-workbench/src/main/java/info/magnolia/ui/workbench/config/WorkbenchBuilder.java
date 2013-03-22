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
package info.magnolia.ui.workbench.config;

import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

/**
 * Builder for creating a workbench definition.
 *
 * @see info.magnolia.ui.workbench.definition.WorkbenchDefinition
 * @see WorkbenchConfig
 */
public class WorkbenchBuilder {

    private ConfiguredWorkbenchDefinition definition = new ConfiguredWorkbenchDefinition();

    public WorkbenchBuilder name(String name) {
        definition.setName(name);
        return this;
    }

    public WorkbenchBuilder workspace(String workspace) {
        definition.setWorkspace(workspace);
        return this;
    }

    public WorkbenchBuilder path(String path) {
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

    public WorkbenchBuilder nodeTypes(NodeTypeBuilder... nodeTypes) {
        for (NodeTypeBuilder nodeType : nodeTypes) {
            definition.addNodeType(nodeType.exec());
        }
        return this;
    }

    public WorkbenchBuilder includeProperties(boolean includeProperties) {
        definition.setIncludeProperties(includeProperties);
        return this;
    }

    public WorkbenchBuilder editable(boolean editable) {
        definition.setEditable(editable);
        return this;
    }

    public WorkbenchBuilder dialogWorkbench(boolean isDialogWorkbench) {
        definition.setDialogWorkbench(isDialogWorkbench);
        return this;
    }

    public ConfiguredWorkbenchDefinition exec() {
        return definition;
    }

    public WorkbenchBuilder dropConstraintClass(Class<? extends DropConstraint> dropConstraintClass) {
        definition.setDropConstraintClass(dropConstraintClass);
        return this;
    }
}
