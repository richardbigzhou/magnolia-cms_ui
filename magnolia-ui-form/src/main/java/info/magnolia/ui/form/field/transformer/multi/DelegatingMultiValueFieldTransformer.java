/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
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

    protected String childNodeType = NodeTypes.ContentNode.NAME;

    protected String subItemBaseName;

    private List<String> delegateItemNames = new ArrayList<>();

    private PropertysetItem delegateAggregatorItem = new PropertysetItem();


    /**
     * @deprecated since 5.4.2 - use {@link #DelegatingMultiValueFieldTransformer(Item, ConfiguredFieldDefinition, Class, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public DelegatingMultiValueFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, I18nContentSupport i18nContentSupport) {
        this(relatedFormItem, definition, type, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Inject
    public DelegatingMultiValueFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, I18NAuthoringSupport i18nAuthoringSupport) {
        super(relatedFormItem, definition, type, i18nAuthoringSupport);
        this.subItemBaseName = getSubItemBaseName();
    }

    /**
     * Returns a representation of the child items as a {@link PropertysetItem};
     * this is merely a map whose keys are the positions in the <code>MultiField</code>, and whose values are the child items, wrapped as {@link ObjectProperty ObjectProperties}.
     * <p>
     * Please note that this list of child items is filtered based on the <i>subItemBaseName</i> and current locale.
     */
    @Override
    public PropertysetItem readFromItem() {
        // Only read it once
        if (delegateAggregatorItem.getItemPropertyIds().isEmpty()) {
            JcrNodeAdapter rootItem = getRootItem();
            // The root Item was never populated, add relevant child Item based on the stored nodes.
            if (!rootItem.hasChildItemChanges()) {
                populateStoredChildItems(rootItem);
            }
            // Get a list of childNodes
            int position = 0;
            for (String itemName : rootItem.getChildren().keySet()) {
                if (itemName.matches(childItemRegexRepresentation())) {
                    delegateAggregatorItem.addItemProperty(position, new ObjectProperty<Item>(rootItem.getChild(itemName)));
                    delegateItemNames.add(itemName);
                    ++position;
                }
            }
        }
        return delegateAggregatorItem;
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
        final String newItemName = createNewItemName();

        final JcrNodeAdapter child;

        // Should check the new Item Name existed because it can be created already from other language form.
        if (getRootItem().getChild(newItemName) instanceof JcrNodeAdapter) {
            child = (JcrNodeAdapter) getRootItem().getChild(newItemName);
        } else {
            child = new JcrNewNodeAdapter(getRootItem().getJcrItem(), childNodeType, newItemName);
        }

        getRootItem().addChild(child);
        Property<?> res = new ObjectProperty<Item>(child);
        delegateAggregatorItem.addItemProperty(delegateAggregatorItem.getItemPropertyIds().size(), res);

        return res;
    }

    @Override
    public void removeProperty(Object id) {
        Property<?> propertyToRemove = delegateAggregatorItem.getItemProperty(id);
        if (propertyToRemove != null && propertyToRemove.getValue() != null) {
            JcrNodeAdapter toRemove = (JcrNodeAdapter) propertyToRemove.getValue();
            toRemove.getParent().removeChild(toRemove);
        }
        delegateAggregatorItem.removeItemProperty(id);
        reorganizeIndex((Integer) id);
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        ImmutableList<Object> propertyIds = ImmutableList.copyOf(delegateAggregatorItem.getItemPropertyIds());
        for (Object id : propertyIds) {
            delegateAggregatorItem.removeItemProperty(id);
        }
    }

    /**
     * Ensure that id of the {@link PropertysetItem} stay coherent.<br>
     * Assume that we have 3 values 0:a, 1:b, 2:c, and 1 is removed <br>
     * If we just remove 1, the {@link PropertysetItem} will contain 0:a, 2:c, .<br>
     * But we should have : 0:a, 1:c, .
     */
    private void reorganizeIndex(int fromIndex) {
        int valuesSize = delegateAggregatorItem.getItemPropertyIds().size();
        if (fromIndex == valuesSize) {
            return;
        }
        while (fromIndex < valuesSize) {
            int toIndex = fromIndex;
            ++fromIndex;
            delegateAggregatorItem.addItemProperty(toIndex, delegateAggregatorItem.getItemProperty(fromIndex));
            delegateAggregatorItem.removeItemProperty(fromIndex);
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
            rootItem.addChild(item);
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
     * <li><i>subItemBaseName</i> by default we use the {@link info.magnolia.ui.form.field.definition.FieldDefinition#getName()}
     * <li><i>increment</i> is the next available index for the current base name
     * <li><i>i18nSuffix</i> is the default i18n suffix (typically something formatted like '_de')
     * </ul>
     * .
     */
    protected String createNewItemName() {
        int increment = 0;
        final List<String> childNodeNames = getChildItemNames();
        String name;
        do {
            name = deriveLocaleAwareName(String.format("%s%d", subItemBaseName, increment));
            ++increment;
        } while (childNodeNames.contains(name));
        return name;
    }

    /**
     * @return The regex used to filter child items based on i18n support and current locale
     */
    protected String childItemRegexRepresentation() {
        if (hasI18NSupport()) {
            if (getLocale() == null || getI18NAuthoringSupport().isDefaultLocale(getLocale(), relatedFormItem)) {
                // i18n set, current locale is the default locale
                // match all node name that do not define locale extension
                return subItemBaseName + incrementRegexRepresentation() + "((?!(_\\w{2}){1,3}))$";
            } else {
                // i18n set, not default locale used
                return getI18NAuthoringSupport().deriveLocalisedPropertyName(subItemBaseName + incrementRegexRepresentation(), getLocale());
            }
        } else {
            return subItemBaseName + incrementRegexRepresentation();
        }
    }

    protected String incrementRegexRepresentation() {
        return "(\\d{1,3})";
    }

    private List<String> getChildItemNames() {
        List<String> res = new ArrayList<>();
        res.addAll(delegateItemNames);
        for (Object delegateIds : delegateAggregatorItem.getItemPropertyIds()) {
            Object value = delegateAggregatorItem.getItemProperty(delegateIds).getValue();
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
        log.warn("DelegatingMultiValueFieldTransformer.setI18NPropertyName is deprecated since 5.4.2 - should you need a different locale-specific node name, it is possible to alter #i18nSuffix field in #setLocale() method.");
    }
}
