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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.dialog.DialogView.DialogActionCallback;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Base implementation of {@link DialogPresenter}.
 */
public class BaseDialogPresenter implements DialogPresenter {
        
    private EventBus eventBus = new SimpleEventBus();
   
    private DialogActionFactory actionFactory;
    
    private final DialogView view;
    
    private final EventBus adminCentralEventBus;
    
    @Inject
    public BaseDialogPresenter(final DialogActionFactory actionFactory, DialogView view, @Named("admincentral") EventBus eventBus) {
        this.actionFactory = actionFactory;
        this.view = view;
        this.adminCentralEventBus = eventBus;
    }
    
    @Override
    public Callback getCallback() {
        return null;
    }

    @Override
    public DialogView getView() {
        return view;
    }

    @Override
    public EventBus getEventBus() {
        return adminCentralEventBus;
    }

    @Override
    public void closeDialog() {
        eventBus.fireEvent(new DialogCloseEvent(this));
    }

    @Override
    public void addActionFromDefinition(final DialogActionDefinition dialogActionDefinition) {
        addAction(dialogActionDefinition.getName(), dialogActionDefinition.getLabel(), new DialogActionCallback() {    
            @Override
            public void onActionExecuted() {
                final ActionDefinition actionDefinition = dialogActionDefinition.getActionDefinition();
                final Action action = actionFactory.createAction(actionDefinition, BaseDialogPresenter.this);
                try {
                    action.execute();
                } catch (final ActionExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public HandlerRegistration addDialogCloseListener(DialogCloseEvent.Handler handler) {
        return eventBus.addHandler(DialogCloseEvent.class, handler);
    }

    @Override
    public void addAction(String actionName, String actionLabel, DialogActionCallback callback) {
        view.asVaadinComponent().addAction(actionName, actionLabel, callback);
        
    }

    @Override
    public void addActionCallback(String actionName, DialogActionCallback callback) {
        view.asVaadinComponent().addActionCallback(actionName, callback);
    }

}
