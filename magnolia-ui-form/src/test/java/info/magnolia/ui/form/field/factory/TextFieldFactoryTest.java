/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import org.junit.Test;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.TextFieldFactory}.
 */
public class TextFieldFactoryTest extends AbstractFieldFactoryTestCase<TextFieldDefinition> {

    private TextFieldFactory dialogEdit;

    @Test
    public void createSingleRowEditFieldTest() {
        // GIVEN
        dialogEdit = new TextFieldFactory(definition, baseItem);
        dialogEdit.setComponentProvider(new MockComponentProvider());

        // WHEN
        Field field = dialogEdit.createField();

        // THEN
        assertEquals(true, field instanceof TextField);
        assertEquals(-1, ((TextField) field).getMaxLength());
    }

    @Test
    public void createMultiRowEditField() {
        // GIVEN
        definition.setRows(2);
        definition.setMaxLength(250);
        dialogEdit = new TextFieldFactory(definition, baseItem);
        dialogEdit.setComponentProvider(new MockComponentProvider());

        // WHEN
        Field field = dialogEdit.createField();

        // THEN
        assertEquals(true, field instanceof TextArea);
        assertEquals(2, ((TextArea) field).getRows());
        assertEquals(250, ((TextArea) field).getMaxLength());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        TextFieldDefinition fieldDefinition = new TextFieldDefinition();
        fieldDefinition = (TextFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);

        fieldDefinition.setRows(0);

        this.definition = fieldDefinition;
    }

}
