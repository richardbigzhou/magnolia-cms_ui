/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.form.field;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Switchable field.<br>
 * <b> Validation </b>
 * Only the selected field is validated. <br>
 */
public class SwitchableField extends CustomField<String> {
    private static final Logger log = LoggerFactory.getLogger(SwitchableField.class);

    private final HashMap<String, Field<?>> fieldMap;
    private AbstractSelect selectField;

    // Define layout and component
    private final VerticalLayout rootLayout = new VerticalLayout();
    private final HorizontalLayout fieldLayout = new HorizontalLayout();

    public SwitchableField(HashMap<String, Field<?>> fieldMap, AbstractSelect selectField) {
        this.fieldMap = fieldMap;
        this.selectField = selectField;
    }

    @Override
    protected Component initContent() {
        // Initialize root
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        // Add Select section
        rootLayout.addComponent(selectField);
        selectField.addValueChangeListener(createSelectValueChangeListener());
        // Add Field section
        rootLayout.addComponent(fieldLayout);

        return rootLayout;
    }

    /**
     * Change Listener bound to the select field. Once a selection is done, <br>
     * the value change listener will switch to the field linked to the current select value.
     */
    private ValueChangeListener createSelectValueChangeListener() {
        ValueChangeListener listener ;
        listener = new ValueChangeListener() {

            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                final String valueString = String.valueOf(event.getProperty()
                        .getValue());
                switchField(valueString);
            }

        };
        return listener;
    }

    /**
     * Switch to the desired field. It the field is not part of the List, display a warn label.
     */
    private void switchField(String fieldName) {
        fieldLayout.removeAllComponents();
        if (fieldMap.containsKey(fieldName)) {
            fieldLayout.addComponent(fieldMap.get(fieldName));
        } else {
            log.warn("{} not associated to a field. Nothing will be displayed", fieldName);
            fieldLayout.addComponent(new Label("No field define for the following selection : " + fieldName));
        }
    }

    /**
     * Called after initComponent().<br>
     * In addition of the datasource setting,
     * set the default field based on the stored or configured information.
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        selectField.setPropertyDataSource(newDataSource);
        switchField((String) newDataSource.getValue());
    }

    /**
     * Only validate the current selected field.
     */
    @Override
    public boolean isValid() {
        Component currentComponent = getSelectedComponent();
        if (currentComponent instanceof Field) {
            return ((Field) currentComponent).isValid();
        } else {
            return true;
        }
    }

    /**
     * Only get the error message from the current selected field.
     */
    @Override
    public ErrorMessage getErrorMessage() {
        Component currentComponent = getSelectedComponent();
        if (currentComponent instanceof AbstractComponent) {
            return ((AbstractComponent) currentComponent).getErrorMessage();
        } else {
            return null;
        }
    }

    /**
     * Mainly used for test purpose.
     */
    public Component getSelectedComponent() {
        return fieldLayout.getComponentCount() > 0 ? fieldLayout.getComponent(0) : null;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return (String) selectField.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException, ConversionException {
        selectField.setValue(newValue);
        switchField(newValue);
    }

    @Override
    public Property getPropertyDataSource() {
        return selectField.getPropertyDataSource();
    }
}
