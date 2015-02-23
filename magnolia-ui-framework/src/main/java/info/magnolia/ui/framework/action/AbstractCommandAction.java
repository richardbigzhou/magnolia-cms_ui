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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.UI;

/**
 * Base action supporting execution of commands.
 *
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class AbstractCommandAction<D extends CommandActionDefinition> extends AbstractMultiItemAction<D> {

    public static final String COMMAND_RESULT = "command_result";

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandAction.class);

    private final CommandsManager commandsManager;
    private final UiContext uiContext;
    private final MessagesManager messagesManager;
    private SimpleTranslator i18n;

    private Command command;
    private Map<String, Object> params;

    private boolean isCompletingAsync;

    @Inject
    public AbstractCommandAction(D definition, List<JcrItemAdapter> items, CommandsManager commandsManager, UiContext uiContext, MessagesManager messagesManager, SimpleTranslator i18n) {
        super(definition, items, uiContext);

        this.commandsManager = commandsManager;
        this.uiContext = uiContext;
        this.messagesManager = messagesManager;
        this.i18n = i18n;

        String commandName = getDefinition().getCommand();
        String catalogName = getDefinition().getCatalog();
        this.command = getCommandsManager().getCommand(catalogName, commandName);
    }

    @Deprecated
    public AbstractCommandAction(D definition, JcrItemAdapter item, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, Arrays.asList(item), commandsManager, uiContext, i18n);
    }

    @Deprecated
    public AbstractCommandAction(D definition, List<JcrItemAdapter> items, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) {
        this(definition, items, commandsManager, uiContext, Components.getComponent(MessagesManager.class), i18n);
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
     * Wraps the whole execution of the action in a separate thread, if it is configured to run async.
     */
    @Override
    public void execute() throws ActionExecutionException {

        if (!getDefinition().isAsynchronous()) {
            super.execute();
            return;
        }

        long start = System.currentTimeMillis();
        final Context ctx = new SimpleContext();

        // TODO reimplement or deprecate parallel
        final Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    MgnlContext.setInstance(ctx);

                    // artificially taking time to force async
                    if (getDefinition().getDelay() == 99) {
                        try { Thread.sleep(5000); } catch (InterruptedException e) {}
                    }

                    AbstractCommandAction.super.execute();

                } catch (ActionExecutionException e) {
                    // super implementation swallows exceptions to keep track of failedItems
                    // so this exception should never occur, but if it does occur then we want to know
                    log.error("An exception occurred while executing async Thread for action {}.", getDefinition().getName(), e);
                } finally {
                    MgnlContext.release();
                    MgnlContext.setInstance(null);
                }
            }
        };
        thread.start();

        // Check every half a second whether async task is still ongoing, until timeout
        try {
            while (thread.isAlive() && System.currentTimeMillis() - start < getDefinition().getTimeToWait()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
        }

        if (thread.isAlive()) {
            isCompletingAsync = true;

            // notify user that this action will take some time and when finished he will by notified via pulse
            uiContext.openNotification(MessageStyleTypeEnum.INFO, true, i18n.translate("ui-framework.abstractcommand.asyncaction.long"));

            // Enable polling and set frequency to 1 second
            log.debug("Action will complete async, turning polling on to be notified when it completes");
            UI.getCurrent().setPollInterval(1000);

        } else {
            log.debug("CommandAction executed successfully in {} ms ", System.currentTimeMillis() - start);
        }

        // TODO decide what to do for multi-item actions, set command result only upon end, right?
        // MgnlContext.getInstance().setAttribute(COMMAND_RESULT, result, Context.LOCAL_SCOPE);

    }

    @Override
    protected void actionComplete() {

        if (getDefinition().isAsynchronous() && isCompletingAsync) {

            // 1. Notify the UI through the Pulse
            if (getDefinition().isNotifyUser()) {

                if (getFailedItems().isEmpty()) {
                    // String message = getSuccessMessage();
                    String successTitle = i18n.translate("ui-framework.abstractcommand.asyncaction.successTitle", getDefinition().getLabel());
                    String successMessage = i18n.translate("ui-framework.abstractcommand.asyncaction.successMessage", getDefinition().getLabel(), "appName", "item.getJcrItem().getPath()");
                    messagesManager.sendLocalMessage(new Message(MessageType.INFO, successTitle, successMessage));

                } else {
                    String errorTitle = i18n.translate("ui-framework.abstractcommand.asyncaction.errorTitle", getDefinition().getLabel());
                    String errorMessage = i18n.translate("ui-framework.abstractcommand.asyncaction.errorMessage", getDefinition().getLabel(), "appName", "item.getJcrItem().getPath()");
                    String errorDetail = getErrorNotification();
                    Message msg = new Message(MessageType.WARNING, errorTitle, errorMessage);
                    msg.setView("ui-admincentral:longRunning");
                    msg.addProperty("comment", i18n.translate("ui-framework.abstractcommand.asyncaction.errorComment") + " " + errorDetail);
                    messagesManager.sendLocalMessage(msg);
                }
            }

            // 2. Stop polling
            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {

                @Override
                public void run() {
                    log.debug("Action has completed, turning polling off.");
                    UI.getCurrent().setPollInterval(-1);
                }
            });

        } else {
            // Notify the UI normally through notifications
            super.actionComplete();
        }
    }

    @Override
    protected String getSuccessMessage() {
        return getDefinition().getSuccessMessage();
    }

    @Override
    protected String getFailureMessage() {
        return getDefinition().getFailureMessage();
    }

    /**
     * Handles the retrieval of the {@link Command} instance defined in the {@link CommandActionDefinition} associated with this action and then
     * performs the actual command execution.
     *
     * @throws info.magnolia.ui.api.action.ActionExecutionException if no command is found or if command execution throws an exception.
     */
    @Override
    protected void executeOnItem(JcrItemAdapter item) throws ActionExecutionException {

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
            log.debug("Executing command [{}] from catalog [{}] with the following parameters [{}]...", new Object[] { getDefinition().getCommand(), getDefinition().getCatalog(), getParams() });

            boolean result = commandsManager.executeCommand(command, getParams());
            MgnlContext.getInstance().setAttribute(COMMAND_RESULT, result, Context.LOCAL_SCOPE);
            onPostExecute();
            log.debug("Command executed successfully in {} ms ", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.debug("Command execution failed after {} ms ", System.currentTimeMillis() - start, e);
            getFailedItems().put(item, e);
            onError(e);
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

}
