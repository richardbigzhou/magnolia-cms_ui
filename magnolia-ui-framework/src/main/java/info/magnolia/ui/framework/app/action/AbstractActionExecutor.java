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
package info.magnolia.ui.framework.app.action;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;

import javax.inject.Inject;

/**
 * A base implementation of {@link ActionExecutor}. Subclasses need only to implement {@link #getActionDefinition(String)}.
 * Creates the {@link Action} from the implementation class using componentProvider and binds the ActionDefinition to the Action.
 */
public abstract class AbstractActionExecutor implements ActionExecutor {

    private ComponentProvider componentProvider;

    @Inject
    public AbstractActionExecutor(final ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public final void execute(String actionName, Object... args) throws ActionExecutionException {
        try {
            Action action = createAction(actionName, args);
            action.execute();
        }
        catch (ConfigurationException e) {
            throw new ActionExecutionException(e);
        }
    }

    /**
     * Creates an action using the implementation configured for the given action definition. The
     * parameters are made available for injection when the instance is created. The definition
     * object given is also available for injection.
     */
    private Action createAction(String actionName, Object... args) throws ConfigurationException {
        final ActionDefinition actionDefinition = getActionDefinition(actionName);
        if (actionDefinition != null) {
            Class<? extends Action> implementationClass = actionDefinition.getImplementationClass();
            if (implementationClass != null) {
                Object[] combinedParameters = new Object[args.length + 1];
                combinedParameters[0] = actionDefinition;
                System.arraycopy(args, 0, combinedParameters, 1, args.length);

                return componentProvider.newInstance(implementationClass, combinedParameters);
            }
        }

        throw new ConfigurationException("Could not create action: " + actionName);
    }

}
