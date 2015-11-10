/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.cms.security.User;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.async.AsyncActionExecutor;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Base action supporting execution of commands. Will delegate execution to {@link AsyncActionExecutor} if {@link CommandActionDefinition#asynchronous}
 * is set.
 *
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class AbstractCommandAction<D extends CommandActionDefinition> extends AbstractMultiItemAction<D> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandAction.class);

    public static final String COMMAND_RESULT = "command_result";
    public static final String LONG_RUNNING_ACTION_NOTIFICATION = "ui-framework.abstractcommand.asyncaction.long";
    public static final String PARALLEL_EXECUTION_NOT_ALLOWED_NOTIFICATION = "ui-framework.abstractcommand.parallelExecutionNotAllowed";

    private final CommandsManager commandsManager;
    private final SimpleTranslator i18n;
    private final User user;
    private final Command command;
    private final AsyncActionExecutor asyncExecutor;

    private Map<String, Object> params;
    private String successMessage;
    private String failureMessage;

    public AbstractCommandAction(final D definition, final JcrItemAdapter item, final CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, Lists.newArrayList(item), commandsManager, uiContext, i18n);
    }

    public AbstractCommandAction(final D definition, final List<JcrItemAdapter> items, final CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, items, uiContext);
        this.commandsManager = commandsManager;
        this.i18n = i18n;
        this.user = MgnlContext.getUser();
        // Init Command.
        this.command = commandsManager.getCommand(getDefinition().getCatalog(), getDefinition().getCommand());
        this.asyncExecutor = Components.getComponentProvider().newInstance(AsyncActionExecutor.class, getDefinition(), getUiContext());
    }

    /**
     * Builds a map of parameters which will be passed to the current command
     * for execution. Called by {@link #onPreExecute()}. Default implementation returns
     * a map containing the parameters defined at
     * {@link CommandActionDefinition#getParams()}. It also adds the following
     * parameters with values retrieved from the passed node.
     * <ul>
     * <li>Context.ATTRIBUTE_REPOSITORY = current node's workspace name
     * <li>Context.ATTRIBUTE_UUID = current node's identifier
     * <li>Context.ATTRIBUTE_PATH = current node's path
     * </ul>
     * Subclasses can override this method to add further parameters to the
     * command execution. E.g.
     *
     * <pre>
     * protected Map&lt;String, Object&gt; buildParams(final Node node) {
     *     Map&lt;String, Object&gt; params = super.buildParams(node);
     *     params.put(Context.ATTRIBUTE_RECURSIVE, getDefinition().isRecursive());
     *     return params;
     * }
     * </pre>
     */
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (!getDefinition().getParams().isEmpty()) {
            params.putAll(getDefinition().getParams());
        }
        try {
            final String path = jcrItem.getPath();
            final String workspace = jcrItem.getSession().getWorkspace().getName();
            final String identifier = jcrItem.isNode() ? ((Node) jcrItem).getIdentifier() : jcrItem.getParent().getIdentifier();

            params.put(Context.ATTRIBUTE_REPOSITORY, workspace);
            // really only the identifier should be used to identify a piece of content and nothing else
            params.put(Context.ATTRIBUTE_UUID, identifier);
            params.put(Context.ATTRIBUTE_PATH, path);
            params.put(Context.ATTRIBUTE_REQUESTOR, user.getName());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return params;
    }

    /**
     * @return the <em>immutable</em> map of parameters to be used for command execution.
     * @see AbstractCommandAction#buildParams(javax.jcr.Item)
     */
    public final Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public final CommandsManager getCommandsManager() {
        return commandsManager;
    }

    /**
     * Handles the retrieval of the {@link Command} instance defined in the {@link CommandActionDefinition} associated with this action and then
     * performs the actual command execution.
     *
     * @throws info.magnolia.ui.api.action.ActionExecutionException if no command is found or if command execution throws an exception.
     */
    @Override
    protected void executeOnItem(JcrItemAdapter item) throws ActionExecutionException {
        this.failureMessage = null;
        this.successMessage = null;
        try {
            onPreExecute();
        } catch (Exception e) {
            onError(e);
            log.debug("Command execution failed during pre execution tasks.");
            throw new ActionExecutionException(e);
        }
        if (command == null) {
            throw new ActionExecutionException(String.format("Could not find command [%s] in any catalog", getDefinition().getCommand()));
        }

        long start = System.currentTimeMillis();

        // this was set after scheduling the job.. I don't see, why it can't be set here.. it's used in e.g. in ActivationAction#onPostExecute()
        boolean stopProcessing = false;

        try {
            log.debug("Executing command [{}] from catalog [{}] with the following parameters [{}]...", getDefinition().getCommand(), getDefinition().getCatalog(), getParams());

            if (isInvokeAsynchronously()) {
                try {
                    boolean inBackground = asyncExecutor.execute(item, params);

                    // override the configured successMessage stating that the execution will take longer and is running in background.
                    if (inBackground) {
                        this.successMessage = i18n.translate(LONG_RUNNING_ACTION_NOTIFICATION);
                    }
                }
                catch (AsyncActionExecutor.ParallelExecutionException e) {
                    this.failureMessage = i18n.translate(PARALLEL_EXECUTION_NOT_ALLOWED_NOTIFICATION);
                    stopProcessing = true;
                }
            } else {
                stopProcessing = commandsManager.executeCommand(command, getParams());
            }
            MgnlContext.getInstance().setAttribute(COMMAND_RESULT, stopProcessing, Context.LOCAL_SCOPE);

            onPostExecute();
            log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
        } catch (Exception e) {
            onError(e);
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start);
            log.debug(e.getMessage(), e);
            throw new ActionExecutionException(e);
        }
    }

    protected boolean isInvokeAsynchronously() {
        return getDefinition().isAsynchronous();
    }

    /**
     * Pre Command Execution. Class that implement CommansActionBase should use
     * this in order to perform pre Command Tasks.
     * When overriding make sure to call super to build the parameter map.
     */
    protected void onPreExecute() throws Exception {
        this.params = buildParams(getCurrentItem().getJcrItem());
    }

    /**
     * Post Command Execution. Class that implement CommansActionBase should use
     * this in order to perform post Command Tasks.
     */
    protected void onPostExecute() throws Exception {
        // Sub Class can override this method.
    }

    /**
     * Class that implement CommansActionBase should use
     * this in order to perform tasks or notification in case of error.
     */
    protected void onError(Exception e) {
        String message = i18n.translate("ui-framework.abstractcommand.executionfailure");
        getUiContext().openNotification(MessageStyleTypeEnum.ERROR, true, message);
    }

    /**
     * @return current command.
     */
    protected Command getCommand() {
        return this.command;
    }

    @Override
    protected String getSuccessMessage() {
        // by default, we expect the command-based actions to be limited to single-item, but we still extend MultiAction to make our lives miserable
        return successMessage;
    }

    @Override
    protected String getFailureMessage() {
        // by default, we expect the command-based actions to be limited to single-item
        return failureMessage;
    }
}
