/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.form.field.factory;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;

/**
 * Creates and initializes a selection field based on a field definition.
 *
 * @param <D> type of definition
 */
public class SelectFieldFactory<D extends SelectFieldDefinition> extends AbstractFieldFactory<D, Object> {

    private static final Logger log = LoggerFactory.getLogger(SelectFieldFactory.class);

    private List<String> initialSelectedKeys = new ArrayList<>();
    private String optionValueName;
    private String optionLabelName;
    private final String optionIconName = SelectFieldDefinition.OPTION_ICONSRC_PROPERTY_NAME;
    private boolean hasOptionIcon = false;
    private boolean sortOptions = true;

    protected AbstractSelect select;

    @Inject
    public SelectFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
    }

    /**
     * @deprecated since 5.4.7 - use {@link #SelectFieldFactory(SelectFieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public SelectFieldFactory(D definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected AbstractSelect createFieldComponent() {
        // Get name of the Label and Value property.
        optionValueName = definition.getValueProperty();
        optionLabelName = definition.getLabelProperty();
        sortOptions = definition.isSortOptions();

        select = createSelectionField();
        select.setContainerDataSource(buildOptions());
        select.setNullSelectionAllowed(false);
        select.setMultiSelect(false);
        select.setNewItemsAllowed(false);
        if (select instanceof ComboBox) {
            ((ComboBox) select).setFilteringMode(definition.getFilteringMode());
            ((ComboBox) select).setTextInputAllowed(definition.isTextInputAllowed());
            ((ComboBox) select).setPageLength(definition.getPageLength());
        }
        select.setItemCaptionMode(ItemCaptionMode.PROPERTY);
        select.setItemCaptionPropertyId(optionLabelName);

        return select;
    }

    /**
     * Used to initialize the desired subclass of AbstractSelect field component. Subclasses can override this method.
     */
    protected AbstractSelect createSelectionField() {
        return new ComboBox();
    }

    /**
     * Create a IndexContainer containing the options.
     * First element of the container is the Value.
     * Second element is the Label.
     * Third element is the Icon is defined.
     * By default, options labels are sorted alphabetically (in ascending order) unless diversely specified by {@link SelectFieldDefinition#setSortOptions(boolean)}.
     */
    @SuppressWarnings("unchecked")
    private IndexedContainer buildOptions() {
        IndexedContainer optionContainer = new IndexedContainer();

        List<SelectFieldOptionDefinition> options = getOptions();
        if (sortOptions) {
            if (definition.getComparatorClass() != null) {
                Comparator<SelectFieldOptionDefinition> comparator = initializeComparator(definition.getComparatorClass());
                Collections.sort(options, comparator);
            } else {
                Collections.sort(options, new DefaultOptionComparator());
            }
        }
        if (!options.isEmpty()) {
            Class<?> fieldType = DefaultPropertyUtil.getFieldTypeClass(definition.getType());
            optionContainer.addContainerProperty(optionValueName, fieldType, null);
            optionContainer.addContainerProperty(optionLabelName, String.class, null);
            if (hasOptionIcon) {
                optionContainer.addContainerProperty(optionIconName, Resource.class, null);
            }
            for (SelectFieldOptionDefinition option : options) {
                Object value = DefaultPropertyUtil.createTypedValue(fieldType, option.getValue());
                Item item = optionContainer.addItem(value);
                item.getItemProperty(optionValueName).setValue(value);
                item.getItemProperty(optionLabelName).setValue(option.getLabel());
                if (StringUtils.isNotBlank(option.getIconSrc())) {
                    item.getItemProperty(optionIconName).setValue(getIconResource(option));
                }
            }
        }
        return optionContainer;
    }

    protected Comparator<SelectFieldOptionDefinition> initializeComparator(Class<? extends Comparator<SelectFieldOptionDefinition>> comparatorClass) {
        return getComponentProvider().newInstance(comparatorClass, item, definition, getFieldType());
    }

    /**
     * Get the list of SelectFieldOptionDefinition.
     *
     * If there is an explicitly configured option list in the definition - use it.
     * Else, if path is not empty, build an option list based on the node referred to
     * the path and property value.
     * Else, if nothing is defined, return an empty list.
     * <b>Default value and i18n of the Label is also part of the responsibility of this method.</b>
     */
    public List<SelectFieldOptionDefinition> getOptions() {
        // Method body is kept inside #getSelectFieldOptionDefinition for compatibility
        // TODO when deprecated method is removed, inline #getSelectFieldOptionDefinition here
        return getSelectFieldOptionDefinition();
    }

    /**
     * @since 5.4.9 renamed to {@link #getOptions}
     */
    @Deprecated
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        List<SelectFieldOptionDefinition> res = new ArrayList<>();

        if (definition.getOptions() != null && !definition.getOptions().isEmpty()) {
            for (SelectFieldOptionDefinition option : definition.getOptions()) {
                option.setValue(getValue(option));
                if (option.isSelected()) {
                    initialSelectedKeys.add(getValue(option));
                }
                if (!hasOptionIcon && StringUtils.isNotBlank(option.getIconSrc())) {
                    hasOptionIcon = true;
                }
                res.add(option);
            }
        } else if (StringUtils.isNotBlank(definition.getPath())) {
            // Build an option based on the referred node.
            buildRemoteOptions(res);
        }

        return res;
    }

    /**
     * Default Implementation to retrieve an Icon.
     * Sub class should override this method in order to retrieve
     * others Resource.
     */
    public Resource getIconResource(SelectFieldOptionDefinition option) {
        return new ThemeResource(option.getIconSrc());
    }

    /**
     * Backward compatibility.
     * If value is null, <br>
     * - get the Name as value.<br>
     * - If Name is empty, set Label as value.
     */
    private String getValue(SelectFieldOptionDefinition option) {
        if (StringUtils.isBlank(option.getValue())) {
            if (StringUtils.isNotBlank(option.getName())) {
                return option.getName();
            } else {
                return option.getLabel();
            }
        } else {
            return option.getValue();
        }
    }

    /**
     * If value is null but single-select field mandates a non-null value, then pick the first value as default.
     */
    @Override
    public void setPropertyDataSourceAndDefaultValue(Property property) {
        super.setPropertyDataSourceAndDefaultValue(property);

        // whenever previous value is null (i.e. not just for new node adapters)
        boolean shouldPreselectFirstValue = !select.isNullSelectionAllowed() && !select.isMultiSelect() && property.getValue() == null;
        if (shouldPreselectFirstValue) {
            // sanity check — make sure there's a first value up for grabs
            if (select.getItemIds() != null && !select.getItemIds().isEmpty()) {
                property.setValue(select.getItemIds().iterator().next());
            }
        }
    }

    @Override
    protected Object getConfiguredDefaultValue() {
        if (initialSelectedKeys.isEmpty()) {
            return null;
        }
        if (select.isMultiSelect()) {
            return new HashSet<>(initialSelectedKeys);
        } else {
            return initialSelectedKeys.get(0);
        }
    }

    /**
     * Build options based on a remote Node.
     * Simply get the remote Node, Iterate his child nodes and for every child
     * try to get the Value and Label property.
     * In addition create an ArrayList<SelectFieldOptionDefinition> representing this options.
     */
    private void buildRemoteOptions(List<SelectFieldOptionDefinition> res) {
        Node parent = SessionUtil.getNode(definition.getRepository(), definition.getPath());
        if (parent != null) {
            try {
                // Get only relevant child nodes
                Iterable<Node> iterable = NodeUtil.getNodes(parent, createRemoteOptionFilterPredicate());
                Iterator<Node> iterator = iterable.iterator();
                // Iterate parent children
                while (iterator.hasNext()) {
                    SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                    Node child = iterator.next();
                    // Get Label and Value
                    String label = getRemoteOptionsName(child, optionLabelName);
                    String value = getRemoteOptionsValue(child, optionValueName);
                    option.setLabel(label);
                    option.setValue(value);

                    if (child.hasProperty(SelectFieldDefinition.OPTION_SELECTED_PROPERTY_NAME) && Boolean.parseBoolean(child.getProperty(SelectFieldDefinition.OPTION_SELECTED_PROPERTY_NAME).getString())) {
                        option.setSelected(true);
                        initialSelectedKeys.add(option.getValue());
                    }
                    if (child.hasProperty(SelectFieldDefinition.OPTION_NAME_PROPERTY_NAME)) {
                        option.setName(child.getProperty(SelectFieldDefinition.OPTION_NAME_PROPERTY_NAME).getString());
                    }
                    if (child.hasProperty(SelectFieldDefinition.OPTION_ICONSRC_PROPERTY_NAME)) {
                        option.setIconSrc(child.getProperty(SelectFieldDefinition.OPTION_ICONSRC_PROPERTY_NAME).getString());
                        hasOptionIcon = true;
                    }
                    res.add(option);
                }
            } catch (Exception e) {
                log.warn("Not able to build options based on option node " + parent.toString(), e);
            }
        }
    }

    /**
     * @return {@link Predicate} used to filter the remote children option nodes.
     */
    protected Predicate createRemoteOptionFilterPredicate() {
        return NodeUtil.MAGNOLIA_FILTER;
    }

    /**
     * Get the specific node property. <br>
     * If this property is not defined, return the node name.
     * Expose this method in order to let subclass define their own implementation.
     */
    protected String getRemoteOptionsName(Node option, String propertyName) throws RepositoryException {
        if (option.hasProperty(propertyName)) {
            return option.getProperty(propertyName).getString();
        } else {
            return option.getName();
        }
    }

    /**
     * Get the specific node property. <br>
     * If this property is not defined, return the node name.
     * Expose this method in order to let subclass define their own implementation.
     */
    protected String getRemoteOptionsValue(Node option, String propertyName) throws RepositoryException {
        return getRemoteOptionsName(option, propertyName);
    }

    /**
     * A null safe comparator based on the label.
     */
    public static class DefaultOptionComparator implements Comparator<SelectFieldOptionDefinition> {

        @Override
        public int compare(SelectFieldOptionDefinition def, SelectFieldOptionDefinition otherDef) {
            // Null safe comparison of Comparables. null is assumed to be less than a non-null value.
            return ComparatorUtils.nullLowComparator(String.CASE_INSENSITIVE_ORDER).compare(def.getLabel(), otherDef.getLabel());
        }
    }
}
