/**
 * This file Copyright (c) 2015 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for claiming tasks.
 */
public class ClaimTasksAction extends AbstractAction<ClaimTasksActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ClaimTasksAction.class);

    private final UiContext uiContext;
    private final List<String> taskIds;
    private final TasksManager tasksManager;

    @Inject
    public ClaimTasksAction(ClaimTasksActionDefinition definition, List<String> taskIds, TasksManager taskManager, UiContext uiContext) {
        super(definition);
        this.tasksManager = taskManager;
        this.uiContext = uiContext;
        this.taskIds = taskIds;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final String userId = MgnlContext.getUser().getName();
        for (String taskId : taskIds) {
            log.debug("User [{}] is claiming workflow human task id [{}]", userId, taskId);

            tasksManager.claim(taskId, userId);
        }

        uiContext.openNotification(MessageStyleTypeEnum.INFO, true, getDefinition().getSuccessMessage());
    }
}
