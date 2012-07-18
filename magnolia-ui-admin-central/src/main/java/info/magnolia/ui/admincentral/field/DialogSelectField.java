/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.field;

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.SelectFieldDefinition;
import info.magnolia.ui.model.field.definition.SelectFieldOptionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Field;
import com.vaadin.ui.NativeSelect;


/**
 * Initialize a Selection Field based on the configured informations.
 */
public class DialogSelectField extends AbstractDialogField<FieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(DialogSelectField.class);

    private String initialSelecteKey;
    private String optionValueName;
    private String optionLabelName;
    protected List<SelectFieldOptionDefinition>  options;
    protected AbstractSelect select;

    public DialogSelectField(FieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected Field buildField() {
        select = createVaadinSeletionField();
        select.setNullSelectionAllowed(false);
        select.setInvalidAllowed(false);
        select.setMultiSelect(false);
        select.setNewItemsAllowed(false);
        //Set Style
        if(StringUtils.isNotBlank(((SelectFieldDefinition)getFieldDefinition()).getCssClass())) {
            setStyleName(((SelectFieldDefinition)getFieldDefinition()).getCssClass());
        }
        //Get Options
        Map<String, String> options = getOptions();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            select.addItem(entry.getKey());
            select.setItemCaption(entry.getKey(), entry.getValue());
        }

        return select;
    }

    /**
     * Used to initialize the desired subclass of VaadinSelect field component.
     * Subclass of DialogSelectField should override this method.
     */
    protected AbstractSelect createVaadinSeletionField() {
        return new NativeSelect();
    }

    /**
     * Default implementation that build the Options map.
     * If options is not empty, took the options defined in this field definition.
     * Else, if path is not empty, build an options list based on the node refereed by
     *    the path and property value.
     * Else nothing is define, return an empty option.
     *
     * @return Key: Stored value, Value: Displayed Value
     */
    public Map<String, String>  getOptions() {
        Map<String, String> res = new HashMap<String, String>();
        SelectFieldDefinition selectFieldDefinition = (SelectFieldDefinition)getFieldDefinition();

        this.options = selectFieldDefinition.getOptions();

        if(this.options !=null && !this.options.isEmpty()){
            for(SelectFieldOptionDefinition option : this.options) {
                res.put(option.getValue(), option.getLabel());
                if(option.isSelected()) {
                    initialSelecteKey = option.getValue();
                }
            }
        }else if(StringUtils.isNotBlank(selectFieldDefinition.getPath())) {
            //Build an option based on the referred node.
            buildRemoteOptions(res, selectFieldDefinition);
        }

        return res;
    }

    /**
     * Set the select item if the datasource is not empty.
     */
    @Override
    public void setPropertyDataSource(Property dataSource) {
        super.setPropertyDataSource(dataSource);
        setDefaultSelectedItem(dataSource);
    }

    /**
     * Set the value selected.
     * Set selectedItem to the last stored value.
     * If not yet stored, set initialSelectedKey as selectedItem
     * Else, set the first element of the list.
     */
    private void setDefaultSelectedItem(Property dataSource) {
        String selectedValue = null;
        if(!dataSource.getValue().toString().isEmpty()) {
            selectedValue = dataSource.getValue().toString();
        } else if (initialSelecteKey !=null) {
            selectedValue = initialSelecteKey;
        } else if (options !=null && !options.isEmpty()) {
            selectedValue = options.get(0).getValue();
        }
        this.field.setValue(selectedValue);
    }

    /**
     * Build Options based on a remote Node.
     * Simply get the remote Node, Iterate his child nodes and for every child
     * try to get the Value and Label property.
     * In addition create an ArrayList<SelectFieldOptionDefinition> representing this options.
     */
    private void buildRemoteOptions(Map<String, String> res, SelectFieldDefinition selectFieldDefinition) {
        Node parent = SessionUtil.getNode(selectFieldDefinition.getRepository(), selectFieldDefinition.getPath());
        if (parent != null) {
            optionValueName = selectFieldDefinition.getValueNodeData();
            optionLabelName = selectFieldDefinition.getLabelNodeData();
            options = new ArrayList<SelectFieldOptionDefinition>();
            //Iterate parent childs
            try {
                NodeIterator iterator = parent.getNodes();
                while(iterator.hasNext()) {
                    SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                    Node child = iterator.nextNode();
                    if(child.hasProperty(optionValueName) && child.hasProperty(optionLabelName) ) {
                        option.setLabel(child.getProperty(optionLabelName).getString());
                        option.setValue(child.getProperty(optionValueName).getString());

                        res.put(option.getValue(), option.getLabel());
                        if(child.hasProperty(SelectFieldDefinition.OPTION_SELECTED_PROPERTY_NAME)) {
                            option.setSelected(true);
                            initialSelecteKey = option.getValue();
                        }
                        if(child.hasProperty(SelectFieldDefinition.OPTION_NAME_PROPERTY_NAME)) {
                            option.setName(child.getProperty(SelectFieldDefinition.OPTION_NAME_PROPERTY_NAME).getString());
                        }
                        if(child.hasProperty(SelectFieldDefinition.OPTION_ICONSRC_PROPERTY_NAME)) {
                            option.setIconSrc(child.getProperty(SelectFieldDefinition.OPTION_ICONSRC_PROPERTY_NAME).getString());
                        }
                    }
                    options.add(option);
                }
            } catch(Exception e) {
                log.warn("Not able to buld Options based on option node "+parent.toString(),e);
            }
        }
    }
}
