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
package info.magnolia.ui.framework.setup;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;

import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class ReplaceMultiLinkFieldDefinitionTaskTest extends RepositoryTestCase {

    private Node field;
    private String query = " select * from [nt:base] as t where contains(t.*,'info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition') ";
    private ReplaceMultiLinkFieldDefinitionTask queryTask;
    private InstallContextImpl installContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        installContext = new InstallContextImpl(moduleRegistry);

        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("config.modules.categorization.dialogs.generic.xml");
        DataTransporter.importXmlStream(
                xmlStream,
                RepositoryConstants.CONFIG,
                "/modules",
                "",
                false,
                ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
                true,
                true);

        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        field = session.getNode("/modules/generic/tabCategorization/fields/categories");

        queryTask = new ReplaceMultiLinkFieldDefinitionTask("name", "description", RepositoryConstants.CONFIG, query);

    }

    @Test
    public void testCreationOfFieldSubNode() throws RepositoryException, TaskExecutionException {
        // GIVEN

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertTrue(field.hasNode("field"));
        assertTrue(field.getNode("field").hasProperty("class"));
        assertEquals(LinkFieldDefinition.class.getName(), field.getNode("field").getProperty("class").getString());

    }

    @Test
    public void testMoveOfIdentifierToPathConverterNode() throws RepositoryException, TaskExecutionException {
        // GIVEN

        // WHEN
        queryTask.execute(installContext);
        field.getSession().save();

        // THEN
        assertFalse(field.hasNode("identifierToPathConverter"));
        assertTrue(field.getNode("field").hasNode("identifierToPathConverter"));
    }



    @Test
    public void testMoveOfPropertiesToField() throws RepositoryException, TaskExecutionException {
        // GIVEN

        // WHEN
        queryTask.execute(installContext);

        // THEN
        Node newField = field.getNode("field");
        assertFalse(field.hasProperty("appName"));
        assertTrue(newField.hasProperty("appName"));
        assertFalse(field.hasProperty("buttonSelectNewLabel"));
        assertTrue(newField.hasProperty("buttonSelectNewLabel"));
        assertFalse(field.hasProperty("buttonSelectOtherLabel"));
        assertTrue(newField.hasProperty("buttonSelectOtherLabel"));
        assertFalse(field.hasProperty("fieldEditable"));
        assertTrue(newField.hasProperty("fieldEditable"));
        assertFalse(field.hasProperty("targetWorkspace"));
        assertTrue(newField.hasProperty("targetWorkspace"));
        assertFalse(field.hasProperty("type"));
        assertTrue(newField.hasProperty("type"));
    }

    @Test
    public void testOnlyTakeActionOnNodeDefinedUnderFieldsDefinition() throws RepositoryException, TaskExecutionException {
        // GIVEN
        Node tabCategorization = field.getParent().getParent();
        Node notField = tabCategorization.addNode("notField", NodeTypes.ContentNode.NAME);
        notField.setProperty("class", "info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition");

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertFalse(notField.hasNode("field"));

    }

}
