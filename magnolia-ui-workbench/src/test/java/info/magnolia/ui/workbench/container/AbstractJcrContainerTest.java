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
package info.magnolia.ui.workbench.container;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;
import info.magnolia.ui.workbench.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.Arrays;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Tests.
 */
public class AbstractJcrContainerTest extends RepositoryTestCase {

    private JcrContainerTestImpl jcrContainer;

    private ConfiguredJcrContentConnectorDefinition connectorDefinition;

    private final String workspace = "config";

    private final String colName1 = "name";

    private final String colName2 = "shortname";

    private final String TEST_PATH = "/test";

    private Session session;

    private Node rootNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();


        // Add view
        ConfiguredContentPresenterDefinition contentPresenterDef = new TreePresenterDefinition();
        configuredWorkbench.addContentView(contentPresenterDef);

        // Add columns
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_" + colName1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_" + colName2);

        contentPresenterDef.addColumn(colDef1);
        contentPresenterDef.addColumn(colDef2);

        ConfiguredJcrContentConnectorDefinition configuredConnector = new ConfiguredJcrContentConnectorDefinition();
        ConfiguredNodeTypeDefinition mainNodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        mainNodeTypeDefinition.setName(NodeTypes.Content.NAME);
        mainNodeTypeDefinition.setStrict(true);
        configuredConnector.addNodeType(mainNodeTypeDefinition);
        configuredConnector.setDefaultOrder(colName2);
        configuredConnector.setWorkspace(workspace);

        connectorDefinition = configuredConnector;
        jcrContainer = new JcrContainerTestImpl(connectorDefinition);
        jcrContainer.addSortableProperty(colDef1.getName());


        // Init session
        session = MgnlContext.getSystemContext().getJCRSession(workspace);
        rootNode = session.getRootNode();
    }

    @Test
    public void testGetItem() throws Exception {
        // GIVEN
        final Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        node1.getSession().save();
        Object containerItemId = JcrItemUtil.getItemId(node1);
        // WHEN
        final com.vaadin.data.Item item = jcrContainer.getItem(containerItemId);

        // THEN
        assertEquals(node1.getIdentifier(), ((JcrNodeAdapter) item).getItemId().getUuid());
    }

    @Test
    public void testGetItemAfterNodeDeletionReturnsNull() throws Exception {
        // GIVEN
        final Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        node1.getSession().save();
        Object containerItemId = JcrItemUtil.getItemId(node1);
        com.vaadin.data.Item item = jcrContainer.getItem(containerItemId);
        assertNotNull(item);

        Session session = node1.getSession();
        session.removeItem(node1.getPath());
        session.save();

        // WHEN
        item = jcrContainer.getItem(containerItemId);

        // THEN
        assertNull(item);
    }

    @Test
    public void testNextItemId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        Node node2 = createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();

        Object containerItemId1 = JcrItemUtil.getItemId(node1);
        Object containerItemId2 = JcrItemUtil.getItemId(node2);

        setSorter("name", true);

        // WHEN
        JcrItemId containerItemId2Res = jcrContainer.nextItemId(containerItemId1);

        // THEN
        assertEquals(containerItemId2, containerItemId2Res);
        assertEquals(node2.getIdentifier(), ((JcrNodeAdapter) jcrContainer.getItem(containerItemId2Res)).getItemId().getUuid());
    }

    @Test
    public void testPrevItemId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        Node node2 = createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();

        Object containerItemId1 = JcrItemUtil.getItemId(node1);
        Object containerItemId2 = JcrItemUtil.getItemId(node2);

        setSorter("name", true);
        // WHEN
        JcrItemId containerItemId1Res = jcrContainer.prevItemId(containerItemId2);

        // THEN
        assertEquals(containerItemId1, containerItemId1Res);
    }

    @Test
    public void testFirstItemId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getIdentifier();
        setSorter("name", true);

        // WHEN
        JcrItemId containerItemRes = jcrContainer.firstItemId();

        // THEN
        assertEquals(containerItemId1, containerItemRes.getUuid());
    }

    @Test
    public void testLastItemId() throws Exception {
        // GIVEN
        final Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        final Node node2 = createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();
        final String containerItemId2 = node2.getIdentifier();
        setSorter("name", true);

        jcrContainer.updateSize();

        // WHEN
        final JcrItemId containerItemRes = jcrContainer.lastItemId();

        // THEN
        assertEquals(containerItemId2, containerItemRes.getUuid());
    }

    @Test
    public void testIsFirstId() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        Node node2 = createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();
        Object containerItemId1 = JcrItemUtil.getItemId(node1);
        Object containerItemId2 = JcrItemUtil.getItemId(node2);
        setSorter("name", true);

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
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        Node node2 = createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();

        Object containerItemId1 = JcrItemUtil.getItemId(node1);
        Object containerItemId2 = JcrItemUtil.getItemId(node2);

        setSorter("name", true);
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
        Node node1 = rootNode.addNode("node1", NodeTypes.Content.NAME);
        Object containerItemId = JcrItemUtil.getItemId(node1);
        node1.getSession().save();
        // WHEN
        com.vaadin.data.Item item = jcrContainer.addItem(containerItemId);

        // THEN

        assertEquals(node1.getIdentifier(), ((JcrNodeAdapter) item).getItemId().getUuid());
    }

    @Test
    public void testGetContainerProperty() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        Object containerItemId = JcrItemUtil.getItemId(node1);
        node1.getSession().save();
        // WHEN
        Property property = jcrContainer.getContainerProperty(containerItemId, "name");

        // THEN
        assertEquals(true, property instanceof DefaultProperty);
        assertEquals("name1", property.getValue().toString());
    }

    @Test
    public void testSortAscending() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();
        String containerItemId1 = node1.getIdentifier();
        boolean[] ascending = { true };
        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), ascending);

        // THEN
        assertEquals(containerItemId1, jcrContainer.firstItemId().getUuid());
    }

    @Test
    public void testSortDescending() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        Node node2 = createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();

        Object containerItemId2 = JcrItemUtil.getItemId(node2);

        boolean[] ascending = { false };

        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), ascending);

        // THEN
        assertEquals(containerItemId2, jcrContainer.firstItemId());
    }

    @Test
    public void testOffestIsResetAfterSort() throws Exception {
        // GIVEN
        // set mini pageLength to not have to create tons of items
        jcrContainer.setPageLength(1);
        Node node1 = createNode(rootNode, "node0", NodeTypes.Content.NAME, "name", "name0");
        for (int i = 1; i <= 5; i++) {
            createNode(rootNode, "node" + i, NodeTypes.Content.NAME, "name", "name" + i);
        }
        node1.getSession().save();

        // trigger an update of the currentOffset
        jcrContainer.updateSize();
        jcrContainer.lastItemId();
        assertEquals(4, jcrContainer.getCurrentOffset());

        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), new boolean[] { true });

        // THEN
        assertEquals(0, jcrContainer.getCurrentOffset());
    }

    @Test
    public void itemIndexesAreClearedAfterSort() throws Exception {
        // GIVEN
        // set mini pageLength to not have to create tons of items
        jcrContainer.setPageLength(1);
        final int totalNumberOfItems = 3;
        for (int i = 0; i < totalNumberOfItems; i++) {
            createNode(rootNode, "node" + i, NodeTypes.Content.NAME, "name", "name" + i);
        }
        rootNode.getSession().save();

        // force all items being in the itemIndexes
        for (int i = 0; i < totalNumberOfItems; i++) {
            jcrContainer.getIdByIndex(i);
        }
        assertEquals(totalNumberOfItems, jcrContainer.getItemIndexes().size());

        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), new boolean[] { true });

        // THEN
        assertEquals("After sort only the pageLengths * ratio should have been retrieved from jcr and hence be cached in itemIndexes.", jcrContainer.getPageLength() * jcrContainer.getCacheRatio(), jcrContainer.getItemIndexes().size());
    }

    @Test
    public void testContainsIdWithNull() throws Exception {
        // WHEN
        final boolean result = jcrContainer.containsId(null);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testContainsIdWhenNotAround() throws Exception {
        // WHEN
        final boolean result = jcrContainer.containsId("/notAround");

        // THEN
        assertFalse(result);
    }

    @Test
    public void testContainsId() throws Exception {
        // GIVEN
        final Node node1 = createNode(rootNode, "nodeName", NodeTypes.Content.NAME, "name", "name1");
        node1.getSession().save();
        Object existingKey = JcrItemUtil.getItemId(node1);

        // WHEN
        final boolean result = jcrContainer.containsId(existingKey);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testConstructJCRQueryWithoutSort() throws Exception {
        // WHEN
        final String result = jcrContainer.constructJCRQuery(false);

        // THEN
        assertEquals(getExpectedSelectStatementWithNodeTypesRestrictions(), result);
    }

    @Test
    public void testConstructJCRQueryWithoutSortWithPathClause() throws Exception {
        // GIVEN
        connectorDefinition.setRootPath(TEST_PATH);

        // WHEN
        final String result = jcrContainer.constructJCRQuery(false);

        // THEN
        String expectedResult = AbstractJcrContainer.SELECT_TEMPLATE + " where (([jcr:primaryType] = 'mgnl:content') and ";
        expectedResult += String.format(AbstractJcrContainer.WHERE_TEMPLATE_FOR_PATH, TEST_PATH) + ") ";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testConstructJCRQueryWithDefaultSort() throws Exception {
        // GIVEN

        // WHEN
        final String result = jcrContainer.constructJCRQuery(true);

        // THEN
        assertEquals(getExpectedQueryWithOrderByAscending(), result);
    }

    @Test
    public void testConstructJCRQuerySortBySortableColumn() throws Exception {
        // GIVEN
        jcrContainer.sort(new String[] { ModelConstants.JCR_NAME }, new boolean[] { true });

        // WHEN
        final String result = jcrContainer.constructJCRQuery(true);

        // THEN
        assertEquals(getExpectedQueryWithOrderByAscending(), result);
    }

    @Test
    public void testConstructJCRQuerySortByNonSortableColumn() {
        // GIVEN
        jcrContainer.sort(new String[] { colName2 }, new boolean[] { true });

        // WHEN
        final String result = jcrContainer.constructJCRQuery(true);

        // THEN
        assertEquals(getExpectedQueryWithOrderByAscending(), result);
    }

    @Test
    public void testGetMainNodeTypeWhenNoNodeTypeIsDefined() throws Exception {
        // GIVEN
        // we cannot use default jcrContainer from setUp here - it already has a different NodeType as main NodeType (first in nodeTypes).
        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.setWorkspace(workspace);
        jcrContainer = new JcrContainerTestImpl(connectorDefinition);

        // WHEN
        final String result = jcrContainer.getMainNodeType();

        // THEN
        assertEquals(AbstractJcrContainer.DEFAULT_NODE_TYPE, result);
    }

    @Test
    public void testOrderOfNodeTypesInConfigurationDoesNotMatter() throws Exception {
        // GIVEN
        // we cannot use default jcrContainer from setUp here as its ctor has been already called thus the searchable node types have already been determined . See AbstractJcrContainer.findSearchableNodeTypes()
        final String testNodeType = "mgnl:contentNode";
        ConfiguredNodeTypeDefinition def = new ConfiguredNodeTypeDefinition();
        def.setName(testNodeType);

        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.addNodeType(def);
        connectorDefinition.setWorkspace(workspace);

        jcrContainer = new JcrContainerTestImpl(connectorDefinition);

        // WHEN
        // Before 5.1 the main node type was the first one declared in the configuration.
        final String mainNodeType = jcrContainer.getMainNodeType();
        final String select = getExpectedSelectStatementWithNodeTypesRestrictions();

        // THEN
        assertTrue(select.contains(mainNodeType));
        assertTrue(select.contains(testNodeType));
    }

    @Test
    public void testGetQueryWhereClauseWorkspacePathWithPath() throws Exception {
        // GIVEN
        connectorDefinition.setRootPath(TEST_PATH);

        // WHEN
        final String result = jcrContainer.getQueryWhereClauseWorkspacePath();

        // THEN
        assertEquals(String.format(AbstractJcrContainer.WHERE_TEMPLATE_FOR_PATH, TEST_PATH), result);
    }

    @Test
    public void testGetQueryWhereClauseWorkspacePathWithRoot() throws Exception {
        // GIVEN
        final String testPath = "/";
        connectorDefinition.setRootPath(testPath);

        // WHEN
        final String result = jcrContainer.getQueryWhereClauseWorkspacePath();

        // THEN
        assertEquals("", result);
    }

    @Test
    public void testGetQueryWhereClauseWorkspacePathWithNull() throws Exception {
        // GIVEN
        connectorDefinition.setRootPath(null);

        // WHEN
        final String result = jcrContainer.getQueryWhereClauseWorkspacePath();

        // THEN
        assertEquals("", result);
    }

    @Test
    public void testGetQueryWhereClauseWorkspacePathWithEmptyString() throws Exception {
        // GIVEN
        final String testPath = "";
        connectorDefinition.setRootPath(testPath);

        // WHEN
        final String result = jcrContainer.getQueryWhereClauseWorkspacePath();

        // THEN
        assertEquals("", result);
    }

    @Test
    public void testGetQueryWhereClausePrependWhereKeywordWhenWorkspacePathIsNotRoot() {
        // GIVEN
        connectorDefinition.setRootPath(TEST_PATH);
        final String whereClauseWorkspacePath = jcrContainer.getQueryWhereClauseWorkspacePath();

        // WHEN
        final String result = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where (([jcr:primaryType] = 'mgnl:content') and " + whereClauseWorkspacePath + ") ", result);
    }

    @Test
    public void testGetQueryWhereClauseReturnsEmptyStringWhenWorkspacePathIsRoot() throws Exception {
        // GIVEN
        final String testPath = "/";
        connectorDefinition.setRootPath(testPath);

        // WHEN
        final String result = jcrContainer.getQueryWhereClauseWorkspacePath();

        // THEN
        assertEquals("", result);
    }

    @Test
    public void testConstructJCRQueryReturnDefaultSelectStatement() throws Exception {
        // GIVEN
        final String expected = getExpectedSelectStatementWithNodeTypesRestrictions();

        // WHEN
        final String result = jcrContainer.constructJCRQuery(false);

        // THEN
        assertTrue(result.contains(expected));
    }

    @Test
    public void testSortShouldIgnoreCase() throws Exception {
        // GIVEN
        // Capital Q comes before lowercase f in UTF-8 character set, yet we expect 'foo' to precede 'QUX' in our ascending sorting
        Node fooNode = createNode(rootNode, "foo", NodeTypes.Content.NAME, "name", "foo");
        createNode(rootNode, "QUX", NodeTypes.Content.NAME, "name", "qux");
        fooNode.getSession().save();
        JcrItemId fooItemId = JcrItemUtil.getItemId(fooNode);
        boolean[] ascending = { true };
        // WHEN
        jcrContainer.sort(Arrays.asList("name").toArray(), ascending);

        // THEN
        assertEquals(fooItemId, jcrContainer.firstItemId());
    }

    @Test
    public void testGetQueryWhereClauseNodeTypesDoesNotSearchForHiddenInListNodes() throws Exception {
        // GIVEN
        ConfiguredNodeTypeDefinition fooDef = new ConfiguredNodeTypeDefinition();
        fooDef.setName("mgnl:foo");
        fooDef.setHideInList(true);

        ConfiguredNodeTypeDefinition barDef = new ConfiguredNodeTypeDefinition();
        barDef.setName("mgnl:bar");

        connectorDefinition.addNodeType(fooDef);
        connectorDefinition.addNodeType(barDef);

        // WHEN
        String query = jcrContainer.constructJCRQuery(false);

        // THEN
        assertEquals(getExpectedSelectStatementWithNodeTypesRestrictions(), query);
    }

    @Test
    public void testGetQueryWhereClauseNodeTypesDoesNotIncludeMgnlFolder() throws Exception {
        // GIVEN
        ConfiguredNodeTypeDefinition fooDef = new ConfiguredNodeTypeDefinition();
        fooDef.setName("mgnl:foo");
        fooDef.setHideInList(true);

        ConfiguredNodeTypeDefinition folderDef = new ConfiguredNodeTypeDefinition();
        folderDef.setName("mgnl:folder");

        connectorDefinition.addNodeType(fooDef);
        connectorDefinition.addNodeType(folderDef);

        // WHEN
        String query = jcrContainer.constructJCRQuery(false);

        // THEN
        assertFalse(query.contains("mgnl:folder"));
    }

    @Test
    public void testGetSearchableNodeTypesIncludeMixins() throws Exception {
        // GIVEN
        ConfiguredNodeTypeDefinition mixinDef = new ConfiguredNodeTypeDefinition();
        mixinDef.setName("mgnl:mixin");

        ConfiguredNodeTypeDefinition primaryNtDef = new ConfiguredNodeTypeDefinition();
        primaryNtDef.setName("mgnl:primaryNt");

        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.addNodeType(mixinDef);
        connectorDefinition.addNodeType(primaryNtDef);

        connectorDefinition.setWorkspace(workspace);

        final NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate mixinTemplate = nodeTypeManager.createNodeTypeTemplate();
        mixinTemplate.setName(mixinDef.getName());
        mixinTemplate.setMixin(true);
        NodeType mixinNodeType = nodeTypeManager.registerNodeType(mixinTemplate, true);

        NodeTypeTemplate primaryTemplate = nodeTypeManager.createNodeTypeTemplate();
        primaryTemplate.setName(primaryNtDef.getName());
        NodeType primaryNodeType = nodeTypeManager.registerNodeType(primaryTemplate, true);

        jcrContainer = new JcrContainerTestImpl(connectorDefinition);

        // WHEN
        final Set<NodeType> searchableNodeTypes = jcrContainer.getSearchableNodeTypes();

        // THEN
        assertTrue(searchableNodeTypes.contains(mixinNodeType));
        assertTrue(searchableNodeTypes.contains(primaryNodeType));

    }

    @Test
    public void testGetSearchableNodeTypesExcludeHiddenInListNodes() throws Exception {
        // GIVEN
        ConfiguredNodeTypeDefinition mixinDef = new ConfiguredNodeTypeDefinition();
        mixinDef.setName("mgnl:mixin");

        ConfiguredNodeTypeDefinition primaryNtDef = new ConfiguredNodeTypeDefinition();
        primaryNtDef.setName("mgnl:primaryNt");
        primaryNtDef.setHideInList(true);

        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.addNodeType(mixinDef);
        connectorDefinition.addNodeType(primaryNtDef);

        connectorDefinition.setWorkspace(workspace);

        final NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate mixinTemplate = nodeTypeManager.createNodeTypeTemplate();
        mixinTemplate.setName(mixinDef.getName());
        mixinTemplate.setMixin(true);
        NodeType mixinNodeType = nodeTypeManager.registerNodeType(mixinTemplate, true);

        NodeTypeTemplate primaryTemplate = nodeTypeManager.createNodeTypeTemplate();
        primaryTemplate.setName(primaryNtDef.getName());
        NodeType primaryNodeType = nodeTypeManager.registerNodeType(primaryTemplate, true);

        jcrContainer = new JcrContainerTestImpl(connectorDefinition);

        // WHEN
        final Set<NodeType> searchableNodeTypes = jcrContainer.getSearchableNodeTypes();

        // THEN
        assertTrue(searchableNodeTypes.contains(mixinNodeType));
        assertFalse(searchableNodeTypes.contains(primaryNodeType));

    }

    @Test
    public void testGetSearchableNodeTypesExcludeSubTypesWhenSupertypeIsDefinedAsStrict() throws Exception {
        // GIVEN
        final NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        // mgnl:content is defined strict by default
        final NodeType nodeType = nodeTypeManager.getNodeType(NodeTypes.Content.NAME);
        final NodeTypeIterator subtypes = nodeType.getSubtypes();

        // WHEN
        final Set<NodeType> searchableNodeTypes = jcrContainer.getSearchableNodeTypes();

        // THEN
        assertTrue(searchableNodeTypes.contains(nodeType));
        while (subtypes.hasNext()) {
            NodeType nt = subtypes.nextNodeType();
            assertFalse(searchableNodeTypes.contains(nt));
        }
    }

    @Test
    public void testGetSearchableNodeTypesIncludeSubTypesWhenSupertypeIsNotDefinedAsStrict() throws Exception {
        // GIVEN
        ConfiguredNodeTypeDefinition def = new ConfiguredNodeTypeDefinition();
        def.setName(NodeTypes.Content.NAME);

        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.addNodeType(def);
        connectorDefinition.setWorkspace(workspace);

        jcrContainer = new JcrContainerTestImpl(connectorDefinition);

        final NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        final NodeType nodeType = nodeTypeManager.getNodeType(NodeTypes.Content.NAME);
        final NodeTypeIterator subtypes = nodeType.getSubtypes();

        // WHEN
        final Set<NodeType> searchableNodeTypes = jcrContainer.getSearchableNodeTypes();

        // THEN
        assertTrue(searchableNodeTypes.contains(nodeType));
        while (subtypes.hasNext()) {
            assertTrue(searchableNodeTypes.contains(subtypes.nextNodeType()));
        }
    }

    @Test
    public void testOrderByJcrName() throws Exception {
        // GIVEN
        connectorDefinition.setDefaultOrder(ModelConstants.JCR_NAME);

        // WHEN
        String query = jcrContainer.constructJCRQuery(true);

        // THEN
        assertTrue(query.contains("order by lower(name(t)) asc"));
    }

    @Test
    public void testExecuteQueryWithMixins() throws Exception {
        // GIVEN
        final String mixinNodeTypeName = "mgnl:mixin";
        ConfiguredNodeTypeDefinition mixinDef = new ConfiguredNodeTypeDefinition();
        mixinDef.setName(mixinNodeTypeName);

        ConfiguredNodeTypeDefinition primaryNtDef = new ConfiguredNodeTypeDefinition();
        primaryNtDef.setName("mgnl:primaryNt");

        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.addNodeType(mixinDef);
        connectorDefinition.addNodeType(primaryNtDef);

        connectorDefinition.setWorkspace(workspace);

        final NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate mixinTemplate = nodeTypeManager.createNodeTypeTemplate();
        mixinTemplate.setName(mixinDef.getName());
        mixinTemplate.setMixin(true);
        nodeTypeManager.registerNodeType(mixinTemplate, true);

        NodeTypeTemplate primaryTemplate = nodeTypeManager.createNodeTypeTemplate();
        primaryTemplate.setName(primaryNtDef.getName());
        nodeTypeManager.registerNodeType(primaryTemplate, true);

        jcrContainer = new JcrContainerTestImpl(connectorDefinition);

        Node mixin1 = createNode(rootNode, "mixin1", NodeTypes.Content.NAME, null, null);
        mixin1.addMixin(mixinNodeTypeName);
        Node mixin2 = createNode(rootNode, "mixin2", NodeTypes.Content.NAME, null, null);
        mixin2.addMixin(mixinNodeTypeName);
        createNode(rootNode, "primary1", "mgnl:primaryNt", null, null);
        mixin1.getSession().save();

        // WHEN
        final String query = jcrContainer.constructJCRQuery(false);

        final QueryResult qr = jcrContainer.executeQuery(query, Query.JCR_SQL2, 0, 0);
        final RowIterator iterator = qr.getRows();

        // THEN
        assertEquals(3, iterator.getSize());
        while (iterator.hasNext()) {
            final Node node = iterator.nextRow().getNode(AbstractJcrContainer.SELECTOR_NAME);
            if ("mixin1".equals(node.getName()) || "mixin2".equals(node.getName())) {
                assertTrue(node.isNodeType(mixinNodeTypeName));
            }
        }
    }

    @Test
    public void testUdpateSize() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();
        assertEquals(Integer.MIN_VALUE, jcrContainer.size());

        // WHEN
        jcrContainer.updateSize();

        // THEN
        assertEquals(2, jcrContainer.size());
    }

    @Test
    public void testUdpateSizeJcrQueryIssue() throws Exception {
        // GIVEN
        Node node1 = createNode(rootNode, "node1", NodeTypes.Content.NAME, "name", "name1");
        createNode(rootNode, "node2", NodeTypes.Content.NAME, "name", "name2");
        node1.getSession().save();
        jcrContainer.updateSize();
        assertEquals(2, jcrContainer.size());
        jcrContainer.createWrongQuery();

        // WHEN
        jcrContainer.updateSize();

        // THEN
        assertEquals(0, jcrContainer.size());
    }

    /**
     * Define the sorting criteria.
     */
    private void setSorter(String sortingPorperty, boolean ascending) {
        boolean[] ascendingOrder = { ascending };
        jcrContainer.sort(Arrays.asList(sortingPorperty).toArray(), ascendingOrder);
    }

    private String getExpectedSelectStatementWithNodeTypesRestrictions() {
        return AbstractJcrContainer.SELECT_TEMPLATE + " where ((" + jcrContainer.getQueryWhereClauseNodeTypes() + ") ) ";
    }

    private String getExpectedQueryWithOrderByAscending() {
        return getExpectedSelectStatementWithNodeTypesRestrictions() + AbstractJcrContainer.ORDER_BY
                + AbstractJcrContainer.SELECTOR_NAME + ".[" + colName2 + "]" + AbstractJcrContainer.ASCENDING_KEYWORD;
    }

    public static Node createNode(Node rootNode, String nodename, String nodeType, String nodePropertyName, String nodePropertyValue) throws RepositoryException {
        Node node = rootNode.addNode(nodename, nodeType);
        if (nodePropertyName != null) {
            node.setProperty(nodePropertyName, nodePropertyValue);
        }
        return node;
    }

    /**
     * Dummy Implementation of the {AbstractJcrContainer}.
     */
    public class JcrContainerTestImpl extends AbstractJcrContainer {

        private String whereStatement;

        public JcrContainerTestImpl(JcrContentConnectorDefinition contentConnectorDefinition) {
            super(contentConnectorDefinition);
        }

        @Override
        public void addItemSetChangeListener(ItemSetChangeListener listener) {
        }

        @Override
        public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        }

        public void createWrongQuery() {
            whereStatement = "some*()W*!@BullShit";
        }

        @Override
        protected String getQueryWhereClause() {
            if (whereStatement == null) {
                return super.getQueryWhereClause();
            }
            return whereStatement;
        }
    }
}
