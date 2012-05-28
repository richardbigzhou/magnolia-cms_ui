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
package info.magnolia.ui.admincentral.dialog.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionProvider;

import java.util.ArrayList;
import java.util.List;



/**
 * DialogProvider that instantiates a dialog from dummy data.
 *
 * @version $Id$
 */
public class DummyDialogDefinitionProvider implements DialogDefinitionProvider {

    private String id;
    private DialogDefinition dialogDefinition;

    public DummyDialogDefinitionProvider(String id) {
        this.id = id;
        this.dialogDefinition = new DialogDefinition();
        this.dialogDefinition.setId(id);
        this.dialogDefinition.setLabel("testlabel");
        this.dialogDefinition.setName("testname");
        List<TabDefinition> tabs = new ArrayList<TabDefinition>();

        TabDefinition tab1 = new TabDefinition();
        tab1.setName("Test Tab Name");
        FieldDefinition field = new FieldDefinition();
        field.setLabel("Test Field");
        tab1.addField(field);

        tabs.add(tab1);

        this.dialogDefinition.setTabs(tabs);
        if (this.dialogDefinition != null) {
            this.dialogDefinition.setId(id);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DialogDefinition getDefinition() throws RegistrationException {
        return dialogDefinition;
    }
}
