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
package info.magnolia.ui.workbench.search;

import static org.junit.Assert.assertEquals;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.workbench.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SearchJcrContainerTest extends RepositoryTestCase {

    private SearchJcrContainer jcrContainer;
    private String workspace = "config";
    private String colName1 = "name";
    private String colName2 = "shortname";
    private Session session;
    private ConfiguredWorkbenchDefinition configuredWorkbench;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init
        configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(workspace);
        configuredWorkbench.setPath("/");

        // Add view
        ConfiguredContentPresenterDefinition contentView = new SearchPresenterDefinition();
        configuredWorkbench.addContentView(contentView);

        // Add columns
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_" + colName1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_" + colName2);

        contentView.addColumn(colDef1);
        contentView.addColumn(colDef2);

        jcrContainer = new SearchJcrContainer(configuredWorkbench);

        // Init session
        session = MgnlContext.getJCRSession(workspace);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        configuredWorkbench.setPath("/");
    }

    @Test
    public void testGetQueryWhereClauseReturnsEmptyStringWhenFullTextExpressionIsBlank() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals("", stmt);
    }

    @Test
    public void testGetQueryWhereClauseReturnsEmptyStringWhenFullTextExpressionIsNull() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression(null);

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals("", stmt);
    }

    @Test
    public void testGetQueryWhereClauseReturnsFullTextSearchExpressionORedWithLocalname() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("foo");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where ( (localname() LIKE '%foo%' or contains(t.*, 'foo')))", stmt);
    }

    @Test
    public void testGetQueryWhereClauseEscapesQuotesInFullTextSearchExpression() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("foo OR 'baz bar'   ");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where ( (localname() LIKE '%foo OR ''baz bar''%' or contains(t.*, 'foo OR ''baz bar''')))", stmt);
    }

    @Test
    public void testGetQueryWhereClauseWhenWorkspacePathIsNotRoot() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("foo");
        configuredWorkbench.setPath("/qux");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where ( ISDESCENDANTNODE('/qux') and  (localname() LIKE '%foo%' or contains(t.*, 'foo')))", stmt);
    }

}
