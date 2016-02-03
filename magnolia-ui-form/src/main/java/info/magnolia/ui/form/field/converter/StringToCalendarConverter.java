/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.form.field.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToDateConverter;

/**
 * A converter that converts from {@link Calendar} to {@link String} and back. Uses
 * the given locale and {@link java.text.DateFormat} for formatting and parsing.
 *
 * Delegates to {@link StringToDateConverter}.
 */
public class StringToCalendarConverter implements Converter<String, Calendar> {

    final StringToDateConverter dateConverter = new StringToDateConverter();

    @Override
    public Calendar convertToModel(String value, Class<? extends Calendar> targetType, Locale locale) throws ConversionException {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Date date = dateConverter.convertToModel(value, Date.class, locale);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    @Override
    public String convertToPresentation(Calendar value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return dateConverter.convertToPresentation(value.getTime(), targetType, locale);
    }

    @Override
    public Class<Calendar> getModelType() {
        return Calendar.class;
    }

    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }
}
