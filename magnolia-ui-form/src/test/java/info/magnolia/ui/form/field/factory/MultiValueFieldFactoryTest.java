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
package info.magnolia.ui.form.field.factory;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.ui.form.field.MultiField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.Iterator;

import org.junit.Test;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.MultiValueFieldFactory}.
 */
public class MultiValueFieldFactoryTest extends AbstractFieldFactoryTestCase<MultiValueFieldDefinition> {

    private MultiValueFieldFactory multiFieldFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FieldFactoryFactory fieldFactoryFactory = mock(FieldFactoryFactory.class);
        ConfiguredFieldDefinition fieldDefinition = new ConfiguredFieldDefinition();

        final FieldFactory fieldFactory = mock(FieldFactory.class);
        final AbstractField field = mock(AbstractField.class);


        multiFieldFactory = new MultiValueFieldFactory(definition, baseItem, fieldFactoryFactory, componentProvider, i18NAuthoringSupport);
        baseItem.addItemProperty(propertyName, DefaultPropertyUtil.newDefaultProperty(String.class, "value"));

        definition.setDefaultValue("defaultValue");
        definition.setField(fieldDefinition);

        doReturn(fieldFactory).when(fieldFactoryFactory).createFieldFactory(eq(fieldDefinition), anyVararg());
        doReturn(field).when(fieldFactory).createField();
    }

    @Test
    public void testGetField() throws Exception {
        // GIVEN

        // WHEN
        Field field = multiFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof MultiField);
    }

    @Test
    public void areButtonsInvisibleWhenReadOnly() {

        // GIVEN
        definition.setReadOnly(true);

        // WHEN
        MultiField multiField = (MultiField) multiFieldFactory.createField();

        // THEN
        assertTrue(multiField.isReadOnly());
        assertTrue(definition.getField().isReadOnly());
        assertFalse(isExistButtons(multiField));
    }

    @Test
    public void areButtonsVisibleWhenEditable() {

        // GIVEN
        definition.setReadOnly(false);

        // WHEN
        MultiField multiField = (MultiField) multiFieldFactory.createField();

        // THEN
        assertFalse(multiField.isReadOnly());
        assertFalse(definition.getField().isReadOnly());
        assertTrue(isExistButtons(multiField));
    }

    private boolean isExistButtons(MultiField multiField) {
        VerticalLayout root = (VerticalLayout) multiField.iterator().next();
        Iterator<Component> it = root.iterator();
        boolean isExistButtons = false;
        while (it.hasNext()) {
            Component component = it.next();
            if (component instanceof NativeButton || component instanceof Button) {
                isExistButtons = true;
            }
        }

        return isExistButtons;
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        MultiValueFieldDefinition fieldDefinition = new MultiValueFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
