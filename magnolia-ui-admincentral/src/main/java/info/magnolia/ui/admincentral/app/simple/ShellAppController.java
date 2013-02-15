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
package info.magnolia.ui.admincentral.app.simple;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherShellApp;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesShellApp;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseShellApp;
import info.magnolia.ui.framework.app.ShellApp;
import info.magnolia.ui.framework.app.ShellAppContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.event.AdminCentralEventBusConfigurer;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangeRequestedEvent;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Manages the shell apps and raises callbacks to the app.
 */
@Singleton
public class ShellAppController implements LocationChangedEvent.Handler, LocationChangeRequestedEvent.Handler {

    private final Map<String, ShellAppContextImpl> contexts = new HashMap<String, ShellAppContextImpl>();

    private final ComponentProvider componentProvider;

    private final Shell shell;

    private ViewPort viewPort;

    @Inject
    public ShellAppController(ComponentProvider componentProvider, Shell shell, @Named(AdminCentralEventBusConfigurer.EVENT_BUS_NAME) EventBus admincentralEventBus) {
        this.componentProvider = componentProvider;
        this.shell = shell;

        addShellApp("applauncher", AppLauncherShellApp.class);
        addShellApp("pulse", PulseShellApp.class);
        addShellApp("favorite", FavoritesShellApp.class);

        admincentralEventBus.addHandler(LocationChangedEvent.class, this);
        admincentralEventBus.addHandler(LocationChangeRequestedEvent.class, this);
    }

    private void addShellApp(String name, Class<? extends ShellApp> clazz) {
        ShellAppContextImpl appContext = new ShellAppContextImpl(name);
        appContext.setAppClass(clazz);
        appContext.start();
        contexts.put(name, appContext);
    }

    public void setViewPort(ViewPort viewPort) {
        this.viewPort = viewPort;
    }

    @Override
    public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
    }

    @Override
    public void onLocationChanged(LocationChangedEvent event) {

        Location newLocation = event.getNewLocation();
        if (!newLocation.getAppType().equals(Location.LOCATION_TYPE_SHELL_APP)) {
            viewPort.setView(null);
            return;
        }

        ShellAppContextImpl nextContext = contexts.get(newLocation.getAppId());
        if (nextContext == null) {
            viewPort.setView(null);
            return;
        }

        nextContext.onLocationUpdate(newLocation);
        viewPort.setView(nextContext.getView());
    }

    public Location getCurrentLocation(String name) {
        ShellAppContextImpl context = contexts.get(name);
        return context != null ? context.getCurrentLocation() : null;
    }

    private class ShellAppContextImpl implements ShellAppContext {

        private final String name;
        private ShellApp shellApp;
        private View view;
        private Location currentLocation;
        private Class<? extends ShellApp> appClass;

        public ShellAppContextImpl(String name) {
            this.name = name;
            this.currentLocation = new DefaultLocation(Location.LOCATION_TYPE_SHELL_APP, name, "", "");
        }

        public String getName() {
            return name;
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        public View getView() {
            return view;
        }

        @Override
        public void setAppLocation(Location location) {
            currentLocation = location;
            shell.setFragment(location.toString());
        }

        public void setAppClass(Class<? extends ShellApp> clazz) {
            appClass = clazz;
        }

        public void start() {
            shellApp = componentProvider.newInstance(appClass);
            view = shellApp.start(this);
        }

        public void onLocationUpdate(Location newLocation) {
            shellApp.locationChanged(newLocation);
        }
    }
}
