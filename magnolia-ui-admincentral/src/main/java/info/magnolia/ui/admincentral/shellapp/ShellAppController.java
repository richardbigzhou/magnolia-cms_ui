/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangeRequestedEvent;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

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

    private final ShellImpl shell;

    private Viewport viewport;

    @Inject
    public ShellAppController(ComponentProvider componentProvider, Shell shell, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus) {
        this.componentProvider = componentProvider;
        this.shell = (ShellImpl) shell;
        this.shell.setShellAppLocationProvider(new ShellImpl.ShellAppLocationProvider() {

            @Override
            public Location getShellAppLocation(String name) {
                ShellAppContextImpl context = contexts.get(name);
                return context != null ? context.getCurrentLocation() : null;
            }
        });

        admincentralEventBus.addHandler(LocationChangedEvent.class, this);
        admincentralEventBus.addHandler(LocationChangeRequestedEvent.class, this);
    }

    public void addShellApp(String name, Class<? extends ShellApp> clazz) {
        ShellAppContextImpl appContext = new ShellAppContextImpl(name);
        appContext.setAppClass(clazz);
        appContext.start();
        contexts.put(name, appContext);
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
    }

    @Override
    public void onLocationChanged(LocationChangedEvent event) {

        Location newLocation = event.getNewLocation();
        if (!newLocation.getAppType().equals(Location.LOCATION_TYPE_SHELL_APP)) {
            return;
        }

        ShellAppContextImpl nextContext = contexts.get(newLocation.getAppName());
        if (nextContext == null) {
            viewport.setView(null);
            return;
        }

        nextContext.onLocationUpdate(newLocation);
        viewport.setView(nextContext.getView());
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
            shell.registerShellApp(ShellAppType.valueOf(name.toUpperCase()), view.asVaadinComponent());
        }

        public void onLocationUpdate(Location newLocation) {
            shellApp.locationChanged(newLocation);
        }

        @Override
        public void updateIndication(int incrementOrDecrement) {
            shell.updateShellAppIndication(ShellAppType.valueOf(name.toUpperCase()), incrementOrDecrement);
        }

        @Override
        public void setIndication(int indication) {
            shell.setIndication(ShellAppType.valueOf(name.toUpperCase()), indication);
        }
    }
}
