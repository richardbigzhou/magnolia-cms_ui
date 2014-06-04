/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.form.field.transformer.item;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Path;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Implementation of a {@link Transformer} that handle a Binary Item instead of a simple property.<br>
 * 
 * @param <T> property type used by the related field.
 */
public class FileTransformer<T extends UploadReceiver> implements Transformer<T> {

    private static final Logger log = LoggerFactory.getLogger(FileTransformer.class);

    protected Item relatedFormItem;
    protected final BasicUploadFieldDefinition definition;
    protected final Class<T> type;
    private Locale locale;
    // item name
    protected String basePropertyName;
    // i18n item name
    protected String i18NPropertyName;

    @Inject
    public FileTransformer(Item relatedFormItem, BasicUploadFieldDefinition definition, Class<T> type) {
        this.definition = definition;
        this.relatedFormItem = relatedFormItem;
        this.type = type;
        this.basePropertyName = getBasePropertyName();
        if (hasI18NSupport()) {
            this.i18NPropertyName = this.basePropertyName;
        }
    }

    /**
     * Base on the validity of the received property, populate or not the property to the related Item.<br>
     * Call {@link FileTransformer#populateItem(Object, Item)} in case {@link FileTransformer#isValid(Object, Item)} return true. <br>
     * Otherwise call {@link FileTransformer#handleInvalide(Object, Item)}.
     */
    @Override
    public void writeToItem(T newValue) {
        Item item = getOrCreateFileItem();
        if (isValid(newValue, item)) {
            populateItem(newValue, item);
        } else {
            handleInvalide(newValue, item);
        }
    }

    /**
     * Get the stored Item, and based of this Item, return {@link FileTransformer#createPropertyFromItem(Item)} .
     */
    @Override
    public T readFromItem() {
        Item item = getOrCreateFileItem();
        return createPropertyFromItem(item);
    }

    /**
     * @return the existing Item otherwise create an empty new Item.
     */
    protected Item getOrCreateFileItem() {
        String itemName = getItemName();
        Item child = ((AbstractJcrNodeAdapter) relatedFormItem).getChild(itemName);
        if (child != null) {
            return child;
        }
        Node node = null;
        try {
            node = ((JcrNodeAdapter) relatedFormItem).getJcrItem();
            if (node.hasNode(itemName) && !(relatedFormItem instanceof JcrNewNodeAdapter)) {
                child = new JcrNodeAdapter(node.getNode(itemName));
            } else {
                child = new JcrNewNodeAdapter(node, NodeTypes.Resource.NAME, itemName);
            }
        } catch (RepositoryException e) {
            log.error("Could get or create a child Item for {} ", NodeUtil.getPathIfPossible(node), e);
        }
        ((AbstractJcrNodeAdapter) relatedFormItem).addChild((AbstractJcrNodeAdapter) child);
        return child;
    }

    /**
     * Based on the i18n information, define the item name to use.
     */
    protected String getItemName() {
        if (hasI18NSupport()) {
            return this.i18NPropertyName;
        }
        return this.basePropertyName;
    }

    /**
     * @return related property initialized based on the Item.
     */
    public T createPropertyFromItem(Item item) {
        T uploadReceiver = initializeUploadReceiver();

        // Set File if the binary Item has data
        if (item.getItemProperty(JcrConstants.JCR_DATA) != null && item.getItemProperty(JcrConstants.JCR_DATA).getValue() != null) {
            String fileName = item.getItemProperty(FileProperties.PROPERTY_FILENAME) != null ? (String) item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue() : "";
            String MIMEType = item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE) != null ? String.valueOf(item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue()) : "";
            OutputStream out = uploadReceiver.receiveUpload(fileName, MIMEType);
            InputStream in;
            try {
                in = ((BinaryImpl) item.getItemProperty(JcrConstants.JCR_DATA).getValue()).getStream();
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return uploadReceiver;
    }

    protected T initializeUploadReceiver() {
        return (T) new UploadReceiver(Path.getTempDirectory(), Components.getComponent(SimpleTranslator.class));
    }

    /**
     * @return true it the 'newValue' property is valid for being populated to the Item {@link FileTransformer#populateItem(Object, Item)}.
     */
    protected boolean isValid(T newValue, Item item) {
        return newValue != null && !newValue.isEmpty();
    }

    /**
     * Populate the related Item with the values of 'newItem' in case {@link FileTransformer#isValid(Object, Item)} return true.<br>
     * 
     * @see {@link FileTransformer#writeToItem(Object)}.
     */
    public Item populateItem(T newValue, Item item) {
        // Populate Data
        Property<Binary> data = getOrCreateProperty(item, JcrConstants.JCR_DATA, Binary.class);
        if (newValue != null) {
            try {
                data.setValue(ValueFactoryImpl.getInstance().createBinary(new FileInputStream(newValue.getFile())));
            } catch (Exception re) {
                log.error("Could not get Binary. Upload will not be performed", re);
                ((AbstractJcrNodeAdapter) item).getParent().removeChild((AbstractJcrNodeAdapter) item);
                return null;
            }
        }
        getOrCreateProperty(item, FileProperties.PROPERTY_FILENAME, String.class).setValue(StringUtils.substringBeforeLast(newValue.getFileName(), "."));
        getOrCreateProperty(item, FileProperties.PROPERTY_CONTENTTYPE, String.class).setValue(newValue.getMimeType());
        getOrCreateProperty(item, FileProperties.PROPERTY_LASTMODIFIED, Date.class).setValue(new Date());
        getOrCreateProperty(item, FileProperties.PROPERTY_SIZE, Long.class).setValue(newValue.getFileSize());
        getOrCreateProperty(item, FileProperties.PROPERTY_EXTENSION, String.class).setValue(newValue.getExtension());
        return item;
    }

    /**
     * Handle the related Item in case {@link FileTransformer#isValid(Object, Item)} return false.<br>
     * 
     * @see {@link FileTransformer#writeToItem(Object)}.
     */
    protected void handleInvalide(T newValue, Item item) {
        ((AbstractJcrNodeAdapter) item).getParent().removeChild((AbstractJcrNodeAdapter) item);
    }

    /**
     * Get the required property from the Item, or create it if it does not exist.
     */
    protected Property getOrCreateProperty(Item item, String propertyName, Class<?> type) {
        if (item.getItemProperty(propertyName) == null) {
            item.addItemProperty(propertyName, new DefaultProperty(type, null));
        }
        return item.getItemProperty(propertyName);
    }

    @Override
    public String getBasePropertyName() {
        return this.definition.getBinaryNodeName();
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }


    /* I18nAwareHandler impl */

    @Override
    public boolean hasI18NSupport() {
        return definition.isI18n();
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setI18NPropertyName(String i18nPropertyName) {
        this.i18NPropertyName = i18nPropertyName;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

}
