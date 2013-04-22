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

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.shellapp.ShellApp;
import info.magnolia.ui.admincentral.shellapp.ShellAppContext;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.view.View;

import java.net.URI;

import javax.inject.Inject;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

import com.vaadin.server.Page;

/**
 * Favorites shell app.
 */
public class FavoritesShellApp implements ShellApp {

    private FavoritesView favoritesView;

    private FavoritesPresenter favoritesPresenter;

    private AppDescriptorRegistry appDescriptorRegistry;

    @Inject
    public FavoritesShellApp(FavoritesPresenter favoritesPresenter, AppDescriptorRegistry appDescriptorRegistry) {
        this.favoritesPresenter = favoritesPresenter;
        this.appDescriptorRegistry = appDescriptorRegistry;
    }

    @Override
    public View start(ShellAppContext context) {
        favoritesView = favoritesPresenter.start();
        return favoritesView;
    }

    @Override
    public void locationChanged(Location location) {
        JcrNewNodeAdapter favoriteLocation = determinePreviousLocation();
        favoritesView.setFavoriteLocation(favoriteLocation);
    }

    private JcrNewNodeAdapter determinePreviousLocation() {
        // at this point the current location in the browser hasn't yet changed to favorite shellapp,
        // so it is what we need to pre-populate the form for creating a new favorite
        final URI previousLocation = Page.getCurrent().getLocation();
        final String previousLocationFragment = previousLocation.getFragment();
        final String appId = DefaultLocation.extractAppId(previousLocationFragment);
        final String appType = DefaultLocation.extractAppType(previousLocationFragment);
        // TODO should this be added to DefaultLocation as a convenience static method?
        final String path = StringUtils.substringBetween(previousLocationFragment, ";", ":");
        Node favoritesNode = favoritesPresenter.getFavoritesNodeForCurrentUser();
        JcrNewNodeAdapter favoriteLocation = null;
        // skip bookmarking shell apps
        if (Location.LOCATION_TYPE_SHELL_APP.equals(appType)) {
            favoriteLocation = createNewFavorite(favoritesNode, "", "", "");
        } else {
            AppDescriptor appDescriptor;
            try {
                appDescriptor = appDescriptorRegistry.getAppDescriptor(appId);
            } catch (RegistrationException e) {
                throw new RuntimeException(e);
            }
            final String appIcon = StringUtils.defaultIfEmpty(appDescriptor.getIcon(), "icon-app");
            final String title = appDescriptor.getLabel(); // + " " + (path == null ? "/" : path);
            favoriteLocation = createNewFavorite(favoritesNode, previousLocation.toString(), title, appIcon);
        }
        return favoriteLocation;
    }

    private JcrNewNodeAdapter createNewFavorite(Node favoritesNode, String location, String title, String icon) {
        JcrNewNodeAdapter newFavorite = new JcrNewNodeAdapter(favoritesNode, AdmincentralNodeTypes.Favorite.NAME);
        newFavorite.addItemProperty(ModelConstants.JCR_NAME, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", title));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.URL, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", location));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.ICON, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", icon));
        return newFavorite;
    }
}
