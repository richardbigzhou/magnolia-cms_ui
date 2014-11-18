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
package info.magnolia.ui.framework.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Add IsPublishedRule to all deactivation actions.
 */
public class AddIsPublishedRuleToAllDeactivateActionsTask extends AbstractTask {

    private static String actions = "actions";
    private static String deactivate = "deactivate";
    private static String extend = "extends";
    private static String availability = "availability";
    private static String rules = "rules";
    private static String rule = "IsPublishedRule";
    private static String propertyName = "implementationClass";
    private static String propertyValue = "info.magnolia.ui.framework.availability.IsPublishedRule";
    private String appRootNode;

    private NodeVisitor nodeVisitor = new NodeVisitor() {
        @Override
        public void visit(Node node) throws RepositoryException {
            if (node.getName().equals(actions)) {
                if (node.hasNode(deactivate)  && !node.getNode(deactivate).hasProperty(extend)) {
                    Node action = node.getNode(deactivate);
                    if (!action.hasNode(availability)) {
                        action.addNode(availability, NodeTypes.ContentNode.NAME);
                    }
                    Node actionAvailability = action.getNode(availability);
                    if (!actionAvailability.hasNode(rules)) {
                        actionAvailability.addNode(rules, NodeTypes.ContentNode.NAME);
                    }
                    Node availabilityRules = actionAvailability.getNode(rules);
                    if (!availabilityRules.hasNode(rule)) {
                        availabilityRules.addNode(rule, NodeTypes.ContentNode.NAME);
                    }
                    Node newRule = availabilityRules.getNode(rule);
                    if (!newRule.hasProperty(propertyName)) {
                        newRule.setProperty(propertyName, propertyValue);
                    }
                }
            }
        }
    };

    public AddIsPublishedRuleToAllDeactivateActionsTask(String taskDescription, String appRootNode) {
        super("Add IsPublishedRule to all deactivation actions.", taskDescription);
        this.appRootNode = appRootNode;
    }

    @Override
    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            Session session = ctx.getJCRSession(RepositoryConstants.CONFIG);
            Node rootNode = session.getNode(appRootNode);
            NodeUtil.visit(rootNode, nodeVisitor);
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }
}
