/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.content.view.builder;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.list.container.FlatJcrContainer;
import info.magnolia.ui.admincentral.list.view.ListViewImpl;
import info.magnolia.ui.admincentral.search.container.SearchJcrContainer;
import info.magnolia.ui.admincentral.search.view.SearchViewImpl;
import info.magnolia.ui.admincentral.thumbnail.view.LazyThumbnailViewImpl;
import info.magnolia.ui.admincentral.thumbnail.view.ThumbnailContainer;
import info.magnolia.ui.admincentral.tree.container.HierarchicalJcrContainer;
import info.magnolia.ui.admincentral.tree.view.TreeViewImpl;
import info.magnolia.ui.model.imageprovider.definition.ImageProvider;
import info.magnolia.ui.model.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.io.Serializable;

import javax.inject.Inject;


/**
 * ContentView Factory.
 */
public class ContentViewBuilderImpl implements ContentViewBuilder, Serializable {

    private final ComponentProvider componentProvider;

    @Inject
    public ContentViewBuilderImpl(final ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public ContentView build(final WorkbenchDefinition workbenchDefinition, final ViewType type) {
        switch (type) {

            case TREE :
                final HierarchicalJcrContainer hierarchicalContainer = new HierarchicalJcrContainer(workbenchDefinition);
                return componentProvider.newInstance(TreeViewImpl.class, workbenchDefinition, hierarchicalContainer);
            case LIST :
                final FlatJcrContainer flatContainer = new FlatJcrContainer(workbenchDefinition);
                return componentProvider.newInstance(ListViewImpl.class, workbenchDefinition, flatContainer);
            case SEARCH :
                final SearchJcrContainer searchContainer = new SearchJcrContainer(workbenchDefinition);
                return componentProvider.newInstance(SearchViewImpl.class, workbenchDefinition, searchContainer);
            case THUMBNAIL :
                ImageProviderDefinition imageProviderDef = workbenchDefinition.getImageProvider();
                ThumbnailContainer thumbnailContainer;
                if (imageProviderDef != null) {
                    final ImageProvider imageProvider = componentProvider.newInstance(imageProviderDef.getImageProviderClass(),
                        imageProviderDef);
                    thumbnailContainer = new ThumbnailContainer(workbenchDefinition, imageProvider);
                } else {
                    thumbnailContainer = new ThumbnailContainer(workbenchDefinition, null);
                }
                return componentProvider.newInstance(LazyThumbnailViewImpl.class, workbenchDefinition, thumbnailContainer);
            default :
                throw new RuntimeException("The provided view type [" + type + "] is not valid.");
        }
    }
}
