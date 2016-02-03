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
package info.magnolia.ui.form.field;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * A field wrapper for a single CheckBox, supporting the FormLayout-managed (left) caption.
 */
public class CheckBoxField extends CustomField<Boolean> {

    private CheckBox checkBox;

    public CheckBoxField() {
        checkBox = new CheckBox();
    }

    @Override
    protected Component initContent() {
        return checkBox;
    }

    @Override
    public Boolean getValue() {
        return checkBox.getValue();
    }

    @Override
    public void setValue(Boolean newValue) throws ReadOnlyException {
        checkBox.setValue(newValue);
    }

    /**
     * Use this setter for the checkbox caption on the right-hand side; the regular field caption is left for the FormLayout.
     */
    public void setCheckBoxCaption(String caption) {
        checkBox.setCaption(caption);
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

    @Override
    public void addValueChangeListener(Property.ValueChangeListener listener) {
        checkBox.addValueChangeListener(listener);
    }

}
