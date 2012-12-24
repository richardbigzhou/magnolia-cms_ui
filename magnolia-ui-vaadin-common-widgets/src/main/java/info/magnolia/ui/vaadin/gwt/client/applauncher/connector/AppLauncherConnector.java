/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.applauncher.connector;

import info.magnolia.ui.vaadin.applauncher.AppLauncher;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppGroup;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppTile;
import info.magnolia.ui.vaadin.gwt.client.applauncher.widget.AppLauncherView;
import info.magnolia.ui.vaadin.gwt.client.applauncher.widget.AppLauncherView.Presenter;
import info.magnolia.ui.vaadin.gwt.client.applauncher.widget.AppLauncherViewImpl;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * {@link AppLauncherConnector}.
 */
@Connect(AppLauncher.class)
public class AppLauncherConnector extends AbstractComponentConnector {

    private AppLauncherView view;
    
    private EventBus eventBus = new SimpleEventBus();
    
    public AppLauncherConnector() {
        addStateChangeHandler("appGroups", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                view.clear();
                for (final AppGroup appGroup : getState().appGroups.values()) {
                    view.addAppGroup(appGroup);
                    for (final AppTile tile : appGroup.getAppTiles()) {
                        view.addAppTile(tile, appGroup);
                    }
                }
                updateRuningAppTiles();
            }
        });

        addStateChangeHandler("runningApps", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                updateRuningAppTiles();
            }
        });
    }
    
    private void updateRuningAppTiles() {
        for (final AppGroup appGroup : getState().appGroups.values()) {
            for (final AppTile tile : appGroup.getAppTiles()) {
                if (getState().runningApps.contains(tile.getName())) {
                    view.setAppActive(tile.getName(), true);                    
                } else {
                    view.setAppActive(tile.getName(), false);
                }

            }
        }
    }
    
    @Override
    public Widget getWidget() {
        return super.getWidget();
    }
    
    @Override
    protected Widget createWidget() {
        this.view = new AppLauncherViewImpl(eventBus);
        this.view.setPresenter(new Presenter() {
            @Override
            public void activateApp(String appName) {
                History.newItem("app:" + appName, true);
            }
        });
        return view.asWidget();
    }
    
    @Override
    public AppLauncherState getState() {
        return (AppLauncherState)super.getState();
    }
    
    @Override
    protected AppLauncherState createState() {
        return new AppLauncherState();
    }

}
