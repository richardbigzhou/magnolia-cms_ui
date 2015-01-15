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
package info.magnolia.ui.contentapp.definition;

import info.magnolia.ui.dialog.actionarea.definition.ConfiguredEditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.definition.FormActionItemDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;

import java.util.List;

/**
 * Simple implementation of {@link EditorDefinition}.
 *
 * @see EditorDefinition
 */
public class ConfiguredEditorDefinition implements EditorDefinition {

    private FormDefinition form;

    private String label;

    private String i18nBasename;

    private String description;

    private String workspace;

    private NodeTypeDefinition nodeType;

    private EditorActionAreaDefinition actionArea = new ConfiguredEditorActionAreaDefinition();

    private List<FormActionItemDefinition> actions;

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public NodeTypeDefinition getNodeType() {
        return nodeType;
    }

    @Override
    public FormDefinition getForm() {
        return form;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void setForm(FormDefinition form) {
        this.form = form;
    }

    public void setNodeType(NodeTypeDefinition nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public EditorActionAreaDefinition getActionArea() {
        return actionArea;
    }

    public void setActionArea(EditorActionAreaDefinition editorActionAreaDefinition) {
        this.actionArea = editorActionAreaDefinition;
    }

    @Override
    public List<FormActionItemDefinition> getActions() {
        return actions;
    }

    public void setActions(List<FormActionItemDefinition> actions) {
        this.actions = actions;
    }
}
