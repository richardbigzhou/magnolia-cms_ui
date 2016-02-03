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
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.CompositeField;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Field;

/**
 * Factory used to initialize a {@link CompositeField}.
 *
 * @param <D> the field definition type — has to inherit from {@link CompositeFieldDefinition}.
 */
public class CompositeFieldFactory<D extends CompositeFieldDefinition> extends AbstractFieldFactory<D, PropertysetItem> {

    private static final Logger log = LoggerFactory.getLogger(CompositeFieldFactory.class);
    private FieldFactoryFactory fieldFactoryFactory;
    private ComponentProvider componentProvider;
    private final I18NAuthoringSupport i18nAuthoringSupport;

    @Inject
    public CompositeFieldFactory(D definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nAuthoringSupport = i18nAuthoringSupport;
    }

    /**
     * @deprecated since 5.3.5 removing i18nContentSupport dependency (actually unused way before that). Besides, fields should use i18nAuthoringSupport for internationalization.
     */
    @Deprecated
    public CompositeFieldFactory(D definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        this(definition, relatedFieldItem, fieldFactoryFactory, componentProvider, componentProvider.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        definition.setI18nBasename(getMessages().getBasename());

        // we do not support composite fields themselves to be required. Definition is overwritten here. Can't set it on the field after its creation cause otherwise the required asterisk is displayed.
        if (definition.isRequired()) {
            log.warn("Definition of the composite field named [{}] is configured as required which is not supported (it is possible to configure the sub-fields as required though).", definition.getName());
            definition.setRequired(false);
        }
        CompositeField field = new CompositeField(definition, fieldFactoryFactory, componentProvider, item, i18nAuthoringSupport);
        return field;
    }

    /**
     * Create a new Instance of {@link Transformer}.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        List<String> propertyNames = definition.getFieldNames();
        return this.componentProvider.newInstance(transformerClass, item, definition, PropertysetItem.class, propertyNames);
    }
}
