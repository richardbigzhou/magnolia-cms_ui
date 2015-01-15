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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;

import java.util.HashSet;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.OptionGroup;

/**
 * Creates and initializes a select field based on a field definition.
 * 
 * @param <D> type of definition
 */
public class OptionGroupFieldFactory<D extends SelectFieldDefinition> extends SelectFieldFactory<OptionGroupFieldDefinition> {

    private ComponentProvider componentProvider;

    @Inject
    public OptionGroupFieldFactory(OptionGroupFieldDefinition definition, Item relatedFieldItem, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.componentProvider = componentProvider;
    }

    @Override
    protected AbstractSelect createFieldComponent() {
        super.createFieldComponent();
        select.setMultiSelect(getFieldDefinition().isMultiselect());
        select.setNullSelectionAllowed(true);
        if (select.isMultiSelect()) {
            // In case of MultiSelect, type will be set by the getDefaultFieldType().
            // In any case it should be set to a simple type (String, Long...)
            definition.setType(null);
        }
        return select;
    }

    @Override
    protected AbstractSelect createSelectionField() {
        return new OptionGroup();
    }

    /**
     * Override in order to define the field property type.<br>
     * In case of single select, use the default mechanism.<br>
     * In case of multi select, set property type as {@link HashSet}, type used by the Vaadin MultiSelect field.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        return this.componentProvider.newInstance(transformerClass, item, definition, defineType());
    }

    protected Class<?> defineType() {
        if (!select.isMultiSelect()) {
            return getFieldType();
        } else {
            return HashSet.class;
        }
    }

}
