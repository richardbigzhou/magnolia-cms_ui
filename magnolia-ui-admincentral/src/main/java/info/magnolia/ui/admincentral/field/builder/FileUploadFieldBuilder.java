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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.field.upload.UploadFileFieldImpl;
import info.magnolia.ui.admincentral.file.FileItemWrapper;
import info.magnolia.ui.admincentral.file.FileItemWrapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.FileUploadFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Creates and configures a Vaadin UploadField.
 */
public class FileUploadFieldBuilder extends AbstractFieldBuilder<FileUploadFieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(FileUploadFieldBuilder.class);

    private final MagnoliaShell magnoliaShell;

    @Inject
    public FileUploadFieldBuilder(FileUploadFieldDefinition definition, Item relatedFieldItem, Shell shell) {
        super(definition, relatedFieldItem);
        this.magnoliaShell = (MagnoliaShell) shell;
    }

    @Override
    protected Field buildField() {
        // Temp Solution as long as we don't support DMS
        DefaultProperty property = (DefaultProperty) item.getItemProperty("image");
        if (property == null) {
            property = DefaultPropertyUtil.newDefaultProperty("image", null, "upload");
            item.addItemProperty("image", property);
        } else {
            property.setValue("upload");
        }

        // Get or create the File Node adapter.
        JcrItemNodeAdapter binaryDataSubNodeItem = getOrCreateSubItemWithBinaryData();
        // Create the File Wrapper.
        FileItemWrapper fileItem = new FileItemWrapperImpl(binaryDataSubNodeItem);
        // Create Upload Filed.
        UploadFileFieldImpl uploadField = new UploadFileFieldImpl(fileItem, magnoliaShell);
        uploadField.setInfo(true);
        uploadField.setProgressInfo(true);
        uploadField.setFileDeletion(true);
        uploadField.setPreview(definition.isPreview());
        uploadField.setMaxUploadSize(definition.getMaxUploadSize());
        uploadField.setMimeTypeRegExp(definition.getAllowedMimeType());
        return uploadField;
    }

    /**
     * Get or Create the imageBinary Item. If this Item doesn't exist yet,
     * initialize all fields (as Property).
     */
    public JcrItemNodeAdapter getOrCreateSubItemWithBinaryData() {
        // Get the related Node
        Node node = getRelatedNode(item);
        JcrItemNodeAdapter child = null;
        try {
            if (node.hasNode(definition.getImageNodeName()) && !(item instanceof JcrNewNodeAdapter)) {
                child = new JcrNodeAdapter(node.getNode(definition.getImageNodeName()));
                child.setParent((JcrItemNodeAdapter) item);
            } else {
                child = new JcrNewNodeAdapter(node, MgnlNodeType.NT_RESOURCE, definition.getImageNodeName());
                child.setParent((JcrItemNodeAdapter) item);
            }
        } catch (RepositoryException e) {
            log.error("Could get or create item", e);
        }
        return child;
    }

    /**
     * Do not link a DataSource to the Upload Field. Upload Field will handle
     * the creation of the appropriate property.
     */
    @Override
    public void setPropertyDataSource(Property property) {
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return Binary.class;
    }

}