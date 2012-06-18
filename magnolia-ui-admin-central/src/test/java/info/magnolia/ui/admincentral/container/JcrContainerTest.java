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
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.PropertyTypeColumn;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.RowIterator;
import javax.jcr.version.VersionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Main test class for {JcrContainer}
 */
public class JcrContainerTest {

    private JcrContainerTestImpl jcrContainer;
    private WorkbenchDefinition workbenchDefinition;
    private TreeModel treeModel;
    private String workspace = "test";
    private String colName1 = "name";
    private String colName2 = "shortname";
    private MockSession session;
    Node rootNode;

    @Before
    public void setUp() {
        //Init
        workbenchDefinition = new WorkbenchDefinition();
        workbenchDefinition.setWorkspace(workspace);
        workbenchDefinition.setPath("/");
        //Init workBench
        WorkbenchActionFactory workbenchActionFactory = new WorkbenchActionFactoryImpl();
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
        workbenchDefinition.addColumn(colDef1);
        workbenchDefinition.addColumn(colDef2);

        //FIXME.... workbenchDefinition --> has column set && we send also columns???
        treeModel = new TreeModel(workbenchDefinition, columns, workbenchActionFactory);

        jcrContainer = new JcrContainerTestImpl(treeModel, workbenchDefinition);


        //Init session
        session = new MockSession(workspace);
        MockContext ctx = new MockContext();
        ctx.addSession(workspace, session);
        MgnlContext.setInstance(ctx);
        rootNode = session.getRootNode();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
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
    public void testGetJcrItem() throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        // GIVEN
        Node node1 = rootNode.addNode("node1");
        String containerItemId = node1.getPath();

        // WHEN
        Item item = jcrContainer.getJcrItem(containerItemId);

        // THEN
        assertEquals(true, item.isNode());
        assertEquals(node1, item);
    }



//    @Test
//    public void testAddItem() throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
//        // GIVEN
//        Node node1 = rootNode.addNode("node1","mgnl:content");
//        String containerItemId = node1.getPath();
//
//        // WHEN
//        com.vaadin.data.Item item = jcrContainer.addItem(containerItemId);
//
//        // THEN
//
//        assertEquals(node1, item);
//    }


    /**
     * Dummy Implementation of the  {JcrContainer}.
     *
     */
    public class JcrContainerTestImpl extends JcrContainer {

        public JcrContainerTestImpl(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
            super(jcrContainerSource, workbenchDefinition);
        }

        @Override
        public long update(RowIterator iterator) throws RepositoryException {
            return 0;
        }

    }
}
