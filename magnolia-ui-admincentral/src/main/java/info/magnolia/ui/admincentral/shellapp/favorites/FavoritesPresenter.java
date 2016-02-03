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


import info.magnolia.context.MgnlContext;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;

/**
 * Presenter for Favorites.
 */
public final class FavoritesPresenter implements FavoritesView.Listener {

    private final Logger log = LoggerFactory.getLogger(FavoritesPresenter.class);

    private FavoritesView view;
    private FavoritesManager favoritesManager;
    private AppDescriptorRegistry appDescriptorRegistry;

    @Inject
    public FavoritesPresenter(final FavoritesView view, final FavoritesManager favoritesManager, final AppDescriptorRegistry appDescriptorRegistry) {
        this.view = view;
        this.favoritesManager = favoritesManager;
        this.appDescriptorRegistry = appDescriptorRegistry;
    }

    public FavoritesView start() {
        view.setListener(this);
        initializeView();
        return view;
    }

    @Override
    public void removeFavorite(String relPath) {
        favoritesManager.removeFavorite(relPath);
        initializeView();
    }

    @Override
    public void addFavorite(JcrNewNodeAdapter favorite) {
        favoritesManager.addFavorite(favorite);
        initializeView();
    }

    @Override
    public void goToLocation(final String location) {
        final String completeLocation = getCompleteURIFromFragment(location);
        Page.getCurrent().setLocation(completeLocation);
    }

    public JcrNewNodeAdapter determinePreviousLocation() {
        // at this point the current location in the browser hasn't yet changed to favorite shellapp,
        // so it is what we need to pre-populate the form for creating a new favorite
        final URI previousLocation = Page.getCurrent().getLocation();
        final String previousLocationFragment = previousLocation.getFragment();
        final String appName = DefaultLocation.extractAppName(previousLocationFragment);
        final String appType = DefaultLocation.extractAppType(previousLocationFragment);
        // TODO MGNLUI-1190 should this be added to DefaultLocation as a convenience static method?
        final String path = StringUtils.substringBetween(previousLocationFragment, ";", ":");
        JcrNewNodeAdapter favoriteLocation;
        // skip bookmarking shell apps
        if (Location.LOCATION_TYPE_SHELL_APP.equals(appType)) {
            favoriteLocation = createNewFavoriteSuggestion("", "", "");
        } else {
            AppDescriptor appDescriptor;
            try {
                appDescriptor = appDescriptorRegistry.getAppDescriptor(appName);
            } catch (RegistrationException e) {
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

    String getWebAppRootURI() {
        final HttpServletRequest request = MgnlContext.getWebContext().getRequest();
        final String fullProtocolString = request.getProtocol();
        String instancePrefix = fullProtocolString.substring(0, fullProtocolString.indexOf("/")).toLowerCase() + "://" + request.getServerName() + ":" + request.getServerPort();
        instancePrefix += MgnlContext.getContextPath();
        return instancePrefix;
    }

    String getUrlFragmentFromURI(URI location) {
        final String url = location.toString();
        String instancePrefix = getWebAppRootURI();

        final String urlFragment = url.contains(instancePrefix) ? url.substring(url.indexOf(instancePrefix) + instancePrefix.length(), url.length()) : url;
        return urlFragment;
    }

    String getCompleteURIFromFragment(final String fragment) {
        URI uri = null;
        try {
            uri = new URI(fragment);
        } catch (URISyntaxException e) {
            log.warn("Could not creat URI from fragment {}. Exception: {}", fragment, e.toString());
        }
        if (uri == null || uri.isAbsolute()) {
            return fragment;
        }
        return getWebAppRootURI() + fragment;
    }

    private void initializeView() {
        view.init(favoritesManager.getFavorites(), createNewFavoriteSuggestion("", "", ""), createNewGroupSuggestion(), getAvailableGroupsNames());
    }
}
