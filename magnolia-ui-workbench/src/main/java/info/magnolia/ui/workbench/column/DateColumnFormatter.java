/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.workbench.column;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.context.Context;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.ui.Table;

/**
 * Formats a column's value as a date in a compact form.
 */
public class DateColumnFormatter extends AbstractColumnFormatter<AbstractColumnDefinition> {

    private static final String BROWSER_TIMEZONE = "browser";

    private final FastDateFormat dateFormatter;
    private final FastDateFormat timeFormatter;
    private final TimeZone timeZone;

    private final Context context;

    @Inject
    public DateColumnFormatter(AbstractColumnDefinition definition, Context context) {
        super(definition);
        this.context = context;

        final Locale locale = context.getLocale();
        final String timeZoneId = context.getUser().getProperty(MgnlUserManager.PROPERTY_TIMEZONE);
        if (timeZoneId == null || BROWSER_TIMEZONE.equals(timeZoneId)) {
            if (Page.getCurrent() != null) {
                int offset = Page.getCurrent().getWebBrowser().getTimezoneOffset(); //only offset, not raw offset since we don't know the  daylight settings of user timezone
                timeZone = new SimpleTimeZone(offset, BROWSER_TIMEZONE);
            } else {
                timeZone = null;
            }
        } else {
            timeZone = TimeZone.getTimeZone(timeZoneId);
        }
        if (definition instanceof MetaDataColumnDefinition) {
            MetaDataColumnDefinition metaDataColumnDefinition = (MetaDataColumnDefinition) definition;
            dateFormatter = FastDateFormat.getInstance(metaDataColumnDefinition.getDateFormat(), timeZone, locale);
            timeFormatter = FastDateFormat.getInstance(metaDataColumnDefinition.getTimeFormat(), timeZone, locale);
        } else {
            dateFormatter = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, timeZone, locale);
            timeFormatter = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, timeZone, locale);
        }
    }

    /**
     * @deprecated since 5.4.7. Use {@link #DateColumnFormatter(info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition, Context)} instead.
     */
    @Deprecated
    public DateColumnFormatter(AbstractColumnDefinition definition) {
        this(definition, Components.getComponent(Context.class));
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        Item item = source.getItem(itemId);
        Property prop = (item == null) ? null : item.getItemProperty(columnId);

        // Need to check prop.getValue() before prop.getType() to avoid NPE if value is null.
        if (prop != null && prop.getValue() != null && Date.class.equals(prop.getType())) {
            String date = dateFormatter.format(prop.getValue());
            String time = timeFormatter.format(prop.getValue());
            String dateHtml = StringUtils.isEmpty(date) ? StringUtils.EMPTY : String.format("<span class=\"datefield\">%s</span>", date);
            String timeHtml = StringUtils.isEmpty(time) ? StringUtils.EMPTY : String.format("<span class=\"timefield\">%s</span>", time);
            String tooltip = timeZone == null ? StringUtils.EMPTY : "title=\"" + date + " " + time + "  (" + timeZone.getDisplayName(context.getLocale()) + ")" + "\"";
            return String.format("<span class=\"datetimefield\" %s>%s%s</span>", tooltip, dateHtml, timeHtml);
        }
        return null;
    }
}
