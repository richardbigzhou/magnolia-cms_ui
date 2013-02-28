/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.actionbar;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.event.SubAppEventBusConfigurer;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Default presenter for an action bar.
 */
public class ActionbarPresenter extends ActionbarPresenterBase {

    private static final Logger log = LoggerFactory.getLogger(ActionbarPresenter.class);

    private final AppContext appContext;

    @Inject
    public ActionbarPresenter(@Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) EventBus subAppEventBus, AppContext appContext) {
        super(subAppEventBus);
        this.appContext = appContext;
    }

    @Override
    public void onChangeFullScreen(boolean isFullScreen) {
        if (isFullScreen) {
            appContext.enterFullScreenMode();
        } else {
            appContext.exitFullScreenMode();
        }
    }
    
    public void createAndExecuteAction(final ActionDefinition actionDefinition, String workspace, String absPath) {
        if (actionDefinition == null || StringUtils.isBlank(workspace)) {
            Message warn = createMessage(MessageType.WARNING, "Got invalid arguments: action definition is " + actionDefinition + ", workspace is " + workspace, "");
            appContext.sendLocalMessage(warn);
        }
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            if (absPath == null || !session.itemExists(absPath)) {
                log.debug("{} does not exist anymore. Was it just deleted? Resetting path to root...", absPath);
                absPath = "/";
            }
            final javax.jcr.Item item = session.getItem(absPath);
            final Action action = getActionFactory().createAction(actionDefinition, item);
            if (action == null) {
                Message warn = createMessage(MessageType.WARNING, "Could not create action from actionDefinition. Action is null.", "");
                appContext.sendLocalMessage(warn);
            } else {
                action.execute();
            }
            appContext.showConfirmationMessage("Action executed successfully.");
        } catch (RepositoryException e) {
            Message error = createMessage(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            appContext.broadcastMessage(error);
        } catch (ActionExecutionException e) {
            Message error = createMessage(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            appContext.broadcastMessage(error);
        }
    }

    private Message createMessage(MessageType type, String subject, String message) {
        final Message msg = new Message();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setType(type);
        return msg;
    }

}
