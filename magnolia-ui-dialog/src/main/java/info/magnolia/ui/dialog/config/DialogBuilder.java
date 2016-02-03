/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.dialog.config;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.config.ActionBuilder;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.form.config.FormBuilder;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;

/**
 * Builder for building a dialog definition.
 */
public class DialogBuilder {

    private final ConfiguredFormDialogDefinition definition = new ConfiguredFormDialogDefinition();

    public DialogBuilder(String id) {
        definition().setId(id);
    }

    public ConfiguredFormDialogDefinition definition() {
        return definition;
    }

    public DialogBuilder label(String label) {
        definition().setLabel(label);
        return this;
    }

    public DialogBuilder i18nBasename(String i18nBasename) {
        definition().setI18nBasename(i18nBasename);
        return this;
    }

    public DialogBuilder description(String description) {
        definition().setDescription(description);
        return this;
    }

    public DialogBuilder form(FormBuilder builder) {
        this.definition().setForm(builder.definition());
        return this;
    }

    public FormBuilder form() {
        if (this.definition().getForm() == null) {
            this.definition().setForm(new ConfiguredFormDefinition());
        }
        return new FormBuilder((ConfiguredFormDefinition) definition().getForm());
    }

    public DialogBuilder actions(ActionBuilder... builders) {
        for (ActionBuilder builder : builders) {
            definition().addAction(builder.definition());
        }
        return this;
    }

    public DialogBuilder addAction(ActionDefinition actionDefinition) {
        definition().addAction(actionDefinition);
        return this;
    }
}
