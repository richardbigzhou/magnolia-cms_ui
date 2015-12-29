/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.Registry;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;
import com.vaadin.ui.UI;

/**
 * Presenter for Favorites.
 */
public final class FavoritesPresenter implements FavoritesView.Listener {

    private final static Logger log = LoggerFactory.getLogger(FavoritesPresenter.class);

    private I18nizer i18nizer;
    private FavoritesView view;
    private FavoritesManager favoritesManager;
    private AppDescriptorRegistry appDescriptorRegistry;

    @Inject
    public FavoritesPresenter(final FavoritesView view, final FavoritesManager favoritesManager, final AppDescriptorRegistry appDescriptorRegistry, final I18nizer i18nizer) {
        this.view = view;
        this.favoritesManager = favoritesManager;
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.i18nizer = i18nizer;
    }

    public FavoritesView start() {
        view.setListener(this);
        initializeView();
        return view;
    }

    @Override
    public void setToInitialState() {
        // make sure to hide all the icons of the EditableFavoriteItem(s) and to set the to the non-editable-state.
        for (EditableFavoriteItem item : view.getEditableFavoriteItemList()) {
            item.setToNonEditableState();
            item.setIconsVisibility(false);
        }
    }

    @Override
    public void removeFavorite(String relPath) {
        favoritesManager.removeFavorite(relPath);
        initializeView();
    }

    @Override
    public void addFavoriteAndGroup(JcrNewNodeAdapter newFavorite, JcrNewNodeAdapter newGroup) {
        if (newGroup != null && newGroup.getItemProperty("title") != null && !"".equals(newGroup.getItemProperty("title").getValue())) {
            favoritesManager.addGroup(newGroup);
        }
        favoritesManager.addFavorite(newFavorite);
        initializeView();
    }

    @Override
    public void addFavorite(JcrNewNodeAdapter favorite) {
        favoritesManager.addFavorite(favorite);
        initializeView();
    }

    @Override
    public void moveFavorite(String relPath, String group) {
        favoritesManager.moveFavorite(relPath, group);
        initializeView();
    }

    @Override
    public void orderFavoriteBefore(String relPath, String sibling) {
        favoritesManager.orderFavoriteBefore(relPath, sibling);
        initializeView();
    }

    @Override
    public void orderFavoriteAfter(String relPath, String sibling) {
        favoritesManager.orderFavoriteAfter(relPath, sibling);
        initializeView();
    }

    @Override
    public void orderGroupBefore(String relPath, String sibling) {
        favoritesManager.orderGroupBefore(relPath, sibling);
        initializeView();
    }

    @Override
    public void orderGroupAfter(String relPath, String sibling) {
        favoritesManager.orderGroupAfter(relPath, sibling);
        initializeView();

    }

    @Override
    public void setItemsEditable(boolean isVisible) {
        List<EditableFavoriteItem> editableFavoriteItemList = view.getEditableFavoriteItemList();
        for (EditableFavoriteItem editableFavoriteItem : editableFavoriteItemList) {
            editableFavoriteItem.setIconsVisibility(isVisible);
        }
    }

    @Override
    public boolean itemsAreEditable() {
        List<EditableFavoriteItem> editableFavoriteItemList = view.getEditableFavoriteItemList();
        return !editableFavoriteItemList.isEmpty() && editableFavoriteItemList.get(0).iconsAreVisible();
    }

    @Override
    public boolean hasItems() {
        return view.getEditableFavoriteItemList() != null && view.getEditableFavoriteItemList().size() > 0;
    }

    @Override
    public void goToLocation(final String location) {
        // MGNLUI-3539 Can't use Page.setLocation(..) anymore due to https://dev.vaadin.com/ticket/12925
        // Need to use Page.open(..) cause other more apt methods such as Page.setUriFragment(..) seem not to re-sync selection properly.
        // Can't use LocationController#goTo() either because fragment-change has to be initiated from client-side for transitions to work properly.
        Page.getCurrent().open(getCompleteURIFromFragment(location), null);
    }

