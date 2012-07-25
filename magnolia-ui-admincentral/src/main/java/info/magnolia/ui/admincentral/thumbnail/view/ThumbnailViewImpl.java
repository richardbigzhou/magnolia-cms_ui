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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.thumbnail.Thumbnail;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.net.URL;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * ThumbnailViewImpl.
 *
 */
public class ThumbnailViewImpl implements ThumbnailView {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailViewImpl.class);
    private CssLayout layout = new CssLayout();
    private Listener listener;

    private Thumbnail selectedAsset = null;

    public ThumbnailViewImpl(final WorkbenchDefinition definition, final ThumbnailProvider thumbnailProvider) {

        layout.setSizeFull();
        layout.setStyleName("mgnl-workbench-thumbnail-view");
        layout.addListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                Thumbnail clickedAsset = (Thumbnail) event.getClickedComponent();
                if(clickedAsset != null && listener != null) {
                    log.info("Clicked on {}", clickedAsset.getDescription());
                    if(selectedAsset != null) {
                        selectedAsset.removeStyleName("active");
                    }
                    selectedAsset = clickedAsset;
                    clickedAsset.addStyleName("active");
                    listener.onItemSelection(clickedAsset.getNode());
                }
            }
        });

        try {
            Node parent = MgnlContext.getJCRSession(definition.getWorkspace()).getNode(definition.getPath());
            Iterable<Node> assets = NodeUtil.getNodes(parent, NodeUtil.MAGNOLIA_FILTER);
            for(Node asset: assets) {
                final URL url = thumbnailProvider.getThumbnail(asset, 30, 30);
                final Thumbnail image = new Thumbnail(asset, url);
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
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void select(String path) {
        //TODO fgrilli implement or throw UOE
    }

    @Override
    public void refresh() {
        //TODO fgrilli implement or throw UOE
        //throw new UnsupportedOperationException();
    }

    @Override
    public void refreshItem(Item item) {
        //TODO fgrilli implement or throw UOE
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

}
