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
package info.magnolia.ui.framework.instantpreview;

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.instantpreview.InstantPreviewLocationManager.PreviewLocationListener;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.shell.Shell;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InstantPreviewDispatcher.
 */
@Singleton
public class InstantPreviewDispatcher implements PreviewLocationListener {

    private static final Logger log = LoggerFactory.getLogger(InstantPreviewDispatcher.class);

    private final InstantPreviewLocationManager manager;

    private final LocationController controller;

    private final Shell shell;

    private String hostId = null;

    @Inject
    public InstantPreviewDispatcher(InstantPreviewLocationManager manager, LocationController controller, Shell shell, @Named("admincentral")EventBus eventBus) {
        this.manager = manager;
        this.controller = controller;
        this.shell = shell;

        eventBus.addHandler(LocationChangedEvent.class, new LocationChangedEvent.Handler() {
            @Override
            public void onLocationChanged(LocationChangedEvent event) {
                final Location location = event.getNewLocation();
                if (isSharing() && "app".equals(location.getAppType())) {
                    final String appName = location.getAppId();
                    final String params = location.getParameter();
                    if ("pages".equals(appName) && params.contains("preview")) {
                        InstantPreviewDispatcher.this.manager.sendPreviewToken(hostId, params);
                    }
                }
            }
        });
    }

    public boolean isSharing() {
        return hostId != null;
    }

    public String share() {
        this.hostId = manager.registerInstantPreviewHost();
        log.info("Started sharing with host id {}", hostId);
        return hostId;
    }
    public void unshare(String hostId) {
        manager.unregisterInstantPreviewHost(hostId);
        log.info("Stopped sharing host with id {}", hostId);
        this.hostId = null;
    }

    @Override
    public void onPreviewLocationReceived(String path) {
        final Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, "pages", "", path);
        controller.goTo(location);
        shell.pushToClient();
    }

    public void subscribeTo(String hostId) {
        manager.subscribeTo(hostId, this);
        log.info("Subscribed to host with id {}", hostId);
    }

    public void unsubscribeFrom(String hostId) {
        manager.unsubscribeFrom(hostId, this);
        log.info("Unsubscribed from host with id {}", hostId);
    }
}
