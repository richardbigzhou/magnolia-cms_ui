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
package info.magnolia.security.app.dialog.field.property;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * {@link info.magnolia.ui.form.field.property.PropertyHandler} implementation used for {@link info.magnolia.security.app.dialog.field.EnabledFieldFactory}.
 * 
 * @param <T>
 */
public class EnabledFieldTransformer<T> extends BasicTransformer<T> {

    public EnabledFieldTransformer(Item parent, ConfiguredFieldDefinition definition, Class<?> type) {
        super(parent, definition, (Class<T>) type);
    }

    @Override
    public T readFromItem() {
        Property old = relatedFormItem.getItemProperty("enabled");
        String stringValue = "true";
        if (old != null) {
            stringValue = old.toString();
        }
        DefaultProperty<T> prop = new DefaultProperty(Boolean.class, Boolean.parseBoolean(stringValue));
        relatedFormItem.removeItemProperty("enabled");
        relatedFormItem.addItemProperty("enabled", prop);
        return prop.getValue();
    }

}
