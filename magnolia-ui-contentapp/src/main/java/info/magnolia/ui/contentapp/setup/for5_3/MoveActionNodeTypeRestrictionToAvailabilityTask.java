/**
 * This file Copyright (c) 2013 Magnolia International
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
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.contentapp.detail.action.CreateItemActionDefinition;
import info.magnolia.ui.contentapp.detail.action.EditItemActionDefinition;
import info.magnolia.ui.contentapp.detail.action.RestorePreviousVersionActionDefinition;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moves <code>nodeType</code> property from action definition to its availability definition.
 * @see info.magnolia.ui.contentapp.detail.action.AbstractItemActionDefinition and its sub-classes.
 */
public class MoveActionNodeTypeRestrictionToAvailabilityTask extends QueryTask {

    private static final Logger log = LoggerFactory.getLogger(MoveActionNodeTypeRestrictionToAvailabilityTask.class);

    public static final String NODE_TYPE = "nodeType";
    public static final String AVAILABILITY_NODE = "availability";
    public static final String NODE_TYPES = "nodeTypes";
    public static final String QUERY_BASE = "select * from [mgnl:contentNode] as t where ";

    private static Class<?>[] commonActionDefinitionClasses = new Class[] {
            CreateItemActionDefinition.class,
            EditItemActionDefinition.class,
            RestorePreviousVersionActionDefinition.class,
    };

    @Inject
    public MoveActionNodeTypeRestrictionToAvailabilityTask(String path, Class<? extends ActionDefinition>... actionsToMigrateClasses) {
        super(
              "Move nodeType property to availability definition",
              "Fix availability checking of actions defined with AbstractItemActionDefinition sub-class by moving the nodeType property from action definition to availability.",
              RepositoryConstants.CONFIG, constructQuery(path, actionsToMigrateClasses));
    }

    private static String constructQuery(String path, Class<? extends ActionDefinition>[] actionsToMigrateClasses) {
        final List<Class<?>> classes = new LinkedList<Class<?>>();

        classes.addAll(Arrays.asList(commonActionDefinitionClasses));
        classes.addAll(Arrays.asList(actionsToMigrateClasses));

        final StringBuilder sb = new StringBuilder(QUERY_BASE);
        final Iterator<Class<?>> it = classes.iterator();

        while (it.hasNext()) {
            final Class<?> clazz = it.next();
            sb.append("t.class='").append(clazz.getName()).append("' ");
            if (it.hasNext()) {
                sb.append("or ");
            }
        }

        sb.append(String.format(" and isdescendantnode('%s')", path));
        return sb.toString();
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
