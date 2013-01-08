/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.ui.admincentral.field.translator.Base64Translator;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;

/**
 * A base custom field displaying one ore two Password Fields.
 * Implement the Logic to check the validity of typed passwords.
 */
public class PasswordFields extends CustomField<String> {

    private PasswordField passwordField;
    private PasswordField verificationField;
    private boolean verification = false;
    private String verificationErrorMessage;
    private VerticalLayout layout;
    private Base64Translator translator;
    private String verificationMessage;

    /**
     * Create a {@link CustomField} based on a {@link VerticalLayout}.
     * The layout is composed by:
     * - {@link PasswordField}.
     * if verification:
     * - {@link Label} (verificationMessage).
     * - {@link PasswordField}.
     */
    public PasswordFields(boolean verification, String verificationMessage, String verificationErrorMessage, boolean encode) {
        layout = new VerticalLayout();
        passwordField = new PasswordField();
        this.verification = verification;
        this.verificationErrorMessage = verificationErrorMessage;
        this.verificationMessage = verificationMessage;
        // Initialize encoder
        if (encode) {
            this.translator = new Base64Translator();
            passwordField.setConverter(translator);
        }
        initContent();
    }

    @Override
    protected Component initContent() {
        // Init layout
        layout.addComponent(passwordField);
        if (this.verification) {
            Label msg = new Label(verificationMessage);
            layout.addComponent(msg);
            verificationField = new PasswordField();
            layout.addComponent(verificationField);
        }
        return layout;
    }

    public VerticalLayout getVerticalLayout() {
        return this.layout;
    }

    /**
     * Check if both fields are equals.
     */
    @Override
    public void validate() throws InvalidValueException {
        super.validate();
        if (this.verification) {
            if (StringUtils.isBlank(passwordField.getValue().toString()) || StringUtils.isBlank(verificationField.getValue().toString())) {
                throw new InvalidValueException(verificationErrorMessage);
            }
            if (!passwordField.getValue().toString().equals(verificationField.getValue().toString())) {
                throw new InvalidValueException(verificationErrorMessage);
            }
        }
    }

    @Override
    public boolean isValid() {
        if (super.isValid()) {
            try {
                this.validate();
                return true;
            } catch (InvalidValueException ive) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return passwordField.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException {
        passwordField.setValue(newValue);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        passwordField.setPropertyDataSource(newDataSource);
        if (this.verification) {
            verificationField.setValue(String.valueOf(newDataSource.getValue()));
        }
    }

    @Override
    public Property<?> getPropertyDataSource() {
        return passwordField.getPropertyDataSource();
    }
}
