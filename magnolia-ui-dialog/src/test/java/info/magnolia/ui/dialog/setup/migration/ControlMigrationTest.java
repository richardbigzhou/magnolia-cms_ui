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
package info.magnolia.ui.dialog.setup.migration;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.definition.CheckboxFieldDefinition;
import info.magnolia.ui.form.field.definition.DateFieldDefinition;
import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.StaticFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.property.SubNodesValueHandler;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class ControlMigrationTest {

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
        ControlMigration controlMigration = new EditControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(TextFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void HiddenControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "hidden");
        ControlMigration controlMigration = new HiddenControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(HiddenFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void FckEditControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "fckEdit");
        ControlMigration controlMigration = new FckEditControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(RichTextFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void DateControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "date");
        ControlMigration controlMigration = new DateControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(DateFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void SelectControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "edit");
        ControlMigration controlMigration = new SelectControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(SelectFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void CheckBoxRadioControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "radio");
        ControlMigration controlMigration = new CheckBoxRadioControlMigration(false);

        // WHEN
        controlMigration.migrate(controlNode);

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
        ControlMigration controlMigration = new CheckBoxRadioControlMigration(true);

        // WHEN
        controlMigration.migrate(controlNode);

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
        ControlMigration controlMigration = new CheckBoxSwitchControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(CheckboxFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void DamControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "dam");
        ControlMigration controlMigration = new DamControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertFalse(controlNode.hasProperty("allowedMimeType"));
        assertTrue(controlNode.hasProperty("appName"));
        assertEquals("assets", controlNode.getProperty("appName").getString());
        assertTrue(controlNode.hasNode("identifierToPathConverter"));
        assertTrue(controlNode.getNode("identifierToPathConverter").hasProperty("class"));
        assertEquals("info.magnolia.dam.app.assets.field.translator.AssetCompositeIdKeyTranslator", controlNode.getNode("identifierToPathConverter").getProperty("class").getString());
        assertTrue(controlNode.hasProperty("targetWorkspace"));
        assertEquals("dam", controlNode.getProperty("targetWorkspace").getString());

    }

    @Test
    public void LinkControlMigrationRepositoryDmsTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "link");
        controlNode.setProperty("repository", "dms");
        ControlMigration controlMigration = new LinkControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        // Set by DamControlMigration
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertFalse(controlNode.hasProperty("allowedMimeType"));
        assertTrue(controlNode.hasProperty("appName"));
        assertEquals("assets", controlNode.getProperty("appName").getString());
        assertTrue(controlNode.hasNode("identifierToPathConverter"));
        assertTrue(controlNode.getNode("identifierToPathConverter").hasProperty("class"));
        assertEquals("info.magnolia.dam.app.assets.field.translator.AssetCompositeIdKeyTranslator", controlNode.getNode("identifierToPathConverter").getProperty("class").getString());
    }

    @Test
    public void LinkControlMigrationRepositoryWebSiteTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "uuidLink");
        controlNode.setProperty("repository", "website");
        ControlMigration controlMigration = new LinkControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("appName"));
        assertEquals("pages", controlNode.getProperty("appName").getString());
        assertTrue(controlNode.hasNode("identifierToPathConverter"));
        assertTrue(controlNode.getNode("identifierToPathConverter").hasProperty("class"));
        assertEquals(BaseIdentifierToPathConverter.class.getName(), controlNode.getNode("identifierToPathConverter").getProperty("class").getString());

    }

    @Test
    public void LinkControlMigrationRepositoryDataTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "uuidLink");
        controlNode.setProperty("repository", "data");
        controlNode.setProperty("tree", "Contact");
        ControlMigration controlMigration = new LinkControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("appName"));
        assertEquals("contacts", controlNode.getProperty("appName").getString());
        assertTrue(controlNode.hasProperty("targetWorkspace"));
        assertEquals("contacts", controlNode.getProperty("targetWorkspace").getString());
    }

    @Test
    public void MultiSelectControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "categorizationUUIDMultiSelect");
        controlNode.setProperty("saveHandler", "multiple");
        ControlMigration controlMigration = new MultiSelectControlMigration(true);

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(MultiLinkFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
        assertTrue(controlNode.hasProperty("identifier"));
        assertEquals("true", controlNode.getProperty("identifier").getString());
        assertTrue(controlNode.hasNode("saveModeType"));
        assertEquals(SubNodesValueHandler.class.getName(), controlNode.getNode("saveModeType").getProperty("multiValueHandlerClass").getString());
        assertFalse(controlNode.hasProperty("saveHandler"));
    }

    @Test
    public void FileControlMigrationest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "file");
        ControlMigration controlMigration = new FileControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(BasicUploadFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

    @Test
    public void StaticControlMigrationTest() throws RepositoryException {
        // GIVEN
        controlNode.setProperty("controlType", "static");
        ControlMigration controlMigration = new StaticControlMigration();

        // WHEN
        controlMigration.migrate(controlNode);

        // THEN
        assertFalse(controlNode.hasProperty("controlType"));
        assertTrue(controlNode.hasProperty("class"));
        assertEquals(StaticFieldDefinition.class.getName(), controlNode.getProperty("class").getString());
    }

}
