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
package info.magnolia.ui.admincentral.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.PropertyTypeColumn;
import info.magnolia.ui.admincentral.content.view.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionRegistry;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.RowIterator;
import javax.jcr.version.VersionException;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Main test class for {AbstractJcrContainer}.
 */
public class AbstractJcrContainerTest extends RepositoryTestCase{

    private JcrContainerTestImpl jcrContainer;
    private WorkbenchDefinition workbenchDefinition;
    private TreeModel treeModel;
    private String workspace = "config";
    private String colName1 = "name";
    private String colName2 = "shortname";
    private Session session;
    Node rootNode;

    @Override
    @Before
    public void setUp() throws Exception{
        super.setUp();
        //Init
        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(workspace);
        configuredWorkbench.setPath("/");
        //Init workBench
        WorkbenchActionRegistry workbenchActionRegistry = mock(WorkbenchActionRegistry.class);
        when(workbenchActionRegistry.getDefinitionToImplementationMappings()).thenReturn(new ArrayList<DefinitionToImplementationMapping<ActionDefinition,Action>>());
         WorkbenchActionFactory workbenchActionFactory = new WorkbenchActionFactoryImpl(null, workbenchActionRegistry);
        //Init col
        Map<String, Column<?>> columns = new LinkedHashMap<String, Column<?>>();
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_"+colName1);
        Column<AbstractColumnDefinition> col1 = new PropertyTypeColumn(colDef1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_"+colName2);
        Column<AbstractColumnDefinition> col2 = new PropertyTypeColumn(colDef2);

        columns.put(colName1, col1);
        columns.put(colName2, col2);
        configuredWorkbench.addColumn(colDef1);
        configuredWorkbench.addColumn(colDef2);

        treeModel = new TreeModel(configuredWorkbench, workbenchActionFactory);

        jcrContainer = new JcrContainerTestImpl(treeModel, configuredWorkbench);
        workbenchDefinition = configuredWorkbench;


        //Init session
        session = MgnlContext.getSystemContext().getJCRSession(workspace);
        rootNode = session.getRootNode();
    }




    @Test
    public void testInit() {
        // GIVEN

        // WHEN

        // THEN
        // Get initialized resources
        assertEquals(treeModel, jcrContainer.getJcrContainerSource());
        assertEquals(workspace, jcrContainer.getWorkspace());
        assertEquals(1, jcrContainer.getSortableContainerPropertyIds().size());
        assertEquals(colName1, jcrContainer.getSortableContainerPropertyIds().get(0));
    }



    @Test
    public void testGetItem() throws Exception {
        // GIVEN
        final Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        node1.getSession().save();
        final String containerItemId = node1.getPath();

        // WHEN
        final com.vaadin.data.Item item = jcrContainer.getItem(containerItemId);

        // THEN
        assertEquals(node1.getPath(), ((JcrNodeAdapter)item).getJcrItem().getPath());
    }

    @Test
    public void testNextItemId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getPath();
        String containerItemId2 = node2.getPath();
        setSorter("name",true);
        jcrContainer.getItem(containerItemId1);

        // WHEN
        String containerItemId2Res = (String)jcrContainer.nextItemId(containerItemId1);

        // THEN
        assertEquals(containerItemId2, containerItemId2Res);
        assertEquals(node2.getPath(),((JcrNodeAdapter)jcrContainer.getItem(containerItemId2Res)).getJcrItem().getPath());
    }

    @Test
    public void testPrevItemId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getPath();
        String containerItemId2 = node2.getPath();
        setSorter("name",true);
        jcrContainer.getItem(containerItemId1);
        // WHEN
        String containerItemId1Res = (String)jcrContainer.prevItemId(containerItemId2);

