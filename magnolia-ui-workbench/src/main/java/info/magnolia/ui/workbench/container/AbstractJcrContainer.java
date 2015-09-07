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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.ContainerHelpers;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Vaadin container that reads its items from a JCR repository. Implements a simple mechanism for lazy loading items
 * from a JCR repository and a cache for items and item ids.
 */
public abstract class AbstractJcrContainer extends AbstractContainer implements Container.Sortable, Container.Indexed, Container.ItemSetChangeNotifier, Refreshable {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrContainer.class);

    public static final int DEFAULT_PAGE_LENGTH = 30;

    public static final int DEFAULT_CACHE_RATIO = 2;

    /**
     * String separating a properties name and the uuid of its node.
     */
    public static final String PROPERTY_NAME_AND_UUID_SEPARATOR = "@";

    private static final Long LONG_ZERO = Long.valueOf(0);

    /**
     * Node type to use if none is configured.
     */
    public static final String DEFAULT_NODE_TYPE = NodeTypes.Content.NAME;

    private static final String QUERY_LANGUAGE = Query.JCR_JQOM;

    protected static final String SELECTOR_NAME = "t";

    protected static final String SELECT_TEMPLATE = "select * from [nt:base] as " + SELECTOR_NAME;

    protected static final String WHERE_TEMPLATE_FOR_PATH = " ISDESCENDANTNODE('%s')";

    protected static final String ORDER_BY = " order by ";

    protected static final String ASCENDING_KEYWORD = " asc";

    protected static final String DESCENDING_KEYWORD = " desc";

    protected static final String JCR_NAME_FUNCTION = "lower(name(" + SELECTOR_NAME + "))";

    /**
     * Item and index caches.
     */
    private final Map<Long, JcrItemId> itemIndexes = new HashMap<Long, JcrItemId>();

    private final List<String> sortableProperties = new ArrayList<String>();

    protected final List<OrderBy> sorters = new ArrayList<OrderBy>();

    private int size = Integer.MIN_VALUE;

    /**
     * Page length = number of items contained in one page.
     */
    private int pageLength = DEFAULT_PAGE_LENGTH;

    /**
     * Number of items to cache = cacheRatio x pageLength.
     */
    private int cacheRatio = DEFAULT_CACHE_RATIO;

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    /**
     * Starting row number of the currently fetched page.
     */
    private int currentOffset;

    private Set<NodeType> searchableNodeTypes;

    private JcrContentConnectorDefinition contentConnectorDefinition;

    public AbstractJcrContainer(JcrContentConnectorDefinition jcrContentConnectorDefinition) {
        this.contentConnectorDefinition = jcrContentConnectorDefinition;
        this.searchableNodeTypes = findSearchableNodeTypes();
    }

    public void addSortableProperty(final String sortableProperty) {
        sortableProperties.add(sortableProperty);
    }

    public JcrContentConnectorDefinition getConfiguration() {
        return contentConnectorDefinition;
    }

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        addItemSetChangeListener(listener);
    }

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty()) {
                itemSetChangeListeners = null;
            }
        }
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        removeItemSetChangeListener(listener);
    }

    public void fireItemSetChange() {
        log.debug("Firing item set changed");
        if (itemSetChangeListeners != null && !itemSetChangeListeners.isEmpty()) {
            final Container.ItemSetChangeEvent event = new AbstractContainer.ItemSetChangeEvent();
            Object[] array = itemSetChangeListeners.toArray();
            for (Object anArray : array) {
                ItemSetChangeListener listener = (ItemSetChangeListener) anArray;
                listener.containerItemSetChange(event);
            }
        }
    }

    protected Map<Long, JcrItemId> getItemIndexes() {
        return itemIndexes;
    }

    public int getPageLength() {
        return pageLength;
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public int getCacheRatio() {
        return cacheRatio;
    }

    public void setCacheRatio(int cacheRatio) {
        this.cacheRatio = cacheRatio;
    }

    public javax.jcr.Item getJcrItem(Object itemId) {
        if (itemId == null || !(itemId instanceof JcrItemId)) {
            return null;
        }
        try {
            return JcrItemUtil.getJcrItem((JcrItemId) itemId);
        } catch (PathNotFoundException p) {
            log.debug("Could not access itemId {} in workspace {} - {}. Most likely it has been (re)moved in the meantime.", new Object[] { itemId, getWorkspace(), p.toString() });
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Could not retrieve jcr item with id: " + itemId, e);
        }
        return null;
    }

    /**************************************/
    /** Methods from interface Container **/
    /**************************************/
    @Override
    public Item getItem(Object itemId) {
        javax.jcr.Item item = getJcrItem(itemId);
        if (item == null) {
            return null;
        }
        return item.isNode() ? new JcrNodeAdapter((Node) item) : new JcrPropertyAdapter((javax.jcr.Property) item);
    }

    @Override
    public Collection<String> getItemIds() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support this method.");
    }

    @Override
    public Property<?> getContainerProperty(Object itemId, Object propertyId) {
        final Item item = getItem(itemId);
        if (item != null) {
            return item.getItemProperty(propertyId);
        }

        log.warn("Couldn't find item {} so property {} can't be retrieved!", itemId, propertyId);
        return null;
    }

    /**
     * Gets the number of visible Items in the Container.
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsId(Object itemId) {
        return getItem(itemId) != null;
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        fireItemSetChange();
        return getItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**********************************************/
    /** Methods from interface Container.Indexed **/
    /**
     * *****************************************
     */

    @Override
    public int indexOfId(Object itemId) {

        if (!containsId(itemId)) {
            return -1;
        }
        int size = size();
        ensureItemIndices();
        boolean wrappedAround = false;
        while (!wrappedAround) {
            for (Long i : itemIndexes.keySet()) {
                if (itemIndexes.get(i).equals(itemId)) {
                    return i.intValue();
                }
            }
            // load in the next page.
            int nextIndex = (currentOffset / (pageLength * cacheRatio) + 1) * (pageLength * cacheRatio);
            if (nextIndex >= size) {
                // Container wrapped around, start from index 0.
                wrappedAround = true;
                nextIndex = 0;
            }
            updateOffsetAndCache(nextIndex);
        }
        return -1;
    }

    protected void ensureItemIndices() {
        if (itemIndexes.isEmpty()) {
            getPage();
        }
    }

    @Override
    public JcrItemId getIdByIndex(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        final Long idx = Long.valueOf(index);
        if (itemIndexes.containsKey(idx)) {
            return itemIndexes.get(idx);
        }
        log.debug("item id {} not found in cache. Need to update offset, fetch new item ids from jcr repo and put them in cache.", index);
        updateOffsetAndCache(index);
        return itemIndexes.get(idx);
    }

    /**********************************************/
    /** Methods from interface Container.Ordered **/
    /**
     * *****************************************
     */

    @Override
    public JcrItemId nextItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) + 1);
    }

    @Override
    public JcrItemId prevItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) - 1);
    }

    @Override
    public JcrItemId firstItemId() {
        if (size == 0) {
            return null;
        }
        if (!itemIndexes.containsKey(LONG_ZERO)) {
            updateOffsetAndCache(0);
        }
        return itemIndexes.get(LONG_ZERO);
    }

    @Override
    public JcrItemId lastItemId() {
        final Long lastIx = Long.valueOf(size() - 1);
        if (!itemIndexes.containsKey(lastIx)) {
            updateOffsetAndCache(size - 1);
        }
        return itemIndexes.get(lastIx);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return firstItemId().equals(itemId);
    }

    @Override
    public boolean isLastId(Object itemId) {
        return lastItemId().equals(itemId);
    }

    /***********************************************/
    /** Methods from interface Container.Sortable **/
    /**
     * ******************************************
     */
    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        clearItemIndexes();
        resetOffset();
        sorters.clear();
        for (int i = 0; i < propertyId.length; i++) {
            if (sortableProperties.contains(propertyId[i])) {
                OrderBy orderBy = new OrderBy((String) propertyId[i], ascending[i]);
                sorters.add(orderBy);
            }
        }
        getPage();
    }

    @Override
    public List<String> getSortableContainerPropertyIds() {
        return Collections.unmodifiableList(sortableProperties);
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    /************************************/
    /** UNSUPPORTED CONTAINER FEATURES **/
    /**
     * *******************************
     */

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines a new offset for updating the row cache. The offset is calculated from the given index, and will be
     * fixed to match the start of a page, based on the value of pageLength.
     *
     * @param index Index of the item that was requested, but not found in cache
     */
    private void updateOffsetAndCache(int index) {
        if (itemIndexes.containsKey(Long.valueOf(index))) {
            return;
        }
        currentOffset = (index / (pageLength * cacheRatio)) * (pageLength * cacheRatio);
        if (currentOffset < 0) {
            resetOffset();
        }
        getPage();
    }

    /**
     * Triggers a refresh if the current row count has changed.
     */
    private void updateCount(long newSize) {
        if (newSize != size) {
            setSize((int) newSize);
        }
    }

    /**
     * Fetches a page from the data source based on the values of pageLength, cacheRatio and currentOffset.
     */
    private final void getPage() {

        final String stmt = constructJCRQuery(true);
        if (StringUtils.isEmpty(stmt)) {
            return;
        }

        try {
            final QueryResult queryResult = executeQuery(stmt, QUERY_LANGUAGE, pageLength * cacheRatio, currentOffset);
            updateItems(queryResult);
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot get Page with statement: " + stmt, e);
        }
    }

    /**
     * Updates this container by storing the items found in the query result passed as argument.
     *
     * @see #getPage()
     */
    private void updateItems(final QueryResult queryResult) throws RepositoryException {
        long start = System.currentTimeMillis();
        log.debug("Starting iterating over QueryResult");
        final RowIterator iterator = queryResult.getRows();
        long rowCount = currentOffset;
        while (iterator.hasNext()) {
            final Node node = iterator.nextRow().getNode(SELECTOR_NAME);
            final JcrItemId itemId = JcrItemUtil.getItemId(node);
            log.trace("Adding node {} to cached items.", itemId.getUuid());
            itemIndexes.put(rowCount++, itemId);
        }

        log.debug("Done in {} ms", System.currentTimeMillis() - start);
    }

    /**
     * @param considerSorting an optional <code>ORDER BY</code> is added if this parameter is <code>true</code>.
     * @return a string representing a JCR statement to retrieve this container's items.
     * It creates a JCR query in the form {@code select * from [nt:base] as selectorName [WHERE] [ORDER BY]"}.
     * <p>
     * Subclasses can customize the optional <code>WHERE</code> clause by overriding {@link #getQueryWhereClause()} or, at a more fine-grained level, {@link #getQueryWhereClauseNodeTypes()} and {@link #getQueryWhereClauseWorkspacePath()}.
     * <p>
     * A restriction on the node types to be searched for can be applied via {@link #getQueryWhereClauseNodeTypes()}.
     */
    protected String constructJCRQuery(final boolean considerSorting) {
        final String select = getQuerySelectStatement();
        final StringBuilder stmt = new StringBuilder(select);
        // Return results only within the node configured in workbench/path
        stmt.append(getQueryWhereClause());

        if (considerSorting) {
            if (sorters.isEmpty()) {
                // no sorters set - use defaultOrder (always ascending)
                String defaultOrder = contentConnectorDefinition.getDefaultOrder();
                String[] defaultOrders = defaultOrder.split(",");
                for (String current : defaultOrders) {
                    sorters.add(getDefaultOrderBy(current));
                }
            }
            stmt.append(ORDER_BY);
            String sortOrder;
            for (OrderBy orderBy : sorters) {
                String propertyName = orderBy.getProperty();
                sortOrder = orderBy.isAscending() ? ASCENDING_KEYWORD : DESCENDING_KEYWORD;
                if (ModelConstants.JCR_NAME.equals(propertyName)) {
                    stmt.append(getJcrNameOrderByFunction()).append(sortOrder).append(", ");
                    continue;
                }
                stmt.append(SELECTOR_NAME);
                stmt.append(".[").append(propertyName).append("]").append(sortOrder).append(", ");
            }
            stmt.delete(stmt.lastIndexOf(","), stmt.length());
        }
        log.debug("Constructed JCR query is {}", stmt);
        return stmt.toString();
    }

    /**
     * @return an {@link OrderBy} object for the passed in property to be used for the default order by clause.
     */
    protected OrderBy getDefaultOrderBy(final String property) {
        return new OrderBy(property, true);
    }

    /**
     * @return the jcr function used to sort the node name (or jcr name property) column. By default it's {@link #JCR_NAME_FUNCTION}.
     */
    protected String getJcrNameOrderByFunction() {
        return JCR_NAME_FUNCTION;
    }

    /**
     * @return the JCR query where clause to select only node types which are not hidden in list and nodes under the path configured in the workspace as String - if the latter
     * is not configured return a blank string so that all nodes are considered.
     * @see AbstractJcrContainer#getQueryWhereClauseNodeTypes()
     * @see #getQueryWhereClauseWorkspacePath()
     */
    protected String getQueryWhereClause() {
        String whereClause = "";
        String clauseWorkspacePath = getQueryWhereClauseWorkspacePath();
        String clauseNodeTypes = getQueryWhereClauseNodeTypes();
        if (StringUtils.isNotBlank(clauseNodeTypes)) {
            whereClause = " where ((" + clauseNodeTypes + ") ";
            if (StringUtils.isNotBlank(clauseWorkspacePath)) {
                whereClause += "and " + clauseWorkspacePath;
            }
            whereClause += ") ";
        } else {
            whereClause = " where ";
        }

        log.debug("JCR query WHERE clause is {}", whereClause);
        return whereClause;
    }

    /**
     * @return a String containing the node types to be displayed in a list view and searched for in a query. All node types declared in a workbench definition are returned
     * unless their <code>hideInList</code> property is true or the node is of type <code>mgnl:folder</code> (custom implementations of this method may still decide to display folders). Assuming a node types declaration like the following
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
     *   + baz (a mixin type)
     *    * name = nt:baz
     * ...
     * </pre>
     *
     * this method will return the following string <code>[jcr:primaryType] = 'nt:foo' or [jcr:mixinTypes] = 'baz'</code>. This will eventually be used to restrict the node types to be displayed in list views and searched for
     * in search views, i.e. <code>select * from [nt:base] where ([jcr:primaryType] = 'nt:foo' or [jcr:mixinTypes] = 'baz')</code>.
     * @see #findSearchableNodeTypes()
     */
    protected String getQueryWhereClauseNodeTypes() {

        List<String> defs = new ArrayList<String>();

        for (NodeType nt : getSearchableNodeTypes()) {
            if (nt.isNodeType(NodeTypes.Folder.NAME)) {
                continue;
            }
            if (nt.isMixin()) {
                // Mixin type information is found in jcr:mixinTypes property see http://www.day.com/specs/jcr/2.0/10_Writing.html#10.10.3%20Assigning%20Mixin%20Node%20Types
                defs.add("[jcr:mixinTypes] = '" + nt.getName() + "'");
            } else {
                defs.add("[jcr:primaryType] = '" + nt.getName() + "'");
            }
        }
        return StringUtils.join(defs, " or ");
    }

    /**
     * @return if ((JcrContentConnectorDefinition)workbenchDefinition.getContentConnector()).getPath() is not null or root ("/"), an ISDESCENDATNODE constraint narrowing the scope of search under the configured path, else an empty string.
     */
    protected final String getQueryWhereClauseWorkspacePath() {
        // By default, search the root and therefore do not need a query clause.
        String whereClauseWorkspacePath = "";
        String path = contentConnectorDefinition.getRootPath();
        if (StringUtils.isNotBlank(path) && !"/".equals(path)) {
            whereClauseWorkspacePath = String.format(WHERE_TEMPLATE_FOR_PATH, path);
        }
        log.debug("Workspace path where-clause is {}", whereClauseWorkspacePath);
        return whereClauseWorkspacePath;
    }

    /**
     * @return a <code>SELECT</code> statement whose node type is <code>nt:base</code>. Can be customized by subclasses to utilize other item types, i.e. {@code select * from [my:fancytype]}. Used internally by {@link #constructJCRQuery(boolean)}.
     */
    protected String getQuerySelectStatement() {
        return SELECT_TEMPLATE;
    }

    /**
     * @deprecated since 5.1. All node types declared in the workbench definition are equal, meaning that their position doesn't matter when it comes to which ones are used in a query.
     * The discriminating factor is the <code>hideInList</code> boolean property. If that property is <code>true</code>, then the node will be excluded from the query.
     */
    @Deprecated
    protected String getMainNodeType() {
        final List<NodeTypeDefinition> nodeTypes = contentConnectorDefinition.getNodeTypes();
        return nodeTypes.isEmpty() ? DEFAULT_NODE_TYPE : nodeTypes.get(0).getName();
    }

    /**
     * @see #getPage().
     */
    public void updateSize() {
        final String stmt = constructJCRQuery(false);
        try {
            // query for all items in order to get the size
            final QueryResult queryResult = executeQuery(stmt, QUERY_LANGUAGE, 0, 0);

            final long pageSize = queryResult.getRows().getSize();
            log.debug("Query result set contains {} items", pageSize);

            updateCount((int) pageSize);
        } catch (RepositoryException e) {
            updateCount(0);
            handleRepositoryException(log, "Could not update size with statement: " + stmt, e);
        }
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        return ContainerHelpers.getItemIdsUsingGetIdByIndex(startIndex, numberOfItems, this);
    }

    public String getWorkspace() {
        return contentConnectorDefinition.getWorkspace();
    }

    public Set<NodeType> getSearchableNodeTypes() {
        return searchableNodeTypes;
    }

    /**
     * Refreshes the container - clears all caches and resets size and offset. Does NOT remove sorting or filtering
     * rules!
     */
    @Override
    public void refresh() {
        resetOffset();
        clearItemIndexes();
        updateSize();
        fireItemSetChange();
    }

    protected void resetOffset() {
        currentOffset = 0;
    }

    protected void clearItemIndexes() {
        itemIndexes.clear();
    }

    protected int getCurrentOffset() {
        return currentOffset;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected QueryResult executeQuery(String statement, String language, long limit, long offset) throws RepositoryException {
        final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
        final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
        final Query query = jcrQueryManager.createQuery(statement, language);
        if (limit > 0) {
            query.setLimit(limit);
        }
        if (offset >= 0) {
            query.setOffset(offset);
        }
        log.debug("Executing query against workspace [{}] with statement [{}] and limit {} and offset {}...", new Object[] { getWorkspace(), statement, limit, offset });
        long start = System.currentTimeMillis();
        final QueryResult result = query.execute();
        log.debug("Query execution took {} ms", System.currentTimeMillis() - start);

        return result;
    }

    /**
     * Central method for uniform treatment of RepositoryExceptions in JcrContainers.
     *
     * @param logger logger to be used - passed in so subclasses can still user their proper logger
     * @param message message to be used in the handling
     * @param repositoryException exception to be handled
     */
    protected void handleRepositoryException(final Logger logger, final String message, final RepositoryException repositoryException) {
        logger.warn(message + ": " + repositoryException);
    }

    /**
     * @return a Set of searchable {@link NodeType}s. A searchable node type is defined as follows
     * <ul>
     * <li>It is a <a href="http://jackrabbit.apache.org/node-types.html">primary or mixin</a> node type configured under <code>/modules/mymodule/apps/myapp/subApps/browser/workbench/nodeTypes</code>
     * <li>It is not hidden in list and search views (property <code>hideInList=true</code>). By default nodes are not hidden.
     * <li>If not strict (property <code>strict=false</code>), then its subtypes (if any) are searchable too. By default nodes are not defined as strict.
     * <li>Subtypes beginning with <code>jcr:, nt:, mix:, rep:</code> are not taken into account.
     * </ul>
     */
    protected Set<NodeType> findSearchableNodeTypes() {
        final List<String> hiddenInList = new ArrayList<String>();
        final List<NodeTypeDefinition> nodeTypeDefinition = contentConnectorDefinition.getNodeTypes();
        log.debug("Defined node types are {}", nodeTypeDefinition);

        for (NodeTypeDefinition def : nodeTypeDefinition) {
            if (def.isHideInList()) {
                log.debug("{} is hidden in list/search therefore it won't be displayed there.", def.getName());
                hiddenInList.add(def.getName());
            }
        }

        final Set<NodeType> searchableNodeTypes = new HashSet<NodeType>();
        if (getWorkspace() == null) {
            // no workspace, no searchable types
            return searchableNodeTypes;
        }
        try {
            final NodeTypeManager nodeTypeManager = MgnlContext.getJCRSession(getWorkspace()).getWorkspace().getNodeTypeManager();

            for (NodeTypeDefinition def : nodeTypeDefinition) {
                final String nodeTypeName = def.getName();
                if (hiddenInList.contains(nodeTypeName)) {
                    continue;
                }

                final NodeType nt = nodeTypeManager.getNodeType(nodeTypeName);
                searchableNodeTypes.add(nt);

                if (def.isStrict()) {
                    log.debug("{} is defined as strict, therefore its possible subtypes won't be taken into account.", nodeTypeName);
                    continue;
                }

                final NodeTypeIterator subTypesIterator = nt.getSubtypes();

                while (subTypesIterator.hasNext()) {
                    final NodeType subType = subTypesIterator.nextNodeType();
                    final String subTypeName = subType.getName();
                    if (hiddenInList.contains(subTypeName) || subTypeName.startsWith("jcr:") || subTypeName.startsWith("mix:") || subTypeName.startsWith("rep:") || subTypeName.startsWith("nt:")) {
                        continue;
                    }
                    log.debug("Adding {} as subtype of {}", subTypeName, nodeTypeName);
                    searchableNodeTypes.add(subType);
                }
            }
        } catch (LoginException e) {
            handleRepositoryException(log, e.getMessage(), e);

        } catch (RepositoryException e) {
            handleRepositoryException(log, e.getMessage(), e);
        }
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("[");
            for (NodeType nt : searchableNodeTypes) {
                sb.append(String.format("[%s - isMixin? %s] ", nt.getName(), nt.isMixin()));
            }
            sb.append("]");
            log.debug("Found searchable nodetypes (both primary types and mixins) and their subtypes {}", sb.toString());
        }
        return searchableNodeTypes;
    }
}
