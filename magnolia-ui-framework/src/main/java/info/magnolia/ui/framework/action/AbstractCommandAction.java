/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base action supporting execution of commands.
 *
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class AbstractCommandAction<D extends CommandActionDefinition> extends AbstractMultiItemAction<D> {

    public static final String COMMAND_RESULT = "command_result";

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandAction.class);

    private static AtomicInteger idx = new AtomicInteger();

    private CommandsManager commandsManager;

    private Command command;

    private Map<String, Object> params;

    private SimpleTranslator i18n;

    private Object schedulerModule;

    private String commandName;

    private String catalogName;

    private String failureMessage;

    private int timeToWait;


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
        // Scheduler is used only until long-running-actions-concept is implemented. Do not pollute constructors by adding it there.
        ModuleRegistry registry = Components.getComponent(ModuleRegistry.class);
        if (registry.isModuleRegistered("scheduler")) {
            schedulerModule = registry.getModuleInstance("scheduler");
        }
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

            boolean stopProcessing;
            if (isInvokeAsynchronously()) {
                try {
                    // get quartz scheduler (same instance as scheduler module)
                    // due to creation of circular dependency, we can't simply import mgnl scheduler, yet we want to use same factory initialized scheduler in case someone customized it and we want to have all jobs in same group for monitoring purposes
                    Scheduler scheduler = (Scheduler) schedulerModule.getClass().getMethod("getScheduler").invoke(schedulerModule);
                    // create trigger
                    Calendar cal = Calendar.getInstance();
                    // wait for requested period of time before invocation
                    cal.add(Calendar.SECOND, getDefinition().getDelay());

                    // init waiting time before job is started to avoid issues (when job is finished before timeToWait is initialized)
                    timeToWait = getDefinition().getTimeToWait();

                    String appName = getUiContext() instanceof SubAppContext ? ((SubAppContext) getUiContext()).getSubAppDescriptor().getLabel() : null;
                    String userName = MgnlContext.getUser() == null ? null : MgnlContext.getUser().getName();
                    String jobName = "UI Action triggered execution of [" + (StringUtils.isNotEmpty(catalogName) ? (catalogName + ":") : "") + commandName + "] by user [" + StringUtils.defaultIfEmpty(userName, "") + "].";
                    // allowParallel jobs false/true => remove index, or keep index
                    if (getDefinition().isParallel()) {
                        jobName += " (" + idx.getAndIncrement() + ")";
                    }
                    SimpleTrigger trigger = new SimpleTrigger(jobName, "magnolia", cal.getTime());
                    trigger.addTriggerListener(jobName + "_trigger");
                    // create job definition
                    final JobDetail jd = new JobDetail(jobName, "magnolia", Class.forName("info.magnolia.module.scheduler.CommandJob"));
                    jd.getJobDataMap().put("command", commandName);
                    jd.getJobDataMap().put("catalog", catalogName);
                    jd.getJobDataMap().put("params", getParams());

                    scheduler.addTriggerListener(new CommandActionTriggerListener(
                            jobName + "_trigger",
                            i18n.translate("ui-framework.abstractcommand.asyncaction.successTitle", getDefinition().getLabel()),
                            i18n.translate("ui-framework.abstractcommand.asyncaction.successMessage", getDefinition().getLabel(), appName, item.getJcrItem().getPath()),
                            i18n.translate("ui-framework.abstractcommand.asyncaction.errorTitle", getDefinition().getLabel()),
                            i18n.translate("ui-framework.abstractcommand.asyncaction.errorMessage", getDefinition().getLabel(), appName, item.getJcrItem().getPath())));
                    // start the job
                    scheduler.scheduleJob(jd, trigger);
                    // false == all ok, just stop processing. Seems actions are taking up the signaling used by commands (facepalm)
                    stopProcessing = false;

                    // wait until job has been executed
                    Thread.sleep(getDefinition().getDelay() * 1000 + 100);
                    int timeToSleep = 500;
                    // check every 500ms if job is running
                    while (timeToWait > 0) {
                        List<JobExecutionContext> jobs = scheduler.getCurrentlyExecutingJobs();
                        if (isJobRunning(jobs, jobName)) {
                            Thread.sleep(timeToSleep);
                        } else {
                            break;
                        }
                        timeToWait -= timeToSleep;
                    }
                    if (timeToWait == 0) {
                        // notify user that this action will take some time and when finished he will by notified via pulse
                        getDefinition().setSuccessMessage("ui-framework.abstractcommand.asyncaction.long");
                    }
                } catch (ObjectAlreadyExistsException e) {
                    // unfortunately this old version doesn't have any better method of checking whether job already exists
                    // if the job with same name already exists, it was either started by someone else and we don't allow this type of job to be executed multiple times
                    failureMessage = "ui-framework.abstractcommand.parallelExecutionNotAllowed";
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

    private boolean isJobRunning(List<JobExecutionContext> jobs, String jobName) {
        for (JobExecutionContext job : jobs) {
            if (job.getJobDetail().getName().equals(jobName)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isInvokeAsynchronously() {
        return getDefinition().isAsynchronous() && schedulerModule != null;
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

    /**
     * Trigger listener.
     */
    public class CommandActionTriggerListener implements TriggerListener {

        private final String name;
        private String successMessageTitle;
        private final String successMessage;
        private final String errorMessageTitle;
        private final String errorMessage;

        public CommandActionTriggerListener(String name, String successMessageTitle, String successMessage, String errorMessageTitle, String errorMessage) {
            this.name = name;
            this.successMessageTitle = successMessageTitle;
            this.successMessage = successMessage;
            this.errorMessageTitle = errorMessageTitle;
            this.errorMessage = errorMessage;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
        }

        @Override
        public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
            return false;
        }

        @Override
        public void triggerMisfired(Trigger trigger) {
        }

        @Override
        public void triggerComplete(final Trigger trigger, final JobExecutionContext jobExecutionContext, int i) {
            if (!getDefinition().isNotifyUser()) {
                return;
            }
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    // notify user only if action took longer than xx seconds
                    MessagesManager messagesManager = Components.getComponent(MessagesManager.class);
                    // result 1 stands for success, 0 for error - see info.magnolia.module.scheduler.CommandJob
                    if (timeToWait == 0 && (Integer) jobExecutionContext.getResult() == 1) {
                        messagesManager.sendLocalMessage(new Message(MessageType.INFO, successMessageTitle, successMessage));
                    } else if ((Integer) jobExecutionContext.getResult() == 0) {
                        Message msg = new Message(MessageType.WARNING, errorMessageTitle, errorMessage);
                        msg.setView("ui-admincentral:longRunning");
                        msg.addProperty("comment", i18n.translate("ui-framework.abstractcommand.asyncaction.errorComment"));
                        messagesManager.sendLocalMessage(msg);
                    }
                }
            });
        }
    }
}
