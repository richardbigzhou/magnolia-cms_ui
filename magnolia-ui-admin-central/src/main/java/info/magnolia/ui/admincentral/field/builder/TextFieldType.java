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
package info.magnolia.ui.admincentral.field.builder;

import info.magnolia.ui.model.dialog.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.dialog.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.RegexpValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.ValidatorDefinition;
import info.magnolia.ui.model.field.definition.FieldTypeDefinition;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * TextField.
 *
 */
public class TextFieldType extends FieldTypeBase {

    private final String TEXTFIELD_STYLE_NAME = "textfield";

    public TextFieldType(FieldTypeDefinition definition) {
        super(definition);
    }


    @Override
    public Field build(FieldDefinition fieldDefinition) {
        Field field = new TextField();
        field.setCaption(fieldDefinition.getLabel());

        final String label = fieldDefinition.getLabel();
        field.setCaption(label);
        field.setStyleName(TEXTFIELD_STYLE_NAME);
        addValidators(fieldDefinition, field);

        return field;
    }

    static void addValidators(FieldDefinition fieldDefinition, Field input) {
        Validator vaadinValidator = null;
        for (ValidatorDefinition current: ((ConfiguredFieldDefinition) fieldDefinition).getValidators()) {
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
    }

}
