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
package info.magnolia.ui.framework.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a node or property from the repository.
 *
 * @see DeleteItemActionDefinition
 */
public class DeleteItemAction extends AbstractMultiItemAction<DeleteItemActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UiContext uiContext;
    private final EventBus eventBus;

    public DeleteItemAction(DeleteItemActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, Collections.singletonList(item), uiContext);
        this.uiContext = uiContext;
        this.eventBus = eventBus;
    }

    public DeleteItemAction(DeleteItemActionDefinition definition, List<JcrItemAdapter> items, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, items, uiContext);
        this.uiContext = uiContext;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {

        uiContext.openConfirmation(
                MessageStyleTypeEnum.WARNING, getConfirmationQuestion(), "This action can't be undone.", "Yes, Delete", "No", true,     //TODO-TRANSLATE
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

    private String getConfirmationQuestion() {
        if (getItems().size() == 1) {
            return "Do you really want to delete this item?";       //TODO-TRANSLATE
        }
        return "Do you really want to delete these " + getItems().size() + " items?";       //TODO-TRANSLATE
    }

    protected void executeAfterConfirmation() {
        try {
            super.execute();
        } catch (ActionExecutionException e) {
            log.error("Problem occured during deleting items.", e);      //TODO-TRANSLATE
        }
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws Exception {
        try {
            final Item jcrItem = item.getJcrItem();
            if (jcrItem.getDepth() == 0) {
                // cannot delete root node
                throw new IllegalArgumentException("Cannot delete root node.");          //TODO-TRANSLATE-EXCEPTION
            }
            String itemIdOfChangedItem = JcrItemUtil.getItemId(jcrItem.getParent());
            Session session = jcrItem.getSession();
            jcrItem.remove();
            session.save();

            eventBus.fireEvent(new ContentChangedEvent(session.getWorkspace().getName(), itemIdOfChangedItem));
        } catch (PathNotFoundException e) {
            // ignore - the item has been probably already deleted (with a parent node)
        }
    }

    @Override
    protected String getSuccessMessage() {
        return getItems().size() == 1 ? "Item deleted." : getItems().size() + " items deleted.";       //TODO-TRANSLATE
    }

    @Override
    protected String getFailureMessage() {
        return "Failed to delete " + getFailedItems().size() + " of " + getItems().size() + " items: ";            //TODO-TRANSLATE
    }
}
