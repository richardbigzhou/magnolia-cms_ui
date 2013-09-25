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

import info.magnolia.cms.i18n.MessagesUtil;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * ConfirmationDialog.
 */
public class ConfirmationDialog extends LightDialog {

    public static final String CONFIRM_ACTION_NAME = "confirm";

    private Button confirmButton = new Button(MessagesUtil.get("ui-vaadin-common-widgets.confirmationDialog.button.ok", "mgnl-i18n.ui-vaadin-common-widgets-messages"), new ClickListener() {
        @Override
        public void buttonClick(ClickEvent event) {
            fireEvent(new ConfirmationEvent(ConfirmationDialog.this, true));
        }
    });

    private Button cancelButton = new Button(MessagesUtil.get("ui-vaadin-common-widgets.confirmationDialog.button.cancel", "mgnl-i18n.ui-vaadin-common-widgets-messages"), new ClickListener() {
        @Override
        public void buttonClick(ClickEvent event) {
            fireEvent(new ConfirmationEvent(ConfirmationDialog.this, false));
        }
    });

    private String message;

    public ConfirmationDialog(final String message, boolean cancelIsDefault) {
        setMessage(message);
        init(cancelIsDefault);
    }

    public ConfirmationDialog(final Component contents, boolean cancelIsDefault) {
        message = "";
        setContent(contents);
        init(cancelIsDefault);
    }

    public void init(boolean cancelIsDefault) {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addComponent(confirmButton);
        footer.addComponent(cancelButton);

        cancelButton.addStyleName("btn-dialog");
        cancelButton.addStyleName("cancel");
        confirmButton.addStyleName("btn-dialog");
        confirmButton.addStyleName("confirm");

        footer.setComponentAlignment(confirmButton, Alignment.MIDDLE_RIGHT);
        footer.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
        footer.setSpacing(true);
        setFooterToolbar(footer);

        // Add a class to the default button
        if (cancelIsDefault) {
            cancelButton.addStyleName("default");
        } else {
            confirmButton.addStyleName("default");
        }
    }


    public void setConfirmActionLabel(String label) {
        confirmButton.setCaption(label);
    }

    public void setRejectActionLabel(String label) {
        cancelButton.setCaption(label);
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
