/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The holder of the temporary app group tiles - the ones that expand to show the temporary app group.
 */
public class VTemporaryAppGroupBar extends FlowPanel {

    private final Map<VTemporaryAppGroupBarTile, VTemporaryAppTileGroup> groupMap = new HashMap<VTemporaryAppGroupBarTile, VTemporaryAppTileGroup>();

    private VTemporaryAppTileGroup currentOpenGroup = null;

    public VTemporaryAppGroupBar() {
        super();
        addStyleName("app-list");
        addStyleName("sections");
    }

    public VTemporaryAppTileGroup getCurrentOpenGroup() {
        return currentOpenGroup;
    }

    public void addGroup(String caption, VAppTileGroup group) {
        VTemporaryAppGroupBarTile groupTile = new VTemporaryAppGroupBarTile(caption, group, this);
        groupMap.put(groupTile, (VTemporaryAppTileGroup) group);
        add(groupTile);
    }

    protected void handleTileClick(VTemporaryAppTileGroup group, VTemporaryAppGroupBarTile groupTile) {
        closeCurrentOpenExpander();
        if (group != null) {
            if (currentOpenGroup != group) {
                if (currentOpenGroup != null) {
                    currentOpenGroup.closeSection();
                }
                group.showSection();
                currentOpenGroup = group;
                groupTile.openExpander();
            } else {
                currentOpenGroup.closeSection();
                currentOpenGroup = null;
            }
        }
    }

    protected void closeCurrentOpenExpander() {
        if (currentOpenGroup != null) {
            for (Entry<VTemporaryAppGroupBarTile, VTemporaryAppTileGroup> entry : groupMap.entrySet()) {
                if (currentOpenGroup == entry.getValue()) {
                    entry.getKey().closeExpander();
                    break;
                }
            }
        }
    }

}
