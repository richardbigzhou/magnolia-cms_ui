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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for favorites.
 */
public final class FavoritesViewImpl extends CustomComponent implements FavoritesView {

    private VerticalLayout layout = new VerticalLayout();
    private FavoritesView.Listener listener;
    private FavoritesGroup noGroup;
    private FavoritesForm favoriteForm;
    private Shell shell;
    private SplitFeed splitPanel = new SplitFeed();
    private Label emptyPlaceHolder = new Label();
    private final SimpleTranslator i18n;
    private List<EditableFavoriteItem> editableFavoriteItemList;

    @Override
    public String getId() {
        return "favorite";
    }

    @Inject
    public FavoritesViewImpl(Shell shell, SimpleTranslator i18n) {
        super();
        this.shell = shell;
        this.i18n = i18n;
        construct();
    }

    @Override
    public void setListener(FavoritesView.Listener listener) {
        this.listener = listener;
    }

    private void construct() {
        editableFavoriteItemList = new ArrayList<EditableFavoriteItem>();
        layout.addStyleName("favorites");
        layout.setHeight("100%");
        layout.setWidth("900px");

        emptyPlaceHolder.addStyleName("emptyplaceholder");
        emptyPlaceHolder.setContentMode(ContentMode.HTML);
        emptyPlaceHolder.setValue(String.format("<span class=\"icon-favorites\"></span><div class=\"message\">%s</div>", i18n.translate("favorites.empty")));

        splitPanel.setVisible(false);

        layout.addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                Component clickedComponent = event.getClickedComponent();
                reset(clickedComponent);
            }
        });

        layout.addComponent(emptyPlaceHolder);
        layout.addComponent(splitPanel);
        layout.setExpandRatio(splitPanel, 0);
        // Disable the hints
        layout.addStyleName("no-vertical-drag-hints");
        layout.addStyleName("no-horizontal-drag-hints");
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setFavoriteLocation(JcrNewNodeAdapter newFavorite, JcrNewNodeAdapter newGroup, Map<String, String> availableGroupsNames) {
        favoriteForm.setNewFavorite(newFavorite);
        favoriteForm.setNewGroup(newGroup);
        favoriteForm.setAvailableGroups(availableGroupsNames);
    }

    @Override
    public void init(AbstractJcrNodeAdapter favorites, JcrNewNodeAdapter favoriteSuggestion, JcrNewNodeAdapter groupSuggestion, Map<String, String> availableGroups, boolean itemIconsVisible) {

        editableFavoriteItemList = new ArrayList<EditableFavoriteItem>();
        final Map<String, AbstractJcrNodeAdapter> nodeAdapters = favorites.getChildren();

        if (nodeAdapters.isEmpty()) {
            emptyPlaceHolder.setVisible(true);
            splitPanel.setVisible(false);
            layout.setExpandRatio(splitPanel, 0);
            layout.setExpandRatio(emptyPlaceHolder, 1);
        } else {
            emptyPlaceHolder.setVisible(false);
            splitPanel.setVisible(true);
            layout.setExpandRatio(splitPanel, 1);
            layout.setExpandRatio(emptyPlaceHolder, 0);

            noGroup = new FavoritesGroup(i18n);
            splitPanel.getLeftContainer().removeAllComponents();
            splitPanel.getRightContainer().removeAllComponents();
            for (String key : nodeAdapters.keySet()) {
                final AbstractJcrNodeAdapter favoriteAdapter = nodeAdapters.get(key);
                if (AdmincentralNodeTypes.Favorite.NAME.equals(favoriteAdapter.getPrimaryNodeTypeName())) {
                    final FavoritesEntry favEntry = new FavoritesEntry(favoriteAdapter, listener, shell, i18n);
                    final EntryDragAndDropWrapper wrapper = new EntryDragAndDropWrapper(favEntry, listener);
                    favEntry.addEditingListener(new EditingEvent.EditingListener() {
                        @Override
                        public void onEdit(EditingEvent event) {
                            if (event.isEditing()) {
                                wrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.NONE);
                            } else {
                                wrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
                            }
                        }
                    });
                    editableFavoriteItemList.add(favEntry);
                    noGroup.addComponent(wrapper);
                } else {
                    final FavoritesGroup group = new FavoritesGroup(favoriteAdapter, listener, shell, this, i18n, editableFavoriteItemList);
                    editableFavoriteItemList.add(group);
                    group.addEditingListener(new EditingEvent.EditingListener() {
                        @Override
                        public void onEdit(EditingEvent event) {
                            if (event.isEditing()) {
                                group.getDragAndDropWrapper().setDragStartMode(DragAndDropWrapper.DragStartMode.NONE);
                            } else {
                                group.getDragAndDropWrapper().setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
                            }
                        }
                    });
                    splitPanel.getRightContainer().addComponent(group);
                }
            }
            DragAndDropWrapper nogroupWrap = new DragAndDropWrapper(noGroup);
            noGroup.setSizeFull();
            nogroupWrap.setSizeFull();

            nogroupWrap.setDropHandler(new DropHandler() {

                @Override
                public void drop(DragAndDropEvent event) {
                    Component wrappedComponent = ((EntryDragAndDropWrapper) event.getTransferable().getSourceComponent()).getWrappedComponent();
                    String sourcePath = ((FavoritesEntry) wrappedComponent).getRelPath();
                    listener.moveFavorite(sourcePath, null);
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
                            // drop location: can drop anywhere in the target zone.
                            return true;
                        }
                    };
                }

            });
            splitPanel.getLeftContainer().addComponent(nogroupWrap);
        }

        if (favoriteForm == null) {
            favoriteForm = new FavoritesForm(listener, shell, i18n);
            layout.addComponent(favoriteForm);
        }
        setFavoriteLocation(favoriteSuggestion, groupSuggestion, availableGroups);
        favoriteForm.setEditActionEnabled(listener.hasItems());

        for (EditableFavoriteItem item : getEditableFavoriteItemList()) {
            item.setIconsVisibility(itemIconsVisible);
        }

    }

    @Override
    public List<EditableFavoriteItem> getEditableFavoriteItemList() {
        return editableFavoriteItemList;
    }

    /**
     * Clicking outside a group or favorite resets everything.
     */
    private void reset(Component clickedComponent) {
        if (!(clickedComponent instanceof SplitFeed.FeedSection)) {
            return;
        }
        favoriteForm.close();
        for (EditableFavoriteItem item : getEditableFavoriteItemList()) {
            item.setToNonEditableState();
        }
    }

}
