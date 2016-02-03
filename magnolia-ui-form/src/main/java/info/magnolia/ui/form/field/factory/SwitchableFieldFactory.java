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
import info.magnolia.ui.form.field.SwitchableField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.StaticFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.composite.DelegatingCompositeFieldTransformer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(SwitchableFieldFactory.class);
    private FieldFactoryFactory fieldFactoryFactory;
    private ComponentProvider componentProvider;
    private final I18NAuthoringSupport i18nAuthoringSupport;

    @Inject
    public SwitchableFieldFactory(SwitchableFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nAuthoringSupport = i18nAuthoringSupport;
    }

    /**
     * @deprecated since 5.3.5 removing i18nContentSupport dependency (actually unused way before that). Besides, fields should use i18nAuthoringSupport for internationalization.
     */
    @Deprecated
    public SwitchableFieldFactory(SwitchableFieldDefinition definition, Item relatedFieldItem, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider) {
        this(definition, relatedFieldItem, fieldFactoryFactory, componentProvider, componentProvider.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    protected Field<PropertysetItem> createFieldComponent() {
        // FIXME change i18n setting : MGNLUI-1548
        definition.setI18nBasename(getMessages().getBasename());

        // create the select field definition
        if (!containsSelectFieldDefinition()) {
            definition.addField(createSelectFieldDefinition());
        }

        SwitchableField field = new SwitchableField(definition, fieldFactoryFactory, componentProvider, item, i18nAuthoringSupport);
        return field;
    }

    /**
     * Create a new Instance of {@link Transformer}.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        // fieldNames list is unmodifiable, ensure safe usage in transformers (e.g. MailSecurityTransformer)
        List<String> propertyNames = new ArrayList<String>(definition.getFieldNames());
        if (!propertyNames.contains(definition.getName())) {
            propertyNames.add(definition.getName());
        }
        return this.componentProvider.newInstance(transformerClass, item, definition, PropertysetItem.class, propertyNames);
    }

    /**
     * @return true if the select field definition was already initialized.
     */
    private boolean containsSelectFieldDefinition() {
        for (ConfiguredFieldDefinition fieldDefinition : definition.getFields()) {
            if (StringUtils.equals(fieldDefinition.getName(), definition.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@link SelectFieldDefinition} initialized based on the {@link SwitchableFieldDefinition#getOptions()} and relevant options. <br>
     * In case of exception, return a {@link StaticFieldDefinition} containing a warn message.
     */
    protected ConfiguredFieldDefinition createSelectFieldDefinition() {
        try {
            SelectFieldDefinition selectDefinition = null;
            // Create the correct definition class
            String layout = "horizontal";
            if (definition.getSelectionType().equals("radio")) {
                selectDefinition = new OptionGroupFieldDefinition();
                if (definition.getLayout().equals(Layout.vertical)) {
                    layout = "vertical";
                }
            } else {
                selectDefinition = new SelectFieldDefinition();
            }
            // Copy options to the newly created select definition. definition
            selectDefinition.setOptions(definition.getOptions());
            selectDefinition.setTransformerClass(null);
            selectDefinition.setRequired(false);
            selectDefinition.setSortOptions(false);
            selectDefinition.setStyleName(layout);
            selectDefinition.setName(definition.getName());

            if (definition.isI18n() && definition.getTransformerClass().isAssignableFrom(DelegatingCompositeFieldTransformer.class)) {
                selectDefinition.setI18n(definition.isI18n());
            }
            return selectDefinition;
        } catch (Exception e) {
            log.warn("Coudn't create the select field.", e.getMessage());
            StaticFieldDefinition definition = new StaticFieldDefinition();
            definition.setName(this.definition.getName());
            definition.setValue("Select definition not correctly initialised. Please check your field configuration");
            return definition;
        }
    }

}
