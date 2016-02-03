/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.favorites;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.shared.ui.dd.VerticalDropLocation;

/**
 * Drag&Drop wrapper for the {@link FavoritesEntry}.
 */
public class EntryDragAndDropWrapper extends AbstractFavoritesDragAndDropWrapper {

    public EntryDragAndDropWrapper(FavoritesEntry root, FavoritesView.Listener listener) {
        super(root, listener);
    }

    @Override
    protected void init() {
        setDragStartMode(DragStartMode.WRAPPER);
        setSizeUndefined();

        setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                String sourcePath = ((FavoritesEntry) ((EntryDragAndDropWrapper) event.getTransferable().getSourceComponent()).getWrappedComponent()).getRelPath();
                WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
                WrapperTargetDetails details = (WrapperTargetDetails) event.getTargetDetails();
                String verticalDropLocation = (String) details.getData("verticalLocation");
                boolean isDragDown = (details.getMouseEvent().getClientY() - transferable.getMouseDownEvent().getClientY()) > 0;

                if (isDragDown && (verticalDropLocation.equals(VerticalDropLocation.BOTTOM.name()) || verticalDropLocation.equals(VerticalDropLocation.MIDDLE.name()))) {
                    getListener().orderFavoriteAfter(sourcePath, ((FavoritesEntry) getWrappedComponent()).getNodename());
                } else if (!isDragDown && (verticalDropLocation.equals(VerticalDropLocation.TOP.name()) || verticalDropLocation.equals(VerticalDropLocation.MIDDLE.name()))) {
                    getListener().orderFavoriteBefore(sourcePath, ((FavoritesEntry) getWrappedComponent()).getNodename());
                }
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return new ServerSideCriterion() {

                    @Override
                    public boolean accept(DragAndDropEvent dragEvent) {
                        // accept only entries, not groups
                        AbstractFavoritesDragAndDropWrapper wrapper = (AbstractFavoritesDragAndDropWrapper) dragEvent.getTransferable().getSourceComponent();
                        if (!(wrapper.getWrappedComponent() instanceof FavoritesEntry)) {
                            return false;
                        }

                        // and only in the same group
                        String sourceGroup = ((FavoritesEntry) ((EntryDragAndDropWrapper) dragEvent.getTransferable().getSourceComponent()).getWrappedComponent()).getGroup();
                        if (sourceGroup == null) {
                            return ((FavoritesEntry) getWrappedComponent()).getGroup() == null;
                        }
                        return sourceGroup.equals(((FavoritesEntry) getWrappedComponent()).getGroup());
                    }
                };
            }
        });

    }

}
