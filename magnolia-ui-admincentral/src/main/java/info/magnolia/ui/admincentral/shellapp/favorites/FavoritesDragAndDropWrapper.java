/**
 * This file Copyright (c) 2013 Magnolia International
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
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;

/**
 * TODO Type description here.
 */
public class FavoritesDragAndDropWrapper extends DragAndDropWrapper {

    private final FavoritesEntry favorite;
    private final FavoritesView.Listener listener;

    public FavoritesDragAndDropWrapper(FavoritesEntry root, FavoritesView.Listener listener) {
        super(root);
        this.favorite = root;
        this.listener = listener;
        init();
    }

    private void init() {
        setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                String sourcePath = ((FavoritesEntry) ((FavoritesDragAndDropWrapper) event.getTransferable().getSourceComponent()).getWrappedComponent()).getRelPath();
                String verticalDropLocation = (String) event.getTargetDetails().getData("verticalLocation");
                if (verticalDropLocation.equals(VerticalDropLocation.BOTTOM.name())) {
                    listener.orderFavoriteAfter(sourcePath, favorite.getNodename());
                } else {
                    listener.orderFavoriteBefore(sourcePath, favorite.getNodename());
                }
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return new ServerSideCriterion() {

                    @Override
                    public boolean accept(DragAndDropEvent dragEvent) {
                        // drop location
                        String verticalDropLocation = (String) dragEvent.getTargetDetails().getData("verticalLocation");
                        String horizontalDropLocation = (String) dragEvent.getTargetDetails().getData("horizontalLocation");

                        // horizontally, it must be in center
                        if (!horizontalDropLocation.equals(HorizontalDropLocation.CENTER.name())) {
                            return false;
                        }

                        // and only in the same group
                        String sourceGroup = ((FavoritesEntry) ((FavoritesDragAndDropWrapper) dragEvent.getTransferable().getSourceComponent()).getWrappedComponent()).getGroup();
                        if (sourceGroup == null) {
                            return favorite.getGroup() == null;
                        }
                        return sourceGroup.equals(favorite.getGroup());
                    }
                };
            }
        });

    }

    public Component getWrappedComponent() {
        return this.favorite;
    }

}
