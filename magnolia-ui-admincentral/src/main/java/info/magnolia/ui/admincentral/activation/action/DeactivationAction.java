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
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * UI action that allows to deactivate a single page (node).
 */
public class DeactivationAction extends ActionBase<DeactivationActionDefinition> {

    private CommandsManager commandsManager = Components.getComponent(CommandsManager.class);
    private Map<String, Object> params = new HashMap<String, Object>();

    @Inject
    public DeactivationAction(final DeactivationActionDefinition definition, final Node node) {
        super(definition);
        params = buildParams(node);
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            commandsManager.executeCommand("deactivate", params);
        } catch (Exception e) {
            throw new ActionExecutionException("An exception occured during activation ", e);
        }
    }

    private Map<String, Object> buildParams(final Node node) {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            final String path = node.getPath();
            final String workspace = node.getSession().getWorkspace().getName();
            params.put(Context.ATTRIBUTE_REPOSITORY, workspace);

            if (path != null) {

                final String identifier = SessionUtil.getNode(workspace, path).getIdentifier();
                // really only the uuid should be used to identify a piece of content and nothing else
                params.put(Context.ATTRIBUTE_UUID, identifier);
                // retrieve content again using uuid and system context to get unaltered path.
                final String realPath = MgnlContext.getSystemContext().getJCRSession(workspace).getNodeByIdentifier(identifier).getPath();
                params.put(Context.ATTRIBUTE_PATH, realPath);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return params;
    }

}
