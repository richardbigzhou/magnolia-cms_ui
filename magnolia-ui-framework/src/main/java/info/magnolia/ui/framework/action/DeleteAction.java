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

import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a node from the repository usin the delete command.
 * 
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class DeleteAction<D extends CommandActionDefinition> extends AbstractCommandAction<D> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected final UiContext uiContext;
    protected final Item jcrItem;
    protected final EventBus eventBus;
    private String itemIdOfChangedItem;

    @Inject
    public DeleteAction(D definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, item, commandsManager, uiContext);
        this.jcrItem = item.getJcrItem();
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        try {
            itemIdOfChangedItem = JcrItemUtil.getItemId(jcrItem.getParent());
        } catch (RepositoryException e) {
            log.error("Could not execute repository operation.", e);
            onError(e);
        }
    }

    @Override
    public final void execute() throws ActionExecutionException {
        // Warn if we try to remove the root node
        try {
            if (jcrItem.isNode() && jcrItem.getDepth() == 0) {
                uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "Root node can't be deleted.");
                return;
            }

            // Get the related Command Label
            if (StringUtils.isBlank(getDefinition().getCommand())) {
                throw new ActionExecutionException(" No Command defined for this action ");
            }

            // Get the related Confirmation Message
            String confirmationHeader = createConfirmationHeader();
            String confirmationMessage = createConfirmationMessage();

            // Open the Confirmation Dialog
            uiContext.openConfirmation(
                    MessageStyleTypeEnum.WARNING, confirmationHeader, confirmationMessage, "Yes, Delete", "No", true,
                    new ConfirmationCallback() {
                        @Override
                        public void onSuccess() {
                            DeleteAction.this.executeAfterConfirmation();
                        }

                        @Override
                        public void onCancel() {
                            // nothing
                        }
                    });

        } catch (RepositoryException re) {
            log.error("Could not execute repository operation.", re);
            onError(re);
        }
    }

    /**
     * Execute after confirmation.<br>
     * Call the appropriate command.
     */
    protected void executeAfterConfirmation() {
        try {
            // Execute command
            super.execute();
            // Propagate event
            eventBus.fireEvent(new ContentChangedEvent(jcrItem.getSession().getWorkspace().getName(), itemIdOfChangedItem));
        } catch (Exception e) {
            log.error("Could not execute command operation.", e);
            onError(e);
        }
    }

    protected void setItemIdOfChangedItem(String itemIdOfChangedItem) {
        this.itemIdOfChangedItem = itemIdOfChangedItem;
    }

    /**
     * Create the Header of the confirmation message.
     */
    protected String createConfirmationHeader() throws RepositoryException {
        StringBuffer label = new StringBuffer();
        label.append("Delete ");
        if (jcrItem.isNode()) {
            label.append("Node ");
        } else {
            label.append("Property ");
        }
        label.append("?");
        return label.toString();
    }

    /**
     * Create the Body of the confirmation Message.
     */
    protected String createConfirmationMessage() throws RepositoryException {
        StringBuffer label = new StringBuffer();
        label.append("The node<br>");
        label.append(jcrItem.getPath());
        label.append("<br>will be deleted immediately.<br>");
        label.append("Are you sure ?");
        return label.toString();
    }

}
