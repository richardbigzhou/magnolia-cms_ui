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
import info.magnolia.ui.admincentral.app.content.AbstractContentSubApp;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * Sub app for the main tab in the contacts app.
 */
public class ContactsMainSubApp extends AbstractContentSubApp {

    private static final Logger log = LoggerFactory.getLogger(ContactsMainSubApp.class);

    @Inject
    public ContactsMainSubApp(final SubAppContext subAppContext, ContactsView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus) {
        super(subAppContext, view, workbench, subAppEventBus);
    }

    @Override
    public String getCaption() {
        return "Contacts";
    }


    @Override
    public void updateActionbar(final ActionbarPresenter actionbar) {
        super.updateActionbar(actionbar);
        String selectedItemId = getWorkbench().getSelectedItemId();

        // actions disabled based on selection
        if (selectedItemId == null || selectedItemId.equals("/")) {
            actionbar.showSection("contactsActions");
            actionbar.hideSection("folderActions");
        } else {

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
}
