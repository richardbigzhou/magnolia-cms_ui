/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes.Page;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.StaticField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextField;

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
        assertNull(locales);
    }

    @Test
    public void testGetAvailableLocalesReturnsNullIfSiteI18nContentSupportIsNotEnabled() throws Exception {
        // GIVEN
        Node testPage = NodeUtil.createPath(session.getRootNode(), "test-site/test-page", Page.NAME);
        i18n.setEnabled(false);

        // WHEN
        List<Locale> locales = i18nAuthoringSupport.getAvailableLocales(testPage);

        // THEN
        assertNull(locales);
    }

    @Test
    public void i18nizeToJapanese() {
        // GIVEN
        // Create field
        AbstractField<String> field = new TextField();
        TestLocaleTransformer transformer = new TestLocaleTransformer(true, "propertyName");
        TransformedProperty<String> property = new TransformedProperty<String>(transformer);
        field.setPropertyDataSource(property);
        // Create Form
        CssLayout form = new CssLayout();
        form.addComponent(field);
        i18n.setEnabled(true);
        assertNull(field.getLocale());

        // WHEN
        i18nAuthoringSupport.i18nize(form, Locale.JAPANESE);

        // THEN
        assertEquals(Locale.JAPANESE, field.getLocale());
        assertEquals("propertyName_ja", transformer.getI18NPropertyName());
    }

    @Test
    public void i18nizeWithoutI18nSupport() {
        // GIVEN
        // Create field
        AbstractField<String> field = new TextField();
        TestLocaleTransformer transformer = new TestLocaleTransformer(true, "propertyName");
        TransformedProperty<String> property = new TransformedProperty<String>(transformer);
        field.setPropertyDataSource(property);
        // Create Form
        CssLayout form = new CssLayout();
        form.addComponent(field);
        assertNull(field.getLocale());

        // WHEN
        i18nAuthoringSupport.i18nize(form, Locale.JAPANESE);

        // THEN
        assertNull(field.getLocale());
        assertNull(transformer.getI18NPropertyName());
    }

    @Test
    public void i18nizeFieldsRecursively() {
        // GIVEN
        // Create fields
        // Inner field
        AbstractField<String> innerField = new TextField();
        TestLocaleTransformer innerTransformer = new TestLocaleTransformer(true, "subProperty");
        TransformedProperty<String> property = new TransformedProperty<String>(innerTransformer);
        innerField.setPropertyDataSource(property);
        // Outer Field
        TestI18nTransformerDelegator mainTransformer = new TestI18nTransformerDelegator(true, "mainProperty");
        TransformedProperty<String> mainProperty = new TransformedProperty<String>(mainTransformer);
        StaticField mainField = mock(StaticField.class, Answers.CALLS_REAL_METHODS.get());
        when(mainField.getPropertyDataSource()).thenReturn(mainProperty);
        List<Component> fields = new ArrayList<Component>();
        fields.add(innerField);
        when(mainField.iterator()).thenReturn(fields.iterator());

        // Create Form
        CssLayout form = new CssLayout();
        form.addComponent(mainField);
        i18n.setEnabled(true);
        assertNull(mainField.getLocale());

        // WHEN
        i18nAuthoringSupport.i18nize(form, Locale.JAPANESE);

        // THEN
        assertEquals(Locale.JAPANESE, mainField.getLocale());
        assertEquals("mainProperty_ja", mainTransformer.getI18NPropertyName());
        assertEquals(Locale.JAPANESE, innerField.getLocale());
        assertEquals("subProperty_ja", innerTransformer.getI18NPropertyName());
    }

    @Test
    public void i18nizeToJapaneseWhileJapaneseIsDefaultLanguage() {
        // GIVEN
        // Create field
        AbstractField<String> field = new TextField();
        JcrItemAdapter item = mock(JcrItemAdapter.class);
        Node jcrItem = mock(Node.class);
        when(item.getJcrItem()).thenReturn(jcrItem);
        when(jcrItem.isNode()).thenReturn(true);
        TestLocaleTransformer transformer = new TestLocaleTransformer(true, "propertyName", item);
        TransformedProperty<String> property = new TransformedProperty<String>(transformer);
        field.setPropertyDataSource(property);
        // Create Form
        CssLayout form = new CssLayout();
        form.addComponent(field);
        i18n.setEnabled(true);
        i18n.setDefaultLocale(Locale.JAPANESE);
        assertNull(field.getLocale());

        // WHEN
        i18nAuthoringSupport.i18nize(form, Locale.JAPANESE);

        // THEN
        assertEquals(Locale.JAPANESE, field.getLocale());
        assertEquals("propertyName", transformer.getI18NPropertyName());
    }

    @Test
    public void i18nizeToJapaneseWhileJapaneseIsNotDefaultLanguage() {
        // GIVEN
        // Create field
        AbstractField<String> field = new TextField();
        JcrItemAdapter item = mock(JcrItemAdapter.class);
        Node jcrItem = mock(Node.class);
        when(item.getJcrItem()).thenReturn(jcrItem);
        when(jcrItem.isNode()).thenReturn(true);
        TestLocaleTransformer transformer = new TestLocaleTransformer(true, "propertyName", item);
        TransformedProperty<String> property = new TransformedProperty<String>(transformer);
        field.setPropertyDataSource(property);
        // Create Form
        CssLayout form = new CssLayout();
        form.addComponent(field);
        i18n.setEnabled(true);
        assertNull(field.getLocale());

        // WHEN
        i18nAuthoringSupport.i18nize(form, Locale.JAPANESE);

        // THEN
        assertEquals(Locale.JAPANESE, field.getLocale());
        assertEquals("propertyName" + "_" + Locale.JAPANESE, transformer.getI18NPropertyName());
    }

    private LocaleDefinition createLocaleDefinition(Locale locale) {
        return LocaleDefinition.make(locale.getLanguage(), locale.getCountry(), true);
    }

    private class TestI18nTransformerDelegator extends TestLocaleTransformer {
        public TestI18nTransformerDelegator(boolean hasI18nSupport, String propertyName) {
            super(hasI18nSupport, propertyName);
        }
    }

    private class TestLocaleTransformer extends BasicTransformer<String> {
        private Locale locale;
        private boolean hasI18nSupport = false;
        private String i18nPropertyName;
        private String propertyName;

        public TestLocaleTransformer(boolean hasI18nSupport, String propertyName, Item item) {
            super(item, mock(ConfiguredFieldDefinition.class), String.class);
            this.hasI18nSupport = hasI18nSupport;
            this.propertyName = propertyName;
        }

        public TestLocaleTransformer(boolean hasI18nSupport, String propertyName) {
            this(hasI18nSupport, propertyName, mock(Item.class));
        }

        @Override
        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        @Override
        public void setI18NPropertyName(String i18nPropertyName) {
            this.i18nPropertyName = i18nPropertyName;
        }

        public String getI18NPropertyName() {
            return this.i18nPropertyName;
        }

        @Override
        public Locale getLocale() {
            return this.locale;
        }

        @Override
        public String getBasePropertyName() {
            return this.propertyName;
        }

        @Override
        public void writeToItem(String newValue) {
        }

        @Override
        public String readFromItem() {
            return null;
        }

        @Override
        public boolean hasI18NSupport() {
            return this.hasI18nSupport;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }

    }
}
