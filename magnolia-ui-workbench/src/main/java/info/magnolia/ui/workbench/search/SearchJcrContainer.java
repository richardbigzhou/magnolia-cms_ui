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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.workbench.container.OrderBy;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.list.FlatJcrContainer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The jcr container backing the search view. It provides the subset of items returned by the current search. It will include <code>mgnl:folder</code> nodes if the latter are defined as "searchable".
 * 
 * @see #findSearchableNodeTypes()
 */
public class SearchJcrContainer extends FlatJcrContainer {

    private static final Logger log = LoggerFactory.getLogger(SearchJcrContainer.class);

    protected static final String WHERE_TEMPLATE_FOR_SEARCH = "localname() LIKE '%1$s%%' or " + SELECTOR_NAME + ".['%2$s'] IS NOT NULL %3$s";

    protected static final String CONTAINS_TEMPLATE_FOR_SEARCH = "contains(" + SELECTOR_NAME + ".*, '%1$s')";

    protected static final String JCR_SCORE_FUNCTION = "score(" + SELECTOR_NAME + ")";

    private String fullTextExpression;

    private String whereCauseNodeTypes;
    /**
     * Will split a string like the following into simple terms. <em>Get "your facts" first and then "you can distort them" as much "as you please"</em>
     * <ul>
     * <li>Get
     * <li>"your facts"
     * <li>first
     * <li>and
     * <li>then
     * <li>"you can distort them"
     * <li>as
     * <li>much
     * <li>"as you please"
     * </ul>
     */
    private static final Pattern simpleTermsRegexPattern = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");

    private static final String illegalFullTextChars = "-+)\"\\";

    public SearchJcrContainer(WorkbenchDefinition workbenchDefinition) {
        super(workbenchDefinition);
        whereCauseNodeTypes = super.getQueryWhereClauseNodeTypes();

        for (NodeType nt : getSearchableNodeTypes()) {
            // include mgnl:folder if searchable
            if (NodeTypes.Folder.NAME.equals(nt.getName())) {
                whereCauseNodeTypes += " or [jcr:primaryType] = '" + NodeTypes.Folder.NAME + "'";
                break;
            }
        }
    }

    /**
     * Overrides its default implementation to take further constraints from {@link #getQueryWhereClauseSearch()} into account.
     */
    @Override
    protected String getQueryWhereClause() {
        final String clauseWorkspacePath = getQueryWhereClauseWorkspacePath();
        final String whereClauseSearch = getQueryWhereClauseSearch();

        String whereClause = "(" + getQueryWhereClauseNodeTypes() + ")";

        if (!"".equals(whereClauseSearch)) {
            whereClause += " and (" + whereClauseSearch + ") ";
        }

        if (!"".equals(clauseWorkspacePath)) {
            if (!"".equals(whereClause)) {
                whereClause = clauseWorkspacePath + " and " + whereClause;
            } else {
                whereClause += clauseWorkspacePath;
            }
        }

        if (!"".equals(whereClause)) {
            whereClause = " where (" + whereClause + ")";
        }

        log.debug("JCR query WHERE clause is {}", whereClause);
        return whereClause;
    }

    @Override
    protected String getQueryWhereClauseNodeTypes() {
        return whereCauseNodeTypes;
    }

    /**
     * Builds a string representing the constraints to be applied for this search. Used by the overridden {@link #getQueryWhereClause()} to augment the WHERE clause for this query.
     * It basically adds constraints on node names, property names and full-text search on all <code>searchable</code> properties, i.e. those not excluded by Magnolia/JackRabbit's indexing configuration.
     * <p>
     * See /magnolia-core/src/main/resources/info/magnolia/jackrabbit/indexing_configuration.xml
     */
    protected String getQueryWhereClauseSearch() {
        if (StringUtils.isBlank(getFullTextExpression())) {
            return "";
        }
        final String unescapedFullTextExpression = getFullTextExpression();

        final String escapedSearch = Text.escapeIllegalJcrChars(unescapedFullTextExpression);
        final String stmt = String.format(WHERE_TEMPLATE_FOR_SEARCH, escapedSearch, escapedSearch, String.format("or " + CONTAINS_TEMPLATE_FOR_SEARCH, escapeFullTextExpression(unescapedFullTextExpression)));

        log.debug("Search where-clause is {}", stmt);
        return stmt;
    }

