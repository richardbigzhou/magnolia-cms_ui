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
import info.magnolia.objectfactory.Components;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.dialog.setup.migration.ControlMigratorsRegistry;
import info.magnolia.ui.dialog.setup.migration.EditCodeControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FckEditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.LinkControlMigrator;
import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class RadioSwitchControlMigratorTest extends RepositoryTestCase {
    private Node controlNode;
    private RadioSwitchControlMigrator controlMigration;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("config.modules.form.dialogs.form.tabConfirmEmail.confirmContentType.xml");
        DataTransporter.importXmlStream(
                xmlStream,
                "config",
                "/root",
                "name matters only when importing a file that needs XSL transformation",
                false,
                ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
                true,
                true);

        controlNode = MgnlContext.getJCRSession("config").getNode("/root/confirmContentType");

        ControlMigratorsRegistry registery = Components.getComponent(ControlMigratorsRegistry.class);
        registery.register("edit", new EditControlMigrator());
        registery.register("fckEdit", new FckEditControlMigrator());
        registery.register("editCode", new EditCodeControlMigrator());
        registery.register("link", new LinkControlMigrator());

    }

    @Test
    public void testSwitchableFieldCreation() throws RepositoryException {
        // GIVEN
        controlMigration = new RadioSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertNotNull(controlNode);
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(SwitchableFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasNode("options"));
        assertTrue(controlNode.hasNode("fields"));
    }

    @Test
    public void testSwitchableOptionsCreation() throws RepositoryException {
        // GIVEN
        controlMigration = new RadioSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertNotNull(controlNode);
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(SwitchableFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasNode("options"));
        assertTrue(controlNode.hasNode("fields"));
    }

    @Test
    public void testSwitchableFieldFieldsCreation() throws RepositoryException {
        // GIVEN
        controlMigration = new RadioSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("fields"));
        Node fields = controlNode.getNode("fields");
        assertTrue(fields.hasNodes());
        assertTrue(fields.hasNode("html"));
        assertTrue(fields.hasNode("text"));
    }

    @Test
    public void testSwitchableFieldsHtmlCreation() throws RepositoryException {
        // GIVEN
        controlMigration = new RadioSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("fields/html"));
        Node html = controlNode.getNode("fields/html");
        assertTrue(html.hasProperty("class"));
        assertEquals(BasicTextCodeFieldDefinition.class.getName(), html.getProperty("class").getString());
        assertTrue(html.hasProperty("language"));
        assertEquals("html", html.getProperty("language").getString());

    }

    @Test
    public void testSwitchableFieldsTextCreation() throws RepositoryException {
        // GIVEN
        controlMigration = new RadioSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertTrue(controlNode.hasNode("fields/text"));
        Node text = controlNode.getNode("fields/text");
        assertTrue(text.hasProperty("class"));
        assertEquals(TextFieldDefinition.class.getName(), text.getProperty("class").getString());
    }
}
