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

import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.SelectFieldDefinition;

import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Field;
import com.vaadin.ui.NativeSelect;


/**
 * .
 */
public class DialogSelectField extends AbstractDialogField<FieldDefinition> {

    public static final String TEXTFIELD_STYLE_NAME = "textfield";

    public DialogSelectField(FieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected Field buildField() {
        NativeSelect select = new NativeSelect();
        select.setNullSelectionAllowed(false);
        select.setInvalidAllowed(false);
        select.setMultiSelect(false);
        select.setNewItemsAllowed(false);
        select.setSizeFull();
        Map<String, String> options = getOptions();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            select.addItem(entry.getKey());
            select.setItemCaption(entry.getKey(), entry.getValue());
        }
        // We can't leave the field without a value because it will render an extra, blank, option if we do
        if (!options.isEmpty()) {
            select.setValue(options.entrySet().iterator().next().getKey());
        }

        // TODO add focus listener, see http://dev.vaadin.com/ticket/6847

        /**
         * Add an ValueChangeListener to the Select component.
         * On component select, set the value of the related Vaadin property field .
         */
        select.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Property p = dialogField.getPropertyDataSource();
                p.setValue(event.getProperty().getValue());
            }

        });

        return select;
    }

    public Map<String, String>  getOptions() {
        return ((SelectFieldDefinition) getFieldDefinition()).getOptions();
    }

    /**
     * Set the select item if the datasource is not empty.
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        if(!newDataSource.getValue().toString().isEmpty()) {
            dialogField.setValue(newDataSource.getValue());
        }
    }
}
