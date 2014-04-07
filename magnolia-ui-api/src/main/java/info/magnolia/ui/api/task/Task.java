/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.api.task;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Models a task assigned to a user or group.
 */
public class Task {

    private long id;
    private Status status;
    private int priority;
    private String name;
    private String comment;
    private String groupIds;

    private String actorIds;

    private String actorId;
    private boolean skippable;

    private Map<String, Object> content;

    private Map<String, Object> results;

    private Date lastChange;

    public long getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public Status getStatus() {
        return status;
    }

    public String getActorIds() {
        return actorIds;
    }

    public String getGroupIds() {
        return groupIds;
    }

    public String getActorId() {
        return actorId;
    }

    public boolean isSkippable() {
        return skippable;
    }

    /**
     * The data a user needs to complete a task.
     */
    public Map<String, Object> getContent() {
        return content;
    }

    /**
     * The results of a user completing a task.
     */
    public Map<String, Object> getResult() {
        return results;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setGroupId(String groupIds) {
        this.groupIds = groupIds;
    }

    public void setActorIds(String actorIds) {
        this.actorIds = actorIds;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

    /**
     * Task status.
     */
    public enum Status {
        Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
