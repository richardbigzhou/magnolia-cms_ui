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
package info.magnolia.ui.framework.i18n;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes.Page;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link DefaultI18NAuthoringSupport}.
 */
public class DefaultI18nAuthoringSupportTest {

    private static final String WORKSPACE = "website";

    private Session session;

    private DefaultI18nContentSupport i18n;
    private DefaultI18NAuthoringSupport i18nAuthoringSupport;

    @Before
    public void setUp() {
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        MgnlContext.setInstance(ctx);

        i18n = new DefaultI18nContentSupport();
        i18n.addLocale(createLocaleDefinition(Locale.FRENCH));
        i18n.addLocale(createLocaleDefinition(Locale.JAPANESE));
        i18n.addLocale(createLocaleDefinition(Locale.CHINESE));
        i18n.addLocale(createLocaleDefinition(Locale.TAIWAN));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18n);

        i18nAuthoringSupport = new DefaultI18NAuthoringSupport();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testGetAvailableLocales() throws Exception {
        // GIVEN
        Node testPage = NodeUtil.createPath(session.getRootNode(), "test-site/test-page", Page.NAME);
        i18n.setEnabled(true);

        // WHEN
        List<Locale> locales = i18nAuthoringSupport.getAvailableLocales(testPage);

        // THEN
        assertNotNull(locales);
        assertFalse(locales.isEmpty());
        assertTrue(locales.contains(Locale.FRENCH));
        assertTrue(locales.contains(Locale.JAPANESE));
    }

    @Test
    public void testGetAvailableLocalesReturnsNullIfI18nAuthoringSupportIsNotEnabled() throws Exception {
        // GIVEN
        Node testPage = NodeUtil.createPath(session.getRootNode(), "test-site/test-page", Page.NAME);
        i18nAuthoringSupport.setEnabled(false);

        // WHEN
        List<Locale> locales = i18nAuthoringSupport.getAvailableLocales(testPage);

        // THEN
        assertThat(locales, empty());
    }

    @Test
    public void testLocalisedPropertyContainsCountryIfNotEmpty() throws Exception {
        // GIVEN
        i18nAuthoringSupport.setEnabled(true);

        // WHEN
        String localisedPropertyEnglish = i18nAuthoringSupport.deriveLocalisedPropertyName("propertyName", Locale.ENGLISH);
        String localisedPropertyChina = i18nAuthoringSupport.deriveLocalisedPropertyName("propertyName", Locale.SIMPLIFIED_CHINESE);
        String localisedPropertyTaiwan = i18nAuthoringSupport.deriveLocalisedPropertyName("propertyName", Locale.TAIWAN);

        // THEN
        assertEquals("propertyName_en", localisedPropertyEnglish);
        assertEquals("propertyName_zh_CN", localisedPropertyChina);
        assertEquals("propertyName_zh_TW", localisedPropertyTaiwan);
    }

    @Test
    public void testGetAvailableLocalesReturnsNullIfSiteI18nContentSupportIsNotEnabled() throws Exception {
        // GIVEN
        Node testPage = NodeUtil.createPath(session.getRootNode(), "test-site/test-page", Page.NAME);
        i18n.setEnabled(false);

        // WHEN
        List<Locale> locales = i18nAuthoringSupport.getAvailableLocales(testPage);

        // THEN
        assertThat(locales, empty());
    }

    private LocaleDefinition createLocaleDefinition(Locale locale) {
        return LocaleDefinition.make(locale.getLanguage(), locale.getCountry(), true);
    }
}
