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
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed.FeedSection;

import java.util.Iterator;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for favorites.
 */
public class FavoritesViewImpl extends CustomComponent implements FavoritesView {

    private VerticalLayout layout = new VerticalLayout();
    private FavoritesView.Listener listener;
    private JcrItemNodeAdapter favoritesForCurrentUser;
    private FavoritesSection newPages;
    private FavoriteLocation favoriteLocation;

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
        /*
         * newCampaigns.addComponent(new FavoriteEntry("Add a special offer", "icon-add-item"));
         * newCampaigns.addComponent(new FavoriteEntry("Add a landing page", "icon-add-item"));
         * newCampaigns.addComponent(new FavoriteEntry("Edit main landing page", "icon-edit"));
         * newCampaigns.addComponent(new FavoriteEntry("Create a new micro site", "icon-add-item"));
         * newCampaigns.addComponent(new FavoriteEntry("Add a seasonal campaign", "icon-add-item"));
         */

        FavoritesSection assetShortcuts = new FavoritesSection();
        assetShortcuts.setCaption("Asset Shortcuts");
        /*
         * assetShortcuts.addComponent(new FavoriteEntry("Add a product image", "icon-add-item"));
         * assetShortcuts.addComponent(new FavoriteEntry("Upload image(s) to image pool", "icon-assets-app"));
         * assetShortcuts.addComponent(new FavoriteEntry("Upload review video", "icon-assets-app"));
         */

        leftSide.addComponent(newPages);
        leftSide.addComponent(newCampaigns);
        rightSide.addComponent(assetShortcuts);

        layout.addComponent(splitPanel);
        layout.setExpandRatio(splitPanel, 2f);
        Button button = new Button("Add new");
        button.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                JcrNewNodeAdapter newBookmark = new JcrNewNodeAdapter(favoritesForCurrentUser.getNode(), AdmincentralNodeTypes.Favorite.NAME);
                newBookmark.addItemProperty(ModelConstants.JCR_NAME, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", favoriteLocation.getTitle()));
                newBookmark.addItemProperty(AdmincentralNodeTypes.Favorite.URL, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", favoriteLocation.getUrl()));
                newBookmark.addItemProperty(AdmincentralNodeTypes.Favorite.ICON, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", favoriteLocation.getIcon()));
                layout.addComponent(new BookmarkForm(newBookmark));
            }
        });
        layout.addComponent(button);
        layout.setExpandRatio(button, 0.1f);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setFavoriteLocation(FavoriteLocation favoriteLocation) {
        this.favoriteLocation = favoriteLocation;
    }

    /**
     * Favorite entry.
     */
    public static class FavoriteEntry extends CssLayout {

        private final Label titleElement = new Label();

        private final Label iconElement = new Label();

        private final Label urlElement = new Label();

        public FavoriteEntry(final Item favorite) {
            addStyleName("v-favorites-entry");
            setSizeUndefined();
            setTitle(favorite.getItemProperty(ModelConstants.JCR_NAME).getValue().toString());
            setIcon(favorite.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue().toString());
            setUrl(favorite.getItemProperty(AdmincentralNodeTypes.Favorite.URL).getValue().toString());
            iconElement.setContentMode(ContentMode.HTML);
            iconElement.setWidth(null);
            iconElement.setStyleName("icon");
            titleElement.setStyleName("text");
            titleElement.setWidth(null);
            urlElement.setStyleName("text");
            urlElement.setWidth(null);
            addComponent(iconElement);
            addComponent(titleElement);
            addComponent(urlElement);
        }

        public void setUrl(String url) {
            urlElement.setValue(url);
        }

        public void setTitle(String title) {
            titleElement.setValue(title);
        }

        public void setIcon(String icon) {
            iconElement.setValue("<span class=\"" + icon + "\"></span>");
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
    public void setFavorites(JcrItemNodeAdapter favoritesForCurrentUser) {
        this.favoritesForCurrentUser = favoritesForCurrentUser;
        Iterator<JcrItemNodeAdapter> favorites = favoritesForCurrentUser.getChildren().values().iterator();
        while(favorites.hasNext()) {
            JcrItemNodeAdapter favorite = favorites.next();
            newPages.addComponent(new FavoriteEntry(favorite));
        }
    }

    // A form component that allows editing an item
    private class BookmarkForm extends CustomComponent {
        @PropertyId(ModelConstants.JCR_NAME)
        private TextField title = new TextField("Title");
        private TextField url = new TextField("Url");

        public BookmarkForm(final JcrItemNodeAdapter newFavorite) {
            FormLayout layout = new FormLayout();
            layout.addComponent(title);
            layout.addComponent(url);

            // Now use a binder to bind the members
            final FieldGroup binder = new FieldGroup(newFavorite);
            binder.bindMemberFields(this);

            // A button to commit the buffer
            layout.addComponent(new Button("Add", new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        binder.commit();
                        listener.addFavorite(newFavorite);
                        // refresh the app to display the new favorite.
                    } catch (CommitException e) {
                        Notification.show("Something went wrong!");
                    }
                }
            }));

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
