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


/**
 * Responsible for executing actions, doing lookups of action definitions based on action names and evaluating if an
 * action is available. Creates a new instance of the action for each execution and allows the action to receive its
 * action definition object and all parameters given through injection as it is created.
 *
 * @see Action
 * @see ActionDefinition
 */
public interface ActionExecutor {

    /**
     * Creates a new instance of the action for the supplied name and executes it. The arguments passed here along with
     * the action definition are made available to the action instance using injection.
     *
     * @throws ActionExecutionException if the action encounters a problem during execution or if no action definition matches the name
     */
    void execute(String actionName, Object... args) throws ActionExecutionException;

    /**
     * Performs a lookup for an action definition by name.
     *
     * @return the action definition for the supplied action name or null if not found
     */
    ActionDefinition getActionDefinition(String actionName);
}
