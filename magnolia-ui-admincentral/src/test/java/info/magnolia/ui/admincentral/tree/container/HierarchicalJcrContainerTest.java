/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.tree.container;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.container.AbstractJcrContainerTest;
import info.magnolia.ui.admincentral.content.view.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionRegistry;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.model.workbench.definition.ItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.junit.Before;
import org.junit.Test;


/**
 * Main test class for {HierarchicalJcrContainer}
 */
public class HierarchicalJcrContainerTest extends RepositoryTestCase {

    private HierarchicalJcrContainer hierarchicalJcrContainer;

    private WorkbenchDefinition workbenchDefinition;

    private TreeModel treeModel;

    private final String workspace = "config";

    private final String colName1 = "name";

    private final String colName2 = "shortname";

    private Session session;

    Node rootNode;

    @SuppressWarnings("deprecation")
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init
        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(workspace);
        configuredWorkbench.setPath("/");
        // Init workBench
        WorkbenchActionRegistry workbenchActionRegistry = mock(WorkbenchActionRegistry.class);
        when(workbenchActionRegistry.getDefinitionToImplementationMappings()).thenReturn(new ArrayList<DefinitionToImplementationMapping<ActionDefinition,Action>>());
        WorkbenchActionFactory workbenchActionFactory = new WorkbenchActionFactoryImpl(null,workbenchActionRegistry);
        // Init col
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_" + colName1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_" + colName2);

        configuredWorkbench.addColumn(colDef1);
        configuredWorkbench.addColumn(colDef2);

        ItemTypeDefinition itemType = new ConfiguredItemTypeDefinition();
        ((ConfiguredItemTypeDefinition) itemType).setItemType("mgnl:content");
        configuredWorkbench.setItemTypes(Arrays.asList(itemType));

        workbenchDefinition = configuredWorkbench;

        treeModel = new TreeModel(workbenchDefinition, workbenchActionFactory);

        hierarchicalJcrContainer = new HierarchicalJcrContainer(treeModel, workbenchDefinition);

        // Init session
        session = MgnlContext.getSystemContext().getJCRSession(workspace);
        rootNode = session.getRootNode();
    }

    @Test
    public void testGetItem_NodeType() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        node1.getSession().save();
        String containerItemId = node1.getPath();

        // WHEN
        com.vaadin.data.Item item = hierarchicalJcrContainer.getItem(containerItemId);

        // THEN
        assertEquals(true, item instanceof JcrNodeAdapter);
        assertEquals(node1.getPath(), ((JcrNodeAdapter) item).getJcrItem().getPath());
    }

    @Test
    public void testAreChildrenAllowed_true() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        node1.getSession().save();
        String containerItemId = node1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.areChildrenAllowed(containerItemId);

        // THEN
        assertEquals(true, res);
    }

    @Test
    public void testAreChildrenAllowed_false() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        node1.getSession().save();
        String containerItemId = node1.getProperty("name").getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.areChildrenAllowed(containerItemId);

        // THEN
        assertEquals(false, res);
    }

    @Test
    public void testRootItemIds() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        AbstractJcrContainerTest.createNode(node1, "node1_1", "mgnl:content", "name", "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", "mgnl:content", "name", "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();
        String containerItemId2 = node2.getPath();

        // WHEN
        Collection<String> res = hierarchicalJcrContainer.rootItemIds();

        // THEN
        assertEquals(2, res.size());
        assertEquals(true, res.contains(containerItemId1));
        assertEquals(true, res.contains(containerItemId2));
    }

    @Test
    public void testisRoot_true() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        AbstractJcrContainerTest.createNode(node1, "node1_1", "mgnl:content", "name", "name1_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.isRoot(containerItemId1);

        // THEN
        assertEquals(true, res);
    }

    @Test
    public void testisRoot_false() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", "mgnl:content", "name", "name1_1");
        node1.getSession().save();

        String containerItemId1_1 = node1_1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.isRoot(containerItemId1_1);

        // THEN
        assertEquals(false, res);
    }

    @Test
    public void testGetChildren() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", "mgnl:content", "name", "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", "mgnl:content", "name", "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        Collection<String> res = hierarchicalJcrContainer.getChildren(containerItemId1);

        // THEN
        assertEquals(1, res.size());
        assertEquals(true, res.contains(node1_1.getPath()));
    }

    @Test
    public void testGetParent() throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", "mgnl:content", "name", "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", "mgnl:content", "name", "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        String res = hierarchicalJcrContainer.getParent(node1_1.getPath());

        // THEN
        assertEquals(containerItemId1, res);
    }

}
