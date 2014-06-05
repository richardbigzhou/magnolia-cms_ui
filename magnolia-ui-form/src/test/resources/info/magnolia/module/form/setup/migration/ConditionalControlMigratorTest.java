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
package info.magnolia.module.form.setup.migration;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.transformer.composite.NoOpCompositeTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodePropertiesTransformer;

import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class ConditionalControlMigratorTest extends RepositoryTestCase {
    private Node controlNode;
    private ConditionalControlMigrator migrationTask;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("config.modules.form.dialogs.formCondition.tabMain.condition.xml");
        DataTransporter.importXmlStream(
                xmlStream,
                "config",
                "/root",
                "name matters only when importing a file that needs XSL transformation",
                false,
                ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
                true,
                true);

        controlNode = MgnlContext.getJCRSession("config").getNode("/root/condition");

    }

    @Test
    public void testMultiFieldCreation() throws RepositoryException {
        // GIVEN
        migrationTask = new ConditionalControlMigrator();

        // WHEN
        migrationTask.migrate(controlNode, null);

        // THEN
        assertNotNull(controlNode);
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(MultiValueFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("transformerClass"));
        assertEquals(MultiValueSubChildrenNodePropertiesTransformer.class.getName(), controlNode.getProperty("transformerClass").getString());

        assertTrue(controlNode.hasNode("field"));
    }

    @Test
    public void testCompositeFieldCreation() throws RepositoryException {
        // GIVEN
        migrationTask = new ConditionalControlMigrator();

        // WHEN
        migrationTask.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("field"));
        Node field = controlNode.getNode("field");
        assertTrue(field.hasProperty("class"));
        assertEquals(CompositeFieldDefinition.class.getName(), field.getProperty("class").getString());

        assertTrue(field.hasProperty("transformerClass"));
        assertEquals(NoOpCompositeTransformer.class.getName(), field.getProperty("transformerClass").getString());

        assertTrue(field.hasNode("fields"));
    }

    @Test
    public void testCompositeFieldOptionsCreation() throws RepositoryException {
        // GIVEN
        migrationTask = new ConditionalControlMigrator();

        // WHEN
        migrationTask.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("field/fields/condition"));
        Node condition = controlNode.getNode("field/fields/condition");
        assertTrue(condition.hasProperty("class"));
        assertEquals(SelectFieldDefinition.class.getName(), condition.getProperty("class").getString());
        assertTrue(condition.hasNode("options"));
        Node optionsList = condition.getNode("options");
        assertTrue(optionsList.hasNodes());
        assertTrue(optionsList.hasNode("condition1"));
        assertTrue(optionsList.getNode("condition1").hasProperty("label"));
        assertTrue(optionsList.getNode("condition1").hasProperty("value"));
        assertTrue(optionsList.hasNode("condition2"));
        assertTrue(optionsList.hasNode("condition3"));
    }

    @Test
    public void testCompositeFieldTextValueCreation() throws RepositoryException {
        // GIVEN
        migrationTask = new ConditionalControlMigrator();

        // WHEN
        migrationTask.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("field/fields/fieldValue"));
        Node text = controlNode.getNode("field/fields/fieldValue");
        assertTrue(text.hasProperty("class"));
        assertEquals(TextFieldDefinition.class.getName(), text.getProperty("class").getString());
    }

    @Test
    public void testCompositeFieldTextNameCreation() throws RepositoryException {
        // GIVEN
        migrationTask = new ConditionalControlMigrator();

        // WHEN
        migrationTask.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("field/fields/fieldName"));
        Node text = controlNode.getNode("field/fields/fieldName");
        assertTrue(text.hasProperty("class"));
        assertEquals(TextFieldDefinition.class.getName(), text.getProperty("class").getString());

    }
}
