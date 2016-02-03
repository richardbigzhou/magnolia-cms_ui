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
package info.magnolia.ui.admincentral.usermenu;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.usermenu.action.UserActionExecutor;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.message.MessagesManager;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter for the {@link UserMenuView} used to display a users {@link MgnlUserManager#PROPERTY_TITLE} and providing
 * actions obtained by the {@link UserActionExecutor}.
 */
public class UserMenuPresenter implements UserMenuView.Listener {
    private static final Logger log = LoggerFactory.getLogger(UserMenuPresenter.class);

    private final UserMenuView view;
    private UserActionExecutor actionExecutor;
    private MessagesManager messagesManager;

    @Inject
    public UserMenuPresenter(UserMenuView view, UserActionExecutor actionExecutor, MessagesManager messagesManager) {
        this.view = view;
        this.actionExecutor = actionExecutor;
        this.messagesManager = messagesManager;
     }

    public View getView() {
        return view;
    }

    public View start() {
        view.setListener(this);
        String title = MgnlContext.getUser().getProperty(MgnlUserManager.PROPERTY_TITLE);
        String name = MgnlContext.getUser().getName();
        String caption = "";
        if (StringUtils.isNotEmpty(title) && !title.equals(name)) {
            caption = String.format("%s (%s)", title, name);
        } else {
            caption = name;
        }
        view.setCaption(caption);

        for (ActionDefinition action : actionExecutor.getActions()) {
            view.addAction(action.getName(), action.getLabel(), action.getIcon());
        }
        return view;
    }

    @Override
    public void onAction(String actionName) {
        try {
            actionExecutor.execute(actionName);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            messagesManager.sendLocalMessage(error);
        }
    }
}
