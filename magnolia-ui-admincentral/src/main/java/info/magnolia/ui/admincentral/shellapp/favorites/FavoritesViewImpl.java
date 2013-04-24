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

import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed.FeedSection;

import java.util.Iterator;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
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
     * Favorite section.
     */
    private static class FavoritesSection extends CssLayout {

        public FavoritesSection() {
            addStyleName("favorites-section");
        }
    }

    @Override
    public void init(JcrItemNodeAdapter favorites, JcrNewNodeAdapter favoriteSuggestion) {
        newPages.removeAllComponents();
        Iterator<JcrItemNodeAdapter> favoritesIterator = favorites.getChildren().values().iterator();

        while(favoritesIterator.hasNext()) {
            final JcrItemNodeAdapter favorite = favoritesIterator.next();
            final FavoriteEntry fav = new FavoriteEntry(favorite, listener);
            newPages.addComponent(fav);
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
