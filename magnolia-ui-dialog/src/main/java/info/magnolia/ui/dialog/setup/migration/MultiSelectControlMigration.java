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
package info.magnolia.ui.dialog.setup.migration;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;
import info.magnolia.ui.form.field.property.MultiValuesHandler;
import info.magnolia.ui.form.field.property.SingleValueHandler;
import info.magnolia.ui.form.field.property.SubNodesValueHandler;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Migrate an MultiSelect control to a MultiLinkFieldDefinition.
 */
public class MultiSelectControlMigration implements ControlMigration {

    private boolean useIdentifier;

    public MultiSelectControlMigration(boolean useIdentifier) {
        this.useIdentifier = useIdentifier;
    }

    @Override
    public void migrate(Node controlNode) throws RepositoryException {
        controlNode.getProperty("controlType").remove();
        controlNode.setProperty("class", MultiLinkFieldDefinition.class.getName());
        if (controlNode.hasProperty("saveHandler")) {
            String saveHandler = controlNode.getProperty("saveHandler").getString();
            Node saveModeType = controlNode.addNode("saveModeType", NodeTypes.ContentNode.NAME);
            if (saveHandler.equals("list")) {
                saveModeType.setProperty("multiValueHandlerClass", SingleValueHandler.class.getName());
            } else if (saveHandler.equals("multiple")) {
                saveModeType.setProperty("multiValueHandlerClass", SubNodesValueHandler.class.getName());
            } else {
                saveModeType.setProperty("multiValueHandlerClass", MultiValuesHandler.class.getName());
            }
            controlNode.getProperty("saveHandler").remove();
        } else {
            Node saveModeType = controlNode.addNode("saveModeType", NodeTypes.ContentNode.NAME);
            saveModeType.setProperty("multiValueHandlerClass", MultiValuesHandler.class.getName());
        }
        if (controlNode.hasProperty("tree")) {
            String workspace = controlNode.getProperty("tree").getString();
            controlNode.setProperty("workspace", workspace);
            controlNode.getProperty("tree").remove();
            if (workspace.equals("category")) {
                controlNode.setProperty("dialogName", "pages:link");
                controlNode.setProperty("appName", "categories");
            }
        }
        controlNode.setProperty("identifier", this.useIdentifier);
        controlNode.setProperty("buttonSelectAddLabel", "field.link.select.add");
        controlNode.setProperty("buttonSelectNewLabel", "field.link.select.new");
        controlNode.setProperty("buttonSelectOtherLabel", "field.link.select.another");

    }

}
