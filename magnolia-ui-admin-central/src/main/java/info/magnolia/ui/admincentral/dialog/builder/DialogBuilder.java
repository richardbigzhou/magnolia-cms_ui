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

import info.magnolia.ui.admincentral.field.DialogField;
import info.magnolia.ui.admincentral.field.builder.FieldTypeProvider;
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;
import info.magnolia.ui.widget.dialog.DialogView;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;

/**
 * Builder for Dialogs.
 */
public class DialogBuilder {

    static final String FIELD_CONTAINER_STYLE_NAME = "field-container";
    static final String FIELD_STYLE_NAME = "field";

    /**
     * @return DialogView populated with values from DialogDevinition and Item.
     */
    public DialogView build(FieldTypeProvider fieldTypeBuilder, DialogDefinition dialogDefinition, Item item, DialogView view) {


        view.setItemDataSource(item);

        if (StringUtils.isNotBlank(dialogDefinition.getDescription())) {
            view.setDescription(dialogDefinition.getDescription());
        }

        for (TabDefinition tabDefinition : dialogDefinition.getTabs()) {
            String tabName = tabDefinition.getName();

            CssLayout fieldContainer = new CssLayout();
            fieldContainer.setStyleName(FIELD_CONTAINER_STYLE_NAME);

            for (FieldDefinition fieldDefinition : tabDefinition.getFields()) {

                CssLayout fieldLayout = new CssLayout();
                fieldLayout.setStyleName(FIELD_STYLE_NAME);

                //Create the DialogField
                DialogField fieldType = fieldTypeBuilder.create(fieldDefinition, fieldDefinition, item);
                //Get the Vaadin  Field
                Field field = fieldType.getField();

                fieldLayout.addComponent(field);
                fieldContainer.addComponent(fieldLayout);

                view.addField(field);
            }

            view.addTab(fieldContainer, tabName);

        }

        for (DialogActionDefinition action : dialogDefinition.getActions()) {
            view.addAction(action.getName(), action.getLabel());
        }
        return view;
    }

}

