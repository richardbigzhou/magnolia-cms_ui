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
package info.magnolia.ui.model.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.SessionUtil;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base action supporting execution of commands.
 *
 * @param <D> {@link CommandActionDefinition}.
 */
public class CommandActionBase<D extends CommandActionDefinition> extends ActionBase<D> {

    private static final Logger log = LoggerFactory.getLogger(CommandActionBase.class);

    private CommandsManager commandsManager;

    private Map<String, Object> params;

    @Inject
    public CommandActionBase(final D definition, final Node node, final CommandsManager commandsManager) {
        super(definition);
        this.commandsManager = commandsManager;
        this.params = buildParams(node);
    }

    /**
     * Builds a map of parameters which will be passed to the current command for execution.
     * Called by the constructor. Default implementation returns a map containing the parameters defined at {@link CommandActionDefinition#getParams()}.
     * It also adds the following parameters with values retrieved from the passed node.
     * <ul>
     * <li>Context.ATTRIBUTE_REPOSITORY = current node's workspace name
     * <li>Context.ATTRIBUTE_UUID = current node's identifier
     * <li>Context.ATTRIBUTE_PATH = current node's path
     * </ul>
     */
    protected Map<String, Object> buildParams(final Node node) {
        Map<String, Object> params = getDefinition().getParams() == null ? new HashMap<String, Object>() : getDefinition().getParams();
        try {
            final String path = node.getPath();
            final String workspace = node.getSession().getWorkspace().getName();
            final String identifier = SessionUtil.getNode(workspace, path).getIdentifier();

            params.put(Context.ATTRIBUTE_REPOSITORY, workspace);
            // really only the uuid should be used to identify a piece of content and nothing else
            params.put(Context.ATTRIBUTE_UUID, identifier);
            // retrieve content again using uuid and system context to get unaltered path.
            final String realPath = MgnlContext.getSystemContext().getJCRSession(workspace).getNodeByIdentifier(identifier).getPath();
            params.put(Context.ATTRIBUTE_PATH, realPath);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        return params;

    }

    /**
     * @return the map of parameters to be used for command execution.
     * @see CommandActionBase#buildParams(Node).
     */
    public final Map<String, Object> getParams() {
        return params;
    }

    public final CommandsManager getCommandsManager() {
        return commandsManager;
    }

    /**
     * Handles the retrieval of the {@link Command} instance defined in the {@link CommandActionDefinition} associated with this action and then
     * performs the actual command execution. First calls {@link #onPreExecute()} to perform additional operations subclasses might need before running the command.
     * 
     * @throws ActionExecutionException if no command is found or if command execution throws an exception.
     */
    @Override
    public final void execute() throws ActionExecutionException {

        final String commandName = getDefinition().getCommand();
        final String catalog = getDefinition().getCatalog();
        final Command command = getCommandsManager().getCommand(catalog, commandName);

        if (command == null) {
            throw new ActionExecutionException(String.format("Could not find command [%s] in any catalog", commandName));
        }

        long start = System.currentTimeMillis();
        try {
            log.debug("Executing command [{}] from catalog [{}] with the following parameters [{}]...", new Object[] { commandName, catalog, getParams() });
            commandsManager.executeCommand(command, getParams());
            log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start);
            throw new ActionExecutionException(e);
        }
    }

    /**
     * Called by {@link #execute()} before actually executing a command. Subclasses can override this method to perform some operations
     * i.e. setting additional parameters, available only at runtime, in the context used for command execution. Default implementation is empty.
     * <p>
     * Usage sample
     * 
     * <pre>
     * public void onPreExecute() {
     *     getParams().put(Context.ATTRIBUTE_RECURSIVE, getDefinition().isRecursive());
     * }
     * </pre>
     */
    protected void onPreExecute() {
    }
}
