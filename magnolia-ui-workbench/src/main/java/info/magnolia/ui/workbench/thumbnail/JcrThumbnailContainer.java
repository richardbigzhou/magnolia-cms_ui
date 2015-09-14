/**
 * This file Copyright (c) 2015 Magnolia International
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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.layout.data.PagingThumbnailContainer;
import info.magnolia.ui.workbench.container.Refreshable;
import info.magnolia.ui.workbench.list.FlatJcrContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

/**
 * JCR implementation of {@link ThumbnailContainer} delegating to a {@link FlatJcrContainer}.
 */
public class JcrThumbnailContainer extends FlatJcrContainer implements Refreshable, PagingThumbnailContainer {

    public static final String THUMBNAIL_PROPERTY_ID = "thumbnail";

    private static final Logger log = LoggerFactory.getLogger(JcrThumbnailContainer.class);

    private final ImageProvider imageProvider;

    public JcrThumbnailContainer(ImageProvider imageProvider, final JcrContentConnectorDefinition definition) {
        super(definition);
        this.imageProvider = imageProvider;
    }

    // FILTERING OF JCR NODE-TYPES TO BE DISPLAYED AS THUMBNAILS

    @Override
    protected String getQueryWhereClauseNodeTypes() {
        List<String> defs = new ArrayList<String>();
        for (NodeTypeDefinition type : getConfiguration().getNodeTypes()) {
            if (type.isHideInList() || NodeTypes.Folder.NAME.equals(type.getName())) {
                log.debug("Skipping {} node type. Nodes of such type won't be searched for.", type.getName());
                continue;
            }
            defs.add("[jcr:primaryType] = '" + type.getName() + "'");
        }
        return StringUtils.join(defs, " or ");
    }

    @Override
    public int indexOfId(Object itemId) {
        if (!containsId(itemId)) {
            return -1;
        }
        return super.indexOfId(itemId);
    }

    @Override
    public boolean containsId(Object itemId) {
        final Item item = getJcrItem(itemId);
        if (!item.isNode()) {
            return super.containsId(itemId);
        }

        Node node = (Node) item;
        try {
            if (!getConfiguration().isIncludeSystemNodes() && node.getName().startsWith("jcr:") || node.getName().startsWith("rep:")) {
                return false;
            }

            String primaryNodeTypeName = node.getPrimaryNodeType().getName();
            for (NodeTypeDefinition nodeTypeDefinition : getConfiguration().getNodeTypes()) {
                if (nodeTypeDefinition.isStrict()) {
                    if (primaryNodeTypeName.equals(nodeTypeDefinition.getName())) {
                        return true;
                    }
                } else if (NodeUtil.isNodeType(node, nodeTypeDefinition.getName())) {
                    return true;
                }
            }

        } catch (RepositoryException e) {
            log.error("Failed to check presence of {} in container", itemId, e);
        }
        return false;
    }

    // SUPPORT OF THE THUMBNAIL CONTAINER PROPERTY

    @Override
    public Collection<?> getContainerPropertyIds() {
        return ImmutableSet.builder().addAll(super.getContainerPropertyIds()).add(THUMBNAIL_PROPERTY_ID).build();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        if (THUMBNAIL_PROPERTY_ID.equals(propertyId)) {
            return new ThumbnailContainerProperty(itemId, imageProvider);
        }
        return super.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        if (THUMBNAIL_PROPERTY_ID.equals(propertyId)) {
            return Object.class;
        }
        return super.getType(propertyId);
    }

    @Override
    public Object getThumbnailPropertyId() {
        return THUMBNAIL_PROPERTY_ID;
    }

    @Override
    public Property getThumbnailProperty(Object itemId) {
        return getContainerProperty(itemId, THUMBNAIL_PROPERTY_ID);
    }

    @Override
    public void setPageSize(int pageSize) {
        // TODO WHY 3?
        int pageLength = Double.valueOf(Math.ceil(pageSize / 3.0)).intValue();
        setPageLength(pageLength);
        setCacheRatio(3);
    }

    /**
     * ThumbnailContainer property. Can have a Resource or a String as value.
     */
    public class ThumbnailContainerProperty extends AbstractProperty<Object> {

        private Object resourceId;

        private final ImageProvider imageProvider;

        public ThumbnailContainerProperty(final Object resourceId, ImageProvider imageProvider) {
            this.resourceId = resourceId;
            this.imageProvider = imageProvider;
        }

        @Override
        public Object getValue() {
            if (imageProvider == null) {
                return null;
            }
            return imageProvider.getThumbnailResource(resourceId, ImageProvider.THUMBNAIL_GENERATOR);
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException {
            this.resourceId = newValue;
        }

        @Override
        public Class<Object> getType() {
            return Object.class;
        }
    }

}
