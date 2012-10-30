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
package info.magnolia.ui.app.contacts.cconf.dialog;

import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;
import info.magnolia.ui.model.field.definition.SelectFieldDefinition;
import info.magnolia.ui.model.field.definition.TextFieldDefinition;
import info.magnolia.ui.model.tab.definition.ConfiguredTabDefinition;

/**
 * Builder for building a tab definition.
 */
public class TabBuilder {

    private ConfiguredTabDefinition definition = new ConfiguredTabDefinition();

    public TabBuilder(ConfiguredTabDefinition definition) {
        this.definition = definition;
    }

    public TabBuilder label(String label) {
        definition.setLabel(label);
        return this;
    }

    public TabBuilder i18nBasename(String i18nBasename) {
        definition.setI18nBasename(i18nBasename);
        return this;
    }

    public ConfiguredTabDefinition exec() {
        return definition;
    }

    public TextFieldBuilder textField(String name) {
        TextFieldDefinition textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition.setName(name);
        definition.addField(textFieldDefinition);
        return new TextFieldBuilder(textFieldDefinition);
    }

    public SelectFieldBuilder selectField(String name) {
        SelectFieldDefinition selectFieldDefinition = new SelectFieldDefinition();
        selectFieldDefinition.setName(name);
        definition.addField(selectFieldDefinition);
        return new SelectFieldBuilder(selectFieldDefinition);
    }

    public FileUploadFieldBuilder fileUploadField(String name) {
        FileUploadFieldDefinition fileUploadFieldDefinition = new FileUploadFieldDefinition();
        fileUploadFieldDefinition.setName(name);
        definition.addField(fileUploadFieldDefinition);
        return new FileUploadFieldBuilder(fileUploadFieldDefinition);
    }
}
