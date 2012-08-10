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
package info.magnolia.ui.vaadin.integration.widget.client.applauncher;

import info.magnolia.ui.vaadin.integration.widget.client.applauncher.event.AppActivationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Implementation of AppLauncher view. 
 *
 */
public class VAppLauncherViewImpl extends FlowPanel implements VAppLauncherView, AppActivationEvent.Handler {

    private Map<String, VAppTileGroup> groups = new HashMap<String, VAppTileGroup>();
    
    private VTemporaryAppGroupBar temporarySectionsBar = new VTemporaryAppGroupBar();
    
    private final EventBus eventBus;
    
    private Presenter presenter;
    
    private Element rootEl = DOM.createDiv();
    
    public VAppLauncherViewImpl(final EventBus eventBus) {
        super();
        getElement().appendChild(rootEl);
        this.eventBus = eventBus;
        rootEl.addClassName("v-app-launcher");
        add(temporarySectionsBar);        
        this.eventBus.addHandler(AppActivationEvent.TYPE,  this);
    }

    @Override
    public void addAppGroup(VAppGroupJSO group) {
        if (group.isPermanent()) {
            addPermanentAppGroup(group.getName(), group.getCaption(), group.getBackgroundColor());
        } else {
            addTemporaryAppGroup(group.getName(), group.getCaption(), group.getBackgroundColor());
        }
    }

    public void addTemporaryAppGroup(String name, String caption, String color) {
        final VAppTileGroup group = new VTemporaryAppTileGroup(eventBus, color);
        groups.put(name, group);
        temporarySectionsBar.addGroup(caption, group);
        add(group, rootEl);
    }
    
    public void addPermanentAppGroup(String name, String caption, String color) {
        final VPermanentAppTileGroup group = new VPermanentAppTileGroup(eventBus, caption, color);
        groups.put(name, group);
        add(group, rootEl);
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
                final VAppTile tile = entry.getValue().getAppTile(appName);
                tile.setActive(isActive);
            }
        }
    }

    @Override
    public void addAppTile(VAppTileJSO tileJSO, String groupName) {
        final VAppTile tile = new VAppTile(eventBus, tileJSO);
        final VAppTileGroup group = groups.get(groupName);
        if (group != null) {
            group.addAppTile(tile);
        }
    }
}
