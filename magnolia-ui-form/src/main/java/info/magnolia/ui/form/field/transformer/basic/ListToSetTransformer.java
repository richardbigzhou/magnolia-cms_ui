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
package info.magnolia.ui.form.field.transformer.basic;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Specific transformer for use with multi-select {@link OptionGroupFieldDefinition}.
 * <p>
 * Indeed, Vaadin's {@link com.vaadin.ui.OptionGroup OptionGroup} operates selection with sets, whereas we resolve
 * JCR multi-value properties as lists.
 * <p>
 * Also mind that for single-select definitions, this transformer merely works like a plain {@link BasicTransformer}.
 *
 * @param <T>
 */
public class ListToSetTransformer<T> extends BasicTransformer<T> {

    private static final Logger log = LoggerFactory.getLogger(ListToSetTransformer.class);

    private final boolean multiselect;

    /**
     * @deprecated since 5.4.2 - use {@link #ListToSetTransformer(Item, ConfiguredFieldDefinition, Class, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public ListToSetTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<T> type) {
        this(relatedFormItem, definition, type, Components.getComponent(I18NAuthoringSupport.class));
    }

    public ListToSetTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<T> type, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type, i18NAuthoringSupport);
        if (definition instanceof OptionGroupFieldDefinition) {
            multiselect = ((OptionGroupFieldDefinition) definition).isMultiselect();
        } else {
            multiselect = false;
            log.info("ListToSetTransformer is intended for multiselect OptionGroupFieldDefinition, but definition was of type {}. This may behave like a plain BasicTransformer.", definition);
        }
    }

    @Override
    public void writeToItem(T newValue) {
        if (!multiselect) {
            super.writeToItem(newValue);
            return;
        }

        Property<T> p = getOrCreateProperty(type, false);
        if (isCollectionConversionNeeded(newValue, p.getType())) {
            newValue = (T) new LinkedList((Set) newValue);
        }
        if (newValue instanceof Collection && ((Collection) newValue).isEmpty()) {
            newValue = null;
        }
        p.setValue(newValue);
    }

    /**
     * Check if the newValue has to be transformed from a {@link Set} to a {@link List}. {@link Set} is used by Vaadin multi fields and multi values are stored as {@link List} in Jcr.
     */
    protected boolean isCollectionConversionNeeded(T newValue, Class<?> propertyType) {
        return List.class.isAssignableFrom(propertyType) && newValue instanceof Set;
    }

    @Override
    public T readFromItem() {
        if (!multiselect) {
            return super.readFromItem();
        }

        Property<T> p = getOrCreateProperty(type, false);
        if (definition.isReadOnly()) {
            p.setReadOnly(true);
        }
        T value = p.getValue();
        if (value != null && value instanceof List) {
            return (T) new HashSet((List) value);
        }
        return null;
    }

}
