/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.gwt.client.dialog.connector.BaseDialogState;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.DialogServerRpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Basic implementation of dialogs.
 * Provides Action registration and callbacks to the view.
 * Can be closed.
 */
public class BaseDialog extends AbstractComponent implements HasComponents, DialogView {

    protected final ListMultimap<String, DialogActionListener> actionCallbackMap = ArrayListMultimap.<String, DialogActionListener> create();

    public BaseDialog() {
        super();
        setImmediate(true);
        setContent(createDefaultContent());
        registerRpc(new DialogServerRpc() {
            @Override
            public void fireAction(String actionId) {
                Object[] array = actionCallbackMap.get(actionId).toArray();
                for (Object l : array) {
                    ((DialogActionListener)l).onActionExecuted(actionId);
                }
            }

            @Override
            public void closeSelf() {
                BaseDialog.this.closeSelf();
            }

            @Override
            public void setDescriptionVisibility(boolean isVisible) {
                BaseDialog.this.setDescriptionVisibility(isVisible);
            }
        });
    }

    @Override
    protected BaseDialogState getState() {
        return (BaseDialogState) super.getState();
    }

    @Override
    public BaseDialog asVaadinComponent() {
        return this;
    }

    public void closeSelf() {
        fireEvent(new DialogCloseEvent(this, this));
    }

    public void setDescriptionVisibility(boolean isVisible) {
        fireEvent(new DescriptionVisibilityEvent(this, isVisible));
    }

    @Override
    public void setDialogDescription(String description) {
        getState().componentDescription = description;
    }

    /* Basic component features ------------------------------------------ */

    @Override
    public Iterator<Component> iterator() {
        List<Component> components = new ArrayList<Component>() {
            @Override
            public boolean add(Component c) {
                if (c != null) {
                    return super.add(c);
                }
                return false;
            };
        };

        Collections.<Component> addAll(components, (Component) getState().content, (Component) getState().headerToolbar, (Component) getState().footerToolbar);
        return components.iterator();
    }

    public void setContent(Component newContent) {
        final Component actualContent = newContent == null ? createDefaultContent() : newContent;
        getState().content = actualContent;

        replaceComponent((Component) getState().content);
    }

    public void setHeaderToolbar(Component newHeader) {
        final Component actualHeader = newHeader == null ? createDefaultFooter() : newHeader;
        getState().headerToolbar = actualHeader;

        replaceComponent((Component) getState().headerToolbar);
    }

    public void setFooterToolbar(Component newFooter) {
        final Component actualFooter = newFooter == null ? createDefaultFooter() : newFooter;
        getState().footerToolbar = actualFooter;
        replaceComponent((Component) getState().footerToolbar);
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
    protected void replaceComponent(Component newContent) {
        if (newContent != null) {
            // set new component
            if (newContent.getParent() != null) {
                if (newContent.getParent() == this) {
                    newContent.setParent(null);
                } else {
                    // If the component already has a parent, try to remove it
                    AbstractSingleComponentContainer
                            .removeFromParent(newContent);
                }
            }
            newContent.setParent(this);
        }
        markAsDirty();

    }


    public Component getContent() {
        return (Component) getState().content;
    }

    public void removeAllActions() {
        getState().actions.clear();
        actionCallbackMap.clear();
    }

    public void removeAction(String actionName) {
        getState().actions.remove(actionName);
        actionCallbackMap.removeAll(actionName);
    }

    public void addAction(String actionName, String actionLabel) {
        getState().actions.put(actionName, actionLabel);
    }

    @Deprecated
    public void setActionLabel(String actionName, String actionLabel) {
        addAction(actionName, actionLabel);
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

    public void addAction(String actionName, String actionLabel, DialogActionListener callback) {
        addAction(actionName, actionLabel);
        addActionCallback(actionName, callback);
    }

    public void addActionCallback(String actionName, DialogActionListener callback) {
        actionCallbackMap.put(actionName, callback);
    }

    public void clearCallbacks() {
        actionCallbackMap.clear();
    }

    public void addDialogCloseHandler(Handler handler) {
        addListener("dialogCloseEvent", DialogCloseEvent.class, handler, DialogCloseEvent.ON_DIALOG_CLOSE);
    }

    public void removeDialogCloseHandler(Handler handler) {
        removeListener("dialogCloseEvent", DialogCloseEvent.class, handler);
    }

    public void addDescriptionVisibilityHandler(DescriptionVisibilityEvent.Handler handler) {
        addListener("descriptionVisibilityEvent", DescriptionVisibilityEvent.class, handler, DescriptionVisibilityEvent.ON_DESCRIPTION_VISIBILITY_CHANGED);
    }

    public void removeDescriptionVisibilityHandler(DescriptionVisibilityEvent.Handler handler) {
        removeListener("descriptionVisibilityEvent", DescriptionVisibilityEvent.class, handler);
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

    /**
     * DescriptionVisibilityEvent.
     */
    public static class DescriptionVisibilityEvent extends com.vaadin.ui.Component.Event {
        /**
         * Handler.
         */
        public interface Handler {
            void onDescriptionVisibilityChanged(DescriptionVisibilityEvent event);
        }

        public static final java.lang.reflect.Method ON_DESCRIPTION_VISIBILITY_CHANGED;

        private boolean isVisible;

        static {
            try {
                ON_DESCRIPTION_VISIBILITY_CHANGED = DescriptionVisibilityEvent.Handler.class.getDeclaredMethod("onDescriptionVisibilityChanged", new Class[] { DescriptionVisibilityEvent.class });
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        public DescriptionVisibilityEvent(Component source, boolean isVisible) {
            super(source);
            this.isVisible = isVisible;
        }

        public boolean isVisible() {
            return isVisible;
        }

    }
}
