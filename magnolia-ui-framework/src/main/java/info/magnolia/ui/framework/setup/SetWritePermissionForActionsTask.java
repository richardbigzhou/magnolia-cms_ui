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
package info.magnolia.ui.framework.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This task updates a given set of actions which need write permission to be executed, so that they are disabled if current user doesn't have such permission.
 * <p>
 * Concretely we set the additional action availability {@link info.magnolia.ui.api.availability.AvailabilityDefinition#isWritePermissionRequired() writePermissionRequired} criterion to <code>true</code> on the given actions.
 */
public class SetWritePermissionForActionsTask extends AbstractRepositoryTask {

    private static final Logger log = LoggerFactory.getLogger(SetWritePermissionForActionsTask.class);

    private final String actionsPath;
    private final String[] actionNames;

    /**
     * Instantiate a new update task to set write permission for a given set of actions.
     * 
     * @param actionsPath an absolute config path to the <i>actions</i> node where the given actions should be updated
     * @param actionNames an array of action node names relative to <code>actionsPath</code> which require write permission
     */
    public SetWritePermissionForActionsTask(String actionsPath, String... actionNames) {
        super("Update actions which need write permission to be executed", "This task sets the availability 'writePermissionRequired' criterion to true on a set of actions at " + actionsPath + ".");
        this.actionsPath = actionsPath;
        this.actionNames = actionNames;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {

        // e.g. /modules/pages/apps/pages/subApps/browser/actions
        if (ctx.getConfigJCRSession().nodeExists(actionsPath)) {
            Node actions = ctx.getConfigJCRSession().getNode(actionsPath);

            for (String actionName : actionNames) {

                if (actions.hasNode(actionName)) {
                    Node action = actions.getNode(actionName);

                    // configure availability if not present already
                    if (!action.hasNode("availability")) {
                        log.warn("Action {} has no availability configured, adding one now. Please check that default availability for root, nodes, properties or multiple selection is still accurate.", actionName);
                        action.addNode("availability", NodeTypes.ContentNode.NAME);
                    }

                    // set writePermissionRequired on availability
                    Node availability = action.getNode("availability");
                    availability.setProperty("writePermissionRequired", true);

                } else {
                    log.warn("Action {} could not be found, permission checks will not be added.", actionName);
                }
            }
        } else {
            log.warn("Could not find actions at path {}, permission checks will not be added.", actionsPath);
        }
    }

}
