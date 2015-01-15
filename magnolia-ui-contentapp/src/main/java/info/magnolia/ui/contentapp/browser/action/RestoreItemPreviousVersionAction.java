/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.framework.action.AbstractCommandAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.jcr.Item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restores the previous version of an item and its descendants using a command.
 * 
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class RestoreItemPreviousVersionAction<D extends CommandActionDefinition> extends AbstractCommandAction<RestoreItemPreviousVersionActionDefinition> {

    protected static final Logger log = LoggerFactory.getLogger(RestoreItemPreviousVersionAction.class);

    private final EventBus eventBus;
    private final SimpleTranslator i18n;

    public RestoreItemPreviousVersionAction(RestoreItemPreviousVersionActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, uiContext, i18n);
        if (!item.isNode()) {
            throw new IllegalArgumentException("Item must be a node");
        }
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    public RestoreItemPreviousVersionAction(RestoreItemPreviousVersionActionDefinition definition, List<JcrItemAdapter> items, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, items, commandsManager, uiContext, i18n);
        if (!items.get(0).isNode()) {
            throw new IllegalArgumentException("Item must be a node");
        }
        this.eventBus = eventBus;
        this.i18n = i18n;
    }

    @Override
    protected String getSuccessMessage() {
        return i18n.translate(getDefinition().getSuccessMessage(), getItems().size());
    }

    @Override
    protected String getFailureMessage() {
        return getDefinition().getFailureMessage();
    }

    protected SimpleTranslator getI18n() {
        return i18n;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    @Override
    protected void onPostExecute() throws Exception {
        for (JcrItemAdapter item : getItems()) {
            eventBus.fireEvent(new ContentChangedEvent(item.getItemId()));
        }
    }

    @Override
    protected Map<String, Object> buildParams(Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.put("parentNodeTypeOnly", getDefinition().isParentNodeTypeOnly());
        return params;
    }
}