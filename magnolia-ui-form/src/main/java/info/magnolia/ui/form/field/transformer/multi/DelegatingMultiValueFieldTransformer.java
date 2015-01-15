/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * This delegating {@link info.magnolia.ui.form.field.transformer.Transformer Transformer} is dedicated to the {@link info.magnolia.ui.form.field.MultiField MultiField};
 * it considers entries as child nodes and delegates property handling to their respective sub-fields.
 * <p>
 * The storage strategy is that of the {@link info.magnolia.ui.form.field.transformer.multi.MultiValueChildNodeTransformer MultiValueChildNodeTransformer}:
 * <ul>
 * <li>rootItem (relatedFormItem)
 * <ul>
 * <li>childItem1 (first entry of the MultiField)<br>
 * <li>childItem2 (second entry of the MultiField)<br>
 * <li>...
 * </ul>
 * </ul>
 */
public class DelegatingMultiValueFieldTransformer extends BasicTransformer<PropertysetItem> implements MultiTransformer {

    private static final Logger log = LoggerFactory.getLogger(DelegatingMultiValueFieldTransformer.class);

    private final I18nContentSupport i18nContentSupport;
    protected String childNodeType = NodeTypes.ContentNode.NAME;
    protected String subItemBaseName;
    protected String i18nSuffix = StringUtils.EMPTY;
    private final String defaultLocale;
    // Map used to store PropertysetItem based on language (i18n support)
    private Map<String, PropertysetItem> items = new HashMap<String, PropertysetItem>();;
    private List<String> freezedName = new ArrayList<String>();

