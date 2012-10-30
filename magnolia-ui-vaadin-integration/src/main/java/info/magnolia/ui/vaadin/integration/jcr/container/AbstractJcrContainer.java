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
package info.magnolia.ui.vaadin.integration.jcr.container;

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.MgnlNodeType;
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

import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
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

    private final WorkbenchDefinition workbenchDefinition;

    /** Starting row number of the currently fetched page. */
    private int currentOffset;

    private static final Long LONG_ZERO = Long.valueOf(0);

    /** Item type to use if no other is properly defined. **/
    protected static final String DEFAULT_MAIN_ITEM_TYPE = MgnlNodeType.NT_CONTENT;

    private static final String QUERY_LANGUAGE = Query.JCR_JQOM;

    protected static final String SELECTOR_NAME = "t";

    protected static final String SELECT_TEMPLATE = "select * from [%s] as " + SELECTOR_NAME;

    protected static final String ORDER_BY = " order by ";

    protected static final String ASCENDING_KEYWORD = " asc";

    protected static final String DESCENDING_KEYWORD = " desc";

    protected static final String METADATA_SELECTOR_NAME = "metaData";

    protected static final String JOIN_METADATA = " inner join [" + MgnlNodeType.NT_METADATA + "] as "+ METADATA_SELECTOR_NAME +" on ischildnode("+ METADATA_SELECTOR_NAME +"," + SELECTOR_NAME + ") ";

    /**
     * Caution: this property gets special treatment as we'll have to call a function to be able to order by it.
     */
    protected static final String NAME_PROPERTY = "jcrName";

    protected static final String JCR_NAME_FUNCTION = "name(" + SELECTOR_NAME + ")";

    protected static final String METADATA_NODE_NAME = MetaData.DEFAULT_META_NODE + "/";

    public AbstractJcrContainer(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
        this.jcrContainerSource = jcrContainerSource;
        this.workbenchDefinition = workbenchDefinition;

        for (ColumnDefinition columnDefinition : workbenchDefinition.getColumns()) {
            if (columnDefinition.isSortable()) {
                log.debug("Configuring column [{}] as sortable", columnDefinition.getName());

                String propertyName = columnDefinition.getPropertyName();
                log.debug("propertyName is {}", propertyName);

                if (StringUtils.isBlank(propertyName)) {
                    propertyName = columnDefinition.getName();
                    log.debug(
                            "Column {} is sortable but no propertyName has been defined. Defaulting to column name (sorting may not work as expected).",
                            columnDefinition.getName());
                }

                sortableProperties.add(propertyName);
            }
        }
    }

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
            final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
            if(!jcrSession.itemExists((String) itemId)) {
                return null;
            }
            javax.jcr.Item item = jcrSession.getItem((String) itemId);
            return item.isNode() ? new JcrNodeAdapter((Node) item) : new JcrPropertyAdapter((javax.jcr.Property) item);
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
        }

        log.error("Couldn't find item {} so property {} can't be retrieved!", itemId, propertyId);
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
        if (itemId == null) {
            return false;
        }

        try {
            final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
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
     * Fetches a page from the data source based on the values of pageLength and currentOffset.
     * Internally it executes the following methods in this order:
     * <ul>
     * <li> {@link #constructJCRQuery(boolean)}
     * <li> {@link #executeQuery(String, String, long, long)}
     * <li> {@link #updateItems(QueryResult)}
     * </ul>
     */
    public final void getPage() {

        final String stmt = constructJCRQuery(true);
        if(StringUtils.isEmpty(stmt)) {
            return;
        }

        try {
            final QueryResult queryResult = executeQuery(stmt, QUERY_LANGUAGE, pageLength * cacheRatio, currentOffset);

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
        final RowIterator iterator = queryResult.getRows();
        long rowCount = currentOffset;
        while (iterator.hasNext()) {
            Node node = iterator.nextRow().getNode(SELECTOR_NAME);
            final String id = node.getPath();
            log.debug("Adding node {} to cached items.", id);
            itemIndexes.put(rowCount++, id);
        }

        log.debug("Done in {} ms", System.currentTimeMillis() - start);
    }

    /**
     * @return whether the jcr query requires a join or not. A join is required as soon as there's one or more order by's
     * for a property hosted on the MetaData node.
     */
    protected boolean queryRequiresJoin() {
        for (OrderBy orderBy : sorters) {
          if (orderBy.getProperty().startsWith(METADATA_NODE_NAME)) {
              return true;
          }
        }
        return false;
    }

    /**
     * @return a string representing a JCR statement to retrieve this container's items.
     * @see AbstractJcrContainer#getPage()
     */
    protected String constructJCRQuery(final boolean considerSorting) {
        final String select = String.format(SELECT_TEMPLATE, getMainItemTypeAsString());
        final StringBuilder stmt = new StringBuilder(select);
        if (considerSorting) {
            if (sorters.isEmpty()) {
                // no sorters set - use defaultOrder (always ascending)
                String defaultOrder = workbenchDefinition.getDefaultOrder();
                String[] defaultOrders = defaultOrder.split(",");
                for (String current : defaultOrders) {
                    sorters.add(new OrderBy(current, true));
                }
            }
            if (queryRequiresJoin()) {
                stmt.append(JOIN_METADATA);
            }
            stmt.append(ORDER_BY);
            String sortOrder;
            for (OrderBy orderBy : sorters) {
                String propertyName = orderBy.getProperty();
                sortOrder = orderBy.isAscending() ? ASCENDING_KEYWORD : DESCENDING_KEYWORD;
                if (NAME_PROPERTY.equals(propertyName)) {
                    stmt.append(JCR_NAME_FUNCTION).append(sortOrder).append(", ");
                    continue;
                }
                if (propertyName.startsWith(METADATA_NODE_NAME)) {
                    propertyName = propertyName.substring(METADATA_NODE_NAME.length());
                    stmt.append(METADATA_SELECTOR_NAME);
                } else {
                    stmt.append(SELECTOR_NAME);
                }
                stmt.append(".[").append(propertyName).append("]").append(sortOrder).append(", ");
            }
            stmt.delete(stmt.lastIndexOf(","), stmt.length());
        }
        return stmt.toString();
    }

    /**
     * @return the mainItemType as String - in case it's not properly configured we'll report via log and use a default
     */
    protected String getMainItemTypeAsString() {
        String mainItemType = DEFAULT_MAIN_ITEM_TYPE;
        if (workbenchDefinition.getMainItemType() != null && StringUtils.isNotBlank(workbenchDefinition.getMainItemType().getItemType())) {
            mainItemType = workbenchDefinition.getMainItemType().getItemType();
        } else {
            log.warn("WorkbenchDefinition {} does not properly define a MainItemType - hence we'll use the default value '{}'.", workbenchDefinition.getName(), DEFAULT_MAIN_ITEM_TYPE);
        }
        return mainItemType;
    }

    /**
     * @see #getPage().
     */
    public final void updateSize() {
        try {
            final String stmt = constructJCRQuery(false);
            if(stmt == null) {
                return;
            }
            // query for all items in order to get the size
            final QueryResult queryResult = executeQuery(stmt, QUERY_LANGUAGE, 0, 0);

            final long pageSize = queryResult.getRows().getSize();
            log.debug("Query resultset contains {} items", pageSize);

            updateCount((int) pageSize);
        } catch (RepositoryException e){
            throw new RuntimeRepositoryException(e);
        }
    }

    public String getWorkspace() {
        return workbenchDefinition.getWorkspace();
    }

    /**
     * Refreshes the container - clears all caches and resets size and offset. Does NOT remove
     * sorting or filtering rules!
     */
    public void refresh() {
        resetOffset();
        itemIndexes.clear();
        updateSize();
    }

    protected void resetOffset() {
        currentOffset = 0;
    }

    protected int getCurrentOffset() {
        return currentOffset;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected QueryResult executeQuery(String statement, String language, long limit, long offset) {
        try {
            final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
            final Query query = jcrQueryManager.createQuery(statement, language);
            if (limit > 0) {
                query.setLimit(limit);
            }
            if (offset >= 0) {
                query.setOffset(offset);
            }
            log.debug("Executing query against workspace [{}] with statement [{}] and limit {} and offset {}...", new Object[]{
                    getWorkspace(),
                    statement,
                    limit,
                    offset});
            long start = System.currentTimeMillis();
            final QueryResult result = query.execute();
            log.debug("Query execution took {} ms", System.currentTimeMillis() - start);

            return result;

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
