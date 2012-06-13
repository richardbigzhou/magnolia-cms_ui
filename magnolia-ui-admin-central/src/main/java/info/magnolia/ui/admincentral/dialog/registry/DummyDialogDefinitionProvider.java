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
        dialogDefinition.setId(id);
        dialogDefinition.setName("Contact");
        dialogDefinition.setLabel("Contact");


        List<TabDefinition> tabs = new ArrayList<TabDefinition>();

        TabDefinition personal = new TabDefinition();
        personal.setName("personalTab");
        personal.setLabel("Personal");

        FieldDefinition name = new FieldDefinition();
        name.setName("statustitle");
        name.setLabel("data.dialog.contact.status.label");
        name.setType("edit");
        personal.addField(name);

        FieldDefinition title = new FieldDefinition();
        title.setName("status");
        title.setLabel("data.dialog.contact.title.label");
        title.setType("edit");
        personal.addField(title);

        FieldDefinition givenName = new FieldDefinition();
        givenName.setName("givenName");
        givenName.setLabel("data.dialog.contact.givenName.label");
        givenName.setType("edit");
        personal.addField(givenName);

        FieldDefinition familyName = new FieldDefinition();
        familyName.setName("familyName");
        familyName.setLabel("data.dialog.contact.familyName.label");
        familyName.setType("edit");
        personal.addField(familyName);

        tabs.add(personal);


        TabDefinition company = new TabDefinition();
        company.setName("companyTab");
        company.setLabel("company");

        FieldDefinition organizationName = new FieldDefinition();
        organizationName.setName("organizationName");
        organizationName.setLabel("data.dialog.contact.org.name.label");
        organizationName.setType("edit");
        company.addField(organizationName);

        FieldDefinition organizationUnit = new FieldDefinition();
        organizationUnit.setName("organizationUnit");
        organizationUnit.setLabel("data.dialog.contact.org.unit.label");
        organizationUnit.setType("edit");
        company.addField(organizationUnit);

        FieldDefinition streetAddress = new FieldDefinition();
        streetAddress.setName("streetAddress");
        streetAddress.setLabel("data.dialog.contact.org.street.label");
        streetAddress.setType("edit");
        company.addField(streetAddress);

        FieldDefinition city = new FieldDefinition();
        city.setName("city");
        city.setLabel("data.dialog.contact.city.label");
        city.setType("edit");
        company.addField(city);

        FieldDefinition zip = new FieldDefinition();
        zip.setName("zip");
        zip.setLabel("data.dialog.contact.zip.label");
        zip.setType("edit");
        company.addField(zip);

        FieldDefinition country = new FieldDefinition();
        country.setName("country");
        country.setLabel("data.dialog.contact.country.label");
        country.setType("edit");
        company.addField(country);

        tabs.add(company);

        this.dialogDefinition.setTabs(tabs);

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
