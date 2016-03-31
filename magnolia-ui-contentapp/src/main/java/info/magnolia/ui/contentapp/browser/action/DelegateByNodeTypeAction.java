/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;

/**
 * Action which delegates to another action by node type.
 */
public class DelegateByNodeTypeAction extends AbstractAction<DelegateByNodeTypeActionDefinition> {

    private final JcrItemAdapter item;
    private final ActionExecutor actionExecutor;
    private final AvailabilityChecker availabilityChecker;
    private final ContentConnector contentConnector;

    @Inject
    public DelegateByNodeTypeAction(DelegateByNodeTypeActionDefinition definition, ActionExecutor actionExecutor, JcrItemAdapter item, AvailabilityChecker availabilityChecker, ContentConnector contentConnector) {
        super(definition);
        this.item = item;
        this.actionExecutor = actionExecutor;
        this.availabilityChecker = availabilityChecker;
        this.contentConnector = contentConnector;
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (item.getJcrItem().isNode()) {
            Node node = (Node) item.getJcrItem();
            try {
                String nodeType = node.getPrimaryNodeType().getName();
                String action = resolveActionForNodeType(nodeType);
                if (StringUtils.isNotBlank(action)) {
                    executeAction(action);
                }
            } catch (RepositoryException e) {
                throw new ActionExecutionException("Failed to determine type of action for " + node + ".\n" + e.getMessage(), e);
            }
        }
    }

    private void executeAction(String actionName) throws ActionExecutionException {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        if (actionDefinition != null) {
            AvailabilityDefinition availability = actionDefinition.getAvailability();
            List<Object> itemIdAsList = Arrays.<Object>asList(item.getItemId());
            if (availabilityChecker.isAvailable(availability, itemIdAsList)) {
                Object[] args = new Object[]{contentConnector.getItem(item.getItemId())};
                actionExecutor.execute(actionName, args);
            }
        }
    }

    private String resolveActionForNodeType(String nodeType) {
        for (DelegateByNodeTypeActionDefinition.NodeTypeToActionMapping nodeTypeToActionMapping : getDefinition().getNodeTypeToActionMappings()) {
            if (nodeType.equals(nodeTypeToActionMapping.getNodeType())) {
                return nodeTypeToActionMapping.getAction();
            }
        }
        return null;
    }

}
