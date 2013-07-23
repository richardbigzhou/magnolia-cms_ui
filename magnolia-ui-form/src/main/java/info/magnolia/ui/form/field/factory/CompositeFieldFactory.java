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
package info.magnolia.ui.form.field.factory;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.CompositeField;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.property.multi.MultiHandler;
import info.magnolia.ui.form.field.property.multi.MultiProperty;
import info.magnolia.ui.form.field.property.multi.SubNodesMultiHandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Field;

/**
 * .
 * @param <D>
 */
public class CompositeFieldFactory<D extends FieldDefinition> extends AbstractFieldFactory<CompositeFieldDefinition, PropertysetItem> {

    private static final Logger log = LoggerFactory.getLogger(CompositeFieldFactory.class);
    private FieldFactoryFactory fieldFactoryFactory;
    private I18nContentSupport i18nContentSupport;
    private ComponentProvider componentProvider;

    public CompositeFieldFactory(CompositeFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        definition.setI18nBasename(getMessages().getBasename());

        CompositeField field = new CompositeField(definition, fieldFactoryFactory, i18nContentSupport, componentProvider);
        return field;
    }

    @Override
    protected Property<?> getOrCreateProperty() {
        Class<? extends MultiHandler> multiDelegate = null;
        String itemName = definition.getName();
        List<String> propertyNames = definition.getFieldsName();
        // Get configured MultiValueHandler class
        if (definition.getSaveModeType() != null && definition.getSaveModeType().getMultiHandlerClass() != null) {
            multiDelegate = definition.getSaveModeType().getMultiHandlerClass();
        } else {
            multiDelegate = SubNodesMultiHandler.class;
            log.warn("No MultiHandler defined for this CompositField Field definition. Default one will be taken: '{}'", SubNodesMultiHandler.class.getSimpleName());
        }
        MultiHandler multiHandler = this.componentProvider.newInstance(multiDelegate, item, itemName, propertyNames);
        MultiProperty multiProperty = new MultiProperty(multiHandler);
        return multiProperty;
    }

}
