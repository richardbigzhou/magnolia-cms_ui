/**
 * This file Copyright (c) 2011-2015 Magnolia International
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

import info.magnolia.ui.form.field.definition.DateFieldDefinition;

import java.util.Date;

import com.vaadin.data.Item;
import com.vaadin.server.Sizeable;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Field;
import com.vaadin.ui.PopupDateField;

/**
 * Creates and initializes a date field based on a field definition.
 */
public class DateFieldFactory extends AbstractFieldFactory<DateFieldDefinition, Date> {

    public DateFieldFactory(DateFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    public Field<Date> createField() {
        Field<Date> field = super.createField();
        field.setWidth(Sizeable.SIZE_UNDEFINED, Unit.PIXELS);
        return field;
    }

    @Override
    protected Field<Date> createFieldComponent() {
        DateFieldDefinition definition = getFieldDefinition();
        PopupDateField popupDateField = new PopupDateField();

        String dateFormat = "";

        // set Resolution
        if (definition.isTime()) {
            popupDateField.setResolution(Resolution.MINUTE);
            dateFormat = definition.getDateFormat() + ":" + definition.getTimeFormat();
        } else {
            popupDateField.setResolution(Resolution.DAY);
            dateFormat = definition.getDateFormat();
        }

        popupDateField.setDateFormat(dateFormat);

        return popupDateField;
    }
}
