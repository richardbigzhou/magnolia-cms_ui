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

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.vaadin.gwt.client.dialog.connector.BaseDialogState;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.DialogServerRpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Basic implementation of dialogs.
 * Provides Action registration and callbacks to the view.
 * Can be closed.
 */
public class BaseDialog extends AbstractComponent implements HasComponents {

    public static final String CANCEL_ACTION_NAME = "cancel";

    public static final String COMMIT_ACTION_NAME = "commit";

    public BaseDialog() {
        super();
        setImmediate(true);
        setContent(createDefaultContent());
        registerRpc(new DialogServerRpc() {

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

    public void closeSelf() {
        fireEvent(new DialogCloseEvent(this, this));
    }

    public void setDescriptionVisibility(boolean isVisible) {
        fireEvent(new DescriptionVisibilityEvent(this, isVisible));
    }

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

    public void showCloseButton() {
        getState().hasCloseButton = true;
    }

    public void addDescriptionVisibilityHandler(DescriptionVisibilityEvent.Handler handler) {
        addListener("descriptionVisibilityEvent", DescriptionVisibilityEvent.class, handler, DescriptionVisibilityEvent.ON_DESCRIPTION_VISIBILITY_CHANGED);
    }

    public void removeDescriptionVisibilityHandler(DescriptionVisibilityEvent.Handler handler) {
        removeListener("descriptionVisibilityEvent", DescriptionVisibilityEvent.class, handler);
    }


    public void addDialogCloseHandler(DialogCloseEvent.Handler handler) {
        addListener("dialogCloseEvent", DialogCloseEvent.class, handler, DialogCloseEvent.ON_DIALOG_CLOSE);
    }

    public void removeDialogCloseHandler(DialogCloseEvent.Handler handler) {
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

        public BaseDialog dialog;

        static {
            try {
                ON_DIALOG_CLOSE = DialogCloseEvent.Handler.class.getDeclaredMethod("onClose", new Class[] { DialogCloseEvent.class });
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        public DialogCloseEvent(Component source, BaseDialog dialog) {
            super(source);
            this.dialog = dialog;
        }

        public BaseDialog getDialog() {
            return dialog;
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
