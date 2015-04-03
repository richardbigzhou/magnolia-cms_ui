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
package info.magnolia.security.app.action;

import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DeleteAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.List;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Action that will only delete a folder if it has no sub nodes.
 *
 * @see DeleteEmptyFolderActionDefinition
 */
public class DeleteEmptyFolderAction extends DeleteAction<DeleteEmptyFolderActionDefinition> {

    public DeleteEmptyFolderAction(DeleteEmptyFolderActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n);
    }

    public DeleteEmptyFolderAction(DeleteEmptyFolderActionDefinition definition, List<JcrItemAdapter> items, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, items, commandsManager, eventBus, uiContext, i18n);
    }

    @Override
    public void onPreExecute() throws Exception {

        final JcrItemAdapter item = getCurrentItem();

        if (item != null) {
            try {
                final String workspaceName = item.getWorkspace();
                boolean empty = MgnlContext.doInSystemContext(new JCRSessionOp<Boolean>(workspaceName) {

                    @Override
                    public Boolean exec(Session session) throws RepositoryException {
                        Item jcrItem = JcrItemUtil.getJcrItem(item.getItemId());
                        if (jcrItem.isNode() && NodeUtil.getNodes((Node) jcrItem).iterator().hasNext()) {
                            return false;
                        }
                        return true;
                    }
                });

                if (!empty) {
                    throw new ActionExecutionException(getDefinition().getFolderNotEmptyErrorMessage());
                }

            } catch (RepositoryException e) {
                throw new ActionExecutionException(e);
            }
        }
        super.onPreExecute();
    }

    @Override
    protected void onError(Exception e) {
        // Do nothing. The error message is already displayed by the AbsractMultiItemAction
    }
}
