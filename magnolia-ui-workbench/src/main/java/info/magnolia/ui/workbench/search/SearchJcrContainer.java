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

import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.list.FlatJcrContainer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The jcr container backing the search view. It provides the subset of items returned by the current search. By default it will perform a full-text search OR a search on the jcr name
 */
public class SearchJcrContainer extends FlatJcrContainer {

    private static final Logger log = LoggerFactory.getLogger(SearchJcrContainer.class);

    protected static final String WHERE_TEMPLATE_FOR_SEARCH = " (localname() LIKE '%%%1$s%%' or contains(" + SELECTOR_NAME + ".*, '%1$s'))";
    private String fullTextExpression;

    public SearchJcrContainer(WorkbenchDefinition workbenchDefinition) {
        super(workbenchDefinition);
    }

    @Override
    protected String getQueryWhereClause() {
        final String clauseWorkspacePath = getQueryWhereClauseWorkspacePath();
        String whereClause = getQueryWhereClauseSearch();

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

    protected String getQueryWhereClauseSearch() {
        if (StringUtils.isBlank(getFullTextExpression())) {
            return "";
        }
        // See http://wiki.apache.org/jackrabbit/EncodingAndEscaping
        final String escapedFullTextExpression = getFullTextExpression().replaceAll("'", "''").trim();
        final String stmt = String.format(WHERE_TEMPLATE_FOR_SEARCH, escapedFullTextExpression);
        log.debug("Search where-clause is {}", stmt);
        return stmt;
    }

    public void setFullTextExpression(String fullTextExpression) {
        this.fullTextExpression = fullTextExpression;
    }

    public String getFullTextExpression() {
        return fullTextExpression;
    }

}
