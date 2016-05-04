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

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.DateFieldDefinition;

import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Field;
import com.vaadin.ui.PopupDateField;

/**
 * Creates and initializes a date field based on a field definition.
 */
public class DateFieldFactory extends AbstractFieldFactory<DateFieldDefinition, Date> {

    private static final String BROWSER_TIMEZONE = "browser";

    private final SimpleTranslator simpleTranslator;
    private final Context context;

    @Inject
    public DateFieldFactory(DateFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18NAuthoringSupport, SimpleTranslator simpleTranslator, Context context) {
        super(definition, relatedFieldItem, uiContext, i18NAuthoringSupport);
        this.simpleTranslator = simpleTranslator;
        this.context = context;
    }

    /**
     * @deprecated since 5.4.7. Use {@link #DateFieldFactory(info.magnolia.ui.form.field.definition.DateFieldDefinition, com.vaadin.data.Item, info.magnolia.ui.api.context.UiContext, info.magnolia.ui.api.i18n.I18NAuthoringSupport, info.magnolia.i18nsystem.SimpleTranslator, Context)} instead.
     */
    @Deprecated
    public DateFieldFactory(DateFieldDefinition definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, Components.getComponent(UiContext.class), Components.getComponent(I18NAuthoringSupport.class), Components.getComponent(SimpleTranslator.class), Components.getComponent(Context.class));
    }

    @Override
    public Field<Date> createField() {
        Field<Date> field = super.createField();
        field.setWidthUndefined();
        return field;
    }

    @Override
    protected Field<Date> createFieldComponent() {
        DateFieldDefinition definition = getFieldDefinition();
        PopupDateField popupDateField = new PopupDateField();
        setTimeZone(popupDateField);
        String dateFormat;

        // set Resolution
        if (definition.isTime()) {
            popupDateField.setResolution(Resolution.MINUTE);
            dateFormat = definition.getDateFormat() + " " + definition.getTimeFormat();
        } else {
            popupDateField.setResolution(Resolution.DAY);
            dateFormat = definition.getDateFormat();
        }
        popupDateField.setDateFormat(dateFormat);
        return popupDateField;
    }

    @Override
    protected Class<?> getDefaultFieldType() {
        return Date.class;
    }

    /**
     * When not explicitly setting the current time using {@link Field#setValue} the client side implementation will set
     * it to the browsers time, ignoring timezone differences and therefor making it impossible to get the UTC time.
     */
    private void setTimeZone(PopupDateField popupDateField) {

        final String timeZoneId = context.getUser().getProperty(MgnlUserManager.PROPERTY_TIMEZONE);
        final TimeZone timeZone;

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
        if (timeZone != null) {
            popupDateField.setTimeZone(timeZone);
            //We have to show the timezone in which we're choosing/displaying the date:
            popupDateField.setDescription(simpleTranslator.translate("ui-admincentral.dateField.timeZone.description", timeZone.getDisplayName(false, TimeZone.LONG, context.getLocale()), timeZone.getRawOffset() / 3600000));
            popupDateField.setInputPrompt(simpleTranslator.translate("ui-admincentral.dateField.timeZone.description", timeZone.getDisplayName(false, TimeZone.SHORT, context.getLocale()), timeZone.getRawOffset() / 3600000));
        }
    }
}
