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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.JCRMgnlPropertyHidingPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
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

    private final MultiValueFieldDefinition definition;

    /**
     * @deprecated since 5.4.2 - use {@link #MultiValueSubChildrenNodePropertiesTransformer(Item, MultiValueFieldDefinition, Class, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public MultiValueSubChildrenNodePropertiesTransformer(Item relatedFormItem, MultiValueFieldDefinition definition, Class<PropertysetItem> type) {
        this(relatedFormItem, definition, type, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Inject
    public MultiValueSubChildrenNodePropertiesTransformer(Item relatedFormItem, MultiValueFieldDefinition definition, Class<PropertysetItem> type, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type, i18NAuthoringSupport);
        this.definition = definition;
    }

    @Override
    protected JcrNodeAdapter getRootItem() {
        JcrNodeAdapter res = null;
        try {
            res = getOrCreateChildNode(definition.getName(), childNodeType);
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
            // read values from properties
            PropertyIterator iterator = new JCRMgnlPropertiesFilteringNodeWrapper(child).getProperties();
            while (iterator.hasNext()) {
                Property jcrProperty = iterator.nextProperty();
                Object propertyObject = PropertyUtil.getPropertyValueObject(child, jcrProperty.getName());
                DefaultProperty newProperty = new DefaultProperty(propertyObject);

                newValues.addItemProperty(NumberUtils.isNumber(jcrProperty.getName()) ? Integer.valueOf(jcrProperty.getName()) : jcrProperty.getName(), newProperty);
            }
            // and read values from subnodes (recursively)
            NodeIterator iter = child.getNodes();
            while (iter.hasNext()) {
                Node childNode = iter.nextNode();
                PropertysetItem vals = getValueFromChildNode(childNode);
                MultiValueFieldDefinition subChildDefinition = null;
                for (ConfiguredFieldDefinition field : ((CompositeFieldDefinition) definition.getField()).getFields()) {
                    if (field.getName().equals(childNode.getName())) {
                        subChildDefinition = (MultiValueFieldDefinition) field;
                        break;
                    }
                }
                Transformer<PropertysetItem> subChildTransformer = (Transformer<PropertysetItem>) Components.newInstance(subChildDefinition.getTransformerClass(), new JcrNodeAdapter(childNode), subChildDefinition, PropertysetItem.class);
                TransformedProperty<PropertysetItem> prop = new TransformedProperty<PropertysetItem>(subChildTransformer);
                prop.setValue(vals);
                newValues.addItemProperty(childNode.getName(), prop);
            }
        } catch (RepositoryException re) {
            log.warn("Not able to read property from the following child node {}", NodeUtil.getName(child), re.getLocalizedMessage());
        }
        return newValues;
    }

    @Override
    protected void setChildItemValue(JcrNodeAdapter childItem, Object newValues) {
        if (!(newValues instanceof PropertysetItem)) {
            super.setChildItemValue(childItem, newValues);
            return;
        }
        PropertysetItem newPropertySetValues = (PropertysetItem) newValues;

        // stored property will always be of type string since we don't store names of jcr props in any other format so we need them as strings when checking for removed ones
        List<String> propertyNamesAsString = new ArrayList<String>();
        Iterator<?> propertyNames = newPropertySetValues.getItemPropertyIds().iterator();
        while (propertyNames.hasNext()) {
            // could be string, but could be also number
            Object propertyName = propertyNames.next();
            String propertyNameString = propertyName.toString();
            propertyNamesAsString.add(propertyNameString);
            com.vaadin.data.Property<Object> storedProperty = childItem.getItemProperty(propertyName);
            com.vaadin.data.Property<Object> newProperty = newPropertySetValues.getItemProperty(propertyName);
            if (newProperty != null) {
                if (storedProperty != null) {
                    storedProperty.setValue(newProperty.getValue());
                } else {
                    Object value = newProperty.getValue();
                    if (value instanceof PropertysetItem) {
                        // if this is another set, create subnode for it and recursively call itself to set its properties (or subnodes)
                        JcrNodeAdapter child;
                        try {
                            if (childItem.getJcrItem().hasNode(propertyNameString)) {
                                Node node = childItem.getJcrItem().getNode(propertyNameString);
                                child = new JcrNodeAdapter(node);
                            } else {
                                child = new JcrNewNodeAdapter(childItem.getJcrItem(), NodeTypes.ContentNode.NAME);
                                child.setNodeName(propertyNameString);
                            }
                            childItem.addChild(child);
                            setChildItemValue(child, value);
                        } catch (RepositoryException e) {
                            log.error("Failed to persist property " + propertyName + " (" + propertyName.getClass().getName() + ") with " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    } else {
                        childItem.addItemProperty(propertyName, newProperty);
                    }
                }
            }
        }
        // mark no longer existing properties as removed to be cleaned up when saving dialog
        try {
            FilteringPropertyIterator iter = new FilteringPropertyIterator(childItem.getJcrItem().getProperties(), new JCRMgnlPropertyHidingPredicate());
            while (iter.hasNext()) {
                Property prop = iter.nextProperty();
                if (!propertyNamesAsString.contains(prop.getName())) {
                    childItem.removeItemProperty(prop.getName());
                }
            }
        } catch (RepositoryException e) {
            log.error("Failed to remove old property from " + childItem + " with " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
