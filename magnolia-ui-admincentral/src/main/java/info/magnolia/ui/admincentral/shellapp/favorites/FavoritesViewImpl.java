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
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed;
import info.magnolia.ui.vaadin.splitfeed.SplitFeed.FeedSection;

import java.util.Map;

import javax.inject.Inject;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for favorites.
 */
public class FavoritesViewImpl extends CustomComponent implements FavoritesView {

    private VerticalLayout layout = new VerticalLayout();
    private FavoritesView.Listener listener;
    private FavoritesSection noGroup;
    private Component favoriteForm;
    private FeedSection leftColumn;
    private FeedSection rightColumn;
    private Shell shell;

    @Override
    public String getId() {
        return "favorite";
    }

    @Inject
    public FavoritesViewImpl(Shell shell) {
        super();
        this.shell = shell;
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
        leftColumn = splitPanel.getLeftContainer();
        rightColumn = splitPanel.getRightContainer();

        noGroup = new FavoritesSection();
        noGroup.addStyleName("no-group");
        leftColumn.addComponent(noGroup);

        layout.addComponent(splitPanel);
        layout.setExpandRatio(splitPanel, 1f);

    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setFavoriteLocation(JcrNewNodeAdapter newFavorite, JcrNewNodeAdapter newGroup, Map<String, String> availableGroupsNames) {
        layout.removeComponent(favoriteForm);
        favoriteForm = new FavoritesForm(newFavorite, newGroup, availableGroupsNames, listener, shell);
        layout.addComponent(favoriteForm);
    }

    @Override
    public void init(JcrItemNodeAdapter favorites, JcrNewNodeAdapter favoriteSuggestion, JcrNewNodeAdapter groupSuggestion, Map<String, String> availableGroups) {
        noGroup.removeAllComponents();
        leftColumn.removeAllComponents();
        rightColumn.removeAllComponents();

        for (JcrItemNodeAdapter favoriteAdapter : favorites.getChildren().values()) {
            if (AdmincentralNodeTypes.Favorite.NAME.equals(favoriteAdapter.getPrimaryNodeTypeName())) {
                final FavoritesEntry favEntry = new FavoritesEntry(favoriteAdapter, listener);
                noGroup.addComponent(favEntry);
            } else {
                FavoritesSection group = new FavoritesSection();
                group.setCaption(favoriteAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue().toString());
                for (JcrItemNodeAdapter fav : favoriteAdapter.getChildren().values()) {
                    final FavoritesEntry favEntry = new FavoritesEntry(fav, listener);
                    group.addComponent(favEntry);
                }
                rightColumn.addComponent(group);
            }
        }
        leftColumn.addComponent(noGroup);

        if (favoriteForm != null) {
            layout.removeComponent(favoriteForm);
        }
        favoriteForm = new FavoritesForm(favoriteSuggestion, groupSuggestion, availableGroups, listener, shell);
        layout.addComponent(favoriteForm);
    }

    /**
     * Favorite section.
     */
    private class FavoritesSection extends CssLayout {

        public FavoritesSection() {
            addStyleName("favorites-section");
        }
    }

}
