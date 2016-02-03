/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation of {@link ActionExecutor}. Creates the {@link Action} from the implementation class
 * using a {@link ComponentProvider} and binds the ActionDefinition to the Action. Subclasses need only implement
 * {@link #getActionDefinition(String)}.
 *
 * @see Action
 * @see ActionDefinition
 * @see ActionExecutor
 */
public abstract class AbstractActionExecutor implements ActionExecutor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ComponentProvider componentProvider;

    @Inject
    public AbstractActionExecutor(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public final void execute(String actionName, Object... args) throws ActionExecutionException {
        try {
            Action action = createAction(actionName, args);
            action.execute();
        } catch (RuntimeException e) {
            throw new ActionExecutionException("Action execution failed for action: " + actionName, e);
        }
    }

    /**
     * Creates an action using the implementation configured for the given action definition. The
     * parameters are made available for injection when the instance is created. The definition
     * object is also available for injection.
     */
    protected Action createAction(String actionName, Object... args) throws ActionExecutionException {

        final ActionDefinition actionDefinition = getActionDefinition(actionName);
        if (actionDefinition == null) {
            throw new ActionExecutionException("No definition exists for action: " + actionName);
        }

        Class<? extends Action> implementationClass = actionDefinition.getImplementationClass();
        if (implementationClass == null) {
            throw new ActionExecutionException("No action class set for action: " + actionName);
        }

        Object[] combinedParameters = new Object[args.length + 1];
        combinedParameters[0] = actionDefinition;
        System.arraycopy(args, 0, combinedParameters, 1, args.length);

        try {
            return componentProvider.newInstance(implementationClass, combinedParameters);
        } catch (MgnlInstantiationException e) {
            throw new ActionExecutionException("Could not instantiate action class for action: " + actionName, e);
        }
    }

    @Override
    public boolean isAvailable(String actionName, Item... items) {

        // sanity check
        if (items == null || items.length == 0) {
            return false;
        }

        ActionDefinition actionDefinition = getActionDefinition(actionName);
        if (actionDefinition == null) {
            return false;
        }

        AvailabilityDefinition availability = actionDefinition.getAvailability();

        // If a rule class is set, evaluate it first
        if ((availability.getRuleClass() != null)) {
            // if the rule class cannot be instantiated, or the rule returns false
            AvailabilityRule rule = componentProvider.newInstance(availability.getRuleClass());
            if (rule == null || !rule.isAvailable(items)) {
                return false;
            }
        }

        // We don't support bulk actions, at least not yet
        if (items.length > 1) {
            return false;
        }

        // Validate that the user has all the required roles
        if (!availability.getAccess().hasAccess(MgnlContext.getUser())) {
            return false;
        }

        for (Item item : items) {
            if (!isAvailableForItem(availability, item)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAvailableForItem(AvailabilityDefinition availability, Item item) {

        if (item == null) {
            return availability.isRoot();
        }

        if (!item.isNode()) {
            return availability.isProperties();
        }

        // Must have _any_ of the node types if any are specified, otherwise its available by default
        if (availability.getNodeTypes().isEmpty()) {
            return availability.isNodes();
        }

        for (String nodeType : availability.getNodeTypes()) {
            try {
                if (NodeUtil.isNodeType((Node)item, nodeType)) {
                    return true;
                }
            } catch (RepositoryException e) {
                log.error("Could not determine node type of node " + NodeUtil.getNodePathIfPossible((Node) item));
            }
        }

        return false;
    }
}
