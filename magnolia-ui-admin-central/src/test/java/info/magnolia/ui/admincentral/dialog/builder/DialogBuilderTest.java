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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.dialog.definition.ConfiguredTabDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;
import info.magnolia.ui.widget.dialog.Dialog;
import info.magnolia.ui.widget.dialog.DialogView;

import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;


public class DialogBuilderTest {

    @Test
    public void testBuildingWithoutTabsAndActions() {
        // GIVEN
        final DialogBuilder builder = new DialogBuilder();
        final DialogDefinition def = new ConfiguredDialogDefinition();
        final Dialog dialog = new Dialog();

        // WHEN
        final DialogView result = builder.build(def, null, dialog);

        // THEN
        assertEquals(result, dialog);
    }

    @Test
    public void testBuildingWithTabsAndActions() {
        // GIVEN
        final DialogBuilder builder = new DialogBuilder();
        final DialogDefinition dialogDef = new ConfiguredDialogDefinition();
        final Dialog dialog = new Dialog();
        final TabDefinition tabDef = new ConfiguredTabDefinition();
        final FieldDefinition fieldDef = new ConfiguredFieldDefinition();
        fieldDef.setType(FieldDefinition.TEXT_FIELD_TYPE);
        ((ConfiguredFieldDefinition) fieldDef).setName("test");
        tabDef.addField(fieldDef);
        dialogDef.addTab(tabDef);

        Item item = mock(Item.class);
        Property prop = mock(Property.class);
        when(item.getItemProperty("test")).thenReturn(prop);

        // WHEN
        final DialogView result = builder.build(dialogDef, item, dialog);

        // THEN
        assertEquals(result, dialog);
    }

    @Test
    public void testBuildingCheckBox() {
        // GIVEN
        FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType(FieldDefinition.CHECKBOX_FIELD_TYPE);

        // WHEN
        Field result = FieldBuilder.build(def);

        // THEN
        assertEquals(CheckBox.class, result.getClass());
        assertEquals(FieldBuilder.TEXTFIELD_STYLE_NAME, result.getStyleName());
    }

    @Test
    public void testBuildingNullField() {
        // GIVEN
        FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType("<unkown>");

        // WHEN
        Field result = FieldBuilder.build(def);

        // THEN
        assertNull(result);
    }

}
