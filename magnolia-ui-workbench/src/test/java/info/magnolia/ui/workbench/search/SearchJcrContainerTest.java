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

import static org.junit.Assert.*;

import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.workbench.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.workbench.container.OrderBy;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SearchJcrContainer}.
 */
public class SearchJcrContainerTest extends RepositoryTestCase {

    private SearchJcrContainer jcrContainer;
    private String workspace = "config";
    private String colName1 = "name";
    private String colName2 = "shortname";
    private ConfiguredWorkbenchDefinition configuredWorkbench;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init
        configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(workspace);
        configuredWorkbench.setPath("/");
        ConfiguredNodeTypeDefinition nt = new ConfiguredNodeTypeDefinition();
        nt.setName("mgnl:content");
        nt.setStrict(true);

        configuredWorkbench.addNodeType(nt);

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

    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        configuredWorkbench.setPath("/");
    }

    @Test
    public void testGetQueryWhereClauseReturnsNodeTypesWhereClauseWhenFullTextExpressionIsBlank() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where (([jcr:primaryType] = 'mgnl:content'))", stmt);
    }

    @Test
    public void testGetQueryWhereClauseReturnsNodeTypesWhereClauseWhenFullTextExpressionIsNull() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression(null);

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where (([jcr:primaryType] = 'mgnl:content'))", stmt);
    }

    @Test
    public void testGetQueryWhereClauseReturnsFullTextSearchExpressionORedWithLocalname() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("foo");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertEquals(" where (([jcr:primaryType] = 'mgnl:content') and (lower(localname()) LIKE 'foo%' or t.['foo'] IS NOT NULL or contains(t.*, 'foo')) )", stmt);
    }

    @Test
    public void testGetQueryWhereClauseEscapesQuotesInFullTextSearchExpression() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("foo OR 'baz bar'   ");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains("contains(t.*, 'foo OR ''baz bar''')", stmt);
    }

    @Test
    public void testGetQueryWhereClauseWhenWorkspacePathIsNotRoot() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("foo");
        configuredWorkbench.setPath("/qux");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains(" where ( ISDESCENDANTNODE('/qux') and ([jcr:primaryType] = 'mgnl:content') and (lower(localname()) LIKE 'foo%'", stmt);
    }

    @Test
    public void testInvalidJcrCharsInNodeAndPropertiesNamesAreEncoded() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("*foo");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains("lower(localname()) LIKE '%2Afoo%' or t.['%2Afoo'] IS NOT NULL", stmt);
    }

    @Test
    public void testFullTextExpressionIsLowercasedSoThatInsensitiveCaseSearchOnNodeNamesCanWork() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("FOOBaRbaZ");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains("lower(localname()) LIKE 'foobarbaz%'", stmt);
    }

    @Test
    public void testFullTextExpressionIsNotLowercasedOnPropertNames() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("FOOBaRbaZ");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains("t.['FOOBaRbaZ'] IS NOT NULL or contains(t.*, 'FOOBaRbaZ')", stmt);
    }

    @Test
    public void testDigitsAreNotEncoded() throws Exception {
        // GIVEN
        jcrContainer.setFullTextExpression("1");

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains("lower(localname()) LIKE '1%' or t.['1'] IS NOT NULL", stmt);
    }

    @Test
    public void testFolderNodeTypeIsIncludedInSearch() throws Exception {
        // GIVEN
        ConfiguredNodeTypeDefinition nt = new ConfiguredNodeTypeDefinition();
        nt.setName("mgnl:folder");
        configuredWorkbench.addNodeType(nt);

        // inclusion of mgnl:folder happens at construction time
        SearchJcrContainer jcrContainer = new SearchJcrContainer(configuredWorkbench);

        // WHEN
        String stmt = jcrContainer.getQueryWhereClause();

        // THEN
        assertContains("or [jcr:primaryType] = 'mgnl:folder'", stmt);
    }

    @Test
    public void testSearchResultsAreSortedByJcrScoreDesc() throws Exception {

        // WHEN
        String jcrOrderByFunction = jcrContainer.getJcrNameOrderByFunction();
        OrderBy orderBy = jcrContainer.getDefaultOrderBy(ModelConstants.JCR_NAME);

        // THEN
        assertEquals(SearchJcrContainer.JCR_SCORE_FUNCTION, jcrOrderByFunction);
        assertFalse(orderBy.isAscending());
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsMinus() throws Exception {
        // GIVEN
        String simpleTerm = "-";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals("\\-", escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsMinusBefore() throws Exception {
        // GIVEN
        String simpleTerm = "-abc";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsMinusAfter() throws Exception {
        // GIVEN
        String simpleTerm = "abc-";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsMinusInBetween() throws Exception {
        // GIVEN
        String simpleTerm = "ab-c";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsPlus() throws Exception {
        // GIVEN
        String simpleTerm = "+";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals("\\+", escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsPlusBefore() throws Exception {
        // GIVEN
        String simpleTerm = "+abc";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsPlusAfter() throws Exception {
        // GIVEN
        String simpleTerm = "abc+";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsPlusInBetween() throws Exception {
        // GIVEN
        String simpleTerm = "ab+c";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsDoubleSlash() throws Exception {
        // GIVEN
        String simpleTerm = "\\";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals("\\\\", escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsDoubleSlashBefore() throws Exception {
        // GIVEN
        String simpleTerm = "\\abc";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsDoubleSlashAfter() throws Exception {
        // GIVEN
        String simpleTerm = "abc\\";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsDoubleSlashInBetween() throws Exception {
        // GIVEN
        String simpleTerm = "ab\\c";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsParentheses() throws Exception {
        // GIVEN
        String simpleTerm = ")ab)c)";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals("\\)ab\\)c\\)", escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsSimpleSlash() throws Exception {
        // GIVEN
        String simpleTerm = "\"foo bar\"";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals(simpleTerm, escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsSimpleSlashInBetween() throws Exception {
        // GIVEN
        String simpleTerm = "\"foo \" bar\"";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals("\"foo \\\" bar\"", escaped);
    }

    @Test
    public void testEscapeIllegalJcrFullTextSearchCharsBrackets() throws Exception {
        // GIVEN
        String simpleTerm = "(a)b{c}d[e]f";

        // WHEN
        String escaped = jcrContainer.escapeIllegalFullTextSearchChars(simpleTerm);

        // THEN
        assertEquals("\\(a\\)b\\{c\\}d\\[e\\]f", escaped);
    }

    protected void assertContains(final String searchString, final String string) {
        if (!StringUtils.contains(string, searchString)) {
            Assert.fail(String.format("[%s] was expected to be contained into [%s]", searchString, string));
        }
    }
}
