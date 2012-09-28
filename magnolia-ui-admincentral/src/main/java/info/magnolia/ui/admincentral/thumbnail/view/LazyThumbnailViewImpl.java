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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.container.AbstractJcrContainer;
import info.magnolia.ui.model.thumbnail.ImageProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout.ThumbnailDblClickListener;
import info.magnolia.ui.vaadin.integration.widget.LazyThumbnailLayout.ThumbnailSelectionListener;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * LazyThumbnailViewImpl.
 */
public class LazyThumbnailViewImpl implements ThumbnailView {

    private static final Logger log = LoggerFactory.getLogger(LazyThumbnailViewImpl.class);

    private WorkbenchDefinition workbenchDefinition;

    private Listener listener;

    private LazyThumbnailLayout layout;

    private ImageProvider imageProvider;

    private String jcrSQL2QueryStatement;

    private VerticalLayout margin = new VerticalLayout();

    public LazyThumbnailViewImpl(final WorkbenchDefinition definition, final ImageProvider imageProvider) {
        this.workbenchDefinition = definition;
        this.layout = new LazyThumbnailLayout();
        this.imageProvider = imageProvider;
        layout.setSizeFull();
        layout.addStyleName("mgnl-workbench-thumbnail-view");

        jcrSQL2QueryStatement = prepareJcrSQL2Query();

        layout.addThumbnailSelectionListener(new ThumbnailSelectionListener() {
            @Override
            public void onThumbnailSelected(final String thumbnailId) {
                JcrNodeAdapter node = getThumbnailNodeAdapterByIdentifier(thumbnailId);
                listener.onItemSelection(node);
            }
        });

        layout.addDoubleClickListener(new ThumbnailDblClickListener() {

            @Override
            public void onThumbnailDblClicked(final String thumbnailId) {
                JcrNodeAdapter node = getThumbnailNodeAdapterByIdentifier(thumbnailId);
                listener.onDoubleClick(node);
            }
        });
        margin.setSizeFull();
        margin.setStyleName("mgnl-content-view");
        margin.addComponent(layout);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void select(String path) {
      //do something?
    }

    @Override
    public final void refresh() {
        final List<String> uuids = getAllIdentifiers(workbenchDefinition.getWorkspace(), workbenchDefinition.getPath());
        final ThumbnailContainer container = new ThumbnailContainer(imageProvider, uuids);
        container.setWorkspaceName(workbenchDefinition.getWorkspace());
        container.setThumbnailHeight(73);
        container.setThumbnailWidth(73);
        layout.setContainerDataSource(container);
        layout.setThumbnailSize(73, 73);
    }

    @Override
    public void refreshItem(Item item) {
        //do nothing
    }

    @Override
    public AbstractJcrContainer getContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asVaadinComponent() {
        return margin;
    }

    /**
     * @return a List of JCR identifiers for all the nodes recursively found under <code>initialPath</code>. This method is called in {@link LazyThumbnailViewImpl#refresh()}.
     * You can override it, if you need a different strategy than the default one to fetch the identifiers of the nodes for which thumbnails need to be displayed.
     * @see ThumbnailContainer
     * @see LazyThumbnailLayout#refresh()
     */
    protected List<String> getAllIdentifiers(final String workspaceName, final String initialPath) {
        List<String> uuids = new ArrayList<String>();
        try {
            QueryManager qm = MgnlContext.getJCRSession(workspaceName).getWorkspace().getQueryManager();
            Query q = qm.createQuery(jcrSQL2QueryStatement, Query.JCR_SQL2);

            log.debug("Executing query statement [{}] on workspace [{}]", jcrSQL2QueryStatement, workspaceName);
            long start = System.currentTimeMillis();

            QueryResult queryResult = q.execute();
            NodeIterator iter = queryResult.getNodes();

            while(iter.hasNext()) {
               uuids.add(iter.nextNode().getIdentifier());
            }

            log.debug("Done collecting {} nodes in {}ms", uuids.size(), System.currentTimeMillis() - start);

        } catch (PathNotFoundException e) {
            throw new RuntimeException(e);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return uuids;
    }

    private String prepareJcrSQL2Query(){
        final String[] itemTypes = getItemTypes(workbenchDefinition);
        String stmt = null;
        if(itemTypes != null && itemTypes.length == 1) {
            stmt = "select * from ["+itemTypes[0]+"] as t order by name(t)";
        } else {
            log.warn("Workbench definition contains {} item types. Defaulting to {}", (itemTypes != null ? itemTypes.length : 0), MgnlNodeType.NT_CONTENT);
            stmt = "select * from ["+MgnlNodeType.NT_CONTENT+"] as t order by name(t)";
        }
        return stmt;
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

    private JcrNodeAdapter getThumbnailNodeAdapterByIdentifier(final String thumbnailId) {
        try {
            Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
            final Node imageNode = session.getNodeByIdentifier(thumbnailId);
            return new JcrNodeAdapter(imageNode);
        } catch (LoginException e) {
            log.error(e.getMessage());
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
