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
package info.magnolia.ui.form.field.definition;

import info.magnolia.ui.form.field.property.HandlerAwareProperty;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.form.field.property.composite.CompositeProperty;
import info.magnolia.ui.form.field.property.composite.SimplePropertyCompositeHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Definition used to configure a generic composit field.
 * This field is a composition of fields defined based on their {@ConfiguredFieldDefinition}.
 */

public class CompositeFieldDefinition extends ConfiguredFieldDefinition {

    private List<ConfiguredFieldDefinition> fields = new ArrayList<ConfiguredFieldDefinition>();
    private List<String> fieldsName;


    /**
     * Set default {@link PropertyBuilder}.
     */
    @SuppressWarnings("unchecked")
    public CompositeFieldDefinition() {
        PropertyBuilder propertyBuilder = new PropertyBuilder();
        propertyBuilder.setPropertyHandler((Class<? extends PropertyHandler<?>>) (Object) SimplePropertyCompositeHandler.class);
        propertyBuilder.setPropertyType((Class<? extends HandlerAwareProperty<?>>) (Object) CompositeProperty.class);
        setPropertyBuilder(propertyBuilder);
    }

    public List<ConfiguredFieldDefinition> getFields() {
        return fields;
    }

    public List<String> getFieldsName() {
        if (this.fieldsName == null) {
            initFieldsName();
        }
        return fieldsName;
    }

    public void setFields(List<ConfiguredFieldDefinition> fields) {
        this.fields = fields;
        initFieldsName();
    }


    private void initFieldsName() {
        fieldsName = new LinkedList<String>();
        for (ConfiguredFieldDefinition definition : fields) {
            fieldsName.add(definition.getName());
        }
    }
}
