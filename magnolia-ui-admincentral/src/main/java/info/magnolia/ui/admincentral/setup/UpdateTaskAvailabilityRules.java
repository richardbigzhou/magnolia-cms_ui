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
package info.magnolia.ui.admincentral.setup;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.NodeVisitorTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.admincentral.shellapp.pulse.task.action.availability.TaskAvailabilityRuleDefinition;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates task availability rules using {@link info.magnolia.ui.admincentral.shellapp.pulse.task.action.availability.TaskAvailabilityRuleDefinition}
 * to work with multiple statuses defined under the status node.
 */
public class UpdateTaskAvailabilityRules extends NodeVisitorTask {

    private static final Logger log = LoggerFactory.getLogger(UpdateTaskAvailabilityRules.class);

    public static final String STATUS = "status";

    public UpdateTaskAvailabilityRules(String moduleName) {
        super("Update task availability", "Update task availability to support multiple statuses for module: " + moduleName, RepositoryConstants.CONFIG, "/modules/" + moduleName + "/messageViews");
    }

    @Override
    protected boolean nodeMatches(Node node) {
        try {
            return node.getPrimaryNodeType().getName().equals(NodeTypes.ContentNode.NAME) && hasTaskAvailabilityRule(node);
        } catch (RepositoryException e) {
            log.error("Couldn't evaluate visited node's name or node-type", e);
        }
        return false;
    }

    private boolean hasTaskAvailabilityRule(Node node) {
        try {
            return node.hasProperty("class") &&
                    TaskAvailabilityRuleDefinition.class.getName().equals(node.getProperty("class").getString());

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException("Failed to migrate task availability-rule.", e);
        }
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        try {
            if (node.hasProperty(STATUS)) {
                Property statusProperty = node.getProperty(STATUS);
                String status = statusProperty.getString();

                statusProperty.remove();
                node.addNode(STATUS, NodeTypes.ContentNode.NAME).setProperty(status, status);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException("Failed to migrate task availability-rule.", e);
        }
    }

}
