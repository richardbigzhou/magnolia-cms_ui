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
package info.magnolia.ui.dialog.setup.migration;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.definition.CheckboxFieldDefinition;
import info.magnolia.ui.form.field.definition.DateFieldDefinition;
import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.StaticFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueJSONTransformer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class ControlMigratorTest {

    private MockSession session;
    private final String workspaceName = "workspace";
    private Node controlNode;

    @Before
    public void setUp() throws RepositoryException {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
        controlNode = session.getRootNode().addNode("control");
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void EditControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "edit");
        ControlMigrator controlMigration = new EditControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(TextFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void EditCodeControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "editCode");
        controlNode.setProperty("language", "generic");
        ControlMigrator controlMigration = new EditCodeControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(BasicTextCodeFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertEquals("html", controlNode.getProperty("language").getString());
    }

    @Test
    public void HiddenControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "hidden");
        ControlMigrator controlMigration = new HiddenControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(HiddenFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void FckEditControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "fckEdit");
        ControlMigrator controlMigration = new FckEditControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(RichTextFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void DateControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "date");
        ControlMigrator controlMigration = new DateControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(DateFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void SelectControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "edit");
        ControlMigrator controlMigration = new SelectControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(SelectFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void CheckBoxRadioControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "radio");
        ControlMigrator controlMigration = new CheckBoxRadioControlMigrator(false);

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(OptionGroupFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertFalse(controlNode.hasProperty("multiselect"));
    }

    @Test
    public void CheckBoxRadioControlMigrationMultipleTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "checkbox");
        ControlMigrator controlMigration = new CheckBoxRadioControlMigrator(true);

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(OptionGroupFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("multiselect"));
    }

    @Test
    public void CheckBoxSwitchControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "checkboxSwitch");
        ControlMigrator controlMigration = new CheckBoxSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(CheckboxFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("type"));
        assertEquals("String", controlNode.getProperty("type").getString());
    }

    @Test
    public void CheckBoxSwitchControlMigrationTestTypeDefined() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "checkboxSwitch");
        controlNode.setProperty("type", "Boolean");
        ControlMigrator controlMigration = new CheckBoxSwitchControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(CheckboxFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("type"));
        assertEquals("Boolean", controlNode.getProperty("type").getString());
    }



    @Test
    public void LinkControlMigrationRepositoryWebSiteTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "uuidLink");
        controlNode.setProperty("repository", "website");
        ControlMigrator controlMigration = new LinkControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("appName"));
        assertEquals("pages", controlNode.getProperty("appName").getString());
        assertTrue(controlNode.hasNode("identifierToPathConverter"));
        assertTrue(controlNode.getNode("identifierToPathConverter").hasProperty("class"));
        assertEquals(BaseIdentifierToPathConverter.class.getName(), controlNode.getNode("identifierToPathConverter").getProperty("class").getString());
        assertFalse(controlNode.hasProperty("repository"));
    }

    @Test
    public void LinkControlMigrationRepositoryDataTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "uuidLink");
        controlNode.setProperty("repository", "data");
        controlNode.setProperty("tree", "Contact");
        ControlMigrator controlMigration = new LinkControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("appName"));
        assertEquals("contacts", controlNode.getProperty("appName").getString());
        assertTrue(controlNode.hasProperty("targetWorkspace"));
        assertEquals("contacts", controlNode.getProperty("targetWorkspace").getString());
        assertFalse(controlNode.hasProperty("repository"));
    }

    @Test
    public void MultiSelectControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "multiselect");
        controlNode.setProperty("saveMode", "list");
        ControlMigrator controlMigration = new MultiSelectControlMigrator(true);

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(MultiValueFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasNode("field"));
        Node field = controlNode.getNode("field");
        assertTrue(field.hasNode("identifierToPathConverter"));
        assertEquals(BaseIdentifierToPathConverter.class.getName(), field.getNode("identifierToPathConverter").getProperty("class").getString());
        assertTrue(controlNode.hasProperty("transformerClass"));
        assertEquals(MultiValueJSONTransformer.class.getName(), controlNode.getProperty("transformerClass").getString());
        assertFalse(controlNode.hasProperty("saveHandler"));
    }



    @Test
    public void FileControlMigrationest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "file");
        ControlMigrator controlMigration = new FileControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(BasicUploadFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void StaticControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "static");
        ControlMigrator controlMigration = new StaticControlMigrator();

        // WHEN
        controlMigration.migrate(controlNode, null);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(StaticFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

}
