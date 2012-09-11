/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.app.pages.main;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.instantpreview.InstantPreviewDispatcher;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;


/**
 * PagesMainSubApp.
 */
public class PagesMainSubApp extends AbstractSubApp implements PagesMainView.Listener {

    private static final String CAPTION = "Pages";

    private final PagesMainView view;

    private final ContentWorkbenchPresenter workbench;

    private final InstantPreviewDispatcher dispatcher;

    @Inject
    public PagesMainSubApp(final AppContext appContext, PagesMainView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus, InstantPreviewDispatcher dispatcher) {
        this.view = view;
        this.dispatcher = dispatcher;
        this.view.setListener(this);
        this.workbench = workbench;
        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                appContext.setSubAppLocation(PagesMainSubApp.this, createLocation(event.getPath()));
                updateActions();
            }
        });
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public View start(Location location) {
        view.setWorkbenchView(workbench.start());
        String selectedItemPath = getSelectedItemPath(location);
        if (selectedItemPath != null) {
            workbench.selectPath(selectedItemPath);
        }
        updateActions();
        return view;
    }

    private void updateActions() {
        ActionbarPresenter actionbar = workbench.getActionbarPresenter();

        // actions currently always disabled
        actionbar.disable("move");
        actionbar.disable("duplicate");

        // actions disabled based on selection
        if (workbench.getSelectedItemId() == null || workbench.getSelectedItemId().equals("/")) {
            actionbar.disable("delete");
            actionbar.disable("preview");
            actionbar.disable("edit");
            actionbar.disable("editProperties");
            actionbar.disable("export");
        } else {
            actionbar.enable("delete");
            actionbar.enable("preview");
            actionbar.enable("edit");
            actionbar.enable("editProperties");
            actionbar.enable("export");
        }
    }

    @Override
    public void locationChanged(Location location) {
        String selectedItemPath = getSelectedItemPath(location);
        if (selectedItemPath != null) {
            workbench.selectPath(selectedItemPath);
        }
        updateActions();
    }

    @Override
    public void share() {
        dispatcher.share();
    }

    @Override
    public void subscribe(String hostId) {
        dispatcher.subscribeTo(hostId);
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
        return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", token);
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
