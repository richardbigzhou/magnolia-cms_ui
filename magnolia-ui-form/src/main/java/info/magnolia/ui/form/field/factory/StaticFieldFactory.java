/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.ui.form.field.StaticField;
import info.magnolia.ui.form.field.definition.StaticFieldDefinition;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Creates and initializes an static field definition.
 * 
 * @param <D> type of definition
 */
public class StaticFieldFactory<D extends StaticFieldDefinition> extends AbstractFieldFactory<D, Object> {

    @Inject
    public StaticFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
    }

    /**
     * @deprecated since 5.4.7 - use {@link #StaticFieldFactory(StaticFieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public StaticFieldFactory(D definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Field<Object> createFieldComponent() {
        StaticField field = new StaticField(createFieldValue());
        return field;
    }

    /**
     * Create the value displayed into the static field.
     */
    public String createFieldValue() {
        return definition.getValue();
    }

    /**
     * Do not set datasource for a Static field.
     */
    @Override
    public void setPropertyDataSourceAndDefaultValue(Property property) {
        // do nothing specific
    }

}
