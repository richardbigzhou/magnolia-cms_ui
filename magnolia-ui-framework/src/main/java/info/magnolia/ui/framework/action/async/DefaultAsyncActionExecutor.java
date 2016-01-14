/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.framework.action.async;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.scheduler.CommandJob;
import info.magnolia.module.scheduler.SchedulerConsts;
import info.magnolia.module.scheduler.SchedulerModule;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.listeners.TriggerListenerSupport;

/**
 * {@link AsyncActionExecutor} delegating to Magnolia's {@link SchedulerModule} for asynchronous action execution.
 *
 * @param <D> {@link info.magnolia.ui.api.action.CommandActionDefinition}.
 */
public class DefaultAsyncActionExecutor<D extends CommandActionDefinition> implements AsyncActionExecutor {

    private static AtomicInteger idx = new AtomicInteger();

    private final D definition;
    private final Provider<SchedulerModule> schedulerModuleProvider;
    private final User user;
    private final UiContext uiContext;
    private final String catalogName;
    private final SimpleTranslator i18n;
    private final String commandName;

    @Inject
    public DefaultAsyncActionExecutor(final D definition, final Provider<SchedulerModule> schedulerModuleProvider, final Context context,
                                      final UiContext uiContext, final SimpleTranslator i18n) {
        this.definition = definition;
        this.schedulerModuleProvider = schedulerModuleProvider;
        this.user = context.getUser();
        this.uiContext = uiContext;
        this.i18n = i18n;
        this.commandName = definition.getCommand();
        this.catalogName = definition.getCatalog();
    }

    @Override
    public boolean execute(JcrItemAdapter item, Map<String, Object> params) throws Exception {
        Calendar cal = Calendar.getInstance();
        // wait for requested period of time before invocation
        cal.add(Calendar.SECOND, definition.getDelay());

        // init waiting time before job is started to avoid issues (when job is finished before timeToWait is initialized)
        int timeToWait = definition.getTimeToWait();

        String jobName = "UI Action triggered execution of [" + (StringUtils.isNotEmpty(catalogName) ? (catalogName + ":") : "") + commandName + "] by user [" + StringUtils.defaultIfEmpty(user.getName(), "") + "].";
        // allowParallel jobs false/true => remove index, or keep index
        if (definition.isParallel()) {
            jobName += " (" + idx.getAndIncrement() + ")";
        }
        SimpleTrigger trigger = new SimpleTrigger(jobName, SchedulerConsts.SCHEDULER_GROUP_NAME, cal.getTime());
        trigger.addTriggerListener(jobName + "_trigger");
        // create job definition
        final JobDetail jd = new JobDetail(jobName, SchedulerConsts.SCHEDULER_GROUP_NAME, info.magnolia.module.scheduler.CommandJob.class);
        jd.getJobDataMap().put(SchedulerConsts.CONFIG_JOB_COMMAND, commandName);
        jd.getJobDataMap().put(SchedulerConsts.CONFIG_JOB_COMMAND_CATALOG, catalogName);
        jd.getJobDataMap().put(SchedulerConsts.CONFIG_JOB_PARAMS, params);

        Scheduler scheduler = schedulerModuleProvider.get().getScheduler();
        TriggerListener triggerListener = getListener(jobName, item);
        scheduler.addTriggerListener(triggerListener);
        try {
            scheduler.scheduleJob(jd, trigger);
        }
        catch (SchedulerException e) {
             throw new ParallelExecutionException(e);
        }

        // wait until job has been executed
        Thread.sleep(definition.getDelay() * 1000 + 100);
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

        boolean isRunningInBackground = (timeToWait == 0);
        // Throw error in case job completed and job result is unsuccessful
        if (!isRunningInBackground && triggerListener instanceof DefaultAsyncActionExecutor.CommandActionTriggerListener) {
            CommandActionTriggerListener commandActionTriggerListener = (CommandActionTriggerListener) triggerListener;

            if (commandActionTriggerListener.getException() != null) {
                throw commandActionTriggerListener.getException();
            }
        }
        return isRunningInBackground;
    }

    protected TriggerListener getListener(String jobName, JcrItemAdapter item) throws RepositoryException {
        return new CommandActionTriggerListener(definition, jobName + "_trigger", uiContext, i18n, item.getJcrItem().getPath());
    }

    private boolean isJobRunning(List<JobExecutionContext> jobs, String jobName) {
        for (JobExecutionContext job : jobs) {
            if (job.getJobDetail().getName().equals(jobName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes care of notifying the user about successful or failed executions.
     */
    public class CommandActionTriggerListener extends TriggerListenerSupport {

        private final D definition;
        private final String name;
        private final SimpleTranslator i18n;
        private final String successMessageTitle;
        private final String successMessage;
        private final String errorMessageTitle;
        private final String errorMessage;
        private Exception exception = null;

        @Inject
        public CommandActionTriggerListener(D definition, String triggerName, UiContext uiContext, SimpleTranslator i18n, String path) {
            this.definition = definition;
            this.name = triggerName;
            this.i18n = i18n;

            String appName = uiContext instanceof SubAppContext ? ((SubAppContext) uiContext).getSubAppDescriptor().getLabel() : null;
            this.successMessageTitle = i18n.translate("ui-framework.abstractcommand.asyncaction.successTitle", definition.getLabel());
            this.successMessage = i18n.translate("ui-framework.abstractcommand.asyncaction.successMessage", definition.getLabel(), appName, path);
            this.errorMessageTitle = i18n.translate("ui-framework.abstractcommand.asyncaction.errorTitle", definition.getLabel());
            this.errorMessage = i18n.translate("ui-framework.abstractcommand.asyncaction.errorMessage", definition.getLabel(), appName, path);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void triggerComplete(final Trigger trigger, final JobExecutionContext jobExecutionContext, int i) {
            if (!definition.isNotifyUser()) {
                return;
            }
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    // User should be notified always when the long running action has finished. Otherwise he gets NO feedback.
                    MessagesManager messagesManager = Components.getComponent(MessagesManager.class);
                    // result 1 stands for success, 0 for error - see info.magnolia.module.scheduler.CommandJob
                    CommandJob.JobResult result = (CommandJob.JobResult) jobExecutionContext.getResult();
                    exception = result.getException();
                    if (result.isSuccess()) {
                        messagesManager.sendMessage(user.getName(), new Message(MessageType.INFO, successMessageTitle, successMessage));
                    } else {
                        Message msg = new Message(MessageType.WARNING, errorMessageTitle, errorMessage);
                        msg.setView("ui-admincentral:longRunning");
                        msg.addProperty("exception", ExceptionUtils.getMessage(result.getException()));
                        msg.addProperty("comment", i18n.translate("ui-framework.abstractcommand.asyncaction.errorComment"));
                        messagesManager.sendMessage(user.getName(), msg);
                    }
                }
            });
        }

        public Exception getException() {
            return exception;
        }
    }
}
