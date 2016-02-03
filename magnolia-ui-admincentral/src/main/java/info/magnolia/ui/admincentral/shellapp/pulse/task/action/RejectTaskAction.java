/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.task.action;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.admincentral.shellapp.pulse.task.DefaultTaskDetailPresenter;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * Action used to intercept the reject task workflow by a dialog asking for a comment.
 */
public class RejectTaskAction extends AbstractTaskAction<RejectTaskActionDefinition> {

    private final FormDialogPresenter formDialogPresenter;
    private final UiContext uiContext;

    @Inject
    public RejectTaskAction(RejectTaskActionDefinition definition, Task task, TasksManager taskManager, DefaultTaskDetailPresenter taskPresenter, FormDialogPresenter formDialogPresenter, UiContext uiContext, Shell shell) {
        super(definition, task, taskManager, taskPresenter, shell);
        this.formDialogPresenter = formDialogPresenter;
        this.uiContext = uiContext;
    }

    @Override
    protected void executeTask(final TasksManager taskManager, final Task task) {

        final PropertysetItem propertysetItem = new PropertysetItem();
        propertysetItem.addItemProperty(Context.ATTRIBUTE_COMMENT, new ObjectProperty<String>(null, String.class));

        formDialogPresenter.start(propertysetItem, getDefinition().getDialogName(), uiContext, new EditorCallback() {
            @Override
            public void onSuccess(String actionName) {
                Object comment = propertysetItem.getItemProperty(Context.ATTRIBUTE_COMMENT).getValue();

                Map<String, Object> result = new HashMap<String, Object>();
                result.put(ACTOR_ID, task.getActorId());
                result.put(DECISION, getDefinition().getDecision());

                if (comment != null) {
                    result.put(Context.ATTRIBUTE_COMMENT, comment);
                }

                taskManager.resolve(task.getId(), result);
                log.debug("About to reject human task named [{}]", task.getName());

                formDialogPresenter.closeDialog();

                getTaskPresenter().onNavigateToList();

                getShell().openNotification(MessageStyleTypeEnum.INFO, true, getDefinition().getSuccessMessage());
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
    }

    @Override
    protected void canExecuteTask(Task task) throws IllegalStateException {
        final String currentUser = MgnlContext.getUser().getName();
        if (task.getStatus() != Task.Status.InProgress || !currentUser.equals(task.getActorId())) {
            throw new IllegalStateException("Task status is [" + task.getStatus() + "] and is assigned to user [" + task.getActorId() + "]. Only in progress tasks assigned to yourself can be rejected.");
        }
    }
}
