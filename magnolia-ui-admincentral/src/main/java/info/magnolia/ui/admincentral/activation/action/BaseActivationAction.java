/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.admincentral.activation.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Base activation action supporting execution of commands.
 * 
 * @param <D> BaseActivationActionDefinition.
 * 
 */
public abstract class BaseActivationAction<D extends BaseActivationActionDefinition> extends ActionBase<ActionDefinition> {

    private CommandsManager commandsManager;

    private Map<String, Object> params;

    @Inject
    public BaseActivationAction(final D definition, final Node node) {
        super(definition);
        this.commandsManager = Components.getComponent(CommandsManager.class);
        this.params = buildParams(node);
    }

    /**
     * Builds a map of parameters which will be passed to the (de-)activation command to perform its execution. By default it consists of three objects:
     * 
     * <ul>
     * <li>Context.ATTRIBUTE_REPOSITORY = current node's workspace name
     * <li>Context.ATTRIBUTE_UUID = current node's identifier
     * <li>Context.ATTRIBUTE_PATH = current node's path
     * </ul>
     * 
     * Called by the constructor.
     */
    protected Map<String, Object> buildParams(final Node node) {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            final String path = node.getPath();
            final String workspace = node.getSession().getWorkspace().getName();
            params.put(Context.ATTRIBUTE_REPOSITORY, workspace);

            if (path != null) {

                final String identifier = SessionUtil.getNode(workspace, path).getIdentifier();
                // really only the uuid should be used to identify a piece of content and nothing else
                params.put(Context.ATTRIBUTE_UUID, identifier);
                // retrieve content again using uuid and system context to get unaltered path.
                final String realPath = MgnlContext.getSystemContext().getJCRSession(workspace).getNodeByIdentifier(identifier).getPath();
                params.put(Context.ATTRIBUTE_PATH, realPath);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return params;
    }

    /**
     * @return the map of parameters to be used for command execution.
     * @see BaseActivationAction#buildParams(Node).
     */
    protected final Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * @return an instance of {@link CommandsManager} to be used for action execution. Typically the {@link #execute()} method will do something like
     * <p>
     * {@code
     * getCommandsManager().executeCommand("someCommand", getParams());
     * }
     */
    protected final CommandsManager getCommandsManager() {
        return commandsManager;
    }

}