    public void setFullTextExpression(String fullTextExpression) {
        this.fullTextExpression = fullTextExpression;
    }

    public String getFullTextExpression() {
        return fullTextExpression;
    }

    @Override
    protected String getJcrNameOrderByFunction() {
        return JCR_SCORE_FUNCTION;
    }

    @Override
    /**
     * Order by jcr score descending.
     */
    protected OrderBy getDefaultOrderBy(String property) {
        return new OrderBy(property, false);
    }

    /**
     * See http://wiki.apache.org/jackrabbit/EncodingAndEscaping.
     */
    private String escapeFullTextExpression(final String fulltextExpression) {
        //
        List<String> matchList = findSimpleTerms(fulltextExpression);

        final List<String> simpleTerms = new ArrayList<String>();
        for (String token : matchList) {
            if ("or".equals(token)) { // yes, Jackrabbit doesn't like lowercase or
                simpleTerms.add("OR");
            } else {
                simpleTerms.add(escapeIllegalFullTextSearchChars(token));
            }
        }
        // workaround as our regex does not match one single double quote ["]
        if ("\"".equals(fullTextExpression)) {
            simpleTerms.add("\\\"");
        }
        String returnValue = StringUtils.join(simpleTerms, " ");

        return returnValue.replaceAll("'", "''").trim();
    }

    /**
     * @return a list of simple terms according to JCR 2.0 definition, i.e. SimpleTerm ::= Word | '"' Word {Space Word} '"'
     * (See http://www.day.com/specs/jcr/2.0/6_Query.html#6.7.19%20FullTextSearch)
     */
    private List<String> findSimpleTerms(final String unescapedFullTextExpression) {
        List<String> matchList = new LinkedList<String>();
        Matcher regexMatcher = simpleTermsRegexPattern.matcher(unescapedFullTextExpression);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }
        return matchList;
    }

    /**
     * "Within a term, each “"” (double quote), “-” (minus sign), and “\” (backslash) must be escaped by a preceding “\”. An implementation may, however, restrict acceptable search strings further by augmenting this grammar and expanding the semantics appropriately."
     * (See http://www.day.com/specs/jcr/2.0/6_Query.html#6.7.19%20FullTextSearch)
     * Rules for escaping illegal chars (chars or simple terms included in square brackets for better readability):
     * <ul>
     * <li><code>[-]</code> escape
     * <li><code>[-abc]</code> don't escape
     * <li><code>[abc-]</code> don't escape
     * <li><code>[ab-c]</code> don't escape
     * <li><code>[+]</code> escape
     * <li><code>[+abc]</code> don't escape
     * <li><code>[abc+]</code> don't escape
     * <li><code>[ab+c]</code> don't escape
     * <li><code>[\]</code> escape
     * <li><code>[\abc]</code> don't escape
     * <li><code>[abc\]</code> don't escape
     * <li><code>[a\bc]</code> don't escape
     * <li><code>[)]</code> escape
     * <li><code>["]</code> always escape unless it delimits a simple term, i.e <code>"foo -bar"</code>
     * </ul>
     * <strong>This method has package visibility for testing purposes.</strong>
     */
    final String escapeIllegalFullTextSearchChars(final String simpleTerm) {
        StringBuilder sb = new StringBuilder(simpleTerm.length());

        for (int i = 0; i < simpleTerm.length(); i++) {
            char ch = simpleTerm.charAt(i);
            if (illegalFullTextChars.indexOf(ch) != -1) {
                switch (ch) {
                case '-':
                case '+':
                case '\\':
                    if (simpleTerm.length() == 1) {
                        sb.append('\\');
                    }
                    break;
                case ')': // always escape no matter its position
                    sb.append('\\');
                    break;
                case '\"':
                    if ((simpleTerm.startsWith("\"") && simpleTerm.endsWith("\"")) && (i != 0 && i != simpleTerm.length() - 1)) {
                        sb.append('\\');
                    }
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }

}
