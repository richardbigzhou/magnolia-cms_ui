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
package info.magnolia.ui.form.field.property.multi;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.property.AbstractBaseHandler;
import info.magnolia.ui.form.field.property.PropertyHandler;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Multi values properties implementation of {@link PropertyHandler}.<br>
 * Store the list of values as Jcr Multi-property value.<br>
 * Retrieve the Jcr Multi-property value as a list.
 * 
 * @param <T> type of the element list.
 */
public class MultiValuesPropertyMultiHandler<T> extends AbstractBaseHandler<List<T>> implements PropertyHandler<List<T>> {


    @Inject
    public MultiValuesPropertyMultiHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider) {
        super(parent, definition, componentProvider);
    }

    @Override
    public void writeToDataSourceItem(List<T> newValue) {
        Property<List> property = getOrCreateProperty(List.class, null);
        property.setValue(new LinkedList<T>(newValue));
    }

    @Override
    public List<T> readFromDataSourceItem() {
        Property<List> property = getOrCreateProperty(List.class, null);
        if (property.getValue() == null) {
            property.setValue(new LinkedList<T>());
        }
        return property.getValue();
    }

}
