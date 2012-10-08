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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.admincentral.dialog.Dialog;
import info.magnolia.ui.admincentral.dialog.DialogTab;
import info.magnolia.ui.admincentral.field.FieldBuilder;
import info.magnolia.ui.admincentral.field.builder.DialogFieldFactory;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.tab.definition.TabDefinition;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.dialog.FormDialogView;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Field;

/**
 * Builder for Dialogs.
 */
public class DialogBuilder {
    /**
     * @return DialogView populated with values from DialogDefinition and Item.
     */
    public DialogView buildFormDialog(DialogFieldFactory dialogFieldFactory, DialogDefinition dialogDefinition, Item item, FormDialogView view) {

        final Dialog dialog = new Dialog(dialogDefinition);
        view.setItemDataSource(item);

        final String description = dialogDefinition.getDescription();
        final String label = dialogDefinition.getLabel();
        final String basename = dialogDefinition.getI18nBasename();

        if (StringUtils.isNotBlank(description)) {
            String i18nDescription = MessagesUtil.getWithDefault(description, description, basename);
            view.setDialogDescription(i18nDescription);
        }
        
        if (StringUtils.isNotBlank(label)) {
            String i18nLabel = MessagesUtil.getWithDefault(label, label, basename);
            view.setCaption(i18nLabel);
        }

        for (TabDefinition tabDefinition : dialogDefinition.getTabs()) {
            final DialogTab tab = new DialogTab(tabDefinition);
            tab.setParent(dialog);
            for (final FieldDefinition fieldDefinition : tabDefinition.getFields()) {
                final FieldBuilder dialogField = dialogFieldFactory.create(fieldDefinition, item);
                if (dialogField != null) {
                    dialogField.setParent(tab);
                    final Field field = dialogField.getField();
                    if (field instanceof AbstractComponent) {
                        ((AbstractComponent)field).setImmediate(true);
                    }
                    tab.addField(field);
                    if(StringUtils.isNotBlank(fieldDefinition.getDescription())) {
                        tab.setComponentHelpDescription(field, fieldDefinition.getDescription());
                    }
                    view.addField(field);
                } //This can happen in case of extends/override. FieldDefinition is ConfiguredFieldDefinition and of course no builder is linked to this.
            }
            view.addDialogSection(tab.getMessage(tabDefinition.getLabel()), tab.getContainer());
        }
        
        view.setShowAllEnabled(dialogDefinition.getTabs().size() > 1);
        return view;
    }
}