    public JcrNewNodeAdapter determinePreviousLocation() {
        JcrNewNodeAdapter favoriteLocation;

        // at this point the current location in the browser hasn't yet changed to favorite shellapp,
        // so it is what we need to pre-populate the form for creating a new favorite
        final URI previousLocation = Page.getCurrent().getLocation();
        final String previousLocationFragment = previousLocation.getFragment();

        // skip bookmark resolution if for some reason fragment is empty
        if (previousLocationFragment == null) {
            return createNewFavoriteSuggestion("", "", "");
        }

        final String appName = DefaultLocation.extractAppName(previousLocationFragment);
        final String appType = DefaultLocation.extractAppType(previousLocationFragment);
        // TODO MGNLUI-1190 should this be added to DefaultLocation as a convenience static method?
        final String path = StringUtils.substringBetween(previousLocationFragment, ";", ":");

        // skip bookmark resolution shell apps
        if (Location.LOCATION_TYPE_SHELL_APP.equals(appType)) {
            favoriteLocation = createNewFavoriteSuggestion("", "", "");
        } else {
            final AppDescriptor appDescriptor;

            try {
                DefinitionProvider<AppDescriptor> definitionProvider = appDescriptorRegistry.getProvider(appName);
                appDescriptor = i18nizer.decorate(definitionProvider.get());
            } catch (Registry.NoSuchDefinitionException | IllegalStateException e) {
                throw new RuntimeException(e);
            }

            final String appIcon = StringUtils.defaultIfEmpty(appDescriptor.getIcon(), "icon-app");
            final String title = appDescriptor.getLabel() + " " + (path == null ? "/" : path);
            final String urlFragment = getUrlFragmentFromURI(previousLocation);
            favoriteLocation = createNewFavoriteSuggestion(urlFragment, title, appIcon);
        }
        return favoriteLocation;
    }

    /**
     * @return a {@link info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter} used to pre-populate a form in the UI with a suggestion for a new favorite.
     */
    public JcrNewNodeAdapter createNewFavoriteSuggestion(String location, String title, String icon) {
        return favoritesManager.createFavoriteSuggestion(location, title, icon);
    }

    public JcrNewNodeAdapter createNewGroupSuggestion() {
        return favoritesManager.createFavoriteGroupSuggestion("");
    }

    public Map<String, String> getAvailableGroupsNames() {
        return favoritesManager.getGroupsNames();
    }

    @Override
    public void editFavorite(String relPath, String newTitle) {
        favoritesManager.editFavorite(relPath, newTitle);
        initializeView();
    }

    @Override
    public void addGroup(JcrNewNodeAdapter newGroup) {
        favoritesManager.addGroup(newGroup);
        initializeView();
    }

    @Override
    public void editGroup(String relPath, String newTitle) {
        favoritesManager.editGroup(relPath, newTitle);
        initializeView();
    }

    @Override
    public void removeGroup(String relPath) {
        favoritesManager.removeGroup(relPath);
        initializeView();
    }

    @Override
    public void setCurrentEditedItemId(String itemId) {
        // when somebody starts to edit a EditableFavoriteItem, make sure that all the other items are set to the not-editable-state
        for (EditableFavoriteItem editableItem : view.getEditableFavoriteItemList()) {
            if (!itemId.equals(editableItem.getItemId())) {
                editableItem.setToNonEditableState();
            }
        }
    }

    String getWebAppRootURI() {
        final URI currentUri = UI.getCurrent().getPage().getLocation();
        String instancePrefix = currentUri.getScheme() + "://" + currentUri.getHost();
        if (currentUri.getPort() > -1) {
            instancePrefix += ":" + currentUri.getPort();
        }
        instancePrefix += currentUri.getPath(); // Path contains the ctx
        if (StringUtils.isNotBlank(currentUri.getQuery())) {
            instancePrefix += "?" + currentUri.getQuery();
        }
        return instancePrefix;
    }

    String getUrlFragmentFromURI(URI location) {
        final String url = location.toString();
        String instancePrefix = getWebAppRootURI();

        return url.contains(instancePrefix) ? url.substring(url.indexOf(instancePrefix) + instancePrefix.length(), url.length()) : url;
    }

    /**
     * This method has package visibility for testing purposes only.
     */
    String getCompleteURIFromFragment(final String fragment) {
        URI uri = null;
        try {
            uri = new URI(fragment);
        } catch (URISyntaxException e) {
            log.warn("Could not create URI from fragment {}. Exception: {}", fragment, e.toString());
        }
        if (uri == null || uri.isAbsolute()) {
            return fragment;
        }
        return getWebAppRootURI() + fragment;
    }

    private void initializeView() {
        boolean itemIconsVisible = hasItems() && itemsAreEditable();
        view.init(favoritesManager.getFavorites(), createNewFavoriteSuggestion("", "", ""), createNewGroupSuggestion(), getAvailableGroupsNames(), itemIconsVisible);
    }

}
