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

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.TwinColSelectFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
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

    private String initialSelectedKey;
    private String optionValueName;
    private String optionLabelName;
    private String optionIconName = SelectFieldDefinition.OPTION_ICONSRC_PROPERTY_NAME;
    private boolean hasOptionIcon = false;

    protected AbstractSelect select;

    public SelectFieldFactory(D definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected AbstractSelect createFieldComponent() {
        // Get name of the Label and Value property.
        optionValueName = definition.getValueProperty();
        optionLabelName = definition.getLabelProperty();

        select = createSelectionField();
        select.setContainerDataSource(buildOptions());
        select.setNullSelectionAllowed(false);
        select.setInvalidAllowed(false);
        select.setMultiSelect(false);
        select.setNewItemsAllowed(false);
        if (select instanceof ComboBox) {
            ((ComboBox) select).setFilteringMode(definition.getFilteringMode());
            ((ComboBox) select).setTextInputAllowed(false);
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
     * Second element is the Label
     * Third element is the Icon is defined.
     */
    @SuppressWarnings("unchecked")
    private IndexedContainer buildOptions() {
        IndexedContainer optionContainer = new IndexedContainer();
        List<SelectFieldOptionDefinition> options = getSelectFieldOptionDefinition();
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

    /**
     * Get the list of SelectFieldOptionDefinition.
     * If options is not empty, took the options defined in this field definition.
     * Else, if path is not empty, build an options list based on the node refereed by
     * the path and property value.
     * Else nothing is define, return an empty option.
     * <b>Default value and i18n of the Label is also part of the responsibility of this method.</b>
     */
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        List<SelectFieldOptionDefinition> res = new ArrayList<SelectFieldOptionDefinition>();

        if (definition.getOptions() != null && !definition.getOptions().isEmpty()) {
            for (SelectFieldOptionDefinition option : definition.getOptions()) {
                option.setValue(getValue(option));
                option.setLabel(getMessage(getLabel(option)));
                if (option.isSelected()) {
                    initialSelectedKey = getValue(option);
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
                return getMessage(getLabel(option));
            }
        } else {
            return option.getValue();
        }
    }

    /**
     * Backward compatibility.
     * If label is null, get the Value.
     */
    private String getLabel(SelectFieldOptionDefinition option) {
        if (StringUtils.isBlank(option.getLabel())) {
            return option.getValue();
        } else {
            return option.getLabel();
        }
    }

    /**
     * Set the value selected.
     * Set selectedItem to the last stored value.
     * If not yet stored, set initialSelectedKey as selectedItem
     * Else, set the first element of the list.
     */
    @Override
    protected Object createDefaultValue(Property<?> dataSource) {
        Object selectedValue = null;
        Object datasourceValue = dataSource.getValue();

        if (initialSelectedKey != null) {
            selectedValue = initialSelectedKey;
        } else if (!select.isNullSelectionAllowed() && definition.getOptions() != null && !definition.getOptions().isEmpty() && !(definition instanceof TwinColSelectFieldDefinition)) {
            selectedValue = definition.getOptions().get(0).getValue();
        }
        // Type the selected value
        selectedValue = DefaultPropertyUtil.createTypedValue(getDefinitionType(), (String) selectedValue);
        // Set the selected value (if not null)
        if (datasourceValue != null && datasourceValue instanceof Collection && selectedValue != null) {
            ((Collection) datasourceValue).add(selectedValue);
            selectedValue = datasourceValue;
        }
        return selectedValue;
    }

    @Override
    protected Class<?> getDefinitionType() {
        Class<?> res = super.getDefinitionType();
        if (res == null) {
            res = String.class;
        }
        return res;
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
            // Iterate parent children
            try {
                NodeIterator iterator = parent.getNodes();
                while (iterator.hasNext()) {
                    SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                    Node child = iterator.nextNode();
                    // Get Label and Value
                    String label = getRemoteOptionsName(child, optionLabelName);
                    String value = getRemoteOptionsName(child, optionValueName);
                    option.setLabel(getMessage(label));
                    option.setValue(value);

                    if (child.hasProperty(SelectFieldDefinition.OPTION_SELECTED_PROPERTY_NAME)) {
                        option.setSelected(true);
                        initialSelectedKey = option.getValue();
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
                definition.setOptions(res);
            } catch (Exception e) {
                log.warn("Not able to build options based on option node " + parent.toString(), e);
            }
        }
    }

    /**
     * Get the specific node property. <br>
     * If this property is not defined, return the node name.
     */
    private String getRemoteOptionsName(Node option, String propertyName) throws RepositoryException {
        if (option.hasProperty(propertyName)) {
            return option.getProperty(propertyName).getString();
        } else {
            return option.getName();
        }
    }

}
