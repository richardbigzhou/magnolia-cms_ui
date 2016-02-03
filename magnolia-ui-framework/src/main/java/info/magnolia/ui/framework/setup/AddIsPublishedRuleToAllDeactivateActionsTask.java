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

    private static final String ACTIONS = "actions";
    private static final String DEACTIVATE = "deactivate";
    private static final String EXTENDS = "extends";
    private static final String AVAILABILITY = "availability";
    private static final String RULES = "rules";
    private static final String RULE = "IsPublishedRule";
    private static final String PROPERTY_NAME = "implementationClass";
    private static final String PROPERTY_VALUE = "info.magnolia.ui.framework.availability.IsPublishedRule";
    private String appRootNode;

    private NodeVisitor nodeVisitor = new NodeVisitor() {
        @Override
        public void visit(Node node) throws RepositoryException {
            if (node.getName().equals(ACTIONS)) {
                if (node.hasNode(DEACTIVATE) && !(node.getNode(DEACTIVATE).hasProperty(EXTENDS) && node.getNode(DEACTIVATE).getProperty(EXTENDS).getString().endsWith(DEACTIVATE))) {
                    Node action = node.getNode(DEACTIVATE);
                    if (!action.hasNode(AVAILABILITY)) {
                        action.addNode(AVAILABILITY, NodeTypes.ContentNode.NAME);
                    }
                    Node actionAvailability = action.getNode(AVAILABILITY);
                    if (!actionAvailability.hasNode(RULES)) {
                        actionAvailability.addNode(RULES, NodeTypes.ContentNode.NAME);
                    }
                    Node availabilityRules = actionAvailability.getNode(RULES);
                    if (!availabilityRules.hasNode(RULE)) {
                        availabilityRules.addNode(RULE, NodeTypes.ContentNode.NAME);
                    }
                    Node newRule = availabilityRules.getNode(RULE);
                    if (!newRule.hasProperty(PROPERTY_NAME)) {
                        newRule.setProperty(PROPERTY_NAME, PROPERTY_VALUE);
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
            if (session.nodeExists(appRootNode)) {
                Node rootNode = session.getNode(appRootNode);
                NodeUtil.visit(rootNode, nodeVisitor);
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }
}
