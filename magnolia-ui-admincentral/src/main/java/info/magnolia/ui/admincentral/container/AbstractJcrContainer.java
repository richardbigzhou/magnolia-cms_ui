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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.ui.model.column.definition.ColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;


/**
 * Vaadin container that reads its items from a JCR repository. Implements a simple mechanism for
 * lazy loading items from a JCR repository and a cache for items and item ids. Inspired by
 * http://vaadin.com/directory#addon/vaadin-sqlcontainer.
 */
@SuppressWarnings("serial")
public abstract class AbstractJcrContainer extends AbstractContainer implements Container.Sortable, Container.Indexed, Container.ItemSetChangeNotifier, Container.PropertySetChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrContainer.class);

    public static final String ITEM_ICON_PROPERTY_ID = "mgnl_item_icon";

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    private Set<PropertySetChangeListener> propertySetChangeListeners;

    private final JcrContainerSource jcrContainerSource;

    private int size = Integer.MIN_VALUE;

    /** Page length = number of items contained in one page. */
    private int pageLength = DEFAULT_PAGE_LENGTH;

    public static final int DEFAULT_PAGE_LENGTH = 30;

    /** Number of items to cache = cacheRatio x pageLength.  */
    private int cacheRatio = DEFAULT_CACHE_RATIO;

    public static final int DEFAULT_CACHE_RATIO = 2;

    /** Item and index caches. */
    private final Map<Long, String> itemIndexes = new HashMap<Long, String>();

    private final List<String> sortableProperties = new ArrayList<String>();

    private final List<OrderBy> sorters = new ArrayList<OrderBy>();

    private final String workspace;

    /** Starting row number of the currently fetched page. */
    private int currentOffset;

    private static final Long LONG_ZERO = Long.valueOf(0);

    private static final String CONTENT_SELECTOR_NAME = "[mgnl:content]";

    private static final String METADATA_SELECTOR_NAME = "metaData";

    private static final String SELECT_CONTENT = "//element(*,mgnl:content) ";

    private static final String JOIN_METADATA = "inner join [mgnl:metaData] as metaData on ischildnode(metaData,content) ";

    private static final String ORDER_BY = "order by";

    private static final String NAME_PROPERTY = "name";

    private static final String META_DATA_PROPERTY_TO_BE_REPLACED_FOR_ORDER_BY = "MetaData/";

    // need to use name(..) function as name or jcr:name is not supported by JCR2.
    private static final String JCR_NAME_FUNCTION = "name(" + CONTENT_SELECTOR_NAME + ")";

    public AbstractJcrContainer(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
        this.jcrContainerSource = jcrContainerSource;
        workspace = workbenchDefinition.getWorkspace();

        for (ColumnDefinition column : workbenchDefinition.getColumns()) {
            if (column.isSortable()) {
                log.debug("Configuring column [{}] as sortable", column.getName());
                final String columnProperty = (column.getPropertyName() == null) ?  column.getName() : column.getPropertyName();

                sortableProperties.add(columnProperty);
            }
        }
    }

    /**
     * Updates the container with the items pointed to by the {@link RowIterator} passed as
     * argument.
     * @return the number of rows updated
     */
    public abstract long update(RowIterator iterator) throws RepositoryException;

    @Override
    public void addListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty()) {
                itemSetChangeListeners = null;
            }
        }
    }

    @Override
    public void addListener(PropertySetChangeListener listener) {
        if (propertySetChangeListeners == null) {
            propertySetChangeListeners = new LinkedHashSet<PropertySetChangeListener>();
        }
        propertySetChangeListeners.add(listener);
    }

    @Override
    public void removeListener(PropertySetChangeListener listener) {
        if (propertySetChangeListeners != null) {
            propertySetChangeListeners.remove(listener);
            if (propertySetChangeListeners.isEmpty()) {
                propertySetChangeListeners = null;
            }
        }
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

    public void firePropertySetChange() {

        log.debug("Firing property set changed");
        if (propertySetChangeListeners != null && !propertySetChangeListeners.isEmpty()) {
            final Container.PropertySetChangeEvent event = new AbstractContainer.PropertySetChangeEvent();
            Object[] array = propertySetChangeListeners.toArray();
            for (Object anArray : array) {
                PropertySetChangeListener listener = (PropertySetChangeListener) anArray;
                listener.containerPropertySetChange(event);
            }
        }
    }

    protected Map<Long, String> getItemIndexes() {
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

    /**************************************/
    /** Methods from interface Container **/
    /**************************************/

    @Override
    public Item getItem(Object itemId) {
        if(itemId == null) {
            return null;
        }
        try {
            final Session jcrSession = MgnlContext.getJCRSession(workspace);
            if(!jcrSession.itemExists((String) itemId)) {
                return null;
            }
            Node node = (Node) jcrSession.getItem((String) itemId);
            return new JcrNodeAdapter(node);
        } catch (RepositoryException e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public Collection<String> getItemIds() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support this method.");
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        final Item item = getItem(itemId);
        if (item != null) {
            return item.getItemProperty(propertyId);
        } else {
            log.error("Couldn't find item {} so property {} can't be retrieved!", itemId, propertyId);
            return null;
        }
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
        if (itemId == null) {
            return false;
        }

        try {
            final Session jcrSession = MgnlContext.getJCRSession(workspace);
            return jcrSession.nodeExists((String) itemId);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
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
    /**********************************************/

    @Override
    public int indexOfId(Object itemId) {

        if (!containsId(itemId)) {
            return -1;
        }
        int size = size();
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

    @Override
    public Object getIdByIndex(int index) {
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
    /**********************************************/

    @Override
    public Object nextItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) + 1);
    }

    @Override
    public Object prevItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) - 1);
    }

    @Override
    public Object firstItemId() {
        if (size == 0) {
            return null;
        }
        if (!itemIndexes.containsKey(LONG_ZERO)) {
            updateOffsetAndCache(0);
        }
        return itemIndexes.get(LONG_ZERO);
    }

    @Override
    public Object lastItemId() {
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
    /***********************************************/
    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
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

    protected JcrContainerSource getJcrContainerSource() {
        return jcrContainerSource;
    }

    /************************************/
    /** UNSUPPORTED CONTAINER FEATURES **/
    /************************************/

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
     * Determines a new offset for updating the row cache. The offset is calculated from the given
     * index, and will be fixed to match the start of a page, based on the value of pageLength.
     *
     * @param index Index of the item that was requested, but not found in cache
     */
    private void updateOffsetAndCache(int index) {
        if (itemIndexes.containsKey(Long.valueOf(index))) {
            return;
        }
        currentOffset = (index / (pageLength * cacheRatio)) * (pageLength * cacheRatio);
        if (currentOffset < 0) {
            currentOffset = 0;
        }
        getPage();
    }

    /**
     * Triggers a refresh if the current row count has changed.
     */
    private void updateCount(long newSize) {
        if (newSize != size) {
            size = (int) newSize;
        }
    }

    /**
     * Fetches a page from the data source based on the values of pageLength and currentOffset.
     * Internally it executes the following methods in this order:
     * <ul>
     * <li> {@link #constructJCRQuery()}
     * <li> {@link #executeQuery(String, String, long, long)}
     * <li> {@link #updateItems(QueryResult)}
     * </ul>
     */
    public final void getPage() {

        final String stmt = constructJCRQuery();

        if(StringUtils.isEmpty(stmt)) {
            return;
        }

        try {
            // FIXME sql2 query is much slower than its xpath/sql1 counterpart (on average 80 times
            // slower) see https://issues.apache.org/jira/browse/JCR-2830.
            // However xpath is deprecated and, although query execution is faster, it takes much
            // longer to iterate over the results (for an explanation see comment here
            // https://issues.apache.org/jira/browse/JCR-2715?focusedCommentId=12965273&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-12965273
            // "The SQL2/QOM implementation loads all matching rows into memory during the execute() call, so you'll see an expensive query.execute() but can then very quickly iterate over the query results.")
            // to the point that any benefit gained from faster query execution is lost and overall
            // performance gets worse.
            final QueryResult queryResult = executeQuery(stmt, Query.XPATH, pageLength * cacheRatio, currentOffset);

            updateItems(queryResult);

        } catch (RepositoryException re) {
            throw new RuntimeRepositoryException(re);
        }

    }

    /**
     * Updates this container by storing the items found in the query result passed as argument.
     * @see #getPage()
     */
    protected void updateItems(final QueryResult queryResult) throws RepositoryException {
        long start = System.currentTimeMillis();
        log.debug("Starting iterating over QueryResult");

        final javax.jcr.NodeIterator iterator = queryResult.getNodes();
        long rowCount = currentOffset;
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            final String id = node.getPath();
            log.debug("Adding node {} to cached items.", id);
            itemIndexes.put(rowCount++, id);
        }

        log.warn("Updating Items done in {} ms", System.currentTimeMillis() - start);
    }

    /**
     * @return a string representing a JCR statement to retrieve this container's items.
     * @see AbstractJcrContainer#getPage()
     */
    protected String constructJCRQuery() {
        final StringBuilder stmt = new StringBuilder(SELECT_CONTENT);
        if (sorters.isEmpty()) {
            // no sorters set - apply default (sort by name)
            stmt.append(ORDER_BY)
                    .append(" @jcrName")
                    .append(" ascending");
        } else {
            stmt.append(ORDER_BY);
            String sortOrder;
            for (OrderBy orderBy : sorters) {
                String propertyName = orderBy.getProperty();
                sortOrder = orderBy.isAscending() ? " ascending" : " descending";
                if (propertyName.startsWith(META_DATA_PROPERTY_TO_BE_REPLACED_FOR_ORDER_BY)) {
                    propertyName = propertyName.replaceFirst(META_DATA_PROPERTY_TO_BE_REPLACED_FOR_ORDER_BY, META_DATA_PROPERTY_TO_BE_REPLACED_FOR_ORDER_BY + "@");
                } else {
                    stmt.append("@");
                }
                stmt.append(propertyName)
                        .append(sortOrder)
                        .append(", ");

            }
            stmt.delete(stmt.lastIndexOf(","), stmt.length() - 1);
        }
        return stmt.toString();
    }
    /**
     * @see #getPage().
     */
    public final void updateSize() {
        try {
            final String query = constructJCRQuery();
            if(query == null) {
                return;
            }
            // query for all items in order to get the size
            final QueryResult queryResult = executeQuery(query, Query.XPATH, 0, 0);

            final long pageSize = queryResult.getRows().getSize();
            log.debug("Query resultset contains {} items", pageSize);

            updateCount((int) pageSize);
        } catch (RepositoryException e){
            throw new RuntimeRepositoryException(e);
        }
    }

    public String getWorkspace() {
        return workspace;
    }

    /**
     * Refreshes the container - clears all caches and resets size and offset. Does NOT remove
     * sorting or filtering rules!
     */
    public void refresh() {
        currentOffset = 0;
        itemIndexes.clear();
        updateSize();
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected QueryResult executeQuery(String statement, String language, long limit, long offset) {
        try {
            final Session jcrSession = MgnlContext.getJCRSession(workspace);
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
            final Query query = jcrQueryManager.createQuery(statement, language);
            if (limit > 0) {
                query.setLimit(limit);
            }
            if (offset >= 0) {
                query.setOffset(offset);
            }
            log.warn("Executing query against workspace [{}] with statement [{}] and limit {} and offset {}...", new Object[]{
                    getWorkspace(),
                    statement,
                    limit,
                    offset});
            long start = System.currentTimeMillis();
            final QueryResult result = query.execute();
            log.warn("Query execution took {} ms", System.currentTimeMillis() - start);

            return result;

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
