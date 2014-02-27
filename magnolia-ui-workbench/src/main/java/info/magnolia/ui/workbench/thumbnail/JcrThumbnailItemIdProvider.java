/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.workbench.thumbnail;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 27/02/14
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class JcrThumbnailItemIdProvider implements ThumbnailContainer.IdProvider {

    protected static final String WHERE_TEMPLATE_FOR_PATH = " WHERE (%s) %s ";

    private Logger log = LoggerFactory.getLogger(getClass());

    private WorkbenchDefinition workbenchDefinition;

    public JcrThumbnailItemIdProvider(WorkbenchDefinition workbenchDefinition) {
        this.workbenchDefinition = workbenchDefinition;
    }

    @Override
    public List<Object> getItemIds() {
        List<Object> uuids = new ArrayList<Object>();
        String workspaceName = workbenchDefinition.getWorkspace();
        final String query = constructQuery();
        try {
            QueryManager qm = MgnlContext.getJCRSession(workspaceName).getWorkspace().getQueryManager();
            Query q = qm.createQuery(query, Query.JCR_SQL2);

            log.debug("Executing query statement [{}] on workspace [{}]", query, workspaceName);
            long start = System.currentTimeMillis();

            QueryResult queryResult = q.execute();
            NodeIterator iter = queryResult.getNodes();

            while (iter.hasNext()) {
                uuids.add(iter.nextNode().getIdentifier());
            }

            log.debug("Done collecting {} nodes in {}ms", uuids.size(), System.currentTimeMillis() - start);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return uuids;
    }

    /**
     * Hint: could be dropped once this type bases on AbstractJcrContainer as well (BL-153).
     */
    protected String getMainNodeType() {
        final List<NodeTypeDefinition> nodeTypes = workbenchDefinition.getNodeTypes();
        return nodeTypes.isEmpty() ? AbstractJcrContainer.DEFAULT_NODE_TYPE : nodeTypes.get(0).getName();
    }

    protected String prepareSelectQueryStatement() {
        return String.format("select * from [nt:base] as t ", getMainNodeType());
    }

    protected String prepareFilterQueryStatement() {
        String nodeTypes = getQueryWhereClauseNodeTypes();
        boolean pathIsNotRoot = StringUtils.isNotBlank(workbenchDefinition.getPath()) && !"/".equals(workbenchDefinition.getPath());
        return String.format(WHERE_TEMPLATE_FOR_PATH, nodeTypes, pathIsNotRoot ? " AND ISDESCENDANTNODE('" + workbenchDefinition.getPath() + "')" : "");

    }

    protected String prepareOrderQueryStatement() {
        return " order by name(t)";
    }

    /**
     * @return a String containing the node types to be searched for in a query. All node types declared in a workbench definition are returned
     * unless their <code>hideInList</code> property is true or they are of type <code>mgnl:folder</code>. E.g. assuming a node types declaration like the following
     *
     * <pre>
     * ...
     * + workbench
     *  + nodeTypes
     *   + foo
     *    * name = nt:foo
     *   + bar
     *    * name = nt:bar
     *    * hideInList = true
     *   + baz
     *    * name = nt:baz
     * ...
     * </pre>
     *
     * this method will return the following string <code>[jcr:primaryType] = 'nt:foo' or [jcr:primaryType] = 'baz'</code>. This will eventually be used to restrict the node types to be searched for
     * in list and search views, i.e. <code>select * from [nt:base] where ([jcr:primaryType] = 'nt:foo' or [jcr:primaryType] = 'baz')</code>.
     */
    protected String getQueryWhereClauseNodeTypes() {
        List<String> defs = new ArrayList<String>();
        for (NodeTypeDefinition type : workbenchDefinition.getNodeTypes()) {
            if (type.isHideInList() || NodeTypes.Folder.NAME.equals(type.getName())) {
                log.debug("Skipping {} node type. Nodes of such type won't be searched for.", type.getName());
                continue;
            }
            defs.add("[jcr:primaryType] = '" + type.getName() + "'");
        }
        return StringUtils.join(defs, " or ");
    }

    private String constructQuery() {
        return prepareSelectQueryStatement() + prepareFilterQueryStatement() + prepareOrderQueryStatement();
    }
}
