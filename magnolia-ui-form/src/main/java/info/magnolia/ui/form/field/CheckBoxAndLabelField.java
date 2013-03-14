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
package info.magnolia.ui.form.field;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * A Base Custom Field composed from a checkBox and Label.
 */
public class CheckBoxAndLabelField extends CustomField<Boolean> {

    private CheckBox checkBox;
    private Label label;

    public CheckBoxAndLabelField() {
        checkBox = new CheckBox();
        label = new Label("", Label.CONTENT_XHTML);

    }

    @Override
    protected Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.addComponent(checkBox);
        layout.addComponent(label);
        layout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        return layout;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public Boolean getValue() {
        return checkBox.getValue();
    }

    @Override
    public void setValue(Boolean newValue) throws ReadOnlyException {
        checkBox.setValue(newValue);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        checkBox.setPropertyDataSource(newDataSource);
    }

    @Override
    public Property<Boolean> getPropertyDataSource() {
        return checkBox.getPropertyDataSource();
    }

    @Override
    public Class<? extends Boolean> getType() {
        return Boolean.class;
    }
}
