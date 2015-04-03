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
package info.magnolia.ui.dialog.setup.migration;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Migrate an link or uuidLink control to a HiddenField.
 */
public class LinkControlMigrator implements ControlMigrator {

    @Override
    public void migrate(Node controlNode, InstallContext installContext) throws RepositoryException {
        Property controlType = controlNode.getProperty("controlType");
        String appName = "pages";
        String className = LinkFieldDefinition.class.getName();
        String targetWorkspace = "website";

        // Set IdentifierConvertor
        if (controlType.getString().equals("uuidLink")) {
            controlNode.addNode("identifierToPathConverter", NodeTypes.ContentNode.NAME);
            controlNode.getNode("identifierToPathConverter").setProperty("class", BaseIdentifierToPathConverter.class.getName());
        }

        if (controlNode.hasProperty("repository")) {
            if (controlNode.getProperty("repository").getString().equals("data")) {
                // Handle contacts
                if (controlNode.hasProperty("tree") && controlNode.getProperty("tree").getString().equals("Contact")) {
                    appName = "contacts";
                    targetWorkspace = "contacts";
                    controlNode.addNode("contentPreviewDefinition", NodeTypes.ContentNode.NAME);
                    controlNode.getNode("contentPreviewDefinition").setProperty("contentPreviewClass", "info.magnolia.contacts.app.field.component.ContactPreviewComponent");
                    controlNode.getProperty("repository").remove();
                }

            } else if (controlNode.getProperty("repository").getString().equals("website")) {
                controlNode.getProperty("repository").remove();
            }
        }

        controlNode.setProperty("targetWorkspace", targetWorkspace);
        controlNode.setProperty("appName", appName);
        controlNode.setProperty("class", className);

        controlType.remove();

    }

}
