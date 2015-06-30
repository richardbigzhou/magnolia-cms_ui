/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.CheckBoxField;
import info.magnolia.ui.form.field.definition.CheckboxFieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Creates and initializes a checkBox field based on a field definition.
 */
public class CheckBoxFieldFactory extends AbstractFieldFactory<CheckboxFieldDefinition, Boolean> {

    private static final Logger log = LoggerFactory.getLogger(CheckBoxFieldFactory.class);

    private final I18NAuthoringSupport i18nAuthoringSupport;

    @Inject
    public CheckBoxFieldFactory(CheckboxFieldDefinition definition, Item relatedFieldItem, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem);
        this.i18nAuthoringSupport = i18nAuthoringSupport;
    }

    /**
     * @deprecated since 5.3.10, use {@link #CheckBoxFieldFactory(CheckboxFieldDefinition, Item, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public CheckBoxFieldFactory(CheckboxFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
        this.i18nAuthoringSupport = Components.getComponent(I18NAuthoringSupport.class);
    }

    @Override
    public Field<Boolean> createField() {
        super.createField();
        if (definition.isI18n()) {
            if (item instanceof JcrItemAdapter) {
                javax.jcr.Item jcrItem = ((JcrItemAdapter) item).getJcrItem();
                if (jcrItem.isNode()) {
                    Node node = (Node) jcrItem;
                    List<Locale> locales = i18nAuthoringSupport.getAvailableLocales(node);
                    Locale defaultLocale = i18nAuthoringSupport.getDefaultLocale(node);
                    for (Locale locale : locales) {
                        if (!locale.getLanguage().equals(defaultLocale.getLanguage())) {
                            Property<?> property = initializeLocalizedProperty(locale);
                            setPropertyDataSourceAndDefaultValue(property);
                        }
                    }
                }
            }
        }

        return field;
    }

    private String constructI18NPropertyName(CharSequence basePropertyName, Locale locale) {
        return basePropertyName + "_" + locale.toString();
    }

    @SuppressWarnings("unchecked")
    private Property<?> initializeLocalizedProperty(Locale locale) {
        Class<? extends Transformer<?>> transformerClass = super.getTransformerClass();
        Transformer<?> transformer = initializeTransformer(transformerClass);
        transformer.setLocale(locale);
        final String basePropertyName = transformer.getBasePropertyName();
        final String localizedPropertyName = constructI18NPropertyName(basePropertyName, locale);
        transformer.setI18NPropertyName(localizedPropertyName);

        return new TransformedProperty(transformer);
    }

    @Override
    protected Field<Boolean> createFieldComponent() {
        CheckBoxField field = new CheckBoxField();
        field.setCheckBoxCaption(getMessage(definition.getButtonLabel()));
        return field;
    }

    @Override
    protected Class<?> getDefaultFieldType() {
        return Boolean.class;
    }

    @Override
    public void setPropertyDataSourceAndDefaultValue(Property<?> property) {
        this.field.setPropertyDataSource(property);

        if (property.getValue() == null) {
            setPropertyDataSourceDefaultValue(property);
        }
    }
}
