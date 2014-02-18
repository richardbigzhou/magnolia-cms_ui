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
package info.magnolia.ui.framework.overlay;

import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent.Handler;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * BaseAlertView.
 */
public class BaseAlertView implements View {

    public static final String CONFIRM_ACTION_NAME = "confirm";

    private String message;

    private final LightDialog dialog = new LightDialog();

    private MessageStyleType styleType;

    private HorizontalLayout footer;

    public BaseAlertView(final Component contents, final MessageStyleType styleType) {
        message = "";
        this.styleType = styleType;
        setContent(contents);
        init();
    }

    private void init() {
        footer = new HorizontalLayout();
        footer.setSpacing(true);
        dialog.setFooterToolbar(footer);
        dialog.showCloseButton();
        dialog.addStyleName(styleType.getCssClass());

    }

    public void setMessage(String message) {
        this.message = message;
        if (dialog.getContent() != null && dialog.getContent() instanceof Label) {
            ((Label) dialog.getContent()).setValue(message);
        }
    }

    public String getMessage() {
        return message;
    }

    public void setContent(Component content) {
        content.addStyleName("dialog-content");
        dialog.setContent(content);
    }

    public void addConfirmationHandler(ConfirmationEvent.Handler handler) {
        dialog.addListener("confirmation_event", ConfirmationEvent.class, handler, ConfirmationEvent.ON_CONFIRMATION);
    }

    public void removeConfirmationHandler(ConfirmationEvent.Handler handler) {
        dialog.removeListener("confirmation_event", ConfirmationEvent.class, handler);
    }

    public void addButton(Button button) {
        footer.addComponent(button);
    }

    /**
     * ConfirmationEvent.
     */
    public static final class ConfirmationEvent extends Component.Event {

        /**
         * Handler.
         */
        public interface Handler {
            void onConfirmation(ConfirmationEvent event);
        }

        public static final java.lang.reflect.Method ON_CONFIRMATION;

        static {
            try {
                ON_CONFIRMATION = ConfirmationEvent.Handler.class.getDeclaredMethod("onConfirmation", new Class[] { ConfirmationEvent.class });
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

    @Override
    public Component asVaadinComponent() {
        return dialog;
    }

    public void addCloseHandler(Handler closeHandler) {
        dialog.addDialogCloseHandler(closeHandler);
    }

    public void fireConfirmationEvent(ConfirmationEvent event) {
        dialog.fireEvent(event);
    }

    public void setButtonAlignment(Button button, Alignment alignment) {
        footer.setComponentAlignment(button, alignment);
    }

    public void addStyleName(String style) {
        dialog.addStyleName(style);
    }
}
