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
package info.magnolia.ui.admincentral.dialog.builder;

import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;
import info.magnolia.ui.widget.dialog.DialogView;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

/**
 * DialogBuilder.
 *
 * @author ejervidalo
 */
public class DialogBuilder {

    /**
     * @param dialogDefinition
     * @param dialogPresenter
     * @return
     */
    public DialogView build(DialogDefinition dialogDefinition, Item item, DialogView view) {


        view.setItemDataSource(item);

        for (TabDefinition tabDefinition : dialogDefinition.getTabs()) {
            String tabName = tabDefinition.getName();
            VerticalLayout inputFields = new VerticalLayout();

            for (FieldDefinition field : tabDefinition.getFields()) {
                CssLayout fieldContainer = new CssLayout();
                fieldContainer.setStyleName("field-container");

                Field input = FieldBuilder.build(field);

                bindPropertyToField(field.getName(), item.getItemProperty(field.getName()), input);

                fieldContainer.addComponent(input);
                inputFields.addComponent(fieldContainer);
                view.addField(item.getItemProperty(field.getName()), input);
            }

            view.addTab(inputFields, tabName);

        }

        for (DialogActionDefinition action : dialogDefinition.getActions()) {
            view.addAction(action.getName(), action.getLabel());
        }
        return view;

    }

    protected void bindPropertyToField(final Object propertyId,
            final Property property, final Field field) {
        // check if field has a property that is Viewer set. In that case we
        // expect developer has e.g. PropertyFormatter that he wishes to use and
        // assign the property to the Viewer instead.
        boolean hasFilterProperty = field.getPropertyDataSource() != null
                && (field.getPropertyDataSource() instanceof Property.Viewer);
        if (hasFilterProperty) {
            ((Property.Viewer) field.getPropertyDataSource())
                    .setPropertyDataSource(property);
        } else {
            field.setPropertyDataSource(property);
        }
    }
}

