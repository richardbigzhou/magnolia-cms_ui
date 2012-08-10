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
import info.magnolia.ui.admincentral.container.AbstractJcrContainer;
import info.magnolia.ui.model.thumbnail.ThumbnailProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout.ThumbnailSelectionListener;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.ui.Component;

/**
 * LazyThumbnailViewImpl.
 */
public class LazyThumbnailViewImpl implements ThumbnailView {

    private static final Logger log = LoggerFactory.getLogger(LazyThumbnailViewImpl.class);

    private WorkbenchDefinition workbenchDefinition;

    private AbstractPredicate<Node> filterByItemType;

    private Listener listener;

    private LazyThumbnailLayout layout;

    private ThumbnailProvider thumbnailProvider;


    public LazyThumbnailViewImpl(final WorkbenchDefinition definition, final ThumbnailProvider thumbnailProvider) {
        this.workbenchDefinition = definition;
        this.layout = new LazyThumbnailLayout();
        this.thumbnailProvider = thumbnailProvider;
        layout.setSizeFull();
        layout.addStyleName("mgnl-workbench-thumbnail-view");

        final String[] itemTypes = getItemTypes(definition);
        if(itemTypes != null) {
            filterByItemType = new ItemTypePredicate(itemTypes);
        } else {
            log.warn("Workbench definition contains no item types, node filter will accept all mgnl item types.");
            filterByItemType = NodeUtil.MAGNOLIA_FILTER;
        }
        layout.addThumbnailSelectionListener(new ThumbnailSelectionListener() {
            @Override
            public void onThumbnailSelected(final String thumbnailId) {
                Session session;
                try {
                    session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
                    final Node imageNode = session.getNodeByIdentifier(thumbnailId);
                    listener.onItemSelection(new JcrNodeAdapter(imageNode));
                } catch (LoginException e) {
                    log.error(e.getMessage());
                } catch (RepositoryException e) {
                    log.error(e.getMessage());
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

    }

    @Override
    public final void refresh() {
        final List<String> uuids = getAllIdentifiers(workbenchDefinition.getWorkspace(), workbenchDefinition.getPath());
        final ThumbnailContainer container = new ThumbnailContainer(thumbnailProvider, uuids);
        container.setWorkspaceName(workbenchDefinition.getWorkspace());
        container.setThumbnailHeight(73);
        container.setThumbnailWidth(73);
        layout.setContainerDataSource(container);
        layout.setThumbnailSize(73, 73);
    }

    @Override
    public void refreshItem(Item item) {
        // TODO Auto-generated method stub

    }

    @Override
    public AbstractJcrContainer getContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    /**
     * @return a List of JCR identifiers for the all the nodes recursively found under <code>initialPath</code>. This method is called in {@link LazyThumbnailViewImpl#refresh()}.
     * You can override it, if you need a different strategy than the default one to fetch the identifiers of the nodes for which thumbnails need to be displayed.
     * @see ThumbnailContainer
     * @see LazyThumbnailLayout#refresh()
     */
    protected List<String> getAllIdentifiers(final String workspaceName, final String initialPath) {
        List<String> uuids = new ArrayList<String>();
        try {
            //TODO fgrilli: needs proof but it's probably more performing if we just do a jcr sql 2 query, something like
            // select [jcr:uuid] from [mgnl:content]
            //instead of iterating over everything and then discard  what we don't need
            log.debug("Recursively collecting all children at [{}:{}]...", workspaceName, initialPath);
            long start = System.currentTimeMillis();
            Node parent = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getNode(workbenchDefinition.getPath());
            Iterable<Node> assets = NodeUtil.collectAllChildren(parent, filterByItemType);
            uuids = Lists.transform(NodeUtil.asList(assets), new Function<Node, String>() {
                @Override
                public String apply(Node node) {
                    try {
                        final String uuid = node.getIdentifier();
                        //final String imageNodeName = ThumbnailProvider.getOriginalImageNodeName();
                        //final String thumbnailNodeName = ThumbnailProvider.getThumbnaillNodeName();
                        //ThumbnailUtility.isThumbnailToBeGenerated(uuid, workspace, imageNodeName, thumbnailNodeName);
                        return node.getIdentifier();
                    } catch (RepositoryException e) {
                        return null;
                    }
                }
            });
            log.debug("Done collecting {} children in {}ms", uuids.size(), System.currentTimeMillis() - start);

        } catch (PathNotFoundException e) {
            throw new RuntimeException(e);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return uuids;
    }

    /**
     * Accepts only nodes of the type(s) passed as argument to the constructor.
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
