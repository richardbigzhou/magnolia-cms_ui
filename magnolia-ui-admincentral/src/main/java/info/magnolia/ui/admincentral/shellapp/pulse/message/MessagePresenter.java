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
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.MessageItem;

import javax.inject.Inject;

import com.vaadin.data.util.BeanItem;

/**
 * The message detail presenter.
 */
public final class MessagePresenter extends ItemPresenter<Message> {

    private MessagesManager messagesManager;

    @Inject
    public MessagePresenter(ItemView view, MessagesManager messagesManager, ItemActionExecutor itemActionExecutor, AvailabilityChecker checker, ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter, I18nizer i18nizer) {
        super(view, itemActionExecutor, checker, itemViewDefinitionRegistry, formbuilder, actionbarPresenter, i18nizer);
        this.messagesManager = messagesManager;
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        void showList();
    }

    @Override
    protected String getItemViewName(Message item) {
        return item.getView();
    }

    @Override
    protected void setItemViewTitle(Message item, ItemView view) {
        view.setTitle(item.getSubject());
    }

    @Override
    protected Message getPulseItemById(String itemId) {
        final String userId = MgnlContext.getUser().getName();
        return messagesManager.getMessageById(userId, itemId);
    }

    @Override
    protected BeanItem<Message> asBeanItem(Message item) {
        return new MessageItem(item);
    }
}
