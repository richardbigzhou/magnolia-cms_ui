/**
 * This file Copyright (c) 2012-2014 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.applauncher.widget;

import info.magnolia.ui.vaadin.gwt.client.applauncher.event.AppActivationEvent;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppGroup;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppTile;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Implementation of AppLauncher view.
 */
public class AppLauncherViewImpl extends FlowPanel implements AppLauncherView, AppActivationEvent.Handler {

    private final Map<String, VAppTileGroup> groups = new HashMap<String, VAppTileGroup>();

    private final VTemporaryAppGroupBar temporarySectionsBar = new VTemporaryAppGroupBar();

    private final EventBus eventBus;

    private Presenter presenter;

    private static final double TEMPORARY_PERMANENT_SECTIONS_OFFSET = 170.0;

    private ScrollPanel permanentAppScrollPanel;

    private FlowPanel permanentAppContainer;

    public AppLauncherViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        add(temporarySectionsBar);
        this.eventBus.addHandler(AppActivationEvent.TYPE, this);

        permanentAppScrollPanel = new ScrollPanel();
        permanentAppScrollPanel.addStyleName("permanent-app-scroll-panel");
        permanentAppScrollPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        permanentAppContainer = new FlowPanel() {
            private HandlerRegistration resizeRegistration;

            @Override
            protected void onAttach() {
                super.onAttach();
                setPermanentAppGroupBorder();
                resizeRegistration = Window.addResizeHandler(new ResizeHandler() {

                    @Override
                    public void onResize(ResizeEvent event) {
                        setPermanentAppGroupBorder();
                    }
                });
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                if (resizeRegistration != null) {
                    resizeRegistration.removeHandler();
                }
            }

            @Override
            public void add(Widget w) {
                super.add(w);
                if (this.isAttached()) {
                    setPermanentAppGroupBorder();
                }
            }
        };
        permanentAppScrollPanel.add(permanentAppContainer);
        add(permanentAppScrollPanel);
    }

    @Override
    public void addAppGroup(AppGroup group) {
        if (group.isPermanent()) {
            addPermanentAppGroup(group);
        } else {
            addTemporaryAppGroup(group);
        }
    }

    public void addTemporaryAppGroup(AppGroup groupParams) {
        final VTemporaryAppTileGroup group = new VTemporaryAppTileGroup(groupParams.getBackgroundColor());
        group.setClientGroup(groupParams.isClientGroup());
        // Shrink permanent app container when expanding temporary app group
        group.setShowCallback(new JQueryCallback() {

            @Override
            public void execute(JQueryWrapper query) {
                permanentAppScrollPanel.getElement().getStyle().setBottom(TEMPORARY_PERMANENT_SECTIONS_OFFSET + group.getOffsetHeight(), Unit.PX);
                setPermanentAppGroupBorder();
            }
        });
        // Expand permanent app container when shrinking temporary app group
        group.setCloseCallback(new JQueryCallback() {

            @Override
            public void execute(JQueryWrapper query) {
                permanentAppScrollPanel.getElement().getStyle().setBottom(TEMPORARY_PERMANENT_SECTIONS_OFFSET, Unit.PX);
                setPermanentAppGroupBorder();
            }
        });
        group.setClientGroup(groupParams.isClientGroup());
        groups.put(groupParams.getName(), group);
        temporarySectionsBar.addGroup(groupParams.getCaption(), group);
        add(group);
    }

    public void addPermanentAppGroup(AppGroup groupParams) {
        final VPermanentAppTileGroup group = new VPermanentAppTileGroup(groupParams.getCaption(), groupParams.getBackgroundColor());
        group.setClientGroup(groupParams.isClientGroup());
        groups.put(groupParams.getName(), group);
        permanentAppContainer.add(group);
    }

    private void setPermanentAppGroupBorder() {
        permanentAppScrollPanel.removeStyleName("permanent-app-group-border");
        if (permanentAppScrollPanel.getMaximumVerticalScrollPosition() > 0) {
            permanentAppScrollPanel.addStyleName("permanent-app-group-border");
        }
    }

    @Override
    public void onAppActivated(AppActivationEvent event) {
        presenter.activateApp(event.getAppName());
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setAppActive(String appName, boolean isActive) {
        for (Entry<String, VAppTileGroup> entry : groups.entrySet()) {
            if (entry.getValue().hasApp(appName)) {
                AppTileWidget tile = entry.getValue().getAppTile(appName);
                tile.setActiveState(isActive);
            }
        }
    }

    @Override
    public void addAppTile(AppTile tileData, AppGroup groupData) {
        AppTileWidget tile = new AppTileWidget(eventBus, tileData);
        VAppTileGroup group = groups.get(groupData.getName());
        if (group != null) {
            group.addAppTile(tile);
        }
    }

    @Override
    public void clear() {
        temporarySectionsBar.clear();
        for (final VAppTileGroup group : groups.values()) {
            remove(group);
        }
        groups.clear();
    }

}
