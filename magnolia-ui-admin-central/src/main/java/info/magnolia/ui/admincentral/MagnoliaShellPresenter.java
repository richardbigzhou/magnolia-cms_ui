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
package info.magnolia.ui.admincentral;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherActivity;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherPlace;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesActivity;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesPlace;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseActivity;
import info.magnolia.ui.admincentral.shellapp.pulse.PulsePlace;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.activity.ActivityMapperImpl;
import info.magnolia.ui.framework.app.AppActivityManager;
import info.magnolia.ui.framework.app.AppActivityMapper;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.PlaceActivityMapping;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayout;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapper;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.vaadin.ui.Window;

/**
 * Presenter meant to bootstrap the MagnoliaShell.
 *
 * @version $Id$
 */
public class MagnoliaShellPresenter implements MagnoliaShellView.Presenter {

    private final MagnoliaShellView view;

    @Inject
    public MagnoliaShellPresenter(final MagnoliaShellView view, final EventBus bus, final AppLayoutManager appLauncherLayoutManager,
                                  final AppController appController, final PlaceController controller,
                                  ComponentProvider componentProvider) {
        super();
        this.view = view;
        this.view.setPresenter(this);

        final ActivityMapperImpl shellAppActivityMapper = new ActivityMapperImpl(componentProvider);
        shellAppActivityMapper.setLongLivingActivities(true);
        shellAppActivityMapper.addMapping(AppLauncherPlace.class, AppLauncherActivity.class);
        shellAppActivityMapper.addMapping(PulsePlace.class, PulseActivity.class);
        shellAppActivityMapper.addMapping(FavoritesPlace.class, FavoritesActivity.class);
        final ActivityManager shellAppManager = new ActivityManager(shellAppActivityMapper, bus);
        shellAppManager.setViewPort(view.getRoot().getShellAppViewport());

        final AppActivityMapper appActivityMapper = new AppActivityMapper(componentProvider, appLauncherLayoutManager, bus);
        final ActivityManager appManager = new AppActivityManager(appActivityMapper, bus, appLauncherLayoutManager, appController);
        appManager.setViewPort(view.getRoot().getAppViewport());

        final PlaceHistoryMapper placeHistoryMapper = new PlaceHistoryMapperImpl(getSupportedPlaces(appLauncherLayoutManager.getLayout()));
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(placeHistoryMapper, view.getRoot());

        historyHandler.register(controller, bus, new AppLauncherPlace("test"));
    }

    public void start(final Window window) {
        final MagnoliaShell shell = view.getRoot();
        shell.setSizeFull();
        window.addComponent(shell);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Place>[] getSupportedPlaces(AppLayout appLauncherLayout) {
        List<Class<? extends Place>> places = new ArrayList<Class<? extends Place>>();
        places.add(AppLauncherPlace.class);
        places.add(PulsePlace.class);
        places.add(FavoritesPlace.class);
        for (AppCategory category : appLauncherLayout.getCategories()) {
            for (AppDescriptor descriptor : category.getApps()) {
                for (PlaceActivityMapping mapping : descriptor.getActivityMappings()) {
                    places.add(mapping.getPlace());
                }
            }
        }
        return places.toArray(new Class[places.size()]);
    }
}
