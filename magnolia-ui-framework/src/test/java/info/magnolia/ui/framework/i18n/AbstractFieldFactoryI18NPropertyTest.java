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

import static org.junit.Assert.assertEquals;

import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTestCase;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTest;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.api.i18n.I18NAwareProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.AbstractFieldFactory} i18n property.
 */
public class AbstractFieldFactoryI18NPropertyTest extends AbstractFieldFactoryTestCase<ConfiguredFieldDefinition> {
    private AbstractFieldFactory<FieldDefinition, Object> fieldFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        i18nContentSupport.addLocale(LocaleDefinition.make("de", null, true));
        i18nContentSupport.addLocale(LocaleDefinition.make("it", null, false));
        i18nContentSupport.addLocale(LocaleDefinition.make("fr", null, true));
        i18nContentSupport.addLocale(LocaleDefinition.make("fr", "CH", true));
    }

    @Test
    public void i18nPropertyNotDefined_CurrentIsDefault() throws Exception {
        // GIVEN
        initBuilder();
        Field<Object> field = fieldFactory.createField();
        field.setValue("new Value");
        // WHEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals(true, res.hasProperty(propertyName));
    }


    @Test
    public void i18nPropertyNotDefined_CurrentIsNotDefault() throws Exception {
        // GIVEN
        MgnlContext.getInstance().setLocale(Locale.FRENCH);
        initBuilder();
        Field<Object> field = fieldFactory.createField();
        field.setValue("new Value");
        // WHEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();

        // THEN
        // Should not be sufixed by _fr --> setI18n(false)
        assertEquals(true, res.hasProperty(propertyName));
    }

    @Test
    public void i18nPropertyDefined_CurrentIsDefault() throws Exception {
        // GIVEN
        this.definition.setI18n(true);
        initBuilder();
        Field<Object> field = fieldFactory.createField();
        field.setValue("new Value");
        // WHEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals(true, res.hasProperty(propertyName));
    }

    @Test
    public void i18nPropertyDefined_CurrentIsNotDefault() throws Exception {
        // GIVEN
        this.definition.setI18n(true);
        MgnlContext.getInstance().setLocale(Locale.FRENCH);
        initBuilder();
        Field<Object> field = fieldFactory.createField();
        field.setValue("new Value");

        // THEN
        // Should not be sufixed by _fr --> setI18n(false)
        assertEquals(true, field.getPropertyDataSource() instanceof I18NAwareProperty);
    }

    @Test
    public void i18nPropertyDefined_CurrentIsNotDefaultAndDefaultAlreadyExist() throws Exception {
        // GIVEN
        this.definition.setI18n(true);
        initBuilder();
        Field<Object> field = fieldFactory.createField();
        field.setValue("new Value");
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();
        assertEquals(true, res.hasProperty(propertyName));


        ((I18NAwarePropertyImpl)field.getPropertyDataSource()).setI18NPropertyName(propertyName + "_fr");
        field.setValue("new Value FR");
        // WHEN
        res = ((JcrNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals(true, res.hasProperty(propertyName + "_fr"));
        assertEquals("new Value FR", res.getProperty(propertyName + "_fr").getString());
        assertEquals("new Value", res.getProperty(propertyName).getString());
    }

    protected void initBuilder() {
        fieldFactory = new AbstractFieldFactoryTest.TestTextFieldFactory(definition, baseItem);
        fieldFactory.setI18nContentSupport(i18nContentSupport);

        MockComponentProvider cc = new MockComponentProvider();
        cc.registerImplementation(I18NAwareProperty.class, I18NAwarePropertyImpl.class);
        fieldFactory.setComponentProvider(cc);
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        ConfiguredFieldDefinition configureFieldDefinition = new ConfiguredFieldDefinition();
        configureFieldDefinition.setName(propertyName);
        configureFieldDefinition.setType("String");
        this.definition = configureFieldDefinition;
    }
}
