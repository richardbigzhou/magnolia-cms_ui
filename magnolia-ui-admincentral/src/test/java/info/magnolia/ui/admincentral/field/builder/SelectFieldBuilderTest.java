/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.field.builder;

import static org.junit.Assert.assertEquals;
import info.magnolia.ui.model.field.definition.SelectFieldDefinition;
import info.magnolia.ui.model.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;

import org.junit.Test;

import com.vaadin.ui.Field;
import com.vaadin.ui.NativeSelect;

/**
 * Main testcase for {@link DateFieldBuilder}.
 */
public class SelectFieldBuilderTest extends AbstractBuilderTest<SelectFieldDefinition> {

    private SelectFieldBuilder dialogSelect;

    @Test
    public void simpleSelectFieldTest() throws Exception{
        // GIVEN
        dialogSelect = new SelectFieldBuilder(definition, baseItem);

        // WHEN
        Field field = dialogSelect.getField();

        // THEN
        assertEquals(true, field instanceof NativeSelect);
        Collection<?> items = ((NativeSelect)field).getItemIds();
        assertEquals(3, items.size());
        assertEquals("1", field.getValue().toString());
    }


    @Test
    public void selectFieldTest_DefaultSelected() throws Exception{
        // GIVEN
        SelectFieldOptionDefinition option = definition.getOptions().get(1);
        option.setSelected(true);
        dialogSelect = new SelectFieldBuilder(definition, baseItem);

        // WHEN
        Field field = dialogSelect.getField();

        // THEN
        assertEquals(option.getValue(), field.getValue().toString());
    }

    @Test
    public void selectFieldTest_SavedDefaultSelected() throws Exception{
        // GIVEN
        baseNode.setProperty(propertyName, "3");
        baseItem = new JcrNodeAdapter(baseNode);
        dialogSelect = new SelectFieldBuilder(definition, baseItem);

        // WHEN
        Field field = dialogSelect.getField();

        // THEN
        assertEquals("3", field.getValue().toString());
    }

    @Test
    public void selectFieldTest_RemoteOptions() throws Exception{
        // GIVEN
        // Create a Options node.
        Node options = session.getRootNode().addNode("options");
        Node optionEn = options.addNode("en");
        optionEn.setProperty("value", "en");
        optionEn.setProperty("label", "English");
        Node optionFr = options.addNode("fr");
        optionFr.setProperty("value", "fr");
        optionFr.setProperty("label", "Francais");
        // Set remote Options in configuration
        definition.setPath(options.getPath());
        definition.setRepository(workspaceName);
        definition.setOptions(new ArrayList<SelectFieldOptionDefinition>());
        dialogSelect = new SelectFieldBuilder(definition, baseItem);

        // WHEN
        Field field = dialogSelect.getField();

        // THEN
        Collection<?> items = ((NativeSelect)field).getItemIds();
        assertEquals(2, items.size());
        assertEquals("en", field.getValue().toString());
    }

    @Test
    public void selectFieldTest_RemoteOptions_OtherValueANdLabelName() throws Exception{
        // GIVEN
        // Create a Options node.
        Node options = session.getRootNode().addNode("options");
        Node optionEn = options.addNode("en");
        optionEn.setProperty("x", "en");
        optionEn.setProperty("z", "English");
        Node optionFr = options.addNode("fr");
        optionFr.setProperty("x", "fr");
        optionFr.setProperty("z", "Francais");
        optionFr.setProperty("selected", "true");
        // Set remote Options in configuration
        definition.setPath(options.getPath());
        definition.setRepository(workspaceName);
        definition.setOptions(new ArrayList<SelectFieldOptionDefinition>());
        //Define the name of value and label
        definition.setValueNodeData("x");
        definition.setLabelNodeData("z");

        dialogSelect = new SelectFieldBuilder(definition, baseItem);

        // WHEN
        Field field = dialogSelect.getField();

        // THEN
        Collection<?> items = ((NativeSelect)field).getItemIds();
        assertEquals(2, items.size());
        assertEquals("fr", field.getValue().toString());
    }


    @Override
    protected void createConfiguredFieldDefinition() {
        SelectFieldDefinition fieldDefinition = new SelectFieldDefinition();
        fieldDefinition = (SelectFieldDefinition)AbstractFieldBuilderTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        fieldDefinition.setDefaultValue(null);
        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setLabel("One");
        option1.setValue("1");

        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setLabel("Two");
        option2.setValue("2");

        SelectFieldOptionDefinition option3 = new SelectFieldOptionDefinition();
        option3.setLabel("Three");
        option3.setValue("3");

        fieldDefinition.addOption(option1);
        fieldDefinition.addOption(option2);
        fieldDefinition.addOption(option3);

        this.definition = fieldDefinition;
    }

}
