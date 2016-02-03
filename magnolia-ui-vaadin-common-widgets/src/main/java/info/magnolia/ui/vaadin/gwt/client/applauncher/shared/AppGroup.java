/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.applauncher.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of tiles in the AppLauncher.
 */
public class AppGroup implements Serializable {

    private List<AppTile> appTiles = new ArrayList<AppTile>();

    private String name;

    private String caption;

    private String backgroundColor;

    private boolean isPermanent;

    private boolean clientGroup;

    public AppGroup() {
    }

    public AppGroup(String name, String caption, String backgroundColor, boolean isPermanent, boolean clientGroup) {
        this.name = name;
        this.caption = caption;
        this.backgroundColor = backgroundColor;
        this.isPermanent = isPermanent;
        this.clientGroup = clientGroup;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void addAppTile(final AppTile tile) {
        appTiles.add(tile);
    }

    public void setName(String name) {
        this.name = name;
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

    public void setAppTiles(List<AppTile> appTiles) {
        this.appTiles = appTiles;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public boolean isClientGroup() {
        return clientGroup;
    }

    public void setPermanent(boolean isPermanent) {
        this.isPermanent = isPermanent;
    }

    public void setClientGroup(boolean clientGroup) {
        this.clientGroup = clientGroup;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
