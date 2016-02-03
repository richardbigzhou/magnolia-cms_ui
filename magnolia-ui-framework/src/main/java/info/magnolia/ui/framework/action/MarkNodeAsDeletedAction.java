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
import info.magnolia.commands.impl.MarkNodeAsDeletedCommand;
import info.magnolia.context.Context;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mark node as Deleted Action. This will create a new Node version marked as deleted.
 *
 * @see MarkNodeAsDeletedActionDefinition
 */
public class MarkNodeAsDeletedAction extends DeleteAction<MarkNodeAsDeletedActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public MarkNodeAsDeletedAction(MarkNodeAsDeletedActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, item, commandsManager, eventBus, uiContext);
    }

    /**
     * Create the Header of the confirmation message.
     */
    @Override
    protected String createConfirmationHeader() throws RepositoryException {
        return "Delete Node ?";
    }

    /**
     * Create the Body of the confirmation Message.
     */
    @Override
    protected String createConfirmationMessage() throws RepositoryException {
        StringBuffer label = new StringBuffer();
        label.append("The node<br>");
        label.append(getItem().getJcrItem().getPath());
        label.append("<br>and its sub nodes will be marked for deletion.<br>");
        label.append("Are you sure ?");
        return label.toString();
    }

    /**
     * Create the Body of the confirmation Message.
     */
    @Override
    protected String createSuccessMessage() {
        return "Node and its sub nodes marked for deletion.";
    }

    /**
     * Override the buildParams(..).<br>
     * The relented command is waiting the following values: <br>
     * Context.ATTRIBUTE_UUID : Parent Node Identifier instead of the Node identifier set by super.buildParams()<br>
     * Context.ATTRIBUTE_PATH : Parent Node Path instead of the Node Path set by super.buildParams()<br>
     */
    @Override
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = getDefinition().getParams() == null ? new HashMap<String, Object>() : getDefinition().getParams();
        try {
            final String path = jcrItem.getParent().getPath();
            final String workspace = jcrItem.getSession().getWorkspace().getName();
            final String identifier = jcrItem.getParent().getIdentifier();

            params.put(Context.ATTRIBUTE_REPOSITORY, workspace);
            // really only the identifier should be used to identify a piece of content and nothing else
            params.put(Context.ATTRIBUTE_UUID, identifier);
            params.put(Context.ATTRIBUTE_PATH, path);

            params.put(MarkNodeAsDeletedCommand.DELETED_NODE_PROP_NAME, jcrItem.getName());

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return params;
    }

}
