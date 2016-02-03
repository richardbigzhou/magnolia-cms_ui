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
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Collection;

import org.junit.Test;

import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.OptionGroupFieldFactory}.
 */
public class OptionGroupFieldFactoryTest extends AbstractFieldFactoryTestCase<OptionGroupFieldDefinition> {

    private OptionGroupFieldFactory dialogSelect;

    @Test
    public void simpleRadioFieldTest() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, new MockComponentProvider());
        dialogSelect.setI18nContentSupport(i18nContentSupport);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertEquals(true, field instanceof OptionGroup);
        Collection<?> items = ((OptionGroup) field).getItemIds();
        assertEquals(3, items.size());
        assertEquals("1", field.getValue().toString());
    }

    @Test
    public void selectedRadioFieldTest() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        definition.getOptions().get(2).setSelected(true);
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, new MockComponentProvider());
        dialogSelect.setI18nContentSupport(i18nContentSupport);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertEquals("3", field.getValue().toString());
    }

    @Test
    public void simpleCheckBoxFieldTest() throws Exception {
        // GIVEN
        definition.setMultiselect(true);
        dialogSelect = new OptionGroupFieldFactory(definition, baseItem, new MockComponentProvider());
        dialogSelect.setI18nContentSupport(i18nContentSupport);

        // WHEN
        Field field = dialogSelect.createField();

        // THEN
        assertEquals(true, field instanceof OptionGroup);
        Collection<?> items = ((OptionGroup) field).getItemIds();
        assertEquals(3, items.size());
        assertEquals("[]", field.getValue().toString());
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
