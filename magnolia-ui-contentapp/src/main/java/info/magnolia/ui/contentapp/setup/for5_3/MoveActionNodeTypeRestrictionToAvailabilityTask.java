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
package info.magnolia.ui.contentapp.setup.for5_3;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.NodeVisitorTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.contentapp.detail.action.EditItemActionDefinition;
import info.magnolia.ui.contentapp.detail.action.RestorePreviousVersionActionDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moves <code>nodeType</code> property from action definition to its availability definition.
 * This task normally is not meant to be used standalone.
 *
 * @see {@link info.magnolia.ui.contentapp.detail.action.AbstractItemActionDefinition AbstractItemActionDefinition} and its sub-classes
 * @see {@link ContentAppMigrationTask}
 */
public class MoveActionNodeTypeRestrictionToAvailabilityTask extends NodeVisitorTask {

    private static final Logger log = LoggerFactory.getLogger(MoveActionNodeTypeRestrictionToAvailabilityTask.class);

    public static final String NODE_TYPE = "nodeType";
    public static final String AVAILABILITY_NODE = "availability";
    public static final String NODE_TYPES = "nodeTypes";

    /**
     * The list of the common classes could also contain {@link info.magnolia.ui.contentapp.detail.action.CreateItemActionDefinition}
     * but its <code>nodeType</code> property could have been used for purposes different from availability checking. In reality that
     * property was not used at all, so in order to avoid problems with it - we skip {@link info.magnolia.ui.contentapp.detail.action.CreateItemActionDefinition}.
     */
    private static Class<?>[] commonActionDefinitionClasses = new Class[] {
            EditItemActionDefinition.class,
            RestorePreviousVersionActionDefinition.class,
    };

    private final List<Class<?>> matchingActionDefinitionClasses = new ArrayList<Class<?>>();

    @Inject
    public MoveActionNodeTypeRestrictionToAvailabilityTask(String path, Class<? extends ActionDefinition>... additionalActionDefinitionClasses) {
        super(
              "Move nodeType property to availability definition",
              "Fix availability checking of actions defined with AbstractItemActionDefinition sub-class by moving the nodeType property from action definition to availability.",
                RepositoryConstants.CONFIG, path);

        matchingActionDefinitionClasses.addAll(Arrays.asList(commonActionDefinitionClasses));
        matchingActionDefinitionClasses.addAll(Arrays.asList(additionalActionDefinitionClasses));
    }

    @Override
    protected boolean nodeMatches(Node node) {
        try {
            return node.getPrimaryNodeType().getName().equals(NodeTypes.ContentNode.NAME) &&
                    (node.hasProperty("class") && containsDefinitionClass(node.getProperty("class").getString()));
        } catch (RepositoryException e) {
            log.error("Couldn't evaluate visited node's type or class property", e);
        }
        return false;
    }

    private boolean containsDefinitionClass(String className) {
        Iterator<Class<?>> it = matchingActionDefinitionClasses.iterator();
        while (it.hasNext()) {
            if (className.equals(it.next().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        try {
            if (node.hasProperty(NODE_TYPE)) {
                final Property nodeTypeProperty = node.getProperty(NODE_TYPE);

                final Node availability = NodeUtil.createPath(node, AVAILABILITY_NODE, NodeTypes.ContentNode.NAME, true);
                final Node nodeTypes = NodeUtil.createPath(availability, NODE_TYPES, NodeTypes.ContentNode.NAME, true);

                final String nodeType = nodeTypeProperty.getString();
                nodeTypes.setProperty(Path.getValidatedLabel(nodeType), nodeType);

                nodeTypeProperty.remove();
            }
        } catch (RepositoryException e) {
            log.error("Failed to move nodeType property to action availability for node {0} due to: {1}", node, e.getMessage());
        }
    }
}
