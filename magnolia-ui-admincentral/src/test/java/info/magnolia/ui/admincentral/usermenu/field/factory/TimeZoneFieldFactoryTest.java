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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.framework.i18n.DefaultI18NAuthoringSupport;

import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

public class TimeZoneFieldFactoryTest {

    private TimeZoneFieldFactory factory;
    private WebBrowser browser = mock(WebBrowser.class);

    @Before
    public void setUp() {
        ComponentsTestUtil.setImplementation(I18NAuthoringSupport.class, DefaultI18NAuthoringSupport.class);
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
        MockContext ctx = new MockContext();
        ctx.setLocale(Locale.ENGLISH);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        factory = new TimeZoneFieldFactory(new TimeZoneFieldFactory.Definition(), null, null, null, ctx, new SimpleTranslator(null, null) {
            @Override
            public String translate(String key, Object... args) {
                return (String) args[0];
            }
        });

        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);
        when(page.getWebBrowser()).thenReturn(browser);
        CurrentInstance.setInheritable(UI.class, ui);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        CurrentInstance.setInheritable(UI.class, null);
    }


    @Test
    public void browserTimeZoneIsFirstIfResolved() {
        //GIVEN
        when(browser.getTimezoneOffset()).thenReturn(7200000);

        //WHEN
        List<SelectFieldOptionDefinition> options = factory.getSelectFieldOptionDefinition();

        //THEN
        assertThat(options, not(empty()));
        assertThat(options.get(0).getValue(), is(TimeZoneFieldFactory.BROWSER_TIMEZONE));
        assertThat(options.get(0).getLabel(), is("GMT+02:00"));
    }

}
