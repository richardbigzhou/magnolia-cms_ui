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
import info.magnolia.ui.model.thumbnail.ThumbnailProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout.LazyThumbnailProvider;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout.ThumbnaSeletionListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;

/**
 * ThumbnailViewImpl.
 */
public class LazyThumbnailViewImpl implements ThumbnailView {

    private static final Logger log = LoggerFactory.getLogger(LazyThumbnailViewImpl.class);
    
    private WorkbenchDefinition workbenchDefinition; 
    
    private AbstractPredicate<Node> filterByItemType;
    
    private Listener listener;
    
    private LazyThumbnailLayout layout;
    
    private LazyThumbnailProviderImpl lazyThumbnailProvider;
    
    private class LazyThumbnailProviderImpl implements LazyThumbnailProvider {

        private ThumbnailProvider thumbnailProvider;
    
        private List<Node> nodeList = new ArrayList<Node>();
        
        private Iterator<Node> nodeIterator;
        
        public LazyThumbnailProviderImpl(final ThumbnailProvider thumbnailProvider) {
            this.thumbnailProvider = thumbnailProvider;
        }

        @Override
        public int getThumbnailsAmount() {
            return nodeList.size();
        }

        @Override
        public List<Resource> getThumbnails(int amount) {
            final List<Resource> resources = new ArrayList<Resource>();
            int thumbnailWidth = layout.getThumbnailWidth(); 
            int thumbnailHeight = layout.getThumbnailHeight();
            for (int i = 0; i < amount && nodeIterator.hasNext(); ++i) {
                final String path = thumbnailProvider.getPath(nodeIterator.next(), thumbnailWidth, thumbnailHeight);
                //final Thumbnail image = new Thumbnail(asset, path);
                resources.add(new ExternalResource(path));
            }
            return resources;
        }
        
        public void setNodeList(List<Node> nodeList) {
            this.nodeList = nodeList;
            refresh();
        }

        @Override
        public void refresh() {
            nodeIterator = nodeList.iterator();            
        }
    }
    
    public LazyThumbnailViewImpl(final WorkbenchDefinition definition, final ThumbnailProvider thumbnailProvider) {
        this.workbenchDefinition = definition;
        this.lazyThumbnailProvider = new LazyThumbnailProviderImpl(thumbnailProvider);
        this.layout = new LazyThumbnailLayout(lazyThumbnailProvider);
        
        layout.setSizeFull();
        
        final String[] itemTypes = getItemTypes(definition);
        if(itemTypes != null) {
            filterByItemType = new ItemTypePredicate(itemTypes);
        } else {
            log.warn("Workbench definition contains no item types, node filter will accept all mgnl item types.");
            filterByItemType = NodeUtil.MAGNOLIA_FILTER;
        }
        
        layout.refresh();
        layout.addThumbnailSelectionListener(new ThumbnaSeletionListener() {
            @Override
            public void onThumbnailSelected() {
                
            }
        });
        
    }
    
    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void select(String path) {
        
    }

    @Override
    public void refresh() {
        layout.clear();
        try {
            //FIXME fgrilli the arg to get node must take into account that the current path can change if we navigate in hierarchy.
            Node parent = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getNode(workbenchDefinition.getPath());
            Iterable<Node> assets = NodeUtil.collectAllChildren(parent, filterByItemType);
            lazyThumbnailProvider.setNodeList(NodeUtil.asList(assets));
            layout.setThumbnailSize(73, 73);
            layout.refresh();
            

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
        // TODO Auto-generated method stub
        
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
