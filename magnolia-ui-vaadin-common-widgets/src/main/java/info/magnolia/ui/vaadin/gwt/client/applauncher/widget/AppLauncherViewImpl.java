/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;

/**
 * Implementation of AppLauncher view.
 */
public class AppLauncherViewImpl extends FlowPanel implements AppLauncherView, AppActivationEvent.Handler {

    private static final double TEMPORARY_PERMANENT_SECTIONS_OFFSET = 170.0;

    private static final String PERMANENT_APP_GROUP_BORDER = "permanent-app-group-border";

    private static final String PERMANENT_APP_GROUP_BORDER_BOTTOM = PERMANENT_APP_GROUP_BORDER + "-bottom";

    private static final String PERMANENT_APP_GROUP_BORDER_TOP = PERMANENT_APP_GROUP_BORDER + "-top";

    public static final String PERMANENT_APP_SCROLL_PANEL = "permanent-app-scroll-panel";

    private final Map<String, VAppTileGroup> groups = new HashMap<String, VAppTileGroup>();

    private final VTemporaryAppGroupBar temporarySectionsBar = new VTemporaryAppGroupBar();

    private final EventBus eventBus;

    private Presenter presenter;

    private ScrollPanel permanentAppScrollPanel = new ScrollPanel();;

    private FlowPanel permanentAppContainer = new FlowPanel();;

    private ComputedStyle permanentAppScrollPanelStyle = new ComputedStyle(permanentAppScrollPanel.getElement());

    public AppLauncherViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        add(temporarySectionsBar);
        this.eventBus.addHandler(AppActivationEvent.TYPE, this);

        permanentAppScrollPanel.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        permanentAppScrollPanel.addStyleName(PERMANENT_APP_SCROLL_PANEL);
        permanentAppScrollPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);

        permanentAppScrollPanel.add(permanentAppContainer);
        permanentAppScrollPanel.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                updatePermanentAppGroupBorder();
            }
        });
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
        groups.put(groupParams.getName(), group);
        temporarySectionsBar.addGroup(groupParams.getCaption(), group);
        add(group);

        presenter.registerElementResizeListener(group.getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                final VTemporaryAppTileGroup currentOpenGroup = temporarySectionsBar.getCurrentOpenGroup();
                if (currentOpenGroup == null || group == currentOpenGroup) {
                    permanentAppScrollPanel.getElement().getStyle().setBottom(TEMPORARY_PERMANENT_SECTIONS_OFFSET + group.getOffsetHeight(), Unit.PX);
                    updatePermanentAppGroupBorder();
                }
            }
        });
    }

    public void addPermanentAppGroup(AppGroup groupParams) {
        final VPermanentAppTileGroup group = new VPermanentAppTileGroup(groupParams.getCaption(), groupParams.getBackgroundColor());
        group.setClientGroup(groupParams.isClientGroup());
        groups.put(groupParams.getName(), group);
        permanentAppContainer.add(group);

        presenter.registerElementResizeListener(permanentAppScrollPanel.getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                updatePermanentAppGroupBorder();
            }
        });
    }

    private void updatePermanentAppGroupBorder() {
        permanentAppScrollPanel.removeStyleName(PERMANENT_APP_GROUP_BORDER_BOTTOM);
        permanentAppScrollPanel.removeStyleName(PERMANENT_APP_GROUP_BORDER_TOP);
        int maxScrollPosition = getPermanentAppGroupMaxScrollTop();
        if (maxScrollPosition > 0) {
            int verticalScrollPosition = permanentAppScrollPanel.getVerticalScrollPosition();
            if (verticalScrollPosition > 0) {
                permanentAppScrollPanel.addStyleName(PERMANENT_APP_GROUP_BORDER_TOP);
            }

            if (verticalScrollPosition < maxScrollPosition) {
                permanentAppScrollPanel.addStyleName(PERMANENT_APP_GROUP_BORDER_BOTTOM);
            }

        }
    }

    private int getPermanentAppGroupMaxScrollTop() {
        return permanentAppScrollPanel.getMaximumVerticalScrollPosition() - permanentAppScrollPanelStyle.getBorder()[0] - permanentAppScrollPanelStyle.getBorder()[2];
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
