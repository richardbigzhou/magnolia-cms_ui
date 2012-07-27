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
package info.magnolia.ui.admincentral.thumbnail.view;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.thumbnail.Thumbnail;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**s
 * ThumbnailViewImpl.
 * TODO fgrilli:
 * - implement lazy loading of items, ie load (and cache) only those visible in app view. Keep on loading when scrolling down.
 *   Serve from local cache when scrolling up.
 * - extract methods from ctor and clean up.
 *
 */
public class ThumbnailViewImpl implements ThumbnailView {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailViewImpl.class);
    private CssLayout layout = new CssLayout();
    private Listener listener;

    private Thumbnail selectedAsset;
    private WorkbenchDefinition workbenchDefinition;
    private ThumbnailProvider thumbnailProvider;
    private AbstractPredicate<Node> filterByItemType;

    public ThumbnailViewImpl(final WorkbenchDefinition definition, final ThumbnailProvider thumbnailProvider) {
        this.workbenchDefinition = definition;
        this.thumbnailProvider = thumbnailProvider;
        final String[] itemTypes = getItemTypes(definition);

        if(itemTypes != null) {
            filterByItemType = new ItemTypePredicate(itemTypes);
        } else {
            log.warn("Workbench definition contains no item types, node filter will accept all mgnl item types.");
            filterByItemType = NodeUtil.MAGNOLIA_FILTER;
        }

        layout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        layout.setStyleName("mgnl-workbench-thumbnail-view");
        layout.addListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                final Thumbnail clickedAsset = (Thumbnail) event.getClickedComponent();
                if(clickedAsset != null && listener != null) {
                    log.debug("Clicked on {}", clickedAsset.getDescription());
                    if(selectedAsset != null) {
                        selectedAsset.removeStyleName("active");
                    }
                    selectedAsset = clickedAsset;
                    clickedAsset.addStyleName("active");
                    listener.onItemSelection(clickedAsset.getNode());
                }
            }
        });

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void select(String path) {
        //TODO fgrilli implement or throw UOE
    }

    @Override
    public void refresh() {
        layout.removeAllComponents();
        try {
            //FIXME fgrilli the arg to get node must take into account that the current path can change if we navigate in hierarchy.
            Node parent = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getNode(workbenchDefinition.getPath());

            Iterable<Node> assets = NodeUtil.collectAllChildren(parent, filterByItemType);
            for(Node asset: assets) {
                //FIXME this op can become very long with "lots" of items (tested with 2500 fake thumbnails)
                final String path = thumbnailProvider.getPath(asset, 73, 73);
                final Thumbnail image = new Thumbnail(asset, path);

                layout.addComponent(image);
            }

        } catch (PathNotFoundException e) {
            throw new RuntimeException(e);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refreshItem(Item item) {
        //TODO fgrilli implement or throw UOE or do nothing
        //throw new UnsupportedOperationException();
    }

    @Override
    public JcrContainer getContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    /**
     * Accepts only nodes of the type(s) passed as argument to the costructor.
     */
    private static class ItemTypePredicate extends AbstractPredicate<Node> {
        private String[] itemTypes;

        public ItemTypePredicate(final String...itemTypes) {
            if(itemTypes == null || itemTypes.length == 0) {
                throw new IllegalArgumentException("itemTypes cannot be null or empty.");
            }
            this.itemTypes = itemTypes;
        }
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                final String nodeTypeName = node.getPrimaryNodeType().getName();
                for(int i=0; i < itemTypes.length; i++) {
                    if(nodeTypeName.equals(itemTypes[i])) {
                        log.debug("found match for node [{}] with node type [{}]", node.getName(), itemTypes[i]);
                        return true;
                    }
                }
            } catch (RepositoryException e) {
                log.error("Unable to read nodetype for node {}", node);
            }
            return false;
        }
    }

    private String[] getItemTypes(final WorkbenchDefinition definition) {
        if(definition.getItemTypes() == null) {
            return null;
        }
        final String[] itemTypes = new String[definition.getItemTypes().size()];
        for(int i = 0; i < definition.getItemTypes().size(); i++) {
            itemTypes[i] = definition.getItemTypes().get(i).getItemType();
            log.debug("Adding node filter item type [{}]", itemTypes[i]);
        }
        return itemTypes;
    }

}
