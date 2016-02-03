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

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.List;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes a node from the repository using the delete command.
 *
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class DeleteAction<D extends CommandActionDefinition> extends AbstractCommandAction<D> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected final UiContext uiContext;
    protected final Item jcrItem;
    protected final EventBus eventBus;
    private String itemIdOfChangedItem;
    private final SimpleTranslator i18n;

    public DeleteAction(D definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, uiContext, i18n);
        this.jcrItem = item.getJcrItem();
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.i18n = i18n;

        try {
            itemIdOfChangedItem = JcrItemUtil.getItemId(jcrItem.getParent());
        } catch (RepositoryException e) {
            log.error("Could not execute repository operation.", e);
            onError(e);
        }
    }

    public DeleteAction(D definition, List<JcrItemAdapter> items, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, items, commandsManager, uiContext, i18n);
        this.jcrItem = items.get(0).getJcrItem();
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.i18n = i18n;
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

        if (getCurrentItem().getJcrItem().isNode() && getCurrentItem().getJcrItem().getDepth() == 0) {
            throw new ActionExecutionException("Root node can't be deleted.");
        }
    }

    @Override
    protected void onPostExecute() throws Exception {
        // Propagate event
        eventBus.fireEvent(new ContentChangedEvent((String) getParams().get(Context.ATTRIBUTE_REPOSITORY), itemIdOfChangedItem));
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws ActionExecutionException {
        if (item.isNode()) {
            super.executeOnItem(item);
        } else {
            try {
                onPreExecute();
                Property property = (Property) item.getJcrItem();
                property.remove();
                property.getSession().save();
                onPostExecute();
            } catch (Exception e) {
                onError(e);
                throw new ActionExecutionException(e);
            }
        }
    }

    @Override
    protected String getSuccessMessage() {
        return i18n.translate(getDefinition().getSuccessMessage(), "" + getItems().size());
    }

    @Override
    protected String getFailureMessage() {
        return getDefinition().getFailureMessage();
    }

    protected SimpleTranslator getI18n() {
        return i18n;
    }

}
