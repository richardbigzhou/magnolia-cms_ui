/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailDblClickListener;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailRightClickListener;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailSelectionListener;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.vaadin.data.Container;
import com.vaadin.ui.Component;

/**
 * Default Vaadin implementation of the thumbnail view.
 */
public class ThumbnailViewImpl implements ThumbnailView {

    private Listener listener;

    private final LazyThumbnailLayout thumbnailLayout = new LazyThumbnailLayout();

    public ThumbnailViewImpl() {
        thumbnailLayout.setSizeFull();
        thumbnailLayout.addStyleName("mgnl-workbench-thumbnail-view");
        bindHandlers();
    }

    private void bindHandlers() {
        thumbnailLayout.addThumbnailSelectionListener(new ThumbnailSelectionListener() {

            @Override
            public void onThumbnailSelected(final Object itemId) {
                Set<Object> items = Sets.newHashSet(itemId);
                onThumbnailsSelected(items);
            }

            @Override
            public void onThumbnailsSelected(Set<Object> ids) {
                listener.onItemSelection(ids);
            }
        });

        thumbnailLayout.addDoubleClickListener(new ThumbnailDblClickListener() {

            @Override
            public void onThumbnailDblClicked(final Object itemId) {
                listener.onDoubleClick(itemId);
            }
        });

        thumbnailLayout.addRightClickListener(new ThumbnailRightClickListener() {

            @Override
            public void onThumbnailRightClicked(final Object itemId, int clickX, int clickY) {
                Set<Object> items = new LinkedHashSet<Object>();
                items.add(itemId);
                listener.onItemSelection(items);
                listener.onRightClick(itemId, clickX, clickY);
            }
        });
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void select(List<Object> itemIds) {
        // ThumbnailLayout only supports single selection and because the client requests thumbnails we set the
        // selected itemId so it can update selection when that happens
        Object itemId = itemIds == null || itemIds.isEmpty() ? null : itemIds.get(0);
        thumbnailLayout.setSelectedItemId(itemId);
    }

    @Override
    public void refresh() {
        thumbnailLayout.refresh();
    }

    @Override
    public void setContainer(Container container) {
        thumbnailLayout.setContainerDataSource(container);
    }

    @Override
    public void setThumbnailSize(int width, int height) {
        thumbnailLayout.setThumbnailSize(width, height);
    }

    @Override
    public Component asVaadinComponent() {
        return thumbnailLayout;
    }

    @Override
    public void setMultiselect(boolean multiselect) {
        // does nothing
    }

    @Override
    public void onShortcutKey(int keyCode, int[] modifierKeys) {
        if (listener != null) {
            listener.onShortcutKey(keyCode, modifierKeys);
        }
    }

    @Override
    public void expand(Object itemId) {
    }

}
