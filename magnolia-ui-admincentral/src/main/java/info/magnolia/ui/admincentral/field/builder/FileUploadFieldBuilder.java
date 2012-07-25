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
package info.magnolia.ui.admincentral.field.builder;


import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.ui.admincentral.field.FileUpload;
import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and configures a Vaadin UploadField.
 */
public class FileUploadFieldBuilder extends AbstractFieldBuilder<FileUploadFieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(FileUploadFieldBuilder.class);

    public FileUploadFieldBuilder(FileUploadFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected Field buildField() {
        FileUpload uploadField = new FileUpload((JcrNodeAdapter)getOrCreateItem());
        return uploadField;
    }


    /**
     * Get or Create the imageBinary Item.
     * If this Item doesn't exist yet, initialize all fields (as Property).
     */
    public Item getOrCreateItem() {
        //Get the related Node
        Node node = getRelatedNode(item);
        JcrNodeAdapter child = null;
        try {
            if(node.hasNode(definition.getImageNodeName()) && !(item instanceof JcrNewNodeAdapter)) {
                child = new JcrNodeAdapter(node.getNode(definition.getImageNodeName()));
                ((JcrNodeAdapter)item).addChild(definition.getImageNodeName(), child);
            } else {
                child = new JcrNewNodeAdapter(node, MgnlNodeType.NT_RESOURCE, definition.getImageNodeName());
                ((JcrNodeAdapter)item).addChild(definition.getImageNodeName(), child);
                initImageProperty(child);
            }
        }
        catch (RepositoryException e) {
            log.error("Could get or create item", e);
        }

        return child;
    }

    /**
     * Do not link a DataSource to the Upload Field.
     * Upload Field will handle the creation of the appropriate property.
     */
    @Override
    public void setPropertyDataSource(Property property) {
    }

    /**
     * Init the Item property used to store the Uploaded image.
     */
    private void initImageProperty(JcrNodeAdapter child) {

        child.addItemProperty(MgnlNodeType.JCR_DATA, DefaultPropertyUtil.newDefaultProperty(MgnlNodeType.JCR_DATA, "Binary", null));
        child.addItemProperty(FileProperties.PROPERTY_FILENAME, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_FILENAME, "String", null));
        child.addItemProperty(FileProperties.PROPERTY_CONTENTTYPE, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_CONTENTTYPE, "String", null));
        child.addItemProperty(FileProperties.PROPERTY_LASTMODIFIED, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_LASTMODIFIED, "Date", null));
        child.addItemProperty(FileProperties.PROPERTY_SIZE, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_SIZE, "Long", null));
        child.addItemProperty(FileProperties.PROPERTY_EXTENSION, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_EXTENSION, "String", null));
        child.addItemProperty(FileProperties.PROPERTY_WIDTH, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_WIDTH, "Long", null));
        child.addItemProperty(FileProperties.PROPERTY_HEIGHT, DefaultPropertyUtil.newDefaultProperty(FileProperties.PROPERTY_HEIGHT, "Long", null));
    }

}