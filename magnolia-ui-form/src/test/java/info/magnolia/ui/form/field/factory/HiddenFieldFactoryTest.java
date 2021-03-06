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

import static org.junit.Assert.*;

import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;

import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.HiddenFieldFactory}.
 */
public class HiddenFieldFactoryTest extends AbstractFieldFactoryTestCase<HiddenFieldDefinition> {

    private HiddenFieldFactory factory;

    @Test
    public void testGetHiddenFieldPropertyDataSourceWhenItemNodeDoesNotIncludeHiddenProperty() throws Exception {
        // GIVEN
        definition.setDefaultValue("test");
        factory = new HiddenFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        factory.setComponentProvider(componentProvider);

        // WHEN
        Field<?> field = factory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertNotNull(p);
        assertEquals("test", p.getValue().toString());
        assertEquals("test", baseItem.getItemProperty("hiddenProperty").getValue());
        assertEquals("test", field.getValue());
    }

    @Test
    public void testGetHiddenFieldPropertyDataSourceWhenItemNodeIncludesHiddenProperty() throws Exception {
        // GIVEN
        definition.setDefaultValue("test2");
        baseItem.addItemProperty("hiddenProperty", new ObjectProperty<>("test1"));
        factory = new HiddenFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        factory.setComponentProvider(componentProvider);

        // WHEN
        Field<?> field = factory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertNotNull(p);
        assertEquals("test1", p.getValue().toString());
        assertEquals("test1", baseItem.getItemProperty("hiddenProperty").getValue());
        assertEquals("test1", field.getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        HiddenFieldDefinition fieldDefinition = new HiddenFieldDefinition();
        fieldDefinition = (HiddenFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, "hiddenProperty");

        this.definition = fieldDefinition;
    }

}
