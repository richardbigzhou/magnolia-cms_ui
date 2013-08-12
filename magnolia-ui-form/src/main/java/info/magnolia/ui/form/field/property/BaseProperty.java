/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.form.field.property;


import com.vaadin.data.util.ObjectProperty;

/**
 * .
 * 
 * @param <T>
 */

public class BaseProperty<T> extends ObjectProperty<T> implements HasPropertyHandler<T> {

    protected PropertyHandler<T> handler;


    public BaseProperty(PropertyHandler<T> handler, Class<T> type) {
        super(handler.readFromDataSourceItem(), type);
        this.handler = handler;
    }

    public BaseProperty(PropertyHandler<T> handler) {
        super(handler.readFromDataSourceItem());
        this.handler = handler;
    }

    @Override
    public void setValue(T newValue) throws com.vaadin.data.Property.ReadOnlyException {
        super.setValue(newValue);
        if (handler != null) {
            handler.writeToDataSourceItem(newValue);
        }
    }

    @Override
    public T getValue() {
        return super.getValue();
    }

    @Override
    public PropertyHandler<T> getHandler() {
        return handler;
    }

    public boolean hasI18NSupport() {
        return handler.hasI18NSupport();
    }

    /**
     * In case of i18n change, Reload the Value based on the Handler.
     */
    public void fireI18NValueChange() {
        setValue(handler.readFromDataSourceItem());
        super.fireValueChange();
    }
}
