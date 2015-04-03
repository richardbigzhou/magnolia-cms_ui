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
package info.magnolia.ui.api.action;

import info.magnolia.commands.CommandsManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A basic action definition useful for actions that delegates to commands.
 */
public class CommandActionDefinition extends ConfiguredActionDefinition {

    private String command;
    private String catalog = CommandsManager.DEFAULT_CATALOG;
    private boolean asynchronous = false;
    private int delay = 1;
    private boolean isParallel = true;
    private int timeToWait = 5000; // milliseconds
    private boolean notifyUser = true;

    private Map<String, Object> params = new HashMap<String, Object>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return {@link CommandsManager#DEFAULT_CATALOG} if not set otherwise.
     */
    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isParallel() {
        return isParallel;
    }

    public void setParallel(boolean isParallel) {
        this.isParallel = isParallel;
    }

    public int getTimeToWait() {
        return timeToWait;
    }

    public void setTimeToWait(int timeToWait) {
        this.timeToWait = timeToWait;
    }

    public boolean isNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(boolean notifyUser) {
        this.notifyUser = notifyUser;
    }
}
