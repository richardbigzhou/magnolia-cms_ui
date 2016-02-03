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

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

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
    protected void onPreExecute() throws Exception {
        super.onPreExecute();

        if (jcrItem.isNode() && jcrItem.getDepth() == 0) {
            uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "Root node can't be deleted.");
            throw new ActionExecutionException("Root node can't be deleted.");
        }
    }

    @Override
    public final void execute() throws ActionExecutionException {
        super.execute();
    }

    @Override
    protected void onPostExecute() throws Exception {
        // Propagate event
        eventBus.fireEvent(new ContentChangedEvent((String) getParams().get(Context.ATTRIBUTE_REPOSITORY), itemIdOfChangedItem));

        // Show notification
        uiContext.openNotification(MessageStyleTypeEnum.INFO, true, getSuccessMessage());
    }

    /**
     * Execute after confirmation.<br>
     * Call the appropriate command.
     * @deprecated after 5.0.1 use {@link ConfirmationAction}.
     */
    @Deprecated
    protected void executeAfterConfirmation() {

    }

    @Deprecated
    protected void setItemIdOfChangedItem(String itemIdOfChangedItem) {
        this.itemIdOfChangedItem = itemIdOfChangedItem;
    }

    /**
     * Create the Header of the confirmation message.
     * @deprecated after 5.0.1 use {@link ConfirmationAction}.
     */
    @Deprecated
    protected String createConfirmationHeader() throws RepositoryException {
        StringBuffer label = new StringBuffer();
        label.append("Delete ");
        if (jcrItem.isNode()) {
            label.append("Node");
        } else {
            label.append("Property");
        }
        label.append("?");
        return label.toString();
    }

    /**
     * Create the Body of the confirmation Message.
     * @deprecated after 5.0.1 use {@link ConfirmationAction}.
     */
    @Deprecated
    protected String createConfirmationMessage() throws RepositoryException {
        StringBuffer label = new StringBuffer();
        label.append("The ");
        if (jcrItem.isNode()) {
            label.append("node");
        } else {
            label.append("property");
        }
        label.append("<br>");
        label.append(jcrItem.getPath());
        label.append("<br>will be deleted immediately.<br>");
        label.append("Are you sure ?");
        return label.toString();
    }

    /**
     * Create the Body of the confirmation Message.
     */
    @Deprecated
    protected String createSuccessMessage() {
        StringBuffer label = new StringBuffer();
        if (jcrItem.isNode()) {
            label.append("Node");
        } else {
            label.append("Property");
        }
        label.append(" deleted.");
        return label.toString();
    }

    protected String getSuccessMessage() {
        return MessagesUtil.get(getDefinition().getSuccessMessage(), getDefinition().getI18nBasename(),new String[] {(jcrItem.isNode()) ? "Node" : "Property"});
    }
}
