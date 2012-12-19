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
package info.magnolia.ui.model.dialog.definition;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.form.definition.FormDefinition;

/**
 * A definition of a configured dialog.
 */
public class ConfiguredDialogDefinition implements DialogDefinition {

    public static final String ACTIONS_NODE_NAME = "actions";
    public static final String EXTEND_PROPERTY_NAME = "extends";
    public static final String FORM_NODE_NAME = "formDefinition";

    private String id;

    private String label;

    private String i18nBasename;

    private String description;

    private FormDefinition formDefinition;

    private List<DialogActionDefinition> actions = new ArrayList<DialogActionDefinition>();

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
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFormDefinition(FormDefinition formDefinition) {
        this.formDefinition = formDefinition;
    }

    @Override
    public FormDefinition getFormDefinition() {
        return formDefinition;
    }

    @Override
    public List<DialogActionDefinition> getActions() {
        return actions;
    }

    public void setActions(List<DialogActionDefinition> actions) {
        this.actions = actions;
    }

    public boolean addAction(DialogActionDefinition actionDefinition) {
        return this.actions.add(actionDefinition);
    }

}
