/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.form.AbstractFormItem;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.UndefinedPropertyType;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.form.validator.definition.FieldValidatorDefinition;
import info.magnolia.ui.form.validator.factory.FieldValidatorFactory;
import info.magnolia.ui.form.validator.registry.FieldValidatorFactoryFactory;
import info.magnolia.ui.vaadin.integration.ItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;

/**
 * Abstract FieldFactory implementations. This class handle all common attributes defined in {@link FieldDefinition} and binds Vaadin {@link Field} instances created
 * by subclasses to the {@link Property} they will be reading and writing to.
 *
 * @param <D> definition type
 * @param <T> field value type
 */
public abstract class AbstractFieldFactory<D extends FieldDefinition, T> extends AbstractFormItem implements FieldFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractFieldFactory.class);
    protected Item item;
    private UiContext uiContext;
    private I18NAuthoringSupport i18NAuthoringSupport;

    protected Field<T> field;

    protected D definition;
    private FieldValidatorFactoryFactory fieldValidatorFactoryFactory;
    private ComponentProvider componentProvider;

    private Locale locale;

    @Inject
    public AbstractFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18NAuthoringSupport) {
        this.definition = definition;
        this.item = relatedFieldItem;
        this.uiContext = uiContext;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
    }

    /**
     * @deprecated since 5.4.1 - use {@link #AbstractFieldFactory(FieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public AbstractFieldFactory(D definition, Item relatedFieldItem) {
        // Since we can't use Components utility for retreiving UiContext - leave it as null for the time being
        // and get it via component provider when it is set (#setComponentProvider(..) method)
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Override
    public void setFieldValidatorFactoryFactory(FieldValidatorFactoryFactory fieldValidatorFactoryFactory) {
        this.fieldValidatorFactoryFactory = fieldValidatorFactoryFactory;
    }

    /**
     * @deprecated This is deprecated since 5.3.4; {@link I18nContentSupport} was never used within any {@link FieldFactory}, rightfully so.
     * If any, {@link info.magnolia.ui.api.i18n.I18NAuthoringSupport I18NAuthoringSupport} is the one that should be used.
     */
    @Override
    @Deprecated
    public void setI18nContentSupport(I18nContentSupport i18nContentSupport) {
    }

    @Override
    public Field<T> createField() {
        if (locale == null) {
            if (uiContext instanceof SubAppContext) {
                final Locale authoringLocale = ((SubAppContext) uiContext).getAuthoringLocale();
                setLocale(authoringLocale == null ? i18NAuthoringSupport.getDefaultLocale(item) : authoringLocale);
            }
        }

        if (field == null) {
            // Create the Vaadin field
            this.field = createFieldComponent();

            if (field instanceof AbstractField) {
                final AbstractField field = (AbstractField) this.field;
                if (definition.getConverterClass() != null) {
                    Converter<?, ?> converter = initializeConverter(definition.getConverterClass());
                    field.setConverter(converter);
                }
                field.setLocale(locale);
            }

            Property<?> property = initializeProperty();
            // Set the created property with the default value as field Property datasource.
            setPropertyDataSourceAndDefaultValue(property);

            if (StringUtils.isNotBlank(definition.getStyleName())) {
                this.field.addStyleName(definition.getStyleName());
            }

            field.setWidth(100, Unit.PERCENTAGE);

            setFieldCaption();
            setConstraints();

        }
        return this.field;
    }


    private void setFieldCaption() {
        // Set label and required marker
        if (StringUtils.isNotBlank(getFieldDefinition().getLabel())) {
            String caption = getFieldDefinition().getLabel() + (getFieldDefinition().isRequired() ? "<span class=\"requiredfield\">*</span>" : "");


            if (locale != null && definition.isI18n()) {
                caption = String.format("%s (%s)", caption, locale.getLanguage());
            }

            this.field.setCaption(caption);
        }
    }

    /**
     * Set the DataSource of the current field.<br>
     * Set the default value if : <br>
     * - the item is an instance of {@link ItemAdapter} and this is a new Item (Not yet stored in the related datasource).<br>
     * - the item is not an instance of {@link ItemAdapter}.<br>
     * In this case, the Item is a custom implementation of {@link Item} and we have no possibility to define if it is or not a new Item.<br>
     */
    public void setPropertyDataSourceAndDefaultValue(Property<?> property) {
        this.field.setPropertyDataSource(property);

        if ((item instanceof ItemAdapter && ((ItemAdapter) item).isNew() && property.getValue() == null) || (!(item instanceof ItemAdapter) && property.getValue() == null)) {
            setPropertyDataSourceDefaultValue(property);
        }
    }

    /**
     * Set the Field default value is required.
     */
    protected void setPropertyDataSourceDefaultValue(Property property) {
        Object defaultValue = createDefaultValue(property);
        if (defaultValue != null && !definition.isReadOnly()) {
            if (defaultValue.getClass().isAssignableFrom(property.getType())) {
                property.setValue(defaultValue);
            } else {
                log.warn("Default value {} is not assignable to the field of type {}.", defaultValue, field.getPropertyDataSource().getType().getName());
            }
        }
    }

    /**
     * Create a typed default value.
     */
    protected Object createDefaultValue(Property<?> property) {
        String defaultValue = definition.getDefaultValue();
        if (StringUtils.isNotBlank(defaultValue)) {
            return DefaultPropertyUtil.createTypedValue(property.getType(), defaultValue);
        }
        return null;
    }

    @Override
    public D getFieldDefinition() {
        return this.definition;
    }

    /**
     * Implemented by subclasses to create and initialize the Vaadin Field instance to use.
     */
    protected abstract Field<T> createFieldComponent();

    @Override
    public View getView() {
        final CssLayout fieldView = new CssLayout();
        fieldView.setStyleName("field-view");

        Label label = new Label();
        label.setSizeUndefined();
        label.setCaption(getFieldDefinition().getLabel());

        if (getFieldDefinition().getClass().isAssignableFrom(TextFieldDefinition.class)) {
            final TextFieldDefinition textFieldDefinition = (TextFieldDefinition) getFieldDefinition();
            if (textFieldDefinition.getRows() > 0) {
                label.addStyleName("textarea");
            }
        }
        if (definition.getConverterClass() != null) {
            Converter converter = initializeConverter(definition.getConverterClass());
            label.setConverter(converter);
        }

        Property<?> property = initializeProperty();

        label.setPropertyDataSource(property);

        fieldView.addComponent(label);

        return new View() {
            @Override
            public Component asVaadinComponent() {
                return fieldView;
            }
        };
    }

    /**
     * Initialize the property used as field's Datasource.<br>
     * If no {@link Transformer} is configure to the field definition, use the default {@link BasicTransformer} <br>
     */
    @SuppressWarnings("unchecked")
    private Property<?> initializeProperty() {
        Class<? extends Transformer<?>> transformerClass = definition.getTransformerClass();

        if (transformerClass == null) {
            // Down casting is needed due to API of the #initializeTransformer(Class<? extends Transformer<?>>)
            // the second wildcard in '? extends Transformer< --> ?>' is unnecessary and only forces compiler
            // to claim that BasicTransformer.class is not convertible into Class<? extends Transformer<?>>.
            // At runtime it all works due to type erasure.
            transformerClass = (Class<? extends Transformer<?>>) (Object) BasicTransformer.class;
        }
        Transformer<?> transformer = initializeTransformer(transformerClass);
        transformer.setLocale(locale);
        return new TransformedProperty(transformer);
    }

    /**
     * Exposed method used by field's factory to initialize the property {@link Transformer}.<br>
     * This allows to add additional constructor parameter if needed.<br>
     */
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        /**
         * I18NAuthoringSupport is passed through Components utility for test purposes
         * since version 5.4.1 - otherwise all the existing field factory tests would have to be adapted.
         */
        return this.componentProvider.newInstance(transformerClass, item, definition, getFieldType());
    }

    /**
     * Exposed method used by field's factory to initialize the property {@link Converter}.<br>
     * This allows to add additional constructor parameter if needed.<br>
     */
    protected Converter<?, ?> initializeConverter(Class<? extends Converter<?, ?>> converterClass) {
        return this.componentProvider.newInstance(converterClass, item, definition, getFieldType());
    }

    /**
     * Define the field property value type Class.<br>
     * Return the value defined by the configuration ('type' property).<br>
     * If this value is not defined, return the value of the overriding method {@link AbstractFieldFactory#getDefaultFieldType()}.<br>
     * If this method is not override, return {@link UndefinedPropertyType}.<br>
     * In this case, the {@link Transformer} will have the responsibility to define the property type.
     */
    protected Class<?> getFieldType() {
        Class<?> type = getDefinitionType();
        if (type == null) {
            type = getDefaultFieldType();
        }
        return type;
    }

    /**
     * @return Class Type defined into the field definition or null if not defined.
     */
    protected Class<?> getDefinitionType() {
        if (StringUtils.isNotBlank(definition.getType())) {
            return DefaultPropertyUtil.getFieldTypeClass(definition.getType());
        }
        return null;
    }

    /**
     * Exposed method used by field's factory in order to define a default Field Type (decoupled from the definition).
     */
    protected Class<?> getDefaultFieldType() {
        return UndefinedPropertyType.class;
    }

    @Override
    protected String getI18nBasename() {
        return definition.getI18nBasename();
    }

    /**
     * Set all constraints linked to the field:
     * Build Validation rules.
     * Set Required field.
     * Set Read Only.
     */
    private void setConstraints() {
        // Set Validation
        for (FieldValidatorDefinition validatorDefinition : definition.getValidators()) {
            FieldValidatorFactory validatorFactory = this.fieldValidatorFactoryFactory.createFieldValidatorFactory(validatorDefinition, item);
            if (validatorFactory != null) {
                field.addValidator(validatorFactory.createValidator());
            } else {
                log.warn("Not able to create Validation for the following definition {}", definition.toString());
            }
        }
        // Set Required
        if (definition.isRequired()) {
            field.setInvalidCommitted(true);
            field.setRequired(true);
            field.setRequiredError(definition.getRequiredErrorMessage());
        }

        // Set ReadOnly (field property has to be updated)
        if (field.getPropertyDataSource() != null) {
            field.getPropertyDataSource().setReadOnly(definition.isReadOnly());
        }
    }

    @Override
    public void setComponentProvider(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
        if (uiContext == null) {
            uiContext = componentProvider.getComponent(UiContext.class);
        }
    }

    protected ComponentProvider getComponentProvider() {
        return componentProvider;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
