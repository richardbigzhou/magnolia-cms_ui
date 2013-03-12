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
package info.magnolia.ui.workbench.thumbnail;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.imageprovider.definition.ImageProvider;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailDblClickListener;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailSelectionListener;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * Vaadin UI component that displays thumbnails.
 */
public class LazyThumbnailViewImpl implements ThumbnailView {

    private static final Logger log = LoggerFactory.getLogger(LazyThumbnailViewImpl.class);

    private ThumbnailContainer container;

    private final WorkbenchDefinition workbenchDefinition;

    private Listener listener;

    private final LazyThumbnailLayout layout;

    private final CssLayout margin = new CssLayout();

    private final ImageProvider imageProvider;

    public LazyThumbnailViewImpl(final WorkbenchDefinition definition, final ThumbnailContainer container) {
        this.workbenchDefinition = definition;
        this.imageProvider = container.getImageProvider();
        this.layout = new LazyThumbnailLayout();
        this.container = container;
        layout.setSizeFull();
        layout.addStyleName("mgnl-workbench-thumbnail-view");

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
        // do something?
    }

    @Override
    public final void refresh() {
        this.container = new ThumbnailContainer(workbenchDefinition, imageProvider);
        container.setWorkspaceName(workbenchDefinition.getWorkspace());
        container.setThumbnailHeight(73);
        container.setThumbnailWidth(73);
        layout.setContainerDataSource(container);
        layout.setThumbnailSize(73, 73);
    }

    @Override
    public AbstractJcrContainer getContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asVaadinComponent() {
        return margin;
    }

    private JcrNodeAdapter getThumbnailNodeAdapterByIdentifier(final String thumbnailId) {
        try {
            Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
            final Node imageNode = session.getNodeByIdentifier(thumbnailId);
            return new JcrNodeAdapter(imageNode);
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.THUMBNAIL;
    }
}
