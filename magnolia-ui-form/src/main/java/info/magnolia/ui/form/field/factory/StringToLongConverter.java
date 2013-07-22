/**
 * This file Copyright (c) 2013 Magnolia International
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

import java.util.Locale;

import com.vaadin.data.util.converter.AbstractStringToNumberConverter;

/**
 * The StringToLongConverter.<br>
 * MGNLUI-1855 This should be handled by vaadin, but StringToNumberConverter throws conversion exception when used
 * with a Long property in Vaadin 7.1. This should be fixed, unfortunately not before 7.2, so we need that converter
 * for the time being.<br>
 * As a result, this class should have quite a short life span, this is why we keep it package protected.
 */
class StringToLongConverter extends AbstractStringToNumberConverter<Long> {

    @Override
    public Long convertToModel(String value, Class<? extends Long> targetType, Locale locale) throws ConversionException {
        Number n = convertToNumber(value, targetType, locale);
        return n == null ? null : n.longValue();
    }

    @Override
    public Class<Long> getModelType() {
        return Long.class;
    }
}
