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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * {@link info.magnolia.ui.form.field.transformer.Transformer} used by {@link info.magnolia.ui.form.field.MultiField} that delegates to sub fields {@link info.magnolia.ui.form.field.transformer.Transformer} the handling of their own properties.
 * Storage strategy: <br>
 * - root item (relatedFormItem)<br>
 * -- child item 1 (used to store the first value of the MultiField)<br>
 * -- child item 2 (used to store the second value of the MultiField)<br>
 * ...<br>
 * The {@link BasicMultiItemTransformer#readFromItem()} returns an {@link PropertysetItem} that contains in this case:<br>
 * - as key, a position used by {@link info.magnolia.ui.form.field.MultiField} <br>
 * - as values the child items wrapped into an {@link ObjectProperty} <br>
 * The {@link BasicMultiItemTransformer#createNewElement()} creates a new child items wrapped into an {@link ObjectProperty} based on the defined <br>
 * - child item naming strategy.<br>
 * Implemented child item naming strategy: <br>
 * - 'baseSubItemName'+'increment'+'i18nSuffix'<br>
 * -- 'baseSubItemName' is set using {@link BasicMultiItemTransformer#setBaseSubItemName()}. By default we use the {@link ConfiguredFieldDefinition#getName()}<br>
 * -- 'increment' is set using: <br>
 * --- {@link BasicMultiItemTransformer#intialIncrementValue()} that return the root increment value. '000' is set for the default implementation <br>
 * --- {@link BasicMultiItemTransformer#nextIncrementValue(int)} that return the next increment value. '001', '002', ... for the default implementation <br>
 * --- {@link BasicMultiItemTransformer#incrementRegexRepresentation()} return a regex string representation of the increment format. '(\\d{3})' for the default implementation <br>
 * -- 'i18nSuffix' default i18n suffix used by Magnolia '_de' <br>
 */
public class BasicMultiItemTransformer extends BasicTransformer<PropertysetItem> implements MultiItemTransformer {

    private static final Logger log = LoggerFactory.getLogger(BasicMultiItemTransformer.class);

    private final I18nContentSupport i18nContentSupport;
    protected String childNodeType = NodeTypes.ContentNode.NAME;
    protected String baseSubItemName;
    protected String i18nSuffix = StringUtils.EMPTY;
    private final String defaultLocal;
    // Map used to store PropertysetItem based on language (i18n support)
    private Map<String, PropertysetItem> items;

    @Inject
    public BasicMultiItemTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, I18nContentSupport i18nContentSupport) {
        super(relatedFormItem, definition, type);
        this.i18nContentSupport = i18nContentSupport;
        this.i18nSuffix += this.i18nContentSupport.getDefaultLocale();
        this.defaultLocal = i18nSuffix;
        this.baseSubItemName = setBaseSubItemName();
        items = new HashMap<String, PropertysetItem>();
        items.put(this.i18nSuffix, new PropertysetItem());
    }

    /**
     * Read the already stored child items.<br>
     * Filter all root Item child nodes (based on the baseSubNodeName and current local set).<br>
     * For each child Item, wrap them into an {@link ObjectProperty} and add it into the returned {@link PropertysetItem}.
     * 
     * @return {@link PropertysetItem} related to the current selected local, containing as keys the position (0,1,..) and as values child Items wrapped into an {@link ObjectProperty}.
     */
    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem itemSet = items.get(this.i18nSuffix);
        // Only read it once
        if (!itemSet.getItemPropertyIds().isEmpty()) {
            return itemSet;
        }
        JcrNodeAdapter rootItem = getRootItem();
        // The root Item was never populated, add relevant child Item based on the stored nodes.
        if (!rootItem.hasChangedChildItems()) {
            populateStoredChildItems(rootItem);
        }
        // Get a list of childNodes
        int position = 0;
        for (String itemName : rootItem.getChildren().keySet()) {
            if (itemName.matches(childItemRegexRepresentation())) {
                itemSet.addItemProperty(position, new ObjectProperty<Item>(rootItem.getChild(itemName)));
                position += 1;
            }
        }
        return itemSet;
    }

    @Override
    public void writeToItem(PropertysetItem newValue) {
        // No need to write to the Item as this is done on the Item passed to the subFields.
        log.debug("CALL writeToItem");
    }

    /**
     * Create a new child Item, and bound it with the root Item.
     */
    @Override
    public Property<?> createNewElement() {
        String newItemName = createNewItemName();

        JcrNodeAdapter child = new JcrNewNodeAdapter(getRootItem().getJcrItem(), childNodeType, newItemName);
        child.setParent(getRootItem());
        child.getParent().addChild(child);
        Property<?> res = new ObjectProperty<Item>(child);
        PropertysetItem itemSet = items.get(this.i18nSuffix);
        itemSet.addItemProperty(itemSet.getItemPropertyIds().size() + 1, res);

        return res;
    }

    /**
     * Remove a child Item from the root Item.
     */
    @Override
    public void removeElement(Object id) {
        PropertysetItem itemSet = items.get(this.i18nSuffix);
        Property<?> propertyToRemove = itemSet.getItemProperty(id);
        if (propertyToRemove != null && propertyToRemove.getValue() != null) {
            JcrNodeAdapter toRemove = (JcrNodeAdapter) propertyToRemove.getValue();
            toRemove.getParent().removeChild(toRemove);
        }
        itemSet.removeItemProperty(id);

    }

    /**
     * Used by {@link I18NAuthoringSupport} to define the i18N name.
     */
    @Override
    public String getBasePropertyName() {
        return baseSubItemName;
    }

    /**
     * Define the base sub node name.
     * basic implementation uses the {@link ConfiguredFieldDefinition#getName()}.
     */
    protected String setBaseSubItemName() {
        return definition.getName();
    }

    @Override
    public void setI18NPropertyName(String i18NSubNodeName) {
        String newLocal = StringUtils.substringAfter(i18NSubNodeName, getBasePropertyName() + "_");
        this.i18nSuffix = StringUtils.isBlank(newLocal) ? this.defaultLocal : newLocal;
        log.debug("Change language to '{}'", this.i18nSuffix);
        if (!items.containsKey(this.i18nSuffix)) {
            items.put(this.i18nSuffix, new PropertysetItem());
        }
    }

    /**
     * Define the root Item used to retrieve the child items, and store the newly created child items.
     */
    protected JcrNodeAdapter getRootItem() {
        return (JcrNodeAdapter) relatedFormItem;
    }


    /**
     * Add to rootItem all his children.
     */
    protected void populateStoredChildItems(JcrNodeAdapter rootItem) {
        List<Node> childNodes = getStoredChildNodes(rootItem);
        for (Node child : childNodes) {
            JcrNodeAdapter item = new JcrNodeAdapter(child);
            item.setParent(rootItem);
            item.getParent().addChild(item);
        }
    }

    /**
     * Get all childNodes of parent passing the {@link NodeUtil#MAGNOLIA_FILTER}.
     */
    protected List<Node> getStoredChildNodes(JcrNodeAdapter parent) {
        try {
            if (!(parent instanceof JcrNewNodeAdapter) && parent.getJcrItem().hasNodes()) {
                return NodeUtil.asList(NodeUtil.getNodes(parent.getJcrItem(), NodeUtil.MAGNOLIA_FILTER));
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the Child Nodes of the following Node Identifier {}", parent.getItemId(), re);
        }
        return new ArrayList<Node>();
    }


    /**
     * Create a unique child Item name.<br>
     * baseSubNodeName + number (0...999) + current local (if this is not the default local).
     */
    String createNewItemName() {
        int nb = 0;
        String localAsString = hasI18NSupport() && i18nContentSupport.isEnabled() && !this.defaultLocal.equals(this.i18nSuffix) ? "_" + this.i18nSuffix : StringUtils.EMPTY;
        String name = this.baseSubItemName + intialIncrementValue() + localAsString;
        List<String> childNodeName = getChildItemNames();
        while (childNodeName.contains(name)) {
            nb += 1;
            name = this.baseSubItemName + nextIncrementValue(nb) + localAsString;
        }
        return name;
    }

    /**
     * @return initial/root value of the increment.
     */
    protected String intialIncrementValue() {
        return "000";
    }

    /**
     * @param iterationNumber
     * @return next increment value.
     */
    protected String nextIncrementValue(int iterationNumber) {
        DecimalFormat df = new DecimalFormat("000");
        return df.format(iterationNumber);
    }

    protected String incrementRegexRepresentation() {
        return "(\\d{3})";
    }

    /**
     * @return regex String used to filter the correct children items based on i18n support and current Local.
     */
    protected String childItemRegexRepresentation() {
        if (hasI18NSupport() && i18nContentSupport.isEnabled()) {
            if (defaultLocal.equals(i18nSuffix)) {
                // i18n set, current local is the default local
                // match all node name that do not define locale extension
                return baseSubItemName + incrementRegexRepresentation() + "((?!(_\\w{2}){1,3}))$";
            } else {
                // i18n set, not default local used
                return baseSubItemName + incrementRegexRepresentation() + "_" + i18nSuffix;
            }
        } else {
            return baseSubItemName + incrementRegexRepresentation();
        }
    }

    private List<String> getChildItemNames() {
        List<String> res = new ArrayList<String>();
        PropertysetItem itemSet = items.get(this.i18nSuffix);
        for (Object id : itemSet.getItemPropertyIds()) {
            res.add(((JcrNodeAdapter) itemSet.getItemProperty(id).getValue()).getNodeName());
        }
        return res;
    }

}
