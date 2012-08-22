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
package info.magnolia.ui.app.pages;


import info.magnolia.ui.admincentral.app.content.ContentApp;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.app.pages.editor.PagesEditorSubApp;
import info.magnolia.ui.app.pages.main.PagesMainSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

/**
 * Pages app.
 */
public class PagesApp extends ContentApp {

    public static final String EDITOR_TOKEN = "editor";
    public static final String PREVIEW_TOKEN = "preview";
    public static final String PREVIEW_FULL_TOKEN = "previewfull";

    private AppContext appContext;

    @Inject
    public PagesApp(AppContext context, DialogPresenterFactory dialogPresenterFactory) {
        super(dialogPresenterFactory);
        this.appContext = context;
    }

    @Override
    public void start(Location location) {

        Location mainLocation;
        String selectedItemPath = getSelectedItemPath(location);
        if (selectedItemPath != null) {
            mainLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, appContext.getName(), "main:" + selectedItemPath);
        } else {
            mainLocation = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, appContext.getName(), "main");
        }

        appContext.openSubApp("main", PagesMainSubApp.class, mainLocation, "main");

        // TODO tmattsson - we should start an editor as well if the location is for an editor
    }

    @Override
    public void locationChanged(Location location) {

        List<String> pathParams = parsePathParamsFromToken(location);
        if (pathParams.size() < 2) {
            return;
        }

        final String subAppName = pathParams.get(0);
        final String subAppId = subAppName + ";" + pathParams.get(1);

        if (EDITOR_TOKEN.equals(subAppName)) {
            appContext.openSubApp(PagesApp.EDITOR_TOKEN, PagesEditorSubApp.class, location, subAppId);
        }
    }

    private List<String> parsePathParamsFromToken(Location location) {
        String token = ((DefaultLocation) location).getToken();
        return new ArrayList<String>(Arrays.asList(token.split(";")));
    }

    private String getSelectedItemPath(Location location) {
        DefaultLocation defaultLocation = (DefaultLocation) location;
        if (defaultLocation.getToken().startsWith("main:")) {
            return StringUtils.removeStart(defaultLocation.getToken(), "main:");
        }
        return null;
    }

    @Override
    public void stop() {
    }
}
