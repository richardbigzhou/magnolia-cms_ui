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
import info.magnolia.ui.form.field.MultiField;
import info.magnolia.ui.form.field.definition.MultiFieldDefinition;
import info.magnolia.ui.form.field.property.PropertyHandler;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;


/**
 * Creates and initializes an multi-field based on a field definition.<br>
 * Multi-field basicaly handle: <br>
 * - Add remove Fields <br>
 * This field builder create a {@link ListProperty} based on the definition and set this property as <br>
 * Field property datasource.
 *
 * @param <T>
 */
public class MultiFieldFactory<T> extends AbstractFieldFactory<MultiFieldDefinition, List<T>> {

    private FieldFactoryFactory fieldFactoryFactory;
    private I18nContentSupport i18nContentSupport;
    private ComponentProvider componentProvider;

    public MultiFieldFactory(MultiFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    protected Field<List<T>> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        definition.setI18nBasename(getMessages().getBasename());

        MultiField<T> field = new MultiField<T>(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, item);
        // Set Caption
        field.setButtonCaptionAdd(getMessage(definition.getButtonSelectAddLabel()));
        field.setButtonCaptionRemove(getMessage(definition.getButtonSelectRemoveLabel()));

        return field;
    }

    /**
     * Do not link this field directly to an Item property but to the configured MultivalueHandler.<br>
     * The PropertyHandler has the responsibility to correctly retrieve and store the values used in the MultiField.
     */
    @Override
    protected PropertyHandler<?> initializePropertyHandler(Class<? extends PropertyHandler<?>> handlerClass, Class<?> type) {
        return this.componentProvider.newInstance(handlerClass, item, definition, componentProvider);
    }
}
