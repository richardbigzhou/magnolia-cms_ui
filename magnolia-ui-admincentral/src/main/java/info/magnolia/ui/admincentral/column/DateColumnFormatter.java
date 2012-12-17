/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.column;

import java.util.Date;
import java.util.Locale;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import javax.inject.Inject;
import org.apache.commons.lang.time.FastDateFormat;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

/**
 * Date Column formatter. Formats dates to a compact format.
 */
public class DateColumnFormatter extends AbstractColumnFormatter<MetaDataColumnDefinition> {

    private final FastDateFormat dateFormatter;

    @Inject
    public DateColumnFormatter(MetaDataColumnDefinition definition) {
        super(definition);

        final Locale locale = MgnlContext.getLocale();
        dateFormatter = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.SHORT, locale);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        Item item = source.getItem(itemId);
        Property prop = item.getItemProperty(columnId);
        // Need to check prop.getValue() before prop.getType() to avoid
        // nullpointerexception if value is null.
        if (prop != null && prop.getValue() != null && prop.getType().equals(Date.class)) {
            return dateFormatter.format(prop.getValue());
        }
        return null;
    }
}
