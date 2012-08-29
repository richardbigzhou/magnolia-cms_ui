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
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.RegexpValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.ValidatorDefinition;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.tab.definition.TabDefinition;
import info.magnolia.ui.widget.dialog.MagnoliaDialogView;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.RegexpValidator;
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
    public MagnoliaDialogView build(DialogFieldFactory dialogFieldFactory, DialogDefinition dialogDefinition, Item item, MagnoliaDialogView view) {

        Dialog dialog = new Dialog(dialogDefinition);
        view.setItemDataSource(item);

        final String description = dialogDefinition.getDescription();
        final String label = dialogDefinition.getLabel();
        final String basename = dialogDefinition.getI18nBasename();

        if (StringUtils.isNotBlank(description)) {
            String i18nDescription = MessagesUtil.getWithDefault(description, description, basename);
            view.setDescription(i18nDescription);
        }
        if (StringUtils.isNotBlank(label)) {
            String i18nLabel = MessagesUtil.getWithDefault(label, label, basename);
            view.setCaption(i18nLabel);
        }

        for (TabDefinition tabDefinition : dialogDefinition.getTabs()) {
            DialogTab tab = new DialogTab(tabDefinition);
            tab.setParent(dialog);

            for (FieldDefinition fieldDefinition : tabDefinition.getFields()) {

                // Create the DialogField
                FieldBuilder dialogField = dialogFieldFactory.create(fieldDefinition, item);
                dialogField.setParent(tab);

                // Get the Vaadin Field
                Field field = dialogField.getField();

                if (field instanceof AbstractComponent) {
                    ((AbstractComponent)field).setImmediate(true);
                }
                //Add Validation
                setRestriction(fieldDefinition, field);

                tab.addField(field);
                //Set Help
                if(StringUtils.isNotBlank(fieldDefinition.getDescription())) {
                    //TODO EHE SCRUM-1344 Add i18n to Dialog/Tab definition.
                    tab.setComponentHelpDescription(field, tab.getMessage(fieldDefinition.getDescription()));
                }
                view.addField(field);
            }

            view.addTab(tab.getContainer(), tab.getMessage(tabDefinition.getLabel()));
        }

        if(dialogDefinition.getTabs().size() <=1) {
            view.setShowAllEnabled(false);
        } else {
            view.setShowAllEnabled(true);
        }

        for (DialogActionDefinition action : dialogDefinition.getActions()) {
            view.addAction(action.getName(), action.getLabel());
        }
        return view;
    }


    /**
     * Set all restrictions linked to a field. Add:
     *   Validation rules
     *   Mandatory field
     *   SaveInfo property
     */
    private void setRestriction(FieldDefinition fieldDefinition, Field input) {

        Validator vaadinValidator = null;
        for (ValidatorDefinition current: fieldDefinition.getValidators()) {
            // TODO dlipp - this is what was defined for Sprint III. Of course this has to be enhanced later - when we have a better picture of how we want to validate.
            if (current instanceof EmailValidatorDefinition) {
                EmailValidatorDefinition def = (EmailValidatorDefinition) current;
                vaadinValidator = new EmailValidator(def.getErrorMessage());
            } else if (current instanceof RegexpValidatorDefinition) {
                RegexpValidatorDefinition def = (RegexpValidatorDefinition) current;
                vaadinValidator = new RegexpValidator(def.getPattern(), def.getErrorMessage());
            }

            if (vaadinValidator != null) {
                input.addValidator(vaadinValidator);
            }
        }

        if(fieldDefinition.isRequired()) {
            input.setRequired(true);
            input.setRequiredError(fieldDefinition.getRequiredErrorMessage());
        }
    }
}

