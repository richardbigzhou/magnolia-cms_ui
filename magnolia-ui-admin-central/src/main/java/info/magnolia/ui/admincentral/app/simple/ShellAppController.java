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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherShellApp;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesShellApp;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseShellApp;
import info.magnolia.ui.framework.app.ShellApp;
import info.magnolia.ui.framework.app.ShellAppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangeRequestedEvent;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;

/**
 * Manages the shell apps and raises callbacks to the app.
 *
 * @version $Id$
 */
@Singleton
public class ShellAppController implements LocationChangedEvent.Handler, LocationChangeRequestedEvent.Handler {

    private ComponentProvider componentProvider;
    private Shell shell;
    private EventBus eventBus;
    private ViewPort viewPort;
    private final Map<String, ShellAppContextImpl> contexts = new HashMap<String, ShellAppContextImpl>();
    private ShellAppContextImpl currentAppContext = null;

    @Inject
    public ShellAppController(ComponentProvider componentProvider, Shell shell, EventBus eventBus) {
        this.componentProvider = componentProvider;
        this.shell = shell;
        this.eventBus = eventBus;
        contexts.put("applauncher", create(AppLauncherShellApp.class));
        contexts.put("pulse", create(PulseShellApp.class));
        contexts.put("favorite", create(FavoritesShellApp.class));

        eventBus.addHandler(LocationChangedEvent.class, this);
        eventBus.addHandler(LocationChangeRequestedEvent.class, this);
    }

    private ShellAppContextImpl create(Class<? extends ShellApp> clazz) {
        ShellAppContextImpl appContext = new ShellAppContextImpl();
        appContext.setAppClass(clazz);
        appContext.start();
        return appContext;
    }

    public void setViewPort(ViewPort viewPort) {
        this.viewPort = viewPort;
    }

    @Override
    public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
    }

    @Override
    public void onLocationChanged(LocationChangedEvent event) {

        DefaultLocation newLocation = (DefaultLocation) event.getNewLocation();
        if (!newLocation.getType().equals("shell")) {
            currentAppContext = null;
            viewPort.setView(null);
            return;
        }

        ShellAppContextImpl nextContext = contexts.get(newLocation.getPrefix());
        if (nextContext == null) {
            currentAppContext = null;
            viewPort.setView(null);
            return;
        }

        currentAppContext = nextContext;
        currentAppContext.onLocationUpdate(newLocation);
        currentAppContext.display(viewPort);
    }

    private class ShellAppContextImpl implements ShellAppContext {

        private ShellApp shellApp;
        private View view;
        private Location currentLocation;
        private Class<? extends ShellApp> appClass;

        @Override
        public void setAppLocation(Location location) {
            this.currentLocation = location;
            shell.setFragment(location.toString());
        }

        public void setAppClass(Class<? extends ShellApp> clazz) {
            this.appClass = clazz;
        }

        public void start() {
            shellApp = componentProvider.newInstance(appClass);
            view = shellApp.start(this);
        }

        public void display(ViewPort viewPort) {
            viewPort.setView(view);
        }

        public void onLocationUpdate(DefaultLocation newLocation) {
            shellApp.locationChanged(newLocation);
        }
    }
}
