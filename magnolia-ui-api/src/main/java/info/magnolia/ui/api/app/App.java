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
package info.magnolia.ui.api.app;

import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.Location;

/**
 * Main interface for apps. Apps are started and managed by the {@link AppController}.
 *
 * @see AppDescriptor
 * @see AppController
 */
public interface App {

    /**
     * Called when the app is started. The location passed in is the location that triggered the app to be started.
     *
     * @param location the location that triggered the app to be started
     */
    void start(Location location);

    /**
     * Called when the location changes and the app is already running.
     *
     * @param location the new location
     */
    void locationChanged(Location location);

    /**
     * Called when the user stops the app.
     */
    void stop();

    AppView getView();

    /**
     * Open a dialog which enables a user to choose an item from the app.
     *
     * @param overlayLayer The layer over which the opened dialog should be presented and be modal. See Shell, {@link SubAppContext}, {@link AppContext}.
     * @param callback
     */
    void openChooseDialog(UiContext overlayLayer, String selectedId, ChooseDialogCallback callback);

    void openChooseDialog(UiContext uiContext, String targetTreeRootPath, String selectedId, ChooseDialogCallback callback);
}
