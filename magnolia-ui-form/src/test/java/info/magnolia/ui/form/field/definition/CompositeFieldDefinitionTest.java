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
package info.magnolia.ui.form.field.definition;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CompositeFieldDefinition}.
 */
public class CompositeFieldDefinitionTest {

    private CompositeFieldDefinition compositeFieldDefinition = new CompositeFieldDefinition();

    private List<ConfiguredFieldDefinition> fields;

    @Before
    public void setUp() {
        ConfiguredFieldDefinition field;

        fields = new ArrayList<ConfiguredFieldDefinition>();

        field = new ConfiguredFieldDefinition();
        field.setName("a");
        fields.add(field);
        field = new ConfiguredFieldDefinition();
        field.setName("b");
        fields.add(field);
    }

    /**
     * Ensure that getFieldNames works.
     */
    @Test
    public void testGetFieldNamesWithSetFields() {
        // GIVEN
        compositeFieldDefinition.setFields(fields);

        // WHEN
        List<String> names = compositeFieldDefinition.getFieldNames();

        // THEN
        assertThat(names, is(Arrays.asList("a", "b")));
    }

    /**
     * Ensure that calling getFields before setFields does not impact the result of getFieldNames()
     * This was the key problem in MGNLUI-3402.
     */
    @Test
    public void callGetFieldsNamesBeforeSetFields() {
        // GIVEN
        List<String> names = compositeFieldDefinition.getFieldNames();
        compositeFieldDefinition.setFields(fields);

        // WHEN
        names = compositeFieldDefinition.getFieldNames();

        // THEN
        assertThat(names, is(Arrays.asList("a", "b")));
    }

    /**
     * Ensure that getFieldNames works with setFields, addField and addFieldName.
     */
    @Test
    public void testGetFieldNamesWithAllSetters() {
        // GIVEN
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("added");
        compositeFieldDefinition.addField(field);
        compositeFieldDefinition.addFieldName("just-a-name");
        compositeFieldDefinition.setFields(fields);

        // WHEN
        List<String> names = compositeFieldDefinition.getFieldNames();

        // THEN
        assertThat(names, is(Arrays.asList("added", "just-a-name", "a", "b")));
    }

}
