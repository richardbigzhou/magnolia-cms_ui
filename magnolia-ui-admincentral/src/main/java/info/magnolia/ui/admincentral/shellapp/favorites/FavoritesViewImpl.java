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

import info.magnolia.ui.vaadin.splitfeed.SplitFeed;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed.FeedSection;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for favorites.
 */

public class FavoritesViewImpl extends CustomComponent implements FavoritesView {

    private VerticalLayout layout = new VerticalLayout();
    private FavoritesView.Listener listener;

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

        FavoritesSection newPages = new FavoritesSection();
        newPages.setCaption("New Pages");
        newPages.addComponent(new FavoriteEntry("Add new product page", "icon-add-item"));
        newPages.addComponent(new FavoriteEntry("Add new product review", "icon-add-item"));

        FavoritesSection newCampaigns = new FavoritesSection();
        newCampaigns.setCaption("New Campaigns");
        newCampaigns.addComponent(new FavoriteEntry("Add a special offer", "icon-add-item"));
        newCampaigns.addComponent(new FavoriteEntry("Add a landing page", "icon-add-item"));
        newCampaigns.addComponent(new FavoriteEntry("Edit main landing page", "icon-edit"));
        newCampaigns.addComponent(new FavoriteEntry("Create a new micro site", "icon-add-item"));
        newCampaigns.addComponent(new FavoriteEntry("Add a seasonal campaign", "icon-add-item"));

        FavoritesSection assetShortcuts = new FavoritesSection();
        assetShortcuts.setCaption("Asset Shortcuts");
        assetShortcuts.addComponent(new FavoriteEntry("Add a product image", "icon-add-item"));
        assetShortcuts.addComponent(new FavoriteEntry("Upload image(s) to image pool", "icon-assets-app"));
        assetShortcuts.addComponent(new FavoriteEntry("Upload review video", "icon-assets-app"));

        leftSide.addComponent(newPages);
        leftSide.addComponent(newCampaigns);
        rightSide.addComponent(assetShortcuts);

        layout.addComponent(splitPanel);
        layout.setExpandRatio(splitPanel, 2f);
        Button button = new Button("Add new");
        layout.addComponent(button);
        layout.setExpandRatio(button, 0.1f);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    /**
     * Favorite entry.
     */
    public static class FavoriteEntry extends CssLayout {

        private final Label textElement = new Label();

        private final Label iconElement = new Label();

        public FavoriteEntry(final String text, final String icon) {
            addStyleName("v-favorites-entry");
            setSizeUndefined();
            setText(text);
            setIcon(icon);
            iconElement.setContentMode(ContentMode.HTML);
            iconElement.setWidth(null);
            iconElement.setStyleName("icon");
            textElement.setStyleName("text");
            textElement.setWidth(null);
            addComponent(iconElement);
            addComponent(textElement);
        }

        public void setText(String text) {
            textElement.setValue(text);
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
}
