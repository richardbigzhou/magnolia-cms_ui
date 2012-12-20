/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.dialog;

import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent.Handler;
import info.magnolia.ui.vaadin.gwt.client.dialog.connector.BaseDialogState;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.ActionFiringServerRpc;

import java.util.Iterator;

import com.google.gwt.thirdparty.guava.common.collect.ArrayListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.ListMultimap;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Basic implementation of dialogs.
 * Is capable of displaying any content inside it's content component.
 * Provides Action registration and callbacks to the view.
 */
public class BaseDialog extends AbstractSingleComponentContainer implements DialogView {

    private final ListMultimap<String, DialogActionListener> actionCallbackMap = ArrayListMultimap.<String, DialogActionListener> create();

    public BaseDialog() {
        setImmediate(true);
        setContent(createDefaultContent());
        registerRpc(new ActionFiringServerRpc() {
            @Override
            public void fireAction(String actionId) {
                final Iterator<DialogActionListener> it = actionCallbackMap.get(actionId).iterator();
                while (it.hasNext()) {
                    it.next().onActionExecuted(actionId);
                }
            }
            
            @Override
            public void closeSelf() {
                closeSelf();
            }
        });
    }

    @Override
    public void setContent(Component content) {
        final Component actualContent = content == null ? createDefaultContent() : content;
        super.setContent(actualContent);
    }

    public void closeSelf() {
        fireEvent(new DialogCloseEvent(this, this));
    }
    
    public void addAction(String actionName, String actionLabel) {
        getState().actions.put(actionName, actionLabel);
    }
   
    @Deprecated
    public void setActionLabel(String actionName, String actionLabel) {
        addAction(actionName, actionLabel);
    }

    @Override
    public void setDialogDescription(String description) {
        getState().componentDescription = description;
    }

    @Override
    protected BaseDialogState getState() {
        return (BaseDialogState)super.getState();
    }
    
    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        getContent().setCaption(caption);
    }

    protected Component createDefaultContent() {
        return new VerticalLayout();
    }

    @Override
    public BaseDialog asVaadinComponent() {
        return this;
    }

    public void addAction(String actionName, String actionLabel, DialogActionListener callback) {
        addAction(actionName, actionLabel);
        addActionCallback(actionName, callback);
    }

    public void addActionCallback(String actionName, DialogActionListener callback) {
        actionCallbackMap.put(actionName, callback);
    }

    public void addDialogCloseHandler(Handler handler) {
        addListener("dialogCloseEvent", DialogCloseEvent.class, handler, DialogCloseEvent.ON_DIALOG_CLOSE);
    }
    
    public void removeDialogCloseHandler(Handler handler) {
        removeListener("dialogCloseEvent", DialogCloseEvent.class, handler);
    }
    
    /**
     * DialogCloseEvent. 
     */
    public static class DialogCloseEvent extends com.vaadin.ui.Component.Event {
        
        /**
         * Handler.
         */
        public interface Handler {
            void onClose(DialogCloseEvent event);
        }
        
        public static final java.lang.reflect.Method ON_DIALOG_CLOSE;

        public DialogView view;
        
        static {
            try {
                ON_DIALOG_CLOSE = DialogCloseEvent.Handler.class.getDeclaredMethod(
                        "onClose", new Class[] { DialogCloseEvent.class });
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        public DialogCloseEvent(Component source, DialogView view) {
            super(source);
            this.view = view;
        }

        public DialogView getView() {
            return view;
        }
    }

    public void clearCallbacks() {
        actionCallbackMap.clear();
    }

}
