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
package info.magnolia.ui.admincentral.activation.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.module.activation.commands.ActivationCommand;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.inject.Inject;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI action that allows to activate a single page (node) or recursively with all its sub-nodes depending on the value of {@link ActivationActionDefinition#isRecursive()}.
 */
public class ActivationAction extends BaseActivationAction<ActivationActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ActivationAction.class);

    @Inject
    public ActivationAction(final ActivationActionDefinition definition, final Node node, final CommandsManager commandsManager) {
        super(definition, node, commandsManager);
    }

    @Override
    public void execute() throws ActionExecutionException {
        final String commandName = getDefinition().getCommand();
        final Command command = getCommandsManager().getCommand(commandName);

        if (command == null) {
            throw new ActionExecutionException(String.format("Could not find command [%s] in any catalog", commandName));
        }
        try {
            final ActivationCommand activationCommand = (ActivationCommand) command;
            activationCommand.setRecursive(((ActivationActionDefinition)getDefinition()).isRecursive());

            log.debug("Is activation recursive ? {}", activationCommand.isRecursive());

            getCommandsManager().executeCommand(activationCommand, getParams());

        } catch (Exception e) {
            throw new ActionExecutionException("An exception occured during activation ", e);
        }
    }


}
