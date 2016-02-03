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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;

/**
 * The container that groups semantically similar apps.
 */
public abstract class VAppTileGroup extends ComplexPanel {

    private String color;

    private boolean clientGroup;

    private final Map<String, AppTileWidget> appTileMap = new HashMap<String, AppTileWidget>();

    public VAppTileGroup(String color) {
        super();
        this.color = color;
        setElement(DOM.createElement("section"));

        getElement().getStyle().setDisplay(Display.BLOCK);
        addStyleName("app-list");
        addStyleName("section");
        addStyleName("clearfix");
    }

    protected abstract void construct();

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public boolean isClientGroup() {
        return clientGroup;
    }

    public void setClientGroup(boolean clientGroup) {
        this.clientGroup = clientGroup;
    }

    public void addAppTile(final AppTileWidget tile) {
        add(tile, getElement());
        appTileMap.put(tile.getName(), tile);
    }

    public boolean hasApp(String appName) {
        return appTileMap.containsKey(appName);
    }

    public AppTileWidget getAppTile(String appName) {
        return appTileMap.get(appName);
    }
}
