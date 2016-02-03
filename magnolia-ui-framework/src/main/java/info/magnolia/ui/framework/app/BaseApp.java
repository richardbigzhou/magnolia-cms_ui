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
package info.magnolia.ui.framework.app;

import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.Location;

import javax.inject.Inject;

/**
 * Basic app implementation with default behavior suitable for most apps.
 *
 * @see info.magnolia.ui.api.app.App
 */
public class BaseApp implements App {

    protected AppContext appContext;

    private AppView view;

    @Inject
    protected BaseApp(AppContext appContext, AppView view) {
        this.appContext = appContext;
        this.view = view;
        view.setListener(appContext);
    }

    @Override
    public void locationChanged(Location location) {
        appContext.openSubApp(location);
    }

    @Override
    public void start(Location location) {
        view.setAppName(location.getAppName());
        appContext.openSubApp(location);
    }

    protected AppContext getAppContext() {
        return appContext;
    }

    @Override
    public void stop() {
    }

    @Override
    public AppView getView() {
        return view;
    }

    @Override
    public void openChooseDialog(UiContext overlayLayer, String selectedId, ChooseDialogCallback callback) {
    }
}
