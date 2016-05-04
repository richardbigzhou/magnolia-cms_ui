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
package info.magnolia.ui.workbench.column;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

public class DateColumnFormatterTest {

    private DateColumnFormatter formatter;
    private final User user = mock(User.class);
    private final Table source = mock(Table.class);
    private final Context context = mock(Context.class);

    @Before
    public void setUp() throws Exception {
        when(context.getLocale()).thenReturn(Locale.ENGLISH);
        when(context.getUser()).thenReturn(user);

        Item item = mock(Item.class);
        Date date = new Date();
        date.setTime(0);
        Property<Date> property = new DefaultProperty<>(date);
        when(item.getItemProperty(anyString())).thenReturn(property);
        when(source.getItem(anyObject())).thenReturn(item);

        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        WebBrowser browser = mock(WebBrowser.class);
        when(ui.getPage()).thenReturn(page);
        when(page.getWebBrowser()).thenReturn(browser);
        CurrentInstance.setInheritable(UI.class, ui);
    }

    @After
    public void tearDown() {
        CurrentInstance.setInheritable(UI.class, null);
    }

    @Test
    public void generateCell() throws Exception {
        // GIVEN
        formatter = new DateColumnFormatter(null, context);

        // WHEN
        Object res = formatter.generateCell(source, null, null);

        // THEN
        assertThat(res.toString(), containsString("title=\"Jan 1, 1970 12:00 AM"));
    }

    @Test
    public void showTimeZoneNoDate() throws Exception {
        // GIVEN
        when(user.getProperty(MgnlUserManager.PROPERTY_TIMEZONE)).thenReturn("America/Aruba");
        MetaDataColumnDefinition definition = new MetaDataColumnDefinition();
        definition.setTimeFormat("h:mm a Z");
        definition.setDateFormat(StringUtils.EMPTY);
        formatter = new DateColumnFormatter(definition, context);


        // WHEN
        Object res = formatter.generateCell(source, definition, null);

        // THEN
        assertThat(res.toString(), containsString("8:00 PM -0400"));
    }

}
