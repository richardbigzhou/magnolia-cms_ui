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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.message.definition.MessageViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.message.registry.MessageViewDefinitionRegistry;
import info.magnolia.ui.form.FormBuilder;
import info.magnolia.ui.vaadin.form.FormView;
import info.magnolia.ui.vaadin.view.View;

import javax.inject.Inject;

import com.vaadin.data.Item;

/**
 * MessagePresenter.
 */
public class MessagePresenter implements MessageView.Listener, ActionbarPresenter.Listener {

    private final MessageView view;
    private MessageViewDefinitionRegistry messageViewDefinitionRegistry;
    private FormBuilder formbuilder;
    private ActionbarPresenter actionbarPresenter;
    private Listener listener;

    @Inject
    public MessagePresenter(MessageView view, MessageViewDefinitionRegistry messageViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter) {
        this.view = view;
        this.messageViewDefinitionRegistry = messageViewDefinitionRegistry;
        this.formbuilder = formbuilder;
        this.actionbarPresenter = actionbarPresenter;

        view.setListener(this);
        actionbarPresenter.setListener(this);
    }

    public View start(Item messageItem) {
        String messageView = "ui-admincentral:default";
        try {
            MessageViewDefinition messageViewDefinition = messageViewDefinitionRegistry.get(messageView);
            FormView formView = formbuilder.buildForm(messageViewDefinition.getForm(), messageItem, null);
            view.setMessageView(formView);

            view.setActionbarView(actionbarPresenter.start(messageViewDefinition.getActionbar()));
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
    public void onExecute(String actionName) {

    }

    @Override
    public String getLabel(String actionName) {
        return null;
    }

    @Override
    public String getIcon(String actionName) {
        return null;
    }

    @Override
    public void setFullScreen(boolean fullscreen) {

    }

    public interface Listener {
        void showList();
    }
}
