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
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.MultiField;
import info.magnolia.ui.form.field.definition.MultiFieldDefinition;
import info.magnolia.ui.form.field.property.list.MultiValuesPropertyListHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import org.junit.Test;

import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.MultiFieldFactory}.
 */
public class MultiFieldFactoryTest extends AbstractFieldFactoryTestCase<MultiFieldDefinition> {

    private MultiFieldFactory multiFieldFactory;
    private ComponentProvider componentProvider;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        componentProvider = mock(ComponentProvider.class);
    }

    @Test
    public void testGetField() throws Exception {
        // GIVEN
        when(componentProvider.newInstance(MultiValuesPropertyListHandler.class, baseItem, definition.getName())).thenReturn(new MultiValuesPropertyListHandler((JcrNodeAdapter) baseItem, propertyName));
        multiFieldFactory = new MultiFieldFactory(definition, baseItem, null, null, componentProvider);
        multiFieldFactory.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = multiFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof MultiField);
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        MultiFieldDefinition fieldDefinition = new MultiFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
