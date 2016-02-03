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
package info.magnolia.ui.form.field.factory;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.SwitchableField;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;

import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Field;

/**
 * Creates a @ link SwitchableField} based on definition.<br>
 * {@ link SwitchableField} has two components: <br>
 * - A select section configured based on the Options list of the definition<br>
 * - A field section configured based on the Fields list of the definition<br>
 * The link between select and fields is based on the association of: <br>
 * - The String property defined into the value property of the definition (value = date) <br>
 * and<br>
 * - The Field name defined into the Fields set (Date field named date).
 *
 * @param <D> definition type
 */
public class SwitchableFieldFactory<D extends FieldDefinition> extends AbstractFieldFactory<SwitchableFieldDefinition, PropertysetItem> {

    private FieldFactoryFactory fieldFactoryFactory;
    private I18nContentSupport i18nContentSupport;
    private ComponentProvider componentProvider;

    @Inject
    public SwitchableFieldFactory(SwitchableFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        definition.setI18nBasename(getMessages().getBasename());

        SwitchableField field = new SwitchableField(definition, fieldFactoryFactory, i18nContentSupport, componentProvider, item);
        return field;
    }


    /**
     * Create a new Instance of {@link Transformer}.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        List<String> propertyNames = definition.getFieldsName();
        return this.componentProvider.newInstance(transformerClass, item, definition, PropertysetItem.class, propertyNames);
    }

}
