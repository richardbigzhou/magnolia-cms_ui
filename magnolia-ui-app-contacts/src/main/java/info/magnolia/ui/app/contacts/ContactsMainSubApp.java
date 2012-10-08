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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sub app for the main tab in the contacts app.
 */
public class ContactsMainSubApp extends AbstractSubApp {

    private static final Logger log = LoggerFactory.getLogger(ContactsMainSubApp.class);

    private final ContactsView view;

    private final ContentWorkbenchPresenter workbench;
    
    @Inject
    public ContactsMainSubApp(final AppContext appContext, ContactsView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus) {
        this.view = view;
        this.workbench = workbench;
        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                appContext.setSubAppLocation(ContactsMainSubApp.this, createLocation(event.getPath()));
                updateActions();
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
        updateActions();
        return view;
    }

    private void updateActions() {
        ActionbarPresenter actionbar = workbench.getActionbarPresenter();
        String selectedItemId = workbench.getSelectedItemId();

        // actions disabled based on selection
        if (selectedItemId == null || selectedItemId.equals("/")) {
            actionbar.disable("edit");
            actionbar.disable("delete");
            actionbar.showSection("contactsActions");
            actionbar.hideSection("folderActions");
        } else {
            actionbar.enable("edit");
            actionbar.enable("delete");

            try {

                Session session = MgnlContext.getJCRSession("contacts");

                Node node = session.getNode(selectedItemId);
                if (NodeUtil.isNodeType(node, MgnlNodeType.NT_FOLDER)) {
                    actionbar.hideSection("contactsActions");
                    actionbar.showSection("folderActions");
                } else {
                    actionbar.showSection("contactsActions");
                    actionbar.hideSection("folderActions");
                }
            } catch (RepositoryException e) {
                log.warn("Unable to determine node type of {}", selectedItemId);
            }
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
