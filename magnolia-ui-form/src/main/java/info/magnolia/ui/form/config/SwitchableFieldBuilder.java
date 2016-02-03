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
package info.magnolia.ui.form.config;

import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

import java.util.Collection;

/**
 * Builder for building a switchable field definition.
 */
public class SwitchableFieldBuilder extends AbstractFieldBuilder {

    private SwitchableFieldDefinition definition = new SwitchableFieldDefinition();

    public SwitchableFieldBuilder(String name) {
        definition().setName(name);
    }

    @Override
    public SwitchableFieldDefinition definition() {
        return definition;
    }

    public SwitchableFieldBuilder layout(Layout layout) {
        definition().setLayout(layout);
        return this;
    }

    public SwitchableFieldBuilder selectionType(String selectionType) {
        definition().setSelectionType(selectionType);
        return this;
    }

    public SwitchableFieldBuilder options(OptionBuilder... builders) {
        for (OptionBuilder builder : builders) {
            definition().getOptions().add(builder.definition());
        }
        return this;
    }

    public SwitchableFieldBuilder options(Collection<?> options) {
        for (Object option : options) {
            String value = option.toString();
            definition().getOptions().add(new OptionBuilder().label(value).value(value).definition());
        }
        return this;
    }

    public SwitchableFieldBuilder fields(AbstractFieldBuilder... builders) {
        for (AbstractFieldBuilder builder : builders) {
            definition().getFields().add(builder.definition());
        }
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public SwitchableFieldBuilder label(String label) {
        return (SwitchableFieldBuilder) super.label(label);
    }

    @Override
    public SwitchableFieldBuilder i18nBasename(String i18nBasename) {
        return (SwitchableFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public SwitchableFieldBuilder i18n(boolean i18n) {
        return (SwitchableFieldBuilder) super.i18n(i18n);
    }

    @Override
    public SwitchableFieldBuilder i18n() {
        return (SwitchableFieldBuilder) super.i18n();
    }

    @Override
    public SwitchableFieldBuilder description(String description) {
        return (SwitchableFieldBuilder) super.description(description);
    }

    @Override
    public SwitchableFieldBuilder type(String type) {
        return (SwitchableFieldBuilder) super.type(type);
    }

    @Override
    public SwitchableFieldBuilder required(boolean required) {
        return (SwitchableFieldBuilder) super.required(required);
    }

    @Override
    public SwitchableFieldBuilder required() {
        return (SwitchableFieldBuilder) super.required();
    }

    @Override
    public SwitchableFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (SwitchableFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public SwitchableFieldBuilder readOnly(boolean readOnly) {
        return (SwitchableFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public SwitchableFieldBuilder readOnly() {
        return (SwitchableFieldBuilder) super.readOnly();
    }

    @Override
    public SwitchableFieldBuilder defaultValue(String defaultValue) {
        return (SwitchableFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public SwitchableFieldBuilder styleName(String styleName) {
        return (SwitchableFieldBuilder) super.styleName(styleName);
    }

    @Override
    public SwitchableFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (SwitchableFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public SwitchableFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (SwitchableFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public SwitchableFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (SwitchableFieldBuilder) super.transformerClass(transformerClass);
    }
}
