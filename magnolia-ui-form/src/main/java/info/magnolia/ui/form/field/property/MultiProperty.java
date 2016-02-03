/**
 * This file Copyright (c) 2013-2016 Magnolia International
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


import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.util.ObjectProperty;

/**
 * {@link ObjectProperty} implementation used in order to handle MultiProperty type.<br>
 * This property is set as {@link com.vaadin.ui.Field#setPropertyDataSource(com.vaadin.data.Property)} and handle List of Object.<br>
 * {@link MultiValueHandler} is responsible to retrieve and set this List to the related Property.
 */
public class MultiProperty extends ObjectProperty<List<String>> {

    private MultiValueHandler handler;

    public MultiProperty(MultiValueHandler delegate) {
        super(new ArrayList<String>());
        this.handler = delegate;
        setValue(this.handler.getValue());
    }

    @Override
    public void setValue(List<String> newValue) throws com.vaadin.data.Property.ReadOnlyException {
        super.setValue(newValue);
        if (handler != null) {
            handler.setValue(newValue);
        }
    }

    @Override
    public List<String> getValue() {
        return super.getValue();
    }
}
