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
package info.magnolia.ui.workbench.thumbnail;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.thumbnail.ThumbnailContainer.ThumbnailItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.AbstractProperty;

/**
 * Container that provides thumbnails lazily.
 */
public class ThumbnailContainer extends AbstractInMemoryContainer<String, Object, ThumbnailItem> {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailContainer.class);

    public static final String THUMBNAIL_PROPERTY_ID = "thumbnail";

    private final WorkbenchDefinition workbenchDefinition;

    private final ImageProvider imageProvider;

    protected static final String WHERE_TEMPLATE_FOR_PATH = " where ISDESCENDANTNODE('%s') ";

    private String workspaceName = "";

    private int thumbnailWidth = 0;

    private int thumbnailHeight = 0;

    public ThumbnailContainer(WorkbenchDefinition workbenchDefinition, ImageProvider imageProvider) {
        super();
        this.workbenchDefinition = workbenchDefinition;
        this.imageProvider = imageProvider;
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        return Arrays.asList(THUMBNAIL_PROPERTY_ID);
    }

    @Override
    public ThumbnailContainerProperty getContainerProperty(Object itemId, Object propertyId) {
        if (THUMBNAIL_PROPERTY_ID.equals(propertyId)) {
            return new ThumbnailContainerProperty(String.valueOf(itemId), imageProvider);
        }
        return null;
    }

    @Override
    public Class<?> getType(Object propertyId) {
        if (THUMBNAIL_PROPERTY_ID.equals(propertyId)) {
            return Object.class;
        }
        return null;
    }

    /**
     * Hint: could be dropped once this type bases on AbstractJcrContainer as well (BL-153).
     */
    protected String getMainNodeType() {
        final List<NodeTypeDefinition> nodeTypes = workbenchDefinition.getNodeTypes();
        return nodeTypes.isEmpty() ? AbstractJcrContainer.DEFAULT_NODE_TYPE : nodeTypes.get(0).getName();
    }

    protected String prepareSelectQueryStatement() {
        return String.format("select * from ['%s'] as t ", getMainNodeType());
    }

    protected String prepareFilterQueryStatement() {
        if (StringUtils.isNotBlank(workbenchDefinition.getPath()) && !"/".equals(workbenchDefinition.getPath())) {
            return String.format(WHERE_TEMPLATE_FOR_PATH, workbenchDefinition.getPath());
        }
        return "";
    }

    protected String prepareOrderQueryStatement() {
        return " order by name(t)";
    }

    /**
     * @return a List of JCR identifiers for all the nodes recursively found
     *         under <code>initialPath</code>. This method is called in {@link info.magnolia.ui.workbench.thumbnail.ThumbnailViewImpl#refresh()}. You can override it, if
     *         you need a different strategy than the default one to fetch the
     *         identifiers of the nodes for which thumbnails need to be
     *         displayed.
     * @see info.magnolia.ui.vaadin.layout.LazyThumbnailLayout#refresh()
     */
    protected List<String> getAllIdentifiers(final String workspaceName) {
        List<String> uuids = new ArrayList<String>();
        final String query = prepareSelectQueryStatement();
        try {
            QueryManager qm = MgnlContext.getJCRSession(workspaceName).getWorkspace().getQueryManager();
            Query q = qm.createQuery(constructQuery(), Query.JCR_SQL2);

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

    private String constructQuery() {
        return prepareSelectQueryStatement() + prepareFilterQueryStatement() + prepareOrderQueryStatement();
    }

    public void refresh() {
        getAllItemIds().clear();
        getAllItemIds().addAll(getAllIdentifiers(workbenchDefinition.getWorkspace()));
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ThumbnailItem getUnfilteredItem(Object itemId) {
        return new ThumbnailItem(String.valueOf(itemId));
    }

    public ImageProvider getImageProvider() {
        return imageProvider;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    /**
     * ThumbnailContainer property. Can have a Resource or a String as value.
     */
    public class ThumbnailContainerProperty extends AbstractProperty<Object> {

        private String resourcePath;

        private final ImageProvider imageProvider;

        public ThumbnailContainerProperty(final String resourcePath, ImageProvider imageProvider) {
            this.resourcePath = resourcePath;
            this.imageProvider = imageProvider;
        }

        @Override
        public Object getValue() {
            if (imageProvider == null) {
                return null;
            }
            return imageProvider.getThumbnailResourceById(getWorkspaceName(), resourcePath, ImageProvider.THUMBNAIL_GENERATOR);
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException {
            this.resourcePath = String.valueOf(newValue);
        }

        @Override
        public Class<Object> getType() {
            return Object.class;
        }
    }

    /**
     * Thumbnail Item.
     */
    public class ThumbnailItem implements Item {

        private final String id;

        public ThumbnailItem(final String id) {
            this.id = id;
        }

        @Override
        public Property<?> getItemProperty(Object id) {
            if (THUMBNAIL_PROPERTY_ID.equals(id)) {
                return new ThumbnailContainerProperty(this.id, imageProvider);
            }
            return null;
        }

        public String getItemId() {
            return id;
        }

        @Override
        public Collection<?> getItemPropertyIds() {
            return Arrays.asList(THUMBNAIL_PROPERTY_ID);
        }

        @Override
        public boolean addItemProperty(Object id, @SuppressWarnings("rawtypes") Property property) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }
}
