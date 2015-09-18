/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Collection;

import org.junit.Test;

import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;


public class OptionGroupFieldFactoryTest extends AbstractFieldFactoryTestCase<OptionGroupFieldDefinition> {

    private OptionGroupFieldFactory dialogSelect;

    @Test
    public void simpleRadioFieldTest() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field<?> field = dialogSelect.createField();

        // THEN
        assertTrue(field instanceof OptionGroup);
        assertEquals(3, ((OptionGroup) field).getItemIds().size());
    }

    @Test
    public void testNoPreselectedRadioField() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertNull(field.getValue());
    }

    @Test
    public void testSelectedRadioField() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        definition.getOptions().get(2).setSelected(true);
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertEquals("3", field.getValue().toString());
    }

    @Test
    public void testSelectedRadioFieldOnPreexistingNodeWithNullValue() throws Exception {
        // GIVEN
        // we keep baseItem as a regular JcrNodeAdapter here
        definition.getOptions().get(2).setSelected(true);
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertNull(field.getValue());
    }

    @Test
    public void simpleCheckBoxFieldTest() throws Exception {
        // GIVEN
        definition.setMultiselect(true);
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertTrue(field instanceof OptionGroup);
        Collection<?> items = ((OptionGroup) field).getItemIds();
        assertEquals(3, items.size());
        assertEquals("[]", field.getValue().toString());
    }

    @Test
    public void createFieldSelectsDefaultOption() throws Exception {
        // GIVEN
        SelectFieldOptionDefinition option1 = definition.getOptions().get(0);
        option1.setSelected(true);
        SelectFieldOptionDefinition option2 = definition.getOptions().get(1);
        option2.setSelected(true);

        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertEquals("Option is not multiselect so the first selected is taken ", option1.getValue(), field.getValue().toString());
    }

    @Test
    public void createFieldSelectsDefaultOptions() throws Exception {
        // GIVEN
        definition.setMultiselect(true);
        SelectFieldOptionDefinition option1 = definition.getOptions().get(0);
        option1.setSelected(true);
        SelectFieldOptionDefinition option2 = definition.getOptions().get(1);
        option2.setSelected(true);

        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, componentProvider);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertTrue(field.getValue() instanceof Collection);
        assertTrue(((Collection) field.getValue()).contains("1"));
        assertTrue(((Collection) field.getValue()).contains("2"));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        OptionGroupFieldDefinition fieldDefinition = new OptionGroupFieldDefinition();
        fieldDefinition = (OptionGroupFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
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
