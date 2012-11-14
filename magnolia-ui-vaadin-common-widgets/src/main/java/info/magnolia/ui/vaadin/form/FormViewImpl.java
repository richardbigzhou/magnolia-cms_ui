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
package info.magnolia.ui.vaadin.form;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import info.magnolia.ui.vaadin.dialog.FormSection;
import org.vaadin.rpc.ServerSideHandler;

import java.util.List;

/**
 * FormViewImpl.
 */
public class FormViewImpl extends AbstractComponent implements ServerSideHandler, FormView {

    private Item itemDatasource;

    @Override
    public void setFormDescription(String description) {

    }

    @Override
    public void setCaption(String caption) {

    }

    @Override
    public void addFormSection(String tabName, FormSection inputFields) {

    }

    @Override
    public void addField(Field field) {

    }

    @Override
    public void showValidation(boolean isVisible) {

    }

    @Override
    public void setShowAllEnabled(boolean enabled) {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public List<Field> getFields() {
        return null;
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
        itemDatasource = newDataSource;
    }

    @Override
    public Item getItemDataSource() {
        return itemDatasource;
    }

    @Override
    public Object[] initRequestFromClient() {
        return new Object[0];
    }

    @Override
    public void callFromClient(String method, Object[] params) {

    }
}
