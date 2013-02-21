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
package info.magnolia.ui.admincentral.app.content;

import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.model.workbench.action.WorkbenchActionFactory;

import javax.inject.Inject;

/**
 * ContentActionExecutor.
 */
public class ContentActionExecutor implements ActionExecutor {

    private MessagesManager messagesManager;
    private WorkbenchActionFactory actionFactory;
    private SubAppDescriptor subAppDescriptor;

    @Inject
    public ContentActionExecutor(MessagesManager messagesManager, final WorkbenchActionFactory actionFactory, final SubAppContext subAppContext) {
        this.messagesManager = messagesManager;
        this.actionFactory = actionFactory;
        this.subAppDescriptor = subAppContext.getSubAppDescriptor();
    }

    @Override
    public void execute(String actionName, Object... args) throws ActionExecutionException {

        Message message = new Message();
        message.setSubject("Action execute");
        message.setMessage(actionName);
        message.setType(MessageType.INFO);

        messagesManager.sendLocalMessage(message);
        ActionDefinition actionDefinition = getActionDefinition(actionName);
        Action action = actionFactory.createAction(actionDefinition, args);

        action.execute();

    }

    private ActionDefinition getActionDefinition(String actionName) {
        return subAppDescriptor.getActions().get(actionName);
    }
}
