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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.thirdparty.guava.common.collect.ArrayListMultimap;
import com.google.gwt.thirdparty.guava.common.collect.ListMultimap;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Basic implementation of dialogs.
 * Is capable of displaying any content inside it's content component.
 * Can have header and footer components set.
 * Provides Action registration and callbacks to the view.
 */
public class BaseDialog extends AbstractComponent implements HasComponents, DialogView {

    private final ListMultimap<String, DialogActionListener> actionCallbackMap = ArrayListMultimap.<String, DialogActionListener>create();

    private Component content;
    private Component header;
    private Component footer;

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
                BaseDialog.this.closeSelf();
            }
        });
    }

    /* Basic component features ------------------------------------------ */

    @Override
    public Iterator<Component> iterator() {
        /*if (getCompositionRoot() != null) {
            return Collections.singletonList(getCompositionRoot()).iterator();
        } else {
            return Collections.<Component> emptyList().iterator();
        }*/
        // TODO: I think should return content,headerToolbar,footerToolbar
        // TODO: Should check for null on the components.
        List<Component> components = new ArrayList<Component>() {
            @Override
            public boolean add(Component c) {
                if (c != null) {
                    return super.add(c);
                }
                return false;
            };
        };

        Collections.<Component> addAll(components, content, header, footer);
        return components.iterator();
        // return Collections.<Component> emptyList().iterator();
    }

    public void setContent(Component newContent) {
        final Component actualContent = newContent == null ? createDefaultContent() : newContent;
        replaceComponent(actualContent, content);
    }

    public void setHeader(Component newHeader) {
        final Component actualHeader = newHeader == null ? createDefaultFooter() : newHeader;
        replaceComponent(actualHeader, header);
    }

    public void setFooter(Component newFooter) {
        final Component actualFooter = newFooter == null ? createDefaultFooter() : newFooter;
        replaceComponent(actualFooter, footer);
    }

    /**
     * Sets a Component
     * <p>
     * The composition root must be set to non-null value before the component can be used. The composition root can only be set once.
     * </p>
     * 
     * @param newContent
     *            the root of the composition component tree.
     */
    // TODO: Edit params
    protected void replaceComponent(Component newContent, Component currentContent) {
        if (newContent != currentContent) {
            if (currentContent != null && currentContent.getParent() == this) {
                // remove old component
                currentContent.setParent(null);
            }
            if (newContent != null) {
                // set new component
                if (newContent.getParent() != null) {
                    // If the component already has a parent, try to remove it
                    AbstractSingleComponentContainer
                            .removeFromParent(newContent);
                }
                newContent.setParent(this);
            }
            currentContent = newContent;
            markAsDirty();
        }
    }


    public Component getContent() {
        return content;
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
        return (BaseDialogState) super.getState();
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        getContent().setCaption(caption);
    }

    protected Component createDefaultContent() {
        return new VerticalLayout();
    }

    protected Component createDefaultHeader() {
        return new HorizontalLayout();
    }

    protected Component createDefaultFooter() {
        return new HorizontalLayout();
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
                ON_DIALOG_CLOSE = DialogCloseEvent.Handler.class.getDeclaredMethod("onClose", new Class[]{DialogCloseEvent.class});
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
