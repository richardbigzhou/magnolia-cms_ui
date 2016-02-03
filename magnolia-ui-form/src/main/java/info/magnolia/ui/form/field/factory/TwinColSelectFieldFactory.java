/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
import info.magnolia.ui.form.field.definition.TwinColSelectFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;

import java.util.HashSet;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.TwinColSelect;

/**
 * Creates and initializes a select field based on a field definition.
 *
 * @param <T> the definition
 */
public class TwinColSelectFieldFactory<T extends TwinColSelectFieldDefinition> extends SelectFieldFactory<TwinColSelectFieldDefinition> {

    private ComponentProvider componentProvider;

    @Inject
    public TwinColSelectFieldFactory(TwinColSelectFieldDefinition definition, Item relatedFieldItem, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem);
        this.componentProvider = componentProvider;
    }

    @Override
    protected AbstractSelect createFieldComponent() {
        super.createFieldComponent();
        int rows = select.getContainerDataSource().size();
        ((TwinColSelect) select).setRows(rows < 20 ? rows : 20);
        select.setMultiSelect(definition.isMultiselect());
        select.setNullSelectionAllowed(true);
        ((TwinColSelect) select).setLeftColumnCaption(getMessage(definition.getLeftColumnCaption()));
        ((TwinColSelect) select).setRightColumnCaption(getMessage(definition.getRightColumnCaption()));
        return select;
    }

    @Override
    protected AbstractSelect createSelectionField() {
        return new TwinColSelect();
    }

    /**
     * Override in order to define the field property type.<br>
     * In any case set property type as {@link HashSet}, type used by the Vaadin MultiSelect field.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        return this.componentProvider.newInstance(transformerClass, item, definition, HashSet.class);
    }

}
