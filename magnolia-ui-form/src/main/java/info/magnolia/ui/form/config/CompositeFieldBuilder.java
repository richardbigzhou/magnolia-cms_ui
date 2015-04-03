/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * Builder for building a composite field.
 */
public class CompositeFieldBuilder extends AbstractFieldBuilder {

    private CompositeFieldDefinition definition = new CompositeFieldDefinition();

    public CompositeFieldBuilder(String name) {
        definition().setName(name);
    }

    @Override
    public CompositeFieldDefinition definition() {
        return definition;
    }

    public CompositeFieldBuilder fields(AbstractFieldBuilder... builders) {
        for (AbstractFieldBuilder builder : builders) {
            definition().getFields().add(builder.definition());
        }
        return this;
    }

    public CompositeFieldBuilder layout(Layout layout) {
        definition().setLayout(layout);
        return this;
    }

    // Overrides for methods in parent class changing return type to allow method chaining

    @Override
    public CompositeFieldBuilder label(String label) {
        return (CompositeFieldBuilder) super.label(label);
    }

    @Override
    public CompositeFieldBuilder i18nBasename(String i18nBasename) {
        return (CompositeFieldBuilder) super.i18nBasename(i18nBasename);
    }

    @Override
    public CompositeFieldBuilder i18n(boolean i18n) {
        return (CompositeFieldBuilder) super.i18n(i18n);
    }

    @Override
    public CompositeFieldBuilder i18n() {
        return (CompositeFieldBuilder) super.i18n();
    }

    @Override
    public CompositeFieldBuilder description(String description) {
        return (CompositeFieldBuilder) super.description(description);
    }

    @Override
    public CompositeFieldBuilder type(String type) {
        return (CompositeFieldBuilder) super.type(type);
    }

    @Override
    public CompositeFieldBuilder required(boolean required) {
        return (CompositeFieldBuilder) super.required(required);
    }

    @Override
    public CompositeFieldBuilder required() {
        return (CompositeFieldBuilder) super.required();
    }

    @Override
    public CompositeFieldBuilder requiredErrorMessage(String requiredErrorMessage) {
        return (CompositeFieldBuilder) super.requiredErrorMessage(requiredErrorMessage);
    }

    @Override
    public CompositeFieldBuilder readOnly(boolean readOnly) {
        return (CompositeFieldBuilder) super.readOnly(readOnly);
    }

    @Override
    public CompositeFieldBuilder readOnly() {
        return (CompositeFieldBuilder) super.readOnly();
    }

    @Override
    public CompositeFieldBuilder defaultValue(String defaultValue) {
        return (CompositeFieldBuilder) super.defaultValue(defaultValue);
    }

    @Override
    public CompositeFieldBuilder styleName(String styleName) {
        return (CompositeFieldBuilder) super.styleName(styleName);
    }

    @Override
    public CompositeFieldBuilder validator(ConfiguredFieldValidatorDefinition validatorDefinition) {
        return (CompositeFieldBuilder) super.validator(validatorDefinition);
    }

    @Override
    public CompositeFieldBuilder validator(GenericValidatorBuilder validatorBuilder) {
        return (CompositeFieldBuilder) super.validator(validatorBuilder);
    }

    @Override
    public CompositeFieldBuilder transformerClass(Class<? extends Transformer<?>> transformerClass) {
        return (CompositeFieldBuilder) super.transformerClass(transformerClass);
    }
}
