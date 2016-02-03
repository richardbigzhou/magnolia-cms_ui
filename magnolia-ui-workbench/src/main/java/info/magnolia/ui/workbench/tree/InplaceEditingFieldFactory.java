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
package info.magnolia.ui.workbench.tree;

import java.util.Locale;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.AbstractStringToNumberConverter;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;

/**
 * The InplaceEditingFieldFactory is responsible for creating input fields in table cells for inplace-editing.
 * Compared to a standard Vaadin {@link TableFieldFactory}, this one only creates an input field in one cell at a time,
 * i.e. corresponding to a single combination of itemId and container's propertyId.<br />
 * <br/>
 * It also expects a field {@link BlurListener} to be set as a hook to persist changes.
 */
public class InplaceEditingFieldFactory implements TableFieldFactory {

    private Object editingItemId;
    private Object editingPropertyId;

    private Field<?> field;
    private BlurListener fieldBlurListener;

    /**
     * @return the id of the item currently being edited
     */
    public Object getEditingItemId() {
        return editingItemId;
    }

    /**
     * @return the id of the item's property currently being edited
     */
    public Object getEditingPropertyId() {
        return editingPropertyId;
    }

    public Field<?> getField() {
        return field;
    }

    /**
     * Sets the item and property to edit.
     */
    public void setEditing(Object editingItemId, Object editingPropertyId) {
        this.editingItemId = editingItemId;
        this.editingPropertyId = editingPropertyId;
        if (editingItemId == null || editingPropertyId == null) {
            field = null;
        }
    }

    /**
     * Sets the blur listener that should react when leaving the inplace-editing field.
     */
    public void setFieldBlurListener(BlurListener fieldBlurListener) {
        this.fieldBlurListener = fieldBlurListener;
    }

    @Override
    public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
        // add field only for selected row/column.
        if (itemId.equals(editingItemId) && propertyId.equals(editingPropertyId)) {
            Field<?> field = createFieldAndRegister(container, itemId, propertyId);
            // register component on the table (only for partial updates)
            // uiContext.registerComponent(field);
            this.field = field;
            return field;
        }
        return null;
    }

    /**
     * For partial updates to work, we need to perform a dry run to attach the component to the table beforehand,
     * i.e. before it is actually requested at paint phase by the table.
     */
    private Field<?> createFieldAndRegister(Container container, Object itemId, Object propertyId) {

        Property<?> containerProperty = container.getContainerProperty(itemId, propertyId);
        // the previous call can return null, i.e. when clicking on an empty cell of a node row (i.e. /config/server and then the "value" cell)
        if (containerProperty == null) {
            return null;
        }

        Class<?> type = containerProperty.getType();
        Field<?> field = createFieldByPropertyType(type);
        if (field != null) {
            field.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
            field.setSizeFull();
        }

        // set TextField listeners
        if (field instanceof AbstractTextField) {
            final AbstractTextField tf = (AbstractTextField) field;
            tf.addBlurListener(fieldBlurListener);
            tf.focus();

            tf.addValueChangeListener(new ValueChangeListener() {

                @Override
                public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                    final Object text = event.getProperty().getValue();
                    if (text instanceof String) {
                        tf.selectAll();
                    }
                    tf.removeValueChangeListener(this);
                }
            });
        }
        return field;
    }

    private Field<?> createFieldByPropertyType(Class<?> type) {
        if (type == null) {
            return null;
        }
        Field<?> field = new TextField();
        // FIXME MGNLUI-1855 To remove once Vaadin 7.2 will be used. Currently we need to assign converter for properties with type Long because otherwise Vaadin assigns incompatible StringToNumberConverter.
        if (Long.class.equals(type)) {
            ((AbstractTextField) field).setConverter(new StringToLongConverter());
        }
        return field;
    }

    /**
     * The StringToLongConverter.<br>
     * MGNLUI-1855 This should be handled by vaadin, but StringToNumberConverter throws conversion exception when used
     * with a Long property in Vaadin 7.1. This should be fixed, unfortunately not before 7.2, so we need that converter
     * for the time being.<br>
     * As a result, this class will have a short life span, this is why it is kept private and deprecated.
     */
    @Deprecated
    static class StringToLongConverter extends AbstractStringToNumberConverter<Long> {
        // FIXME MGNLUI-1855 To remove once Vaadin 7.2 will be used.
        @Override
        public Long convertToModel(String value, Class<? extends Long> targetType, Locale locale) throws ConversionException {
            Number n = convertToNumber(value, targetType, locale);
            return n == null ? null : n.longValue();
        }

        @Override
        public Class<Long> getModelType() {
            return Long.class;
        }
    }

}