/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.contentapp.field;

import info.magnolia.ui.workbench.WorkbenchView;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * A base custom field allowing to display a {@link WorkbenchView} and a TextField.
 * <ul>
 * <li>Text field can be hidden or placed on top or button.
 * <li>This field is mainly used to perform some selection in a list and to put the selected value into the text input field.
 * </ul>
 */
public class TextAndContentViewField extends CustomField<String> {

    private WorkbenchView contentView;

    private VerticalLayout layout;

    private TextField textField;

    private boolean displayTextFieldOnTop;

    private boolean isTextFieldVisible;

    public TextAndContentViewField(boolean displayTextField, boolean displayTextFieldOnTop) {
        this.displayTextFieldOnTop = displayTextFieldOnTop;
        this.isTextFieldVisible = displayTextField;
        this.textField = new TextField();
        this.layout = new VerticalLayout();
    }

    @Override
    protected Component initContent() {
        addTextFieldToLayout(isTextFieldVisible);
        addStyleName("text-and-content");
        return layout;
    }

    /**
     * Set textField visible or not.
     */
    private void addTextFieldToLayout(boolean displayTextField) {
        if (!displayTextField) {
            textField.setVisible(false);
            return;
        }
        layout.addComponent(textField);
    }

    public TextField getTextField() {
        return this.textField;
    }

    public WorkbenchView getContentView() {
        return this.contentView;
    }

    /**
     * Set contentView, and Add it to the Layout.
     * Based on displayTextFieldOnTop, put it before or after the TextField.
     */
    public void setContentView(WorkbenchView contentView) {
        if (this.contentView != null) {
            layout.removeComponent(this.contentView.asVaadinComponent());
        }
        this.contentView = contentView;
        if (!displayTextFieldOnTop) {
            layout.addComponentAsFirst(this.contentView.asVaadinComponent());
        } else {
            layout.addComponent(this.contentView.asVaadinComponent());
        }
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException, ConversionException {
        textField.setValue(newValue);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        textField.setPropertyDataSource(newDataSource);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Property getPropertyDataSource() {
        return textField.getPropertyDataSource();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(null);
    }
}
