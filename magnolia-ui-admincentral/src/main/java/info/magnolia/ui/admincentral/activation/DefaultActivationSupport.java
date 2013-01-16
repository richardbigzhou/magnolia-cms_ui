/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.ui.admincentral.activation;

import info.magnolia.cms.exchange.ActivationSupport;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.commands.BaseActivationCommand;
import info.magnolia.objectfactory.Components;

import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;

import org.apache.commons.chain.Command;

/**
 * Implements direct activation and deactivation of content with no workflow involved.
 *
 */
public class DefaultActivationSupport implements ActivationSupport {
    private CommandsManager commandsManager = Components.getComponent(CommandsManager.class);

    @Override
    public void activate(String workspace, String path) throws ExchangeException {
        Command activate = commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "activate");
        try {
            activate.execute(getCommandContext(workspace, path));
        } catch (Exception e) {
            throw new ExchangeException(e);
        }
    }

    @Override
    public void deactivate(String workspace, String path) throws ExchangeException {
        Command deactivate = commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "deactivate");
        try {
            deactivate.execute(getCommandContext(workspace, path));
        } catch (Exception e) {
            throw new ExchangeException(e);
        }

    }

    // FIXME fgrilli this was copy&paste from info.magnolia.module.admininterface.AdminTreeMVCHandler.getCommandContext(String)
    // whose comment says "TODO this is a temporary solution"
    private Context getCommandContext(String workspace, String path) throws ItemNotFoundException, LoginException, RepositoryException {
        Context context = MgnlContext.getInstance();

        // set general parameters (repository, path, ..)
        context.put(Context.ATTRIBUTE_REPOSITORY, workspace);

        context.put(BaseActivationCommand.ATTRIBUTE_SYNDICATOR, null);
        if (path != null) {
            final String identifier = MgnlContext.getJCRSession(workspace).getNode(path).getIdentifier();
            // really only the uuid should be used to identify a piece of content and nothing else
            context.put(Context.ATTRIBUTE_UUID, identifier);
            // retrieve content again using uuid and system context to get unaltered path.
            final String realPath = MgnlContext.getSystemContext().getJCRSession(workspace).getNodeByIdentifier(identifier).getPath();
            context.put(Context.ATTRIBUTE_PATH, realPath);
        }

        return context;
    }

}
