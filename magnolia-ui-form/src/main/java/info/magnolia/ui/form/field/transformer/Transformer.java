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

import info.magnolia.ui.api.i18n.I18NAwareHandler;


/**
 * Base definition for a {@link com.vaadin.data.Property} handler.<br>
 * Implemented Transformer have the responsibility to : <br>
 * - write : Convert the T newValue to a specific Item format (Single Item property, Multi Item property, Multi sub Items...) <br>
 * - read : Transform a specific Item values (single property, Multi. property, sub Items) to a specified type T. <br>
 * 
 * @param <T> type of the element handled.
 */
public interface Transformer<T> extends I18NAwareHandler {

    /**
     * Convert the T newValue to a specific Item format.<br>
     */
    void writeToItem(T newValue);

    /**
     * Transform a specific Item values to a specified type T.<br>
     */
    T readFromItem();

    /**
     * Return true if this Property has to support i18n.
     */
    boolean hasI18NSupport();

    /**
     * @return type
     * the type of the value. <code>value</code> must be assignable
     * to this type.
     */
    Class<T> getType();
}
