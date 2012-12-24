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

import info.magnolia.ui.admincentral.components.SplitFeed;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;


/**
 * Default view implementation for favorites.
 */

public class FavoritesViewImpl extends SplitFeed implements FavoritesView {

    //private final SplitFeed splitPanel = new SplitFeed();

    @Override
    public String getId(){
        return "favorite";
    }


    public FavoritesViewImpl() {
        super();
        addStyleName("favorites");
        setHeight("100%");
        setWidth("900px");
        construct();
    }

    private void construct() {
        final FeedSection leftSide = getLeftContainer();
        final FeedSection rightSide = getRightContainer();

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
    }

    @Override
    public Component asVaadinComponent() {
        return this;
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
            iconElement.setContentMode(Label.CONTENT_XHTML);
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
