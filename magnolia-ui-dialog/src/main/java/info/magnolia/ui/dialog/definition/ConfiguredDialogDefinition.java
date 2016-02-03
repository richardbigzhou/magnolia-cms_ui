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
package info.magnolia.ui.dialog.definition;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.dialog.DialogPresenter;
import info.magnolia.ui.dialog.actionarea.definition.ConfiguredEditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link DialogDefinition}.
 */
public class ConfiguredDialogDefinition implements DialogDefinition {

    public static final String ACTIONS_NODE_NAME = "actions";
    public static final String EXTEND_PROPERTY_NAME = "extends";

    private String id;

    private String label;

    private String i18nBasename;

    private Map<String, ActionDefinition> actions = new LinkedHashMap<String, ActionDefinition>();

    private Class<? extends DialogPresenter> presenterClass;

    private EditorActionAreaDefinition actionArea = new ConfiguredEditorActionAreaDefinition();

    public ConfiguredDialogDefinition() {}

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

    @Override
    public Map<String, ActionDefinition> getActions() {
        return actions;
    }

    @Override
    public Class<? extends DialogPresenter> getPresenterClass() {
        return presenterClass;
    }

    public void setActions(Map<String, ActionDefinition> actions) {
        this.actions = actions;
    }

    public void addAction(ActionDefinition actionDefinition) {
        actions.put(actionDefinition.getName(), actionDefinition);
    }

    public void setPresenterClass(Class<? extends DialogPresenter> presenterClass) {
        this.presenterClass = presenterClass;
    }

    @Override
    public EditorActionAreaDefinition getActionArea() {
        return actionArea;
    }

    public void setActionArea(EditorActionAreaDefinition actionArea) {
        this.actionArea = actionArea;
    }
}
