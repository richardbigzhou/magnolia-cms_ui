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
import info.magnolia.module.InstallContext;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueChildNodeTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueJSONTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodeTransformer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Migrate an MultiSelect control to a MultiLinkFieldDefinition.
 */
public class MultiSelectControlMigrator implements ControlMigrator {

    private final boolean useIdentifier;

    public MultiSelectControlMigrator(boolean useIdentifier) {
        this.useIdentifier = useIdentifier;
    }

    @Override
    public void migrate(Node controlNode, InstallContext installContext) throws RepositoryException {
        controlNode.getProperty("controlType").remove();
        controlNode.setProperty("class", MultiValueFieldDefinition.class.getName());

        // Set transformerClass
        setTransformerClass(controlNode);
        // Create a Field sub node
        Node field = controlNode.addNode("field", NodeTypes.ContentNode.NAME);
        // Set converter
        if (useIdentifier) {
            field.addNode("identifierToPathConverter", NodeTypes.ContentNode.NAME).setProperty("class", BaseIdentifierToPathConverter.class.getName());
        }
        field.setProperty("class", LinkFieldDefinition.class.getName());
        // Set Related Select (App)
        if (controlNode.hasProperty("tree")) {
            setSeletionApp(controlNode, field);
        }
        controlNode.setProperty("buttonSelectAddLabel", "field.link.select.add");
        field.setProperty("buttonSelectNewLabel", "field.link.select.new");
        field.setProperty("buttonSelectOtherLabel", "field.link.select.another");

        if (controlNode.hasProperty("saveHandler")) {
            controlNode.getProperty("saveHandler").remove();
        }
        // set type if define
        if (controlNode.hasProperty("type")) {
            field.setProperty("type", controlNode.getProperty("type").getString());
            controlNode.getProperty("type").remove();
        }
    }

    /**
     * Set Selection App.
     */
    protected void setSeletionApp(Node controlNode, Node fieldNode) throws RepositoryException {
        String workspace = controlNode.getProperty("tree").getString();
        fieldNode.setProperty("targetWorkspace", workspace);
        controlNode.getProperty("tree").remove();
        if (workspace.equals("category")) {
            fieldNode.setProperty("appName", "categories");
        } else if (workspace.equals("website")) {
            fieldNode.setProperty("appName", "pages");
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
            } else if (saveMode.equals("multiple")) {
                controlNode.setProperty("transformerClass", MultiValueChildNodeTransformer.class.getName());
            }
            controlNode.getProperty("saveMode").remove();
        } else {
            controlNode.setProperty("transformerClass", MultiValueSubChildrenNodeTransformer.class.getName());
        }
    }

}
