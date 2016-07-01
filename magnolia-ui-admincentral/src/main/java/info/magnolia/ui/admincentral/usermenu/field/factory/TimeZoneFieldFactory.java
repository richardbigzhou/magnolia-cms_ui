/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.admincentral.usermenu.field.factory;

import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.combobox.FilteringMode;

/**
 * Creates and initializes a timezone field.
 */
public class TimeZoneFieldFactory extends SelectFieldFactory<TimeZoneFieldFactory.Definition> {

    static final String BROWSER_TIMEZONE = "browser";

    private final Context context;
    private final SimpleTranslator simpleTranslator;


    @Inject
    public TimeZoneFieldFactory(Definition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, Context context, SimpleTranslator simpleTranslator) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        this.context = context;
        this.simpleTranslator = simpleTranslator;
    }

    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        final List<SelectFieldOptionDefinition> options = new ArrayList<>();
        SelectFieldOptionDefinition option;
        TimeZone timeZone;

        if (Page.getCurrent() != null) {
            final int offset = Page.getCurrent().getWebBrowser().getTimezoneOffset();
            timeZone = new SimpleTimeZone(offset, BROWSER_TIMEZONE);
            final String timeZoneString = simpleTranslator.translate("ui-admincentral.editUserProfile.preferences.timezone.value.browser.label", timeZone.getDisplayName());
            option = new SelectFieldOptionDefinition();
            option.setValue(BROWSER_TIMEZONE);
            option.setLabel(timeZoneString);
            options.add(option);
        }

        for (String timeZoneStr : TimeZone.getAvailableIDs()) {
            option = new SelectFieldOptionDefinition();
            timeZone = TimeZone.getTimeZone(timeZoneStr);
            option.setValue(timeZoneStr);
            option.setLabel(timeZoneStr + " (" + timeZone.getDisplayName(false, TimeZone.LONG, context.getLocale()) + ")");
            options.add(option);
        }
        return options;
    }

    /**
     * Definition for {@link TimeZoneFieldFactory}.
     */
    public static class Definition extends SelectFieldDefinition {
        public Definition() {
            setSortOptions(false); //We want to have the browser timezone at top
            setFilteringMode(FilteringMode.CONTAINS);
            setTextInputAllowed(true);
        }
    }
}
