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
package info.magnolia.ui.framework.task;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.task.Task;
import info.magnolia.task.event.TaskEvent;
import info.magnolia.task.event.TaskEventHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * LocalTaskDispatcherManager.
 */
@Singleton
public class LocalTaskDispatcherManager implements TaskEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LocalTaskDispatcherManager.class);

    private final ListMultimap<String, TaskEventDispatcher> listeners = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, TaskEventDispatcher>create());

    private Provider<SecuritySupport> securitySupport;

    @Inject
    public LocalTaskDispatcherManager(@Named(SystemEventBus.NAME) final EventBus systemEventBus, Provider<SecuritySupport> securitySupport) {
        this.securitySupport = securitySupport;
        systemEventBus.addHandler(TaskEvent.class, this);
    }

    @Override
    public void taskClaimed(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    @Override
    public void taskAdded(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    @Override
    public void taskResolved(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    @Override
    public void taskFailed(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    @Override
    public void taskArchived(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    @Override
    public void taskRemoved(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    @Override
    public void taskScheduled(TaskEvent taskEvent) {
        Set<String> users = getAllRecipients(taskEvent.getTask());
        sendTaskEvent(taskEvent, users);
    }

    private Set<String> getAllRecipients(final Task task) {

        HashSet<String> users = new HashSet<String>();

        log.debug("Found actorId [{}]", task.getActorId());
        if (StringUtils.isNotBlank(task.getActorId())) {
            users.add(task.getActorId());
        }

        if (task.getActorIds() != null) {
            log.debug("Found actorIds {}", task.getActorIds());
            users.addAll(task.getActorIds());
        }


        log.debug("Found groups {}", task.getGroupIds());
        if (task.getGroupIds() != null) {
            for (String group : task.getGroupIds()) {
                Collection<String> usersOfGroupTransitive = securitySupport.get().getUserManager().getUsersWithGroup(group, true);
                users.addAll(usersOfGroupTransitive);
            }
        }

        return users;
    }

    private void sendTaskEvent(TaskEvent taskEvent, Set<String> users) {

        log.debug("Sending a task event to the following users {}", users);
        for (String userId : users) {
            final List<TaskEventDispatcher> listenerList = listeners.get(userId);
            if (listenerList != null) {
                for (final TaskEventDispatcher listener : listenerList) {
                    listener.onTaskEvent(taskEvent);
                }
            }
        }
    }

    public void registerLocalTasksListener(String userName, TaskEventDispatcher listener) {
        listeners.put(userName, listener);
    }

    public void unregisterLocalTasksListener(String userName, TaskEventDispatcher listener) {
        listeners.remove(userName, listener);
    }

}
