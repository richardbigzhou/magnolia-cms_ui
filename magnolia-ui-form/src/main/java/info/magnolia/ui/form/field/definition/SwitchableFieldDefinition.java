/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.form.field.definition;

import info.magnolia.ui.form.field.transformer.composite.SwitchableTransformer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Field definition for a switchable field.
 */
public class SwitchableFieldDefinition extends CompositeFieldDefinition {
    private static final Logger log = LoggerFactory.getLogger(SwitchableFieldDefinition.class);

    private String selectionType = "radio";
    private List<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();

    private SelectFieldDefinition selectDefinition = null;

    /**
     * Set default {@link info.magnolia.ui.form.field.transformer.Transformer}.
     */
    public SwitchableFieldDefinition() {
        setTransformerClass(SwitchableTransformer.class);
    }

    /**
     * Default selectionType is 'radio'.
     * 
     * @return the desired selection field (radio:OptionGroupFieldDefinition, select:).
     */
    public String getSelectionType() {
        return selectionType;
    }

    /**
     * @return SelectFieldOptionDefinition. The value (for example 'text') of the Option should match with a <br>
     * ConfiguredFieldDefinition name of the fields list.
     */
    public List<SelectFieldOptionDefinition> getOptions() {
        return options;
    }

    public void setSelectionType(String selectionType) {
        this.selectionType = selectionType;
    }

    public void setOptions(List<SelectFieldOptionDefinition> options) {
        this.options = options;
    }

    /**
     * Add the field selection field definition created by {@link SwitchableFieldDefinition#createSelectFieldDefinition()} to the {@link CompositeFieldDefinition#getFields()} and {@link CompositeFieldDefinition#getFieldsName()}.
     */
    @Override
    protected List<String> initFieldsName() {
        List<String> fieldsName = super.initFieldsName();
        if (selectDefinition == null) {
            selectDefinition = (SelectFieldDefinition) createSelectFieldDefinition();
            addField(selectDefinition);
            fieldsName.add(selectDefinition.getName());
        }
        return fieldsName;
    }

    private ConfiguredFieldDefinition createSelectFieldDefinition() {
        try {
            // Create the correct definition class
            String layout = "horizontal";
            if (getSelectionType().equals("radio")) {
                selectDefinition = new OptionGroupFieldDefinition();
                if (getLayout().equals(Layout.vertical)) {
                    layout = "vertical";
                }
            } else {
                selectDefinition = new SelectFieldDefinition();
            }
            // Copy options to the newly created select definition. definition
            selectDefinition.setOptions(getOptions());
            selectDefinition.setTransformerClass(null);
            selectDefinition.setLabel("");
            selectDefinition.setRequired(false);
            selectDefinition.setSortOptions(false);
            selectDefinition.setStyleName(layout);
            selectDefinition.setName(getName());

            if (isI18n()) {
                selectDefinition.setI18n(isI18n());
            }

        } catch (Exception e) {
            log.warn("Coudn't create the select field.", e.getMessage());
        }
        return selectDefinition;
    }
}
