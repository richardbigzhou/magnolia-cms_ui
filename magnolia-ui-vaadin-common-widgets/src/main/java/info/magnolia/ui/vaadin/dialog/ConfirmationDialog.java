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

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * ConfirmationDialog.
 */
public class ConfirmationDialog extends LightDialog {

    public static final String CONFIRM_ACTION_NAME = "confirm";

    private String message;

    public ConfirmationDialog(final String message, boolean cancelIsDefault) {
        setMessage(message);
        init(cancelIsDefault);
    }

    public ConfirmationDialog(final View contents, boolean cancelIsDefault) {
        message = "";
        setContent(contents.asVaadinComponent());
        init(cancelIsDefault);
    }

    public void init(boolean cancelIsDefault) {
        // Add a class to the default button
        if (cancelIsDefault) {
            this.setDefaultAction(CANCEL_ACTION_NAME);
        } else {
            this.setDefaultAction(CONFIRM_ACTION_NAME);
        }

        addAction(CONFIRM_ACTION_NAME, "OK", new DialogActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                fireEvent(new ConfirmationEvent(ConfirmationDialog.this, true));
            }
        });
        // we need to explicitly add an ENTER shortcut bound to the confirm action, because addAction(..) do it only for commit and cancel actions.
        addShortcut(CONFIRM_ACTION_NAME, KeyCode.ENTER);

        addAction(CANCEL_ACTION_NAME, "Cancel", new DialogActionListener() {
            @Override
            public void onActionExecuted(String actionName) {
                fireEvent(new ConfirmationEvent(ConfirmationDialog.this, false));
            }
        });
    }


    public void setConfirmActionLabel(final String label) {
        addAction(CONFIRM_ACTION_NAME, label);
    }

    public void setRejectActionLabel(final String label) {
        addAction(CANCEL_ACTION_NAME, label);
    }

    public void setMessage(String message) {
        this.message = message;
        if (getContent() != null && getContent() instanceof Label) {
            ((Label) getContent()).setValue(message);
        }
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void setContent(Component content) {
        super.setContent(content);
    }

    @Override
    protected Component createDefaultContent() {
        return new Label();
    }

    public void addConfirmationHandler(ConfirmationEvent.Handler handler) {
        addListener("confirmation_event", ConfirmationEvent.class, handler, ConfirmationEvent.ON_CONFIRMATION);
    }

    public void removeConfirmationHandler(ConfirmationEvent.Handler handler) {
        removeListener("confirmation_event", ConfirmationEvent.class, handler);
    }

    /**
     * ConfirmationEvent.
     */
    public static class ConfirmationEvent extends Component.Event {

        /**
         * Handler.
         */
        public interface Handler {
            void onConfirmation(ConfirmationEvent event);
        }

        public static final java.lang.reflect.Method ON_CONFIRMATION;

        static {
            try {
                ON_CONFIRMATION = ConfirmationEvent.Handler.class.getDeclaredMethod("onConfirmation", new Class[]{ConfirmationEvent.class});
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final boolean isConfirmed;

        public ConfirmationEvent(Component source, boolean isConfirmed) {
            super(source);
            this.isConfirmed = isConfirmed;
        }

        public boolean isConfirmed() {
            return isConfirmed;
        }
    }
}
