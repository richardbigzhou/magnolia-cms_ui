/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import static org.junit.Assert.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * test class.
 */
public class DataTypeMigrationTaskTest extends AbstractAbstractDataTypeMigrationTaskTest {

    @Test
    public void testSimpleExecute() throws TaskExecutionException, RepositoryException {
        // GIVEN
        TestAbstractDataTypeToWorkspaceMigrationTask migrationTask = new TestAbstractDataTypeToWorkspaceMigrationTask("taskName", "taskDescription", "/test", "/", getTargetWorkSpaceName());

        // WHEN
        migrationTask.execute(installContext);

        // THEN
        Node rootNode = targetSession.getRootNode();
        assertTrue(!rootNode.hasNode("MetaData"));
        assertTrue(rootNode.hasNode("Family"));
        Node family = rootNode.getNode("Family");
        assertTrue(family.hasProperty("level"));
        assertEquals("mgnl:test", family.getPrimaryNodeType().getName());
        assertTrue(family.hasNode("relatedUUID"));
        Node relatedUUID = family.getNode("relatedUUID");
        assertEquals(NodeTypes.Content.NAME, relatedUUID.getPrimaryNodeType().getName());
        assertTrue(relatedUUID.hasProperty("level"));
        assertTrue(relatedUUID.hasNode("child_1"));
        Node child1 = relatedUUID.getNode("child_1");
        assertEquals(NodeTypes.Content.NAME, child1.getPrimaryNodeType().getName());
        assertTrue(rootNode.hasNode("Sport"));
        Node sport = rootNode.getNode("Sport");
        assertEquals("mgnl:test", sport.getPrimaryNodeType().getName());
        assertFalse(sport.hasNode("MetaData"));
    }

    @Test
    public void testSimpleExecuteWithTargetPathDefine() throws TaskExecutionException, RepositoryException {
        // GIVEN
        TestAbstractDataTypeToWorkspaceMigrationTask migrationTask = new TestAbstractDataTypeToWorkspaceMigrationTask("taskName", "taskDescription", "/test", "/tata/titi/toto", getTargetWorkSpaceName());

        // WHEN
        migrationTask.execute(installContext);

        // THEN
        Node root = targetSession.getRootNode();
        assertTrue(root.hasNode("tata/titi/toto"));
        Node rootNode = root.getNode("tata/titi/toto");
        assertEquals(NodeTypes.Folder.NAME, rootNode.getPrimaryNodeType().getName());
        assertTrue(rootNode.hasNode("Family"));
        Node sport = rootNode.getNode("Sport");
        assertEquals("mgnl:test", sport.getPrimaryNodeType().getName());
        assertFalse(sport.hasNode("MetaData"));
    }

    @Test(expected = TaskExecutionException.class)
    public void testSimpleExecuteWithRootAsSourcePath() throws TaskExecutionException, RepositoryException {
        // GIVEN
        TestAbstractDataTypeToWorkspaceMigrationTask migrationTask = new TestAbstractDataTypeToWorkspaceMigrationTask("taskName", "taskDescription", "/", "/tata/titi", getTargetWorkSpaceName());

        // WHEN
        migrationTask.execute(installContext);

        // THEN
    }

    @Test
    public void testSimpleExecuteWithInvalidSourcePath() throws TaskExecutionException, RepositoryException {
        // GIVEN
        TestAbstractDataTypeToWorkspaceMigrationTask migrationTask = new TestAbstractDataTypeToWorkspaceMigrationTask("taskName", "taskDescription", "/toto", "/tata/titi", getTargetWorkSpaceName());

        // WHEN
        migrationTask.execute(installContext);

        // THEN
        assertTrue(installContext.getMessages().get("General messages").get(0).getMessage().startsWith("Data migration task cancelled. The following "));
    }

    @Override
    protected String getDataRepositoryDefinition() {
        return "test-data-repositories.xml";
    }

    @Override
    protected String getDataNodeStructureDefinitionFileName() {
        return "testNodeTree.properties";
    }

    @Override
    protected String getTargetWorkSpaceName() {
        return "test";
    }

    @Override
    protected String getTargetNodeTypeDefinition() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<nodeTypes" + " xmlns:rep=\"internal\""
                + " xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"" + " xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
                + " xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\"" + " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">" + "<nodeType name=\"mgnl:test\""
                + " isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">"
                + "<supertypes>"
                + "<supertype>mgnl:content</supertype>"
                + "</supertypes>"
                + "</nodeType>" + "</nodeTypes>";
    }

    @Override
    protected String getDataNodeTypeDefinition() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nodeTypes xmlns:rep=\"internal\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">"
                + " <nodeType name=\"dataBase\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>mix:referenceable</supertype><supertype>nt:hierarchyNode</supertype><supertype>mgnl:created</supertype><supertype>mgnl:lastModified</supertype></supertypes><propertyDefinition name=\"*\" requiredType=\"undefined\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"false\"/><childNodeDefinition name=\"*\" defaultPrimaryType=\"\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" sameNameSiblings=\"true\"/></nodeType>"
                + " <nodeType name=\"dataItemBase\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataBase</supertype></supertypes></nodeType>"
                + " <nodeType name=\"dataFolder\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataBase</supertype><supertype>mgnl:activatable</supertype></supertypes></nodeType>"
                + " <nodeType name=\"dataItem\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataItemBase</supertype><supertype>mgnl:activatable</supertype><supertype>mgnl:versionable</supertype></supertypes></nodeType>"
                + " <nodeType name=\"dataItemNode\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataItemBase</supertype><supertype>mgnl:activatable</supertype></supertypes></nodeType>"
                + " </nodeTypes>";
    }

    @Override
    protected String getCustomNodeTypeDefinition() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<nodeTypes" + " xmlns:rep=\"internal\""
                + " xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"" + " xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
                + " xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\"" + " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">" + "<nodeType name=\"test\""
                + " isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">"
                + "<supertypes>"
                + "<supertype>mgnl:content</supertype>"
                + "</supertypes>"
                + "</nodeType>" + "</nodeTypes>";
    }

    private class TestAbstractDataTypeToWorkspaceMigrationTask extends AbstractDataTypeMigrationTask {

        public TestAbstractDataTypeToWorkspaceMigrationTask(String taskName, String taskDescription, String dataRootPath, String newRootPath, String newWorkspaceName) {
            super(taskName, taskDescription, dataRootPath, newRootPath, newWorkspaceName);
        }

        @Override
        protected void initOldToNewNodeTypeMappingElement(HashMap<String, String> oldToNewNodeTypeMapping) {
            oldToNewNodeTypeMapping.put("dataFolder", NodeTypes.Folder.NAME);
            oldToNewNodeTypeMapping.put("test", "mgnl:test");
            oldToNewNodeTypeMapping.put("dataItemNode", NodeTypes.Content.NAME);

        }
    }
}
