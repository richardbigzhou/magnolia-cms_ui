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
package info.magnolia.ui.form.field.transformer;


import com.vaadin.data.util.ObjectProperty;

/**
 * Basic implementation of an {@link ObjectProperty} .<br>
 * This base property delegate to the {@link Transformer} the read and write of the value used by the field.<br>
 *
 * @param <T>.
 */
public class TransformedProperty<T> extends ObjectProperty<T> {

    private final Transformer<T> transformer;

    /**
     * Creates a new instance of TransformedProperty with the given transformer.<br>
     * The super {@link ObjectProperty} is initialized with: <br>
     * - value = {@link Transformer#readFromItem()} in order to get the Item property value,
     * - type = {@link Transformer#getType()}, the type of the value.
     *
     * @param transformer
     * the relate Property {@link Transformer}.
     */
    public TransformedProperty(Transformer<T> transformer) {
        super(transformer.readFromItem(), transformer.getType());
        this.transformer = transformer;
    }

    @Override
    public void setValue(T newValue) throws com.vaadin.data.Property.ReadOnlyException {
        super.setValue(newValue);
        if (transformer != null) {
            transformer.writeToItem(newValue);
        }
    }

    @Override
    public T getValue() {
        return super.getValue();
    }

    @Override
    public boolean isReadOnly() {
        if (transformer != null) {
            return transformer.isReadOnly();
        } else {
            return false;
        }
    }

    /**
     * @return true if the handler support I18N.
     */
    public boolean hasI18NSupport() {
        return transformer.hasI18NSupport();
    }

    /**
     * In case of i18n change, Reload the Value returned by the Handler.
     */
    public void fireI18NValueChange() {
        boolean readOnlyValue = isReadOnly();
        setReadOnly(false);
        super.setValue(transformer.readFromItem());
        setReadOnly(readOnlyValue);
        super.fireValueChange();
    }

    public Transformer<T> getTransformer() {
        return this.transformer;
    }
}
