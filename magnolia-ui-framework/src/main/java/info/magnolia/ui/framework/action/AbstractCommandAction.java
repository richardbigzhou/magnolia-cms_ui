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

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SimpleContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
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

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Base action supporting execution of commands.
 *
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class AbstractCommandAction<D extends CommandActionDefinition> extends AbstractMultiItemAction<D> {

    public static final String COMMAND_RESULT = "command_result";

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandAction.class);

    private CommandsManager commandsManager;

    private Command command;

    private Map<String, Object> params;

    private SimpleTranslator i18n;

    private String commandName;

    private String catalogName;

    private String failureMessage;

    public AbstractCommandAction(final D definition, final JcrItemAdapter item, final CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, uiContext);
        init(commandsManager, i18n);
    }

    public AbstractCommandAction(final D definition, final List<JcrItemAdapter> items, final CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, items, uiContext);
        init(commandsManager, i18n);
    }

    private void init(final CommandsManager commandsManager, final SimpleTranslator i18n) {
        this.commandsManager = commandsManager;
        this.i18n = i18n;
        // Init Command.
        commandName = getDefinition().getCommand();
        catalogName = getDefinition().getCatalog();
        this.command = getCommandsManager().getCommand(catalogName, commandName);
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
            params.put(Context.ATTRIBUTE_REQUESTOR, MgnlContext.getUser().getName());
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
        failureMessage = null;
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
        try {
            log.debug("Executing command [{}] from catalog [{}] with the following parameters [{}]...", getDefinition().getCommand(), getDefinition().getCatalog(), getParams());

            boolean result = false;
            if (getDefinition().isAsynchronous()) {

                final Context ctx = new SimpleContext();

                // TODO reimplement non-parallel
                final AsyncCommandThread thread = new AsyncCommandThread(ctx, command, getParams());
                thread.start();

                // give configured time for async command execution to occur
                // TODO: move at multi-execution level, setup polling only at start, stop it only at end
                // Thread.sleep(getDefinition().getTimeToWait());

                while (thread.isAlive() && System.currentTimeMillis() - start < getDefinition().getTimeToWait()) {
                    Thread.sleep(500);
                }

                if (thread.isAlive()) {
                    // notify user that this action will take some time and when finished he will by notified via pulse
                    getDefinition().setSuccessMessage("ui-framework.abstractcommand.asyncaction.long");

                    // Enable polling and set frequency to 0.5 seconds
                    UI.getCurrent().setPollInterval(1000);
                    log.debug("setting polling to 1s");
                }

            } else {
                result = commandsManager.executeCommand(command, getParams());
                MgnlContext.getInstance().setAttribute(COMMAND_RESULT, result, Context.LOCAL_SCOPE);
                onPostExecute();
                log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
            }

        } catch (Exception e) {
            onError(e);
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start, e);
        }
    }

    @Deprecated
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
        // by default, we expect the command-based actions to be limited to single-item
        return null;
    }

    @Override
    protected String getFailureMessage() {
        // by default, we expect the command-based actions to be limited to single-item
        return failureMessage;
    }

    private void triggerComplete(final boolean success) {
        if (!getDefinition().isNotifyUser()) {
            return;
        }
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            @Override
            public void doExec() {
                // User should be notified always when the long running action has finished. Otherwise he gets NO feedback.
                MessagesManager messagesManager = Components.getComponent(MessagesManager.class);
                // result 1 stands for success, 0 for error - see info.magnolia.module.scheduler.CommandJob
                if (success) {
                    messagesManager.sendLocalMessage(new Message(MessageType.INFO, "successMessageTitle", "successMessage"));
                } else {
                    Message msg = new Message(MessageType.WARNING, "errorMessageTitle", "errorMessage");
                    msg.setView("ui-admincentral:longRunning");
                    msg.addProperty("comment", i18n.translate("ui-framework.abstractcommand.asyncaction.errorComment"));
                    messagesManager.sendLocalMessage(msg);
                }
            }
        });
    }

    /**
     * Own thread for executing commands asynchronously.
     */
    private class AsyncCommandThread extends Thread {

        private final Command command;
        private final Map<String, Object> params;

        // Volatile because read in another thread in access()
        volatile Context ctx;
        volatile boolean success = false;

        AsyncCommandThread(Context ctx, Command command, Map<String, Object> params) {
            this.command = command;
            this.params = params;
            this.ctx = ctx;
        }

        @Override
        public void run() {

            try {
                // init context
                MgnlContext.setInstance(ctx);
                // MgnlContext.setInstance(new SimpleContext(Components.getComponent(SystemContext.class)));
                // MgnlContext.setInstance(new SimpleContext());

                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                }

                try {
                    success = true;
                    success = commandsManager.executeCommand(command, params);
                } catch (Exception e) {
                    // TODO add to failed items
                    // getFailedItems().put(new JcrNodeAdapter(null), e);
                }

                // Update the UI thread-safely
                UI.getCurrent().access(new Runnable() {

                    @Override
                    public void run() {
                        // Stop polling
                        UI.getCurrent().setPollInterval(-1);

                        Notification.show("This is the caption",
                                "This is the description",
                                Notification.Type.WARNING_MESSAGE);

                        // TODO Give the action a callback
                        // getErrorNotification();
                        // triggerComplete(success);
                    }
                });

            } finally {
                MgnlContext.release();
                MgnlContext.setInstance(null);
            }
        }
    }

}
