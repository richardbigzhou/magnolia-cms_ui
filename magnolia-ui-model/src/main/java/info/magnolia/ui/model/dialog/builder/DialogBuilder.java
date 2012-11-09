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
package info.magnolia.ui.model.dialog.builder;

import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;

/**
 * Builder for building a dialog definition.
 */
public class DialogBuilder {

    private final ConfiguredDialogDefinition definition = new ConfiguredDialogDefinition();

    public DialogBuilder(String id) {
        definition.setId(id);
    }

    public DialogBuilder label(String label) {
        definition.setLabel(label);
        return this;
    }

    public DialogBuilder i18nBasename(String i18nBasename) {
        definition.setI18nBasename(i18nBasename);
        return this;
    }

    public DialogBuilder description(String description) {
        definition.setDescription(description);
        return this;
    }

    public DialogDefinition exec() {
        return definition;
    }

    public DialogBuilder tabs(TabBuilder... builders) {
        for (TabBuilder builder : builders) {
            definition.addTab(builder.exec());
        }
        return this;
    }

    public DialogBuilder actions(DialogActionBuilder... builders) {
        for (DialogActionBuilder builder : builders) {
            definition.addAction(builder.exec());
        }
        return this;
    }
}