    @Inject
    public DelegatingMultiValueFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, I18nContentSupport i18nContentSupport) {
        super(relatedFormItem, definition, type);
        this.i18nContentSupport = i18nContentSupport;
        this.i18nSuffix += this.i18nContentSupport.getDefaultLocale();
        this.defaultLocale = i18nSuffix;
        this.subItemBaseName = getSubItemBaseName();
        items.put(this.i18nSuffix, new PropertysetItem());
    }

    /**
     * Returns a representation of the child items as a {@link PropertysetItem};
     * this is merely a map whose keys are the positions in the <code>MultiField</code>, and whose values are the child items, wrapped as {@link ObjectProperty ObjectProperties}.
     * <p>
     * Please note that this list of child items is filtered based on the <i>subItemBaseName</i> and current locale.
     */
    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem item = items.get(this.i18nSuffix);
        // Only read it once
        if (!item.getItemPropertyIds().isEmpty()) {
            return item;
        }
        JcrNodeAdapter rootItem = getRootItem();
        // The root Item was never populated, add relevant child Item based on the stored nodes.
        if (!rootItem.hasChildItemChanges()) {
            populateStoredChildItems(rootItem);
        }
        // Get a list of childNodes
        int position = 0;
        for (String itemName : rootItem.getChildren().keySet()) {
            if (itemName.matches(childItemRegexRepresentation())) {
                item.addItemProperty(position, new ObjectProperty<Item>(rootItem.getChild(itemName)));
                position += 1;
                freezedName.add(itemName);
            }
        }
        return item;
    }

    /**
     * This transformer's write implementation is empty. We do not need to write to the item as this is delegated to the sub-fields.
     */
    @Override
    public void writeToItem(PropertysetItem newValue) {
        log.debug("CALL writeToItem");
    }

    /**
     * Creates a new child item, adds it to the root item, and returns it wrapped as an {@link ObjectProperty}.
     * <p>
     * The child item naming strategy is as follows: <i>subItemBaseName</i> + <i>increment</i> + <i>i18nSuffix</i>
     *
     * @see {@link #createNewItemName()}
     */
    @Override
    public Property<?> createProperty() {
        String newItemName = createNewItemName();

        JcrNodeAdapter child = new JcrNewNodeAdapter(getRootItem().getJcrItem(), childNodeType, newItemName);
        child.setParent(getRootItem());
        child.getParent().addChild(child);
        Property<?> res = new ObjectProperty<Item>(child);
        PropertysetItem item = items.get(this.i18nSuffix);
        item.addItemProperty(item.getItemPropertyIds().size(), res);

        return res;
    }

    @Override
    public void removeProperty(Object id) {
        PropertysetItem item = items.get(this.i18nSuffix);
        Property<?> propertyToRemove = item.getItemProperty(id);
        if (propertyToRemove != null && propertyToRemove.getValue() != null) {
            JcrNodeAdapter toRemove = (JcrNodeAdapter) propertyToRemove.getValue();
            toRemove.getParent().removeChild(toRemove);
        }
        item.removeItemProperty(id);
        reorganizeIndex((Integer) id, item);
    }

    /**
     * Ensure that id of the {@link PropertysetItem} stay coherent.<br>
     * Assume that we have 3 values 0:a, 1:b, 2:c, and 1 is removed <br>
     * If we just remove 1, the {@link PropertysetItem} will contain 0:a, 2:c, .<br>
     * But we should have : 0:a, 1:c, .
     */
    private void reorganizeIndex(int fromIndex, PropertysetItem item) {
        int toIndex = fromIndex;
        int valuesSize = item.getItemPropertyIds().size();
        if (fromIndex == valuesSize) {
            return;
        }
        while (fromIndex < valuesSize) {
            toIndex = fromIndex;
            fromIndex += 1;
            item.addItemProperty(toIndex, item.getItemProperty(fromIndex));
            item.removeItemProperty(fromIndex);
        }
    }

    /**
     * Defines the root item used to retrieve and create child items.
     */
    protected JcrNodeAdapter getRootItem() {
        return (JcrNodeAdapter) relatedFormItem;
    }

    /**
     * Defines the base name to use for retrieving and creating child items.
     * <p>
     * By default, we use the {@link info.magnolia.ui.form.field.definition.FieldDefinition#getName()}.
     */
    protected String getSubItemBaseName() {
        return definition.getName();
    }

    /**
     * Populates the given root item with its child items.
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
     * Fetches child nodes of the given parent from JCR, filtered using the {@link NodeUtil#MAGNOLIA_FILTER} predicate.
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
     * Creates a unique name for the child item, in the following format:
     * <i>subItemBaseName</i> + <i>increment</i> + <i>i18nSuffix</i>
     * <ul>
     * <li><i>subItemBaseName</i> can be set via {@link #setSubItemBaseName()}; by default we use the {@link info.magnolia.ui.form.field.definition.FieldDefinition#getName()}
     * <li><i>increment</i> is the next available index for the current base name
     * <li><i>i18nSuffix</i> is the default i18n suffix (typically something formatted like '_de')
     * </ul>
     * .
     */
    protected String createNewItemName() {
        int nb = 0;
        String localeAsString = hasI18NSupport() && i18nContentSupport.isEnabled() && !this.defaultLocale.equals(this.i18nSuffix) ? "_" + this.i18nSuffix : StringUtils.EMPTY;
        String name = this.subItemBaseName + String.valueOf(nb) + localeAsString;
        List<String> childNodeName = getChildItemNames();
        while (childNodeName.contains(name)) {
            nb += 1;
            name = this.subItemBaseName + String.valueOf(nb) + localeAsString;
        }
        return name;
    }

    /**
     * @return The regex used to filter child items based on i18n support and current locale
     */
    protected String childItemRegexRepresentation() {
        if (hasI18NSupport() && i18nContentSupport.isEnabled()) {
            if (defaultLocale.equals(i18nSuffix)) {
                // i18n set, current locale is the default locale
                // match all node name that do not define locale extension
                return subItemBaseName + incrementRegexRepresentation() + "((?!(_\\w{2}){1,3}))$";
            } else {
                // i18n set, not default locale used
                return subItemBaseName + incrementRegexRepresentation() + "_" + i18nSuffix;
            }
        } else {
            return subItemBaseName + incrementRegexRepresentation();
        }
    }

    protected String incrementRegexRepresentation() {
        return "(\\d{1,3})";
    }

    private List<String> getChildItemNames() {
        List<String> res = new ArrayList<String>();
        res.addAll(freezedName);
        PropertysetItem item = items.get(this.i18nSuffix);
        for (Object id : item.getItemPropertyIds()) {
            Object value = item.getItemProperty(id).getValue();
            if (value instanceof JcrNodeAdapter) {
                res.add(((JcrNodeAdapter) value).getNodeName());
            }
        }
        return res;
    }

    /* I18nAwareHandler impl */

    @Override
    public String getBasePropertyName() {
        return subItemBaseName;
    }

    @Override
    public void setI18NPropertyName(String i18NSubNodeName) {
        String newLocale = StringUtils.substringAfter(i18NSubNodeName, getBasePropertyName() + "_");
        this.i18nSuffix = StringUtils.isBlank(newLocale) ? this.defaultLocale : newLocale;
        log.debug("Change language to '{}'", this.i18nSuffix);
        if (!items.containsKey(this.i18nSuffix)) {
            items.put(this.i18nSuffix, new PropertysetItem());
        }
    }
}
