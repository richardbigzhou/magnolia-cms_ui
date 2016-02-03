/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Basic implementation of dialogs.
 * Provides Action registration and callbacks to the view.
 * Can be closed.
 */
public class BaseDialog extends AbstractComponent implements HasComponents, DialogView {

    public static final String CANCEL_ACTION_NAME = "cancel";
    public static final String COMMIT_ACTION_NAME = "commit";

    protected final ListMultimap<String, DialogActionListener> actionCallbackMap = ArrayListMultimap.<String, DialogActionListener> create();
    private final Map<String, ShortcutListener> actionShortcutMap = new HashMap<String, ShortcutListener>();
    private Panel panel;

    public BaseDialog() {
        super();
        setImmediate(true);
        setContent(createDefaultContent());
        registerRpc(new DialogServerRpc() {
            @Override
            public void fireAction(String actionId) {
                doFireAction(actionId);
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
        // We use Panel to keep keystroke events scoped within the currently focused component. Without it, if you have more than one dialog open,
        // i.e. in different apps running at the same time, then all open dialogs would react to the keyboard event sent on the dialog currently having the focus.
        panel = new Panel(this);
        panel.setWidth(Sizeable.SIZE_UNDEFINED, Unit.PIXELS);
        panel.setHeight(100, Unit.PERCENTAGE); // Required for dynamic dialog shrinking upon window resize.
    }

    @Override
    protected BaseDialogState getState() {
        return (BaseDialogState) super.getState();
    }

    @Override
    public void attach() {
        super.attach();
        panel.focus();
    }

    @Override
    public Component asVaadinComponent() {
        return panel;
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

        Collections.addAll(components, (Component) getState().content, (Component) getState().headerToolbar, (Component) getState().footerToolbar);
        return components.iterator();
    }

    public void setContent(Component newContent) {
        final Component actualContent = newContent == null ? createDefaultContent() : newContent;
        if (getState().content != null) {
            ((Component)getState().content).setParent(null);
        }
        getState().content = actualContent;
        adoptComponent((Component) getState().content);
    }

    public void setHeaderToolbar(Component newHeader) {
        final Component actualHeader = newHeader == null ? createDefaultHeader() : newHeader;
        if (getState().headerToolbar != null) {
            ((Component)getState().headerToolbar).setParent(null);
        }
        getState().headerToolbar = actualHeader;
        adoptComponent((Component) getState().headerToolbar);
    }

    public void setFooterToolbar(Component newFooter) {
        final Component actualFooter = newFooter == null ? createDefaultFooter() : newFooter;
        if (getState().footerToolbar != null) {
            ((Component)getState().footerToolbar).setParent(null);
        }
        getState().footerToolbar = actualFooter;
        adoptComponent((Component) getState().footerToolbar);
    }

    /**
     * Sets a Component
     * <p>
     * The composition root must be set to non-null value before the component can be used. The composition root can only be set once.
     * </p>
     *
     * @param newContent
     * the root of the composition component tree.
     */
    protected void adoptComponent(Component newContent) {
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
        getState().actionOrder.clear();
        getState().actions.clear();
        actionCallbackMap.clear();
    }

    public void removeAction(String actionName) {
        getState().actionOrder.remove(actionName);
        getState().actions.remove(actionName);
        actionCallbackMap.removeAll(actionName);
        removeShortcut(actionName);
    }

    /**
     * If the action name is <code> {@value #COMMIT_ACTION_NAME}</code> a <code>CTRL+S</code> shortcut will be added to perform the action.<br>
     * If the action name is <code> {@value #CANCEL_ACTION_NAME}</code> a <code>CTRL+C</code> and an <code>ESC</code> shortcuts will be added to perform the action.
     */
    public void addAction(String actionName, String actionLabel) {
        if (!getState().actionOrder.contains(actionName)) {
            getState().actionOrder.add(actionName);
        }
        getState().actions.put(actionName, actionLabel);
        if (COMMIT_ACTION_NAME.equals(actionName)) {
            addShortcut(actionName, KeyCode.S, ModifierKey.CTRL, ModifierKey.ALT);
        } else if (CANCEL_ACTION_NAME.equals(actionName)) {
            // addShortcut(actionName, KeyCode.ESCAPE);
            addShortcut(actionName, KeyCode.C, ModifierKey.CTRL, ModifierKey.ALT);
        }
    }

    public void setDefaultAction(String actionName) {
        getState().defaultActionName = actionName;
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

    public void showCloseButton() {
        getState().hasCloseButton = true;
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

    private void doFireAction(final String actionId) {
        Object[] array = actionCallbackMap.get(actionId).toArray();
        for (Object l : array) {
            ((DialogActionListener) l).onActionExecuted(actionId);
        }
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
                ON_DIALOG_CLOSE = DialogCloseEvent.Handler.class.getDeclaredMethod("onClose", new Class[] { DialogCloseEvent.class });
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

    protected void addShortcut(final String actionName, final int keyCode, final int... modifiers) {
        final ShortcutListener shortcut = new ShortcutListener("", keyCode, modifiers) {

            @Override
            public void handleAction(Object sender, Object target) {
                doFireAction(actionName);
            }
        };
        panel.addShortcutListener(shortcut);
        actionShortcutMap.put(actionName, shortcut);
    }

    protected void removeShortcut(String actionName) {
        removeShortcutListener(actionShortcutMap.get(actionName));
        actionShortcutMap.remove(actionName);
    }

}
