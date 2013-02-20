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
package info.magnolia.ui.app.security;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentSubApp;
import info.magnolia.ui.admincentral.app.content.WorkbenchSubAppView;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.app.SubAppEventBusConfigurer;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Users Sub App (the main one) for the Security App.
 */
public class SecurityUsersSubApp extends ContentSubApp {

    private static final Logger log = LoggerFactory.getLogger(SecurityUsersSubApp.class);

    @Inject
    public SecurityUsersSubApp(final SubAppContext subAppContext, WorkbenchSubAppView view, ContentWorkbenchPresenter workbench, @Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) EventBus subAppEventBus) {
        super(subAppContext, view, workbench, subAppEventBus);
    }

    @Override
    public void updateActionbar(ActionbarPresenter actionbar) {
        String selectedItemId = getWorkbench().getSelectedItemId();
        // users can be created only under /admin or /system paths in users workspace
        if ("/admin".equals(selectedItemId) || "/system".equals(selectedItemId)) {
            actionbar.enableGroup("addActions");
            actionbar.disableGroup("editActions");
        } else {
            actionbar.enableGroup("editActions");
            actionbar.disableGroup("addActions");
        }
    }
}
