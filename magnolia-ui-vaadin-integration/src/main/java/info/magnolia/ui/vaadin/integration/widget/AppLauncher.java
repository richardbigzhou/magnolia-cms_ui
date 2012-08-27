/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.integration.widget;

import info.magnolia.ui.vaadin.integration.widget.client.applauncher.VAppLauncher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;

import com.google.gson.Gson;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;


/**
 * Server side of AppLauncher.
 */
@SuppressWarnings("serial")
@ClientWidget(value = VAppLauncher.class, loadStyle = LoadStyle.EAGER)
public class AppLauncher extends AbstractComponent implements ServerSideHandler {

    private final Map<String, AppGroup> appGroups = new LinkedHashMap<String, AppGroup>();

    private final ServerSideProxy proxy = new ServerSideProxy(this);

    private boolean isAttached = false;

    public AppLauncher() {
        super();
        setSizeFull();
        setImmediate(true);
    }

    public void addAppGroup(String name, String caption, String color, boolean isPermanent, boolean clientGroup) {
        final AppGroup group = new AppGroup(name, caption, color, isPermanent, clientGroup);
        appGroups.put(name, group);
        doAddGroup(group);
    }

    private void doAddGroup(final AppGroup group) {
        if (isAttached) {
            proxy.call("addAppGroup", new Gson().toJson(group));
        }
    }

    public void addAppTile(String name, String caption, String icon, String groupName) {
        final AppGroup group = appGroups.get(groupName);
        if (group != null) {
            final AppTile tile = new AppTile(name, caption, icon);
            group.addAppTile(tile);
            doAddAppTile(tile, groupName);
        }
    }

    private void doAddAppTile(final AppTile tile, String groupName) {
        if (isAttached) {
            proxy.call("addAppTile", new Gson().toJson(tile), groupName);
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        proxy.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public Object[] initRequestFromClient() {
        for (final AppGroup group : appGroups.values()) {
            doAddGroup(group);
            for (final AppTile tile : group.getAppTiles()) {
                doAddAppTile(tile, group.getName());
            }
        }
        return new Object[]{};
    }

    @Override
    public void attach() {
        super.attach();
        isAttached = true;
    }

    @Override
    public void detach() {
        super.detach();
        clear();
    }

    public void clear() {
        isAttached = false;
        for (final AppGroup group : appGroups.values()) {
            group.getAppTiles().clear();
        }
        appGroups.clear();
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unknown call from client: " + method);
    }

    public void setAppActive(String appName, boolean isActive) {
        proxy.call("setAppActive", appName, isActive);
    }

    /**
     * Represents one tile in the AppLauncher.
     */
    public static class AppTile implements Serializable {

        private final String name;

        private final String caption;

        private final String icon;

        public AppTile(String name, String caption, String icon) {
            this.name = name;
            this.caption = caption;
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public String getCaption() {
            return caption;
        }

        public String getIcon() {
            return icon;
        }
    }

    /**
     * Represents a group of tiles in the AppLauncher.
     */
    public static class AppGroup implements Serializable {

        private transient List<AppTile> appTiles = new ArrayList<AppTile>();

        private final String name;

        private final String caption;

        private final String backgroundColor;

        private final boolean isPermanent;

        private final boolean clientGroup;

        public AppGroup(String name, String caption, String backgroundColor, boolean isPermanent, boolean clientGroup) {
            this.name = name;
            this.caption = caption;
            this.backgroundColor = backgroundColor;
            this.isPermanent = isPermanent;
            this.clientGroup = clientGroup;
        }

        public void addAppTile(final AppTile tile) {
            appTiles.add(tile);
        }

        public String getName() {
            return name;
        }

        public String getCaption() {
            return caption;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public List<AppTile> getAppTiles() {
            return appTiles;
        }

        public boolean isPermanent() {
            return isPermanent;
        }

        public boolean isClientGroup() {
            return clientGroup;
        }
    }
}
