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
package info.magnolia.ui.form.field.factory;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * Creates and initializes a checkBox field based on a field definition.
 */
public class HiddenFieldFactory extends AbstractFieldFactory<HiddenFieldDefinition, String> {

    @Inject
    public HiddenFieldFactory(HiddenFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18NAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18NAuthoringSupport);
    }

    /**
     * @deprecated since 5.4.7 - use {@link #HiddenFieldFactory(HiddenFieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public HiddenFieldFactory(HiddenFieldDefinition definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Field<String> createFieldComponent() {
        TextField field = new TextField();
        field.setVisible(false);
        return field;
    }

    /**
     * Make sure default value is always used for input hidden if current value is null.
     */
    @Override
    public void setPropertyDataSourceAndDefaultValue(Property<?> property) {
        super.setPropertyDataSourceAndDefaultValue(property);

        if (property.getValue() == null) {
            setPropertyDataSourceDefaultValue(property);
        }
    }
}
