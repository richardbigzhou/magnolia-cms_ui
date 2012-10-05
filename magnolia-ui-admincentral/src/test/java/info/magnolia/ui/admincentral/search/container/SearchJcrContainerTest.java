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
package info.magnolia.ui.admincentral.search.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.content.view.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionRegistry;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

public class SearchJcrContainerTest extends RepositoryTestCase {

    private SearchJcrContainer jcrContainer;
    private TreeModel treeModel;
    private String workspace = "config";
    private String colName1 = "name";
    private String colName2 = "shortname";
    private Session session;
    private Node rootNode;

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
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_"+colName1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_"+colName2);

        configuredWorkbench.addColumn(colDef1);
        configuredWorkbench.addColumn(colDef2);

        treeModel = new TreeModel(configuredWorkbench, workbenchActionFactory);

        jcrContainer = new SearchJcrContainer(treeModel, configuredWorkbench);

        //Init session
        session = MgnlContext.getSystemContext().getJCRSession(workspace);
        rootNode = session.getRootNode();
    }

    @Test
    public void testJCRQueryReturnsNullWhenFullTextExpressionIsBlank() throws Exception {
        //GIVEN
        jcrContainer.setFullTextExpression(null);

        //WHEN
        String stmt = jcrContainer.constructJCRQuery(true);

        //THEN
        assertNull(stmt);

        //GIVEN
        jcrContainer.setFullTextExpression("");

        //WHEN
        stmt = jcrContainer.constructJCRQuery(true);

        //THEN
        assertNull(stmt);
    }

    @Test
    public void testJCRQueryReturnsFullTextSearchExpression() throws Exception {
        //GIVEN
        jcrContainer.setFullTextExpression("foo");

        //WHEN
        String stmt = jcrContainer.constructJCRQuery(true);

        //THEN
        assertEquals("//element(*,mgnl:content) [jcr:contains(.,'foo')]", stmt);
    }

    @Test
    public void testJCRQueryReturnsEscapesQuotesInFullTextSearchExpression() throws Exception {
        //GIVEN
        jcrContainer.setFullTextExpression("foo OR 'baz bar'   ");

        //WHEN
        String stmt = jcrContainer.constructJCRQuery(true);

        //THEN
        assertEquals("//element(*,mgnl:content) [jcr:contains(.,'foo OR ''baz bar''')]", stmt);
    }

}
