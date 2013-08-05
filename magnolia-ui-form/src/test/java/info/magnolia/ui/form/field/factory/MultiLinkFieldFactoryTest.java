/**
 * This file Copyright (c) 2013 Magnolia International
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

import static org.junit.Assert.assertEquals;

import info.magnolia.ui.form.field.MultiLinkField;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;
import info.magnolia.ui.form.field.definition.PropertyBuilder;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.form.field.property.list.ListProperty;
import info.magnolia.ui.form.field.property.list.MultiValuesPropertyListHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.MultiLinkFieldFactory}.
 */
public class MultiLinkFieldFactoryTest extends AbstractFieldFactoryTestCase<MultiLinkFieldDefinition> {

    private MultiLinkFieldFactory multiLinkFieldFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetField() throws Exception {
        // GIVEN
        PropertyBuilder propertyBuilder = new PropertyBuilder();
        propertyBuilder.setPropertyHandler((Class<? extends PropertyHandler<?>>) (Object) MultiValuesPropertyListHandler.class);
        propertyBuilder.setPropertyType((Class<? extends Property<?>>) (Object) ListProperty.class);
        definition.setPropertyBuilder(propertyBuilder);
        MultiValuesPropertyListHandler handler = new MultiValuesPropertyListHandler((JcrNodeAdapter) baseItem, definition, null);
        provider = new SimpleComponentProvider(handler, new ListProperty(handler));

        multiLinkFieldFactory = new MultiLinkFieldFactory(definition, baseItem, null, null, provider);
        multiLinkFieldFactory.setI18nContentSupport(i18nContentSupport);
        multiLinkFieldFactory.setComponentProvider(provider);
        // WHEN
        Field field = multiLinkFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof MultiLinkField);
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        MultiLinkFieldDefinition fieldDefinition = new MultiLinkFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
