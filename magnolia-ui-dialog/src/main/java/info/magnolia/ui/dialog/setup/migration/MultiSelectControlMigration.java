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
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueJSONTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodeTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueTransformer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Migrate an MultiSelect control to a MultiLinkFieldDefinition.
 */
public class MultiSelectControlMigration implements ControlMigration {

    protected boolean useIdentifier;

    public MultiSelectControlMigration(boolean useIdentifier) {
        this.useIdentifier = useIdentifier;
    }

    @Override
    public void migrate(Node controlNode) throws RepositoryException {
        controlNode.getProperty("controlType").remove();
        controlNode.setProperty("class", MultiValueFieldDefinition.class.getName());
        // Set transformerClass
        setTransformerClass(controlNode);
        // Create a Field sub node
        Node field = controlNode.addNode("field", NodeTypes.ContentNode.NAME);
        field.setProperty("class", LinkFieldDefinition.class.getName());
        // Set Related Select (App)
        if (controlNode.hasProperty("tree")) {
            setSeletionApp(controlNode, field);
        }
        field.setProperty("identifier", this.useIdentifier);
        controlNode.setProperty("buttonSelectAddLabel", "field.link.select.add");
        field.setProperty("buttonSelectNewLabel", "field.link.select.new");
        field.setProperty("buttonSelectOtherLabel", "field.link.select.another");

    }

    /**
     * Set Selection App.
     */
    protected void setSeletionApp(Node controlNode, Node fieldNode) throws RepositoryException {
        String workspace = controlNode.getProperty("tree").getString();
        fieldNode.setProperty("workspace", workspace);
        controlNode.getProperty("tree").remove();
        if (workspace.equals("category")) {
            fieldNode.setProperty("dialogName", "pages:link");
            fieldNode.setProperty("appName", "categories");
        }
    }

    /**
     * Set the transformerClass.
     */
    protected void setTransformerClass(Node controlNode) throws RepositoryException {
        if (controlNode.hasProperty("saveMode")) {
            String saveMode = controlNode.getProperty("saveMode").getString();
            if (saveMode.equals("list")) {
                controlNode.setProperty("transformerClass", MultiValueJSONTransformer.class.getName());
            } else {
                controlNode.setProperty("transformerClass", MultiValueTransformer.class.getName());
            }
            controlNode.getProperty("saveMode").remove();
        } else {
            controlNode.setProperty("transformerClass", MultiValueSubChildrenNodeTransformer.class.getName());
        }
    }

}
