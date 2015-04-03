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
package info.magnolia.ui.dialog.formdialog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaViewImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * Test for ItemFormView.
 */
public class ItemFormViewTest {

    private ItemFormView formviewer;

    final LocaleProvider localeProvider = new LocaleProvider() {
        @Override
        public Locale getLocale() {
            return Locale.US;
        }
    };

    @Before
    public void setUp() throws Exception {
        formviewer = new ItemFormView(mock(SimpleTranslator.class),mock(I18NAuthoringSupport.class));
        MockWebContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testAvailableLocales() throws Exception {
        // GIVEN
        MgnlContext.setLocale(Locale.ENGLISH);
        String label= localeProvider.getLocale().getDisplayLanguage(MgnlContext.getLocale());
        if (!localeProvider.getLocale().getDisplayCountry(MgnlContext.getLocale()).isEmpty()) {
            label += " (" + localeProvider.getLocale().getDisplayCountry(MgnlContext.getLocale()) + ")";
        }

        formviewer.setActionAreaView(new EditorActionAreaViewImpl());
        HorizontalLayout layout = (HorizontalLayout) formviewer.getActionAreaView().asVaadinComponent();
        CssLayout toolbar = (CssLayout) layout.getComponent(0);
        List<Locale> locales= new ArrayList<Locale>();
        locales.add(localeProvider.getLocale());

        // WHEN
        formviewer.setAvailableLocales(locales);
        ComboBox selector = (ComboBox) toolbar.getComponent(0);
        String selectorLabel = selector.getItemCaption(selector.getItemIds().toArray()[0]);

        // THEN
        assertEquals("", label, selectorLabel);
    }
}
