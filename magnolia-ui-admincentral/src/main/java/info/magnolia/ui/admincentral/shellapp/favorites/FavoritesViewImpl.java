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
package info.magnolia.ui.admincentral.shellapp.favorites;

import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed.FeedSection;

import java.util.Iterator;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for favorites.
 */
public class FavoritesViewImpl extends CustomComponent implements FavoritesView {

    private VerticalLayout layout = new VerticalLayout();
    private FavoritesView.Listener listener;
    private FavoritesSection newPages;
    private FavoriteForm favoriteForm;

    @Override
    public String getId() {
        return "favorite";
    }

    public FavoritesViewImpl() {
        super();
        construct();
    }

    @Override
    public void setListener(FavoritesView.Listener listener) {
        this.listener = listener;
    }

    private void construct() {
        layout.addStyleName("favorites");
        layout.setHeight("100%");
        layout.setWidth("900px");

        final SplitFeed splitPanel = new SplitFeed();
        final FeedSection leftSide = splitPanel.getLeftContainer();
        final FeedSection rightSide = splitPanel.getRightContainer();

        newPages = new FavoritesSection();
        newPages.setCaption("New Pages");

        FavoritesSection newCampaigns = new FavoritesSection();
        newCampaigns.setCaption("New Campaigns");

        FavoritesSection assetShortcuts = new FavoritesSection();
        assetShortcuts.setCaption("Asset Shortcuts");

        leftSide.addComponent(newPages);
        leftSide.addComponent(newCampaigns);
        rightSide.addComponent(assetShortcuts);

        layout.addComponent(splitPanel);
        layout.setExpandRatio(splitPanel, 1f);

    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setFavoriteLocation(JcrNewNodeAdapter newFavorite) {
        layout.removeComponent(favoriteForm);
        favoriteForm = new FavoriteForm(newFavorite);
        layout.addComponent(favoriteForm);
    }

    /**
     * Favorite entry.
     */
    public class FavoriteEntry extends CssLayout {

        public FavoriteEntry(final Item favorite) {
            addStyleName("v-favorites-entry");

            String location = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.URL).getValue().toString();
            String title = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue().toString();

            String icon = "icon-app";
            if (favorite.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue() != null) {
                icon = favorite.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue().toString();
            }

            Favorite fav = new Favorite(title, icon, location);
            fav.setWidth(null);
            addComponent(fav);

            NativeButton remove = new NativeButton("Remove", new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    listener.removeFavorite((String) event.getButton().getData());
                }
            });
            remove.setWidth(null);
            remove.setData(favorite.getItemProperty(ModelConstants.JCR_NAME).getValue());

            addComponent(remove);

        }

        /**
         * Favorite.
         */
        public class Favorite extends CustomComponent {

            private HorizontalLayout root = new HorizontalLayout();
            private String location;
            private String icon;
            private String title;

            public Favorite(String title, String icon, String location) {
                super();
                this.icon = icon;
                this.title = title;
                this.location = location;
                this.root.setSpacing(true);

                Label iconLabel = new Label();
                iconLabel.setValue("<span class=\"" + icon + "\"></span>");
                iconLabel.setStyleName("icon");
                iconLabel.setContentMode(ContentMode.HTML);
                root.addComponent(iconLabel);

                Label titleLabel = new Label();
                titleLabel.setValue(title);
                titleLabel.setStyleName("text");
                root.addComponent(titleLabel);

                root.addLayoutClickListener(new LayoutClickListener() {

                    @Override
                    public void layoutClick(LayoutClickEvent event) {
                        listener.goToLocation(getLocationValue());
                    }
                });

                setCompositionRoot(root);
            }

            public String getLocationValue() {
                return location;
            }

            public String getIconValue() {
                return icon;
            }

            public String getTitleValue() {
                return title;
            }
        }
    }

    /**
     * Favorite section.
     */
    public static class FavoritesSection extends CssLayout {

        public FavoritesSection() {
            addStyleName("favorites-section");
        }
    }

    @Override
    public void init(JcrItemNodeAdapter favorites, JcrNewNodeAdapter favoriteSuggestion) {
        Iterator<JcrItemNodeAdapter> favoritesIterator = favorites.getChildren().values().iterator();
        newPages.removeAllComponents();

        while(favoritesIterator.hasNext()) {
            JcrItemNodeAdapter favorite = favoritesIterator.next();
            newPages.addComponent(new FavoriteEntry(favorite));
        }
        if (favoriteForm != null) {
            layout.removeComponent(favoriteForm);
        }
        favoriteForm = new FavoriteForm(favoriteSuggestion);
        layout.addComponent(favoriteForm);

    }

    // A form component that allows editing an item
    private class FavoriteForm extends CustomComponent {

        private TextField url = new TextField("Location");

        private TextField title = new TextField("Title");

        public FavoriteForm(final JcrNewNodeAdapter newFavorite) {
            addStyleName("favorites-form");
            FormLayout layout = new FormLayout();
            title.setWidth(800, Unit.PIXELS);
            title.setRequired(true);
            url.setWidth(800, Unit.PIXELS);
            url.setRequired(true);
            layout.addComponent(url);
            layout.addComponent(title);

            // Now use a binder to bind the members
            final FieldGroup binder = new FieldGroup(newFavorite);
            binder.bindMemberFields(this);

            Button addButton = new Button("Add", new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        binder.commit();
                        listener.addFavorite(newFavorite);
                    } catch (CommitException e) {
                        // TODO how do we display validation errors?
                        Notification.show(e.getMessage());
                    }
                }
            });
            addButton.addStyleName("btn-dialog-commit");

            // A button to commit the buffer
            layout.addComponent(addButton);

            // A button to discard the buffer
            layout.addComponent(new Button("Cancel", new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    binder.discard();
                    // TODO remove form
                }
            }));

            setCompositionRoot(layout);
        }
    }
}
