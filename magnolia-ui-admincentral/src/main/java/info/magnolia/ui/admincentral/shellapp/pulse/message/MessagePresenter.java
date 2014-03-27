/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.MessageItem;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

/**
 * The message detail presenter.
 */
public final class MessagePresenter implements ItemView.Listener, ActionbarPresenter.Listener {

    private final ItemView view;
    private MessagesManager messagesManager;
    private ItemActionExecutor itemActionExecutor;
    private ItemViewDefinitionRegistry itemViewDefinitionRegistry;
    private FormBuilder formbuilder;
    private ActionbarPresenter actionbarPresenter;
    private Listener listener;
    private Message message;
    private I18nizer i18nizer;

    @Inject
    public MessagePresenter(ItemView view, MessagesManager messagesManager, ItemActionExecutor itemActionExecutor, ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter, I18nizer i18nizer) {
        this.view = view;
        this.messagesManager = messagesManager;
        this.itemActionExecutor = itemActionExecutor;
        this.itemViewDefinitionRegistry = itemViewDefinitionRegistry;
        this.formbuilder = formbuilder;
        this.actionbarPresenter = actionbarPresenter;
        this.i18nizer = i18nizer;

        view.setListener(this);
        actionbarPresenter.setListener(this);
    }

    public View start(String messageId) {
        this.message = messagesManager.getMessageById(MgnlContext.getUser().getName(), messageId);
        String messageView = "ui-admincentral:default";
        view.setTitle(message.getSubject());
        try {
            final String specificMessageView = message.getView();
            if (StringUtils.isNotEmpty(specificMessageView)) {
                messageView = specificMessageView;
            }
            ItemViewDefinition itemViewDefinition = itemViewDefinitionRegistry.get(messageView);
            itemViewDefinition = i18nizer.decorate(itemViewDefinition);

            itemActionExecutor.setMessageViewDefinition(itemViewDefinition);
            MessageItem messageItem = new MessageItem(message);

            View mView = formbuilder.buildView(itemViewDefinition.getForm(), messageItem);
            view.setItemView(mView);

            view.setActionbarView(actionbarPresenter.start(itemViewDefinition.getActionbar(), itemViewDefinition.getActions()));
        } catch (RegistrationException e) {
            throw new RuntimeException("Could not retrieve messageView for " + messageView, e);
        }
        return view;
    }

    @Override
    public void onNavigateToList() {
        listener.showList();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        try {
            itemActionExecutor.execute(actionName, message, this, itemActionExecutor);

        } catch (ActionExecutionException e) {
            throw new RuntimeException("Could not execute action " + actionName, e);
        }
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        void showList();
    }
}
