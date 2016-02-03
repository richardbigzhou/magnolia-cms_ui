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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.TargetDetailIs;
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;

/**
 * Drag & drop wrapper for the {@link FavoritesGroup}.
 */
public class GroupDragAndDropWrapper extends AbstractFavoritesDragAndDropWrapper {

    private final Logger log = LoggerFactory.getLogger(GroupDragAndDropWrapper.class);

    private final FavoritesGroup group;

    public GroupDragAndDropWrapper(Component root, FavoritesView.Listener listener, FavoritesGroup group) {
        super(root, listener);
        this.group = group;
    }

    @Override
    public Component getWrappedComponent() {
        return this.group;
    }

    @Override
    protected void init() {
        setDragStartMode(DragStartMode.WRAPPER);
        setSizeUndefined();

        setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                AbstractFavoritesDragAndDropWrapper droppedComponent = (AbstractFavoritesDragAndDropWrapper) event.getTransferable().getSourceComponent();
                if (droppedComponent.getWrappedComponent() instanceof FavoritesEntry) {
                    String favoritePath = ((FavoritesEntry) ((EntryDragAndDropWrapper) event.getTransferable().getSourceComponent()).getWrappedComponent()).getRelPath();
                    getListener().moveFavorite(favoritePath, group.getRelPath());
                } else if (droppedComponent.getWrappedComponent() instanceof FavoritesGroup) {

                    String groupToMove = ((FavoritesGroup) droppedComponent.getWrappedComponent()).getRelPath();
                    WrapperTransferable transferable = (WrapperTransferable) event.getTransferable();
                    WrapperTargetDetails details = (WrapperTargetDetails) event.getTargetDetails();
                    String verticalDropLocation = (String) details.getData("verticalLocation");

                    boolean isDragDown = (details.getMouseEvent().getClientY() - transferable.getMouseDownEvent().getClientY()) > 0;

                    if (isDragDown && (verticalDropLocation.equals(VerticalDropLocation.BOTTOM.name()) || verticalDropLocation.equals(VerticalDropLocation.MIDDLE.name()))) {
                        getListener().orderGroupAfter(groupToMove, group.getRelPath());
                    } else if (!isDragDown && (verticalDropLocation.equals(VerticalDropLocation.TOP.name()) || verticalDropLocation.equals(VerticalDropLocation.MIDDLE.name()))) {
                        getListener().orderGroupBefore(groupToMove, group.getRelPath());
                    }

                } else {
                    log.warn("Trying to drop neither entry nor group on a group.");
                }

            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return new And(
                        new TargetDetailIs("verticalLocation", VerticalDropLocation.MIDDLE.name()),
                        new TargetDetailIs("horizontalLocation", HorizontalDropLocation.CENTER.name()));
            }
        });

    }
}
