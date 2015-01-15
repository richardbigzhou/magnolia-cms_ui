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

import info.magnolia.ui.vaadin.gwt.client.applauncher.connector.AppLauncherConnector;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

/**
 * The temporary app group.
 */
public class VTemporaryAppTileGroup extends VAppTileGroup {

    private static final int OPEN_STATE_HEIGHT_PX = 80;
    private static final int VISIBILITY_TOGGLE_SPEED = 200;

    private JQueryCallback layoutRequestCallback = new JQueryCallback() {
        @Override
        public void execute(JQueryWrapper query) {
            AppLauncherConnector connector = AppLauncherUtil.getConnector();
            connector.getLayoutManager().setNeedsMeasure(connector);
            connector.getLayoutManager().layoutNow();
        }
    };

    public VTemporaryAppTileGroup(String color) {
        super(color);
        construct();
    }

    @Override
    protected void construct() {
        addStyleName("temporary");
        closeSection();
    }

    public void closeSection() {
        final AnimationSettings settings = new AnimationSettings();
        settings.setProperty("height", 0);
        settings.addCallback(layoutRequestCallback);

        JQueryWrapper.select(this).animate(VISIBILITY_TOGGLE_SPEED, settings);
    }

    public void showSection() {
        int iRows = 1 + (getChildren().size() - 1) / 9;

        final AnimationSettings settings = new AnimationSettings();
        settings.setProperty("height", OPEN_STATE_HEIGHT_PX * iRows);
        settings.addCallback(layoutRequestCallback);

        JQueryWrapper.select(this).animate(VISIBILITY_TOGGLE_SPEED, settings);
    }

}