        // THEN
        assertEquals(containerItemId1, containerItemId1Res);
    }


    @Test
    public void testFirstItemId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getPath();
        setSorter("name",true);
        jcrContainer.getItem(containerItemId1);

        // WHEN
        String containerItemRes = (String)jcrContainer.firstItemId();

        // THEN
        assertEquals(containerItemId1, containerItemRes);
    }


    @Test
    public void testLastItemId() throws Exception {
        // GIVEN
        final Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        final Node node2 = createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        final String containerItemId1 = node1.getPath();
        final String containerItemId2 = node2.getPath();
        setSorter("name", true);

        jcrContainer.updateSize();

        // WHEN
        final String containerItemRes = (String)jcrContainer.lastItemId();

        // THEN
        assertEquals(containerItemId2, containerItemRes);
    }

    @Test
    public void testIsFirstId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getPath();
        String containerItemId2 = node2.getPath();
        setSorter("name",true);
        jcrContainer.getItem(containerItemId1);

        // WHEN
        boolean containerItemRes1 = jcrContainer.isFirstId(containerItemId1);
        boolean containerItemRes2 = jcrContainer.isFirstId(containerItemId2);

        // THEN
        assertEquals(true, containerItemRes1);
        assertEquals(false, containerItemRes2);
    }


    @Test
    public void testIsLastId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getPath();
        String containerItemId2 = node2.getPath();
        setSorter("name",true);
        jcrContainer.updateSize();

        // WHEN
        boolean containerItemRes1 = jcrContainer.isLastId(containerItemId1);
        boolean containerItemRes2 = jcrContainer.isLastId(containerItemId2);

        // THEN
        assertEquals(false, containerItemRes1);
        assertEquals(true, containerItemRes2);
    }


    @Test
    public void testAddItem() throws Exception {
        // GIVEN
        Node node1 = rootNode.addNode("node1","mgnl:content");
        String containerItemId = node1.getPath();
        node1.getSession().save();
        // WHEN
        com.vaadin.data.Item item = jcrContainer.addItem(containerItemId);

        // THEN

        assertEquals(node1.getPath(), ((JcrNodeAdapter)item).getItemId());
    }

    @Test
    public void testGetContainerProperty() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        String containerItemId = node1.getPath();
        node1.getSession().save();
        // WHEN
        Property property = jcrContainer.getContainerProperty(containerItemId, "name");

        // THEN
        assertEquals(true, property instanceof DefaultProperty);
        assertEquals("name1",property.getValue().toString());
    }


    @Test
    public void testSort_ascending() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getPath();
        boolean[] ascending = {true};
        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), ascending);

        // THEN
        assertEquals(containerItemId1, jcrContainer.firstItemId());
    }


    @Test
    public void testSort_descending() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        node1.getSession().save();
        String containerItemId2 = node2.getPath();
        boolean[] ascending = {false};

        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), ascending);

        // THEN
        assertEquals(containerItemId2, jcrContainer.firstItemId());
    }

    @Test
    public void testContainsIdWithNull() {
        // WHEN
        final boolean result = jcrContainer.containsId(null);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testContainsIdWhenNotAround() {
        // WHEN
        final boolean result = jcrContainer.containsId("/notAround");

        // THEN
        assertFalse(result);
    }

    @Test
    public void testContainsId() throws Exception{
        // GIVEN
        final String existingKey = "existing";
        final Node node1 = createNode(rootNode, existingKey, "mgnl:content", "name", "name1");
        node1.getSession().save();

        // WHEN
        final boolean result = jcrContainer.containsId("/" + existingKey);

        // THEN
        assertTrue(result);
    }

    /**
     * Define the sorting criteria.
     */
    private void setSorter(String sortingPorperty, boolean ascending) {
        boolean[] ascendingOrder = {ascending};
        jcrContainer.sort(Arrays.asList(sortingPorperty).toArray(), ascendingOrder);
    }

    public static Node createNode(Node rootNode, String nodename, String nodeType, String nodePropertyName, String nodePropertyValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException{
        Node node = rootNode.addNode(nodename,nodeType);
        node.setProperty(nodePropertyName, nodePropertyValue);
        return node;
    }

    /**
     * Dummy Implementation of the  {AbstractJcrContainer}.
     *
     */
    public class JcrContainerTestImpl extends AbstractJcrContainer {

        public JcrContainerTestImpl(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
            super(jcrContainerSource, workbenchDefinition);
        }

        @Override
        public long update(RowIterator iterator) throws RepositoryException {
            return 0;
        }

    }
}
