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
package info.magnolia.ui.app.contacts.main;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentSubApp;
import info.magnolia.ui.admincentral.app.content.WorkbenchSubAppView;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBusConfigurer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sub app for the main tab in the contacts app.
 */
public class ContactsMainSubApp extends ContentSubApp {

    private static final Logger log = LoggerFactory.getLogger(ContactsMainSubApp.class);

    @Inject
    public ContactsMainSubApp(final SubAppContext subAppContext, WorkbenchSubAppView view, ContentWorkbenchPresenter workbench, @Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) EventBus subAppEventBus) {
        super(subAppContext, view, workbench, subAppEventBus);
    }

    @Override
    public void updateActionbar(final ActionbarPresenter actionbar) {
        String selectedItemId = getWorkbench().getSelectedItemId();

        // actions disabled based on selection
        if (selectedItemId == null || "/".equals(selectedItemId)) {
            rootNodeActions(actionbar);
        } else {
            try {
                final Session session = MgnlContext.getJCRSession("contacts");
                final Node node = session.getNode(selectedItemId);

                if (NodeUtil.isNodeType(node, NodeTypes.Folder.NAME)) {
                    folderActions(actionbar);
                } else {

                    contactActions(actionbar);
                }
            } catch (RepositoryException e) {
                log.warn("Unable to determine node type of {}", selectedItemId);
            }
        }
    }

    /**
     * contact selected.
     * <p>
     * - can only edit/delete contact
     */
    private void contactActions(final ActionbarPresenter actionbar) {
        actionbar.hideSection("folderActions");
        actionbar.showSection("contactsActions");
        actionbar.disableGroup("addActions");
        actionbar.enableGroup("editActions");
    }

    /**
     * folder selected.
     * <p>
     * - can create/edit/delete folder or create contact
     */
    private void folderActions(final ActionbarPresenter actionbar) {
        actionbar.showSection("contactsActions");
        actionbar.enableGroup("addActions", "contactsActions");
        actionbar.disableGroup("editActions", "contactsActions");
        actionbar.showSection("folderActions");
        actionbar.enableGroup("addActions", "folderActions");
        actionbar.enableGroup("editActions", "folderActions");
    }

    /**
     * initial state (root selected).
     * <p>
     * - can create folder or contact
     */
    private void rootNodeActions(final ActionbarPresenter actionbar) {
        actionbar.showSection("contactsActions");
        actionbar.enableGroup("addActions", "contactsActions");
        actionbar.disableGroup("editActions", "contactsActions");
        actionbar.showSection("folderActions");
        actionbar.enableGroup("addActions", "folderActions");
        actionbar.disableGroup("editActions", "folderActions");
    }
}
