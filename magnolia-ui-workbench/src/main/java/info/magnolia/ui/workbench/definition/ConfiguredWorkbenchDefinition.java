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
package info.magnolia.ui.workbench.definition;

import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Default configured implementation of {@link WorkbenchDefinition}.
 */
public class ConfiguredWorkbenchDefinition implements WorkbenchDefinition {

    private String name;

    private String workspace;

    private String path = "/";

    private String defaultOrder;

    private List<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();

    private boolean dialogWorkbench = false;

    private boolean includeProperties = false;

    private boolean editable;

    private Class<? extends DropConstraint> dropConstraintClass;

    private List<ContentPresenterDefinition> contentViews = new LinkedList<ContentPresenterDefinition>();
    private boolean includeSystemNodes;

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

    public void setNodeTypes(List<NodeTypeDefinition> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void addNodeType(NodeTypeDefinition nodeTypeDefinition) {
        nodeTypes.add(nodeTypeDefinition);
    }

    @Override
    public boolean isIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
    }

    @Override
    public boolean isIncludeSystemNodes() {
        return includeSystemNodes;
    }

    public void setIncludeSystemNodes(boolean includeSystemNodes) {
        this.includeSystemNodes = includeSystemNodes;
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

    @Override
    public Class<? extends DropConstraint> getDropConstraintClass() {
        return this.dropConstraintClass;
    }

    public void setDropConstraintClass(Class<? extends DropConstraint> dropConstraintClass) {
        this.dropConstraintClass = dropConstraintClass;
    }

    @Override
    public List<ContentPresenterDefinition> getContentViews() {
        if (this.contentViews.isEmpty()) {
            ContentPresenterDefinition defaultContentView = new TreePresenterDefinition();
            addContentView(defaultContentView);
        }
        return this.contentViews;
    }

    public void setContentViews(List<ContentPresenterDefinition> contentViews) {
        this.contentViews = contentViews;
    }

    public void addContentView(ContentPresenterDefinition contentView) {
        this.contentViews.add(contentView);
    }
}
