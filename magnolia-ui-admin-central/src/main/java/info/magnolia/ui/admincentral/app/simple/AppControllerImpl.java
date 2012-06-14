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
import java.util.LinkedList;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.vaadin.ui.ComponentContainer;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppPlace;
import info.magnolia.ui.framework.app.AppView;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceChangeEvent;
import info.magnolia.ui.framework.place.PlaceChangeRequestEvent;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * App controller that manages the lifecycle of running apps and raises callbacks to the app.
 *
 * @version $Id$
 */
@Singleton
public class AppControllerImpl implements AppController, PlaceChangeEvent.Handler, PlaceChangeRequestEvent.Handler {

    private ComponentProvider componentProvider;
    private AppLayoutManager appLayoutManager;
    private PlaceController placeController;
    private EventBus eventBus;
    private ViewPort viewPort;

    private final Map<String, AppContextImpl> runningApps = new HashMap<String, AppContextImpl>();
    private final LinkedList<AppContextImpl> appHistory = new LinkedList<AppContextImpl>();

    private AppContextImpl currentApp;

    @Inject
    public AppControllerImpl(ComponentProvider componentProvider, AppLayoutManager appLayoutManager, PlaceController placeController, EventBus eventBus) {
        this.componentProvider = componentProvider;
        this.appLayoutManager = appLayoutManager;
        this.placeController = placeController;
        this.eventBus = eventBus;

        eventBus.addHandler(PlaceChangeEvent.class, this);
        eventBus.addHandler(PlaceChangeRequestEvent.class, this);
    }

    @Override
    public void setViewPort(ViewPort viewPort) {
        this.viewPort = viewPort;
    }

    public void startIfNotAlreadyRunning(String name) {
        doStartIfNotAlreadyRunning(name, null);
    }

    public void startIfNotAlreadyRunningThenFocus(String name) {
        AppContextImpl appContext = doStartIfNotAlreadyRunning(name, null);
        doFocus(appContext);
    }

    public void stopApp(String name) {
        AppContextImpl appContext = runningApps.get(name);
        if (appContext != null) {
            doStop(appContext);
        }
    }

    @Override
    public void stopCurrentApp() {
        final AppContextImpl appContext = appHistory.peekFirst();
        if (appContext != null) {
            stopApp(appContext.getName());
        }
    }

    public boolean isAppStarted(String name) {
        return runningApps.containsKey(name);
    }

    private AppContextImpl doStartIfNotAlreadyRunning(String name, Place place) {
        AppContextImpl appContext = runningApps.get(name);
        if (appContext == null) {
            AppDescriptor descriptor = getAppDescriptor(name);
            appContext = new AppContextImpl(descriptor);

            if (place == null) {
                place = appContext.getDefaultPlace();
            }

            appContext.start(eventBus, place);

            runningApps.put(name, appContext);
            sendEvent(AppEventType.STARTED, descriptor);
        }
        return appContext;
    }

    private void doFocus(AppContextImpl appContext) {
        appContext.focus();
        appHistory.addFirst(appContext);
        sendEvent(AppEventType.FOCUSED, appContext.getAppDescriptor());
    }

    private void doStop(AppContextImpl appContext) {
        appContext.stop();
        while (appHistory.remove(appContext)) {
        }
        runningApps.remove(appContext.getName());
        if (currentApp == appContext) {
            currentApp = null;
            viewPort.setView(null);
        }
        sendEvent(AppEventType.STOPPED, appContext.getAppDescriptor());
        if (!appHistory.isEmpty()) {
            doFocus(appHistory.peekFirst());
        }
    }

    private void sendEvent(AppEventType appEventType, AppDescriptor appDescriptor) {
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor, appEventType));
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {

        Place newPlace = event.getNewPlace();
        AppDescriptor nextApp = getAppForPlace(newPlace);

        if (nextApp == null) {
            return;
        }

        AppContextImpl nextAppContext = runningApps.get(nextApp.getName());

        if (nextAppContext != null) {
            nextAppContext.onPlaceUpdate(newPlace);
        } else {
            nextAppContext = doStartIfNotAlreadyRunning(nextApp.getName(), newPlace);
        }

        nextAppContext.display(viewPort);
        currentApp = nextAppContext;
    }

    @Override
    public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
        if (currentApp != null) {
            final String message = currentApp.mayStop();
            if (message != null) {
                event.setWarning(message);
            }
        }
    }

    private AppDescriptor getAppForPlace(Place newPlace) {
        if (newPlace instanceof AppPlace) {
            AppPlace appPlace = (AppPlace) newPlace;
            return getAppDescriptor(appPlace.getApp());
        }
        return null;
    }

    private AppDescriptor getAppDescriptor(String name) {
        for (AppCategory category : appLayoutManager.getLayout().getCategories()) {
            for (AppDescriptor descriptor : category.getApps()) {
                if (descriptor.getName().equals(name)) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    private class AppContextImpl implements AppContext {

        private AppDescriptor appDescriptor;
        private App app;
        private AppFrameView appFrameView;
        private Location currentLocation;

        public AppContextImpl(AppDescriptor appDescriptor) {
            this.appDescriptor = appDescriptor;
        }

        public String getName() {
            return appDescriptor.getName();
        }

        public AppDescriptor getAppDescriptor() {
            return appDescriptor;
        }

        /**
         * Called when the app is launched from the app launcher OR a place change event triggers it to start.
         */
        public void start(EventBus eventBus, Place place) {

            AppPlace appPlace = (AppPlace) place;

            app = componentProvider.newInstance(appDescriptor.getAppClass());

            appFrameView = new AppFrameView();

            AppView view = app.start(this, new DefaultLocation("app", appDescriptor.getName(), appPlace.getToken()));

            currentLocation = app.getDefaultLocation();

            appFrameView.addTab((ComponentContainer) ((IsVaadinComponent) view).asVaadinComponent(), view.getCaption());
        }

        /**
         * Called when the app is launched from the app launcher OR if another app is closed and this is to show itself.
         */
        public void focus() {
            placeController.goTo(getDefaultPlace());
        }

        /**
         * Called when a place change occurs and the app is already running.
         */
        public void onPlaceUpdate(Place place) {
            app.locationChanged(new DefaultLocation("app", appDescriptor.getName(), ((AppPlace) place).getToken()));
        }

        public void display(ViewPort viewPort) {
            viewPort.setView(appFrameView);
        }

        public String mayStop() {
            return null;
        }

        public void stop() {
            app.stop();
        }

        public Place getDefaultPlace() {
            return new AppPlace(appDescriptor.getName(), "");
        }

        @Override
        public void openAppView(AppView view) {
            appFrameView.addTab((ComponentContainer) ((IsVaadinComponent) view).asVaadinComponent(), view.getCaption());
        }

        @Override
        public void setAppLocation(Location location) {
            currentLocation = location;
//                    shell.setFragment();
        }
    }
}
