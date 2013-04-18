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
import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.app.action.CommandActionBase;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

/**
 * UI action that allows to activate a single page (node) or recursively with all its sub-nodes depending on the value of {@link ActivationActionDefinition#isRecursive()}.
 */
public class ActivationAction extends CommandActionBase<ActivationActionDefinition> {

    private final JcrItemNodeAdapter node;

    private final EventBus eventBus;

    @Inject
    public ActivationAction(final ActivationActionDefinition definition, final JcrItemNodeAdapter item, final CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition, item, commandsManager);
        this.node = item;
        this.eventBus = eventBus;
    }

    @Override
    protected Map<String, Object> buildParams(final Node node) {
        Map<String, Object> params = super.buildParams(node);
        params.put(Context.ATTRIBUTE_RECURSIVE, getDefinition().isRecursive());
        return params;
    }

    @Override
    protected void onPostExecute() throws Exception {
        Node jcrNode = node.getNodeFromRepository();
        eventBus.fireEvent(new ContentChangedEvent(jcrNode.getSession().getWorkspace().getName(), jcrNode.getPath()));
    }
}
