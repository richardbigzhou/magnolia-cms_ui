/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.framework.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a node or property from the repository.
 *
 * @see DeleteItemActionDefinition
 */
public class DeleteItemAction extends AbstractAction<DeleteItemActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UiContext uiContext;
    private final JcrItemAdapter item;
    private final EventBus eventBus;

    public DeleteItemAction(DeleteItemActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition);
        this.item = item;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {

        try {
            // The root node cannot be deleted
            if (item.getJcrItem().getDepth() == 0) {
                return;
            }
        } catch (RepositoryException e) {
            throw new ActionExecutionException("Problem accessing JCR", e);
        }

        uiContext.openConfirmation(
                MessageStyleTypeEnum.WARNING, "Do you really want to delete this item?", "This action can't be undone.", "Yes, Delete", "No", true,
                new ConfirmationCallback() {

                    @Override
                    public void onSuccess() {
                        DeleteItemAction.this.executeAfterConfirmation();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    protected void executeAfterConfirmation() {

        try {
            final Item jcrItem = item.getJcrItem();
            String itemIdOfChangedItem = JcrItemUtil.getItemId(jcrItem.getParent());
            Session session = jcrItem.getSession();
            jcrItem.remove();
            session.save();

            eventBus.fireEvent(new ContentChangedEvent(session.getWorkspace().getName(), itemIdOfChangedItem));

            // Show notification
            uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "Item deleted.");

        } catch (RepositoryException e) {
            log.error("Could not execute repository operation", e);
        }
    }
}
