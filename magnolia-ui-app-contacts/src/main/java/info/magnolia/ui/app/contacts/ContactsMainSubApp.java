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
package info.magnolia.ui.app.contacts;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;

/**
 * Sub app for the main tab in the contacts app.
 */
public class ContactsMainSubApp extends AbstractSubApp {

    private ContactsView view;
    private ContentWorkbenchPresenter workbench;

    @Inject
    public ContactsMainSubApp(final AppContext appContext, ContactsView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus) {
        this.view = view;
        this.workbench = workbench;
        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                appContext.setSubAppLocation(ContactsMainSubApp.this, createLocation(event.getPath()));
            }
        });
    }

    @Override
    public String getCaption() {
        return "Contacts";
    }

    @Override
    public View start(Location location) {
        view.setWorkbenchView(workbench.start());
        String selectedItemPath = getSelectedItemPath(location);
        if (selectedItemPath != null) {
            workbench.selectPath(selectedItemPath);
        }
        return view;
    }

    @Override
    public void locationChanged(Location location) {
        String selectedItemPath = getSelectedItemPath(location);
        if (selectedItemPath != null) {
            workbench.selectPath(selectedItemPath);
        }
    }

    // Location token handling, format is main:<selectedItemPath>

    public static boolean supportsLocation(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 1 && parts.get(0).equals("main");
    }

    public static DefaultLocation createLocation(String selectedItemPath) {
        String token = "main";
        if (StringUtils.isNotEmpty(selectedItemPath)) {
            token = token + ":" + selectedItemPath;
        }
        return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "contacts", token);
    }

    public static String getSubAppId(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.get(0);
    }

    public static String getSelectedItemPath(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 2 ? parts.get(1) : null;
    }

    private static List<String> parseLocationToken(Location location) {

        ArrayList<String> parts = new ArrayList<String>();

        DefaultLocation l = (DefaultLocation) location;
        String token = l.getToken();

        // "main"
        int i = token.indexOf(':');
        if (i == -1) {
            if (!token.equals("main")) {
                return new ArrayList<String>();
            }
            parts.add(token);
            return parts;
        }

        String subAppName = token.substring(0, i);
        if (!subAppName.equals("main")) {
            return new ArrayList<String>();
        }
        parts.add(subAppName);
        token = token.substring(i + 1);

        // selectedItemPath
        if (token.length() > 0) {
            parts.add(token);
        }

        return parts;
    }
}
