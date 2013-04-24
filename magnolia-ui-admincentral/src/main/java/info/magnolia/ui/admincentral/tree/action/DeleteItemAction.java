/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a node from the repository.
 */
public class DeleteItemAction extends ActionBase<DeleteItemActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SubAppContext subAppContext;

    private String path;
    private final JcrItemAdapter item;
    private final EventBus eventBus;

    public DeleteItemAction(DeleteItemActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, SubAppContext subAppContext) {
        super(definition);
        this.item = item;
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
    }

    protected String getItemPath() throws RepositoryException {
        return path;
    }

    @Override
    public void execute() throws ActionExecutionException {

        // avoid JCR logging long stacktraces about root not being removable.
        if ("/".equals(item.getPath())) {
            path = item.getPath();
            return;
        }

        subAppContext.openConfirmation(
                MessageStyleTypeEnum.WARNING, "Do you really want to delete this item?", "This action can't be undone.", "Yes, Delete", "No", true,
                new ConfirmationCallback() {
                    @Override
                    public void onSuccess() {
                        DeleteItemAction.this.executeAfterConfirmation();
                    }

                    @Override
                    public void onCancel() {
                        // nothing
                    }
                });

    }

    protected void executeAfterConfirmation() {

        try {
            path = item.getJcrItem().getParent().getPath();
            Session session = item.getJcrItem().getSession();
            item.getJcrItem().remove();
            session.save();
            eventBus.fireEvent(new ContentChangedEvent(session.getWorkspace().getName(), getItemPath()));

            // Show notification
            subAppContext.openNotification(MessageStyleTypeEnum.INFO, true, "Item deleted.");
        } catch (RepositoryException e) {
            log.error("Could not execute repository operation.", e);
        }
    }
}
