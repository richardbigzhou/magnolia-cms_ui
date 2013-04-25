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
import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.action.CommandActionBase;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

/**
 * UI action that allows to activate a single page (node) or recursively with all its sub-nodes depending on the value of {@link ActivationActionDefinition#isRecursive()}.
 */
public class ActivationAction extends CommandActionBase<ActivationActionDefinition> {

    private final JcrItemNodeAdapter node;

    private final EventBus eventBus;

    private final SubAppContext subAppContext;

    @Inject
    public ActivationAction(final ActivationActionDefinition definition, final JcrItemNodeAdapter item, final CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, SubAppContext subAppContext) {
        super(definition, item, commandsManager, subAppContext);
        this.node = item;
        this.eventBus = eventBus;
        this.subAppContext = subAppContext;
    }

    @Override
    protected Map<String, Object> buildParams(final Node node) {
        Map<String, Object> params = super.buildParams(node);
        params.put(Context.ATTRIBUTE_RECURSIVE, getDefinition().isRecursive());
        return params;
    }

    @Override
    protected void onError(Exception e) {
        subAppContext.openNotification(MessageStyleTypeEnum.ERROR, true, getDefinition().getErrorMessage());
    }

    @Override
    protected void onPostExecute() throws Exception {
        Node jcrNode = node.getNodeFromRepository();
        eventBus.fireEvent(new ContentChangedEvent(jcrNode.getSession().getWorkspace().getName(), jcrNode.getPath()));

        // Display a notification

        Context context = MgnlContext.getInstance();
        boolean result = (Boolean) context.getAttribute(COMMAND_RESULT);
        String message;
        MessageStyleTypeEnum messageStyleType;
        if (result) {
            message = getDefinition().getSuccessMessage();
            messageStyleType = MessageStyleTypeEnum.INFO;
        } else {
            message = getDefinition().getFailureMessage();
            messageStyleType = MessageStyleTypeEnum.ERROR;
        }

        if (StringUtils.isNotBlank(message)) {
            subAppContext.openNotification(messageStyleType, true, message);
        }

    }
}
