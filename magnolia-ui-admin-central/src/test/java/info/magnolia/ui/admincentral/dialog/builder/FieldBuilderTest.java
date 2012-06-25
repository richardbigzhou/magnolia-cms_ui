/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.dialog.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import info.magnolia.ui.model.dialog.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;

import org.junit.Test;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

public class FieldBuilderTest {

    @Test
    public void testBuildingTextField() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType(FieldDefinition.TEXT_FIELD_TYPE);

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertEquals(TextField.class, result.getClass());
        assertEquals(FieldBuilder.TEXTFIELD_STYLE_NAME, result.getStyleName());
    }

    @Test
    public void testBuildingCheckBox() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType(FieldDefinition.CHECKBOX_FIELD_TYPE);

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertEquals(CheckBox.class, result.getClass());
        assertEquals(FieldBuilder.TEXTFIELD_STYLE_NAME, result.getStyleName());
    }

    @Test
    public void testBuildingNullField() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType("<unkown>");

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertNull(result);
    }

}
