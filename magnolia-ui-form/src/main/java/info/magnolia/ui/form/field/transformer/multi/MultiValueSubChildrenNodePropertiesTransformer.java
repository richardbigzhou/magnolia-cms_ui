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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Sub Nodes implementation of {@link info.magnolia.ui.form.field.transformer.Transformer} storing and retrieving properties (as {@link PropertysetItem}) displayed in MultiField.<br>
 * <b> In opposition to {@link MultiValueChildrenNodeTransformer} this implementation handle multiple properties stored under a child node.</b> <br>
 * These multiple properties are put/retrieve into/from a {@link PropertysetItem}.<br>
 * Storage strategy: <br>
 * - root node (relatedFormItem)<br>
 * -- main child node (nodeName = field name) <br>
 * --- child node 1 (used to store the first values set of the MultiField as a property)<br>
 * ---- property 1 (store the first property of the first value of the MultiField)<br>
 * ---- property 2 (store the second property of the first value of the MultiField)<br>
 * ---- property 3 (store the third property of the first value of the MultiField)<br>
 * ---- ...
 * --- child node 2 (used to store the second values of the MultiField as a property)<br>
 * ---- property 1 (store the first property of the second value of the MultiField)<br>
 * ---- property 2 (store the second property of the second value of the MultiField)<br>
 * ---- property 3 (store the third property of the second value of the MultiField)<br>
 * ...<br>
 * This implementation store/retrieve the {@link PropertysetItem} properties under the child node.<br>
 * Used in the case of a {@link info.magnolia.ui.form.field.MultiField} contains a {@link info.magnolia.ui.form.field.CompositeField} or a {@link info.magnolia.ui.form.field.SwitchableField}.<br>
 * In this case, {@link info.magnolia.ui.form.field.CompositeField} or {@link info.magnolia.ui.form.field.SwitchableField} will have to declare a {@link info.magnolia.ui.form.field.transformer.composite.NoOpCompositeTransformer}.
 */
public class MultiValueSubChildrenNodePropertiesTransformer extends MultiValueChildrenNodeTransformer {

    private static final Logger log = LoggerFactory.getLogger(MultiValueSubChildrenNodeTransformer.class);

    public MultiValueSubChildrenNodePropertiesTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        super(relatedFormItem, definition, type);
    }

    @Override
    protected JcrNodeAdapter getRootItem() {
        JcrNodeAdapter res = null;
        try {
            res = getOrCreateChildNode(definition.getName(), NodeTypes.Content.NAME);
        } catch (RepositoryException re) {
            log.warn("Not able to retrieve or create a sub node for the parent node {}", ((JcrNodeAdapter) relatedFormItem).getItemId());
        }
        return res;
    }

    @Override
    protected void handleRootitemAndParent(JcrNodeAdapter rootItem) {
        // Attach the child item to the root item
        if (rootItem.getChildren() != null && !rootItem.getChildren().isEmpty()) {
            ((JcrNodeAdapter) relatedFormItem).addChild(rootItem);
        } else {
            ((JcrNodeAdapter) relatedFormItem).removeChild(rootItem);
        }
    }

    @Override
    protected PropertysetItem getValueFromChildNode(Node child) {
        PropertysetItem newValues = new PropertysetItem();
        try {

            PropertyIterator iterator = new JCRMgnlPropertiesFilteringNodeWrapper(child).getProperties();
            while (iterator.hasNext()) {
                Property jcrPorperty = iterator.nextProperty();
                Object propertyObject = PropertyUtil.getPropertyValueObject(child, jcrPorperty.getName());
                DefaultProperty newProperty = new DefaultProperty(propertyObject);

                newValues.addItemProperty(jcrPorperty.getName(), newProperty);
            }
        } catch (RepositoryException re) {
            log.warn("Not able to read property from the following child node {}", NodeUtil.getName(child), re.getLocalizedMessage());
        }
        return newValues;
    }

    @Override
    protected void setChildItemValue(JcrNodeAdapter childItem, Object newValues) {

        Iterator<?> propertyNames = ((PropertysetItem) newValues).getItemPropertyIds().iterator();
        while (propertyNames.hasNext()) {
            String propertyName = (String) propertyNames.next();
            com.vaadin.data.Property<Object> storedProperty = childItem.getItemProperty(propertyName);

            if (storedProperty != null) {
                storedProperty.setValue(((PropertysetItem) newValues).getItemProperty(propertyName).getValue());
            } else {
                storedProperty = ((PropertysetItem) newValues).getItemProperty(propertyName);
                childItem.addItemProperty(propertyName, storedProperty);
            }
        }
    }
}
