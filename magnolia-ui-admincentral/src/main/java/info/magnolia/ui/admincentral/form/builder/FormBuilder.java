/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.form.builder;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.admincentral.field.FieldBuilder;
import info.magnolia.ui.admincentral.field.builder.FieldFactory;
import info.magnolia.ui.admincentral.form.Form;
import info.magnolia.ui.admincentral.form.FormItem;
import info.magnolia.ui.admincentral.form.FormTab;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.form.definition.FormDefinition;
import info.magnolia.ui.model.form.definition.TabDefinition;
import info.magnolia.ui.vaadin.form.FormView;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Field;

/**
 * Builder for forms.
 */
public class FormBuilder {

    /**
     * @return FormView populated with values from FormDefinition and Item.
     */
    public FormView buildForm(FieldFactory fieldFactory, FormDefinition formDefinition, Item item, FormView view, FormItem parent) {
        final Form form = new Form(formDefinition);
        form.setParent(parent);
        view.setItemDataSource(item);

        final String description = formDefinition.getDescription();
        final String label = formDefinition.getLabel();
        final String basename = formDefinition.getI18nBasename();

        if (StringUtils.isNotBlank(description)) {
            String i18nDescription = MessagesUtil.getWithDefault(description, description, basename);
            view.setFormDescription(i18nDescription);
        }

        if (StringUtils.isNotBlank(label)) {
            String i18nLabel = MessagesUtil.getWithDefault(label, label, basename);
            view.setCaption(i18nLabel);
        }

        for (TabDefinition tabDefinition : formDefinition.getTabs()) {
            final FormTab tab = new FormTab(tabDefinition);
            tab.setParent(form);
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {
                final FieldBuilder formField = fieldFactory.create(fieldDefinition, item);
                if (formField != null) {
                    formField.setParent(tab);
                    final Field field = formField.getField();
                    if (field instanceof AbstractComponent) {
                        ((AbstractComponent) field).setImmediate(true);
                    }
                    tab.addField(field);
                    if (StringUtils.isNotBlank(fieldDefinition.getDescription())) {
                        tab.setComponentHelpDescription(field, fieldDefinition.getDescription());
                    }
                    view.addField(field);
                }
            }
            view.addFormSection(tab.getMessage(tabDefinition.getLabel()), tab.getContainer());
        }
        view.setShowAllEnabled(formDefinition.getTabs().size() > 1);

        return view;
    }
}
