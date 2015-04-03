/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.api.overlay;


import info.magnolia.ui.api.view.View;

/**
 * Implementers can open overlay views (with a degree of modality) over their display area.
 */
public interface OverlayLayer {

    /**
     * The available locations of modality for opening a modal.
     * Represents what will be blocked by the opened modal.
     */
    public static enum ModalityDomain {
        SUB_APP("sub-app"),
        APP("app"),
        SHELL("shell");

        private String cssClass;

        private ModalityDomain(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }

    }

    /**
     * The available levels of modality.
     * Determines how "modal" it is -
     * -STRONG creates a dark background that prevents clicks.
     * -LIGHT adds a border, creates a transparent background that prevents clicks.
     * -NON_MODAL does not prevent clicks.
     */
    public static enum ModalityLevel {
        STRONG("strong", "modality-strong"),
        LIGHT("light", "modality-light center-vertical"),
        NON_MODAL("non-modal", "modality-non-modal");

        private String cssClass;
        private String name;

        private ModalityLevel(String name, String cssClass) {
            this.name = name;
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getName(){
            return name;
        }

    }

    /**
     * Open an Overlay on top of the OverlayLayer implementer.
     *
     * @param view View of the component to be displayed modally.
     */
    OverlayCloser openOverlay(View view);

    /**
     * Open an Overlay on top of the OverlayLayer implementer.
     *
     * @param modalityLevel Modality level
     */
    OverlayCloser openOverlay(View view, ModalityLevel modalityLevel);

    /**
     * Opens an alert dialog of given {@link MessageStyleType type}, with given body but with empty title.
     * 
     * @deprecated since 5.3, you should probably pass the dialog title via {@link #openAlert(MessageStyleType, String, View, String, AlertCallback)}.
     * @param type the message level, i.e. INFO, WARNING or ERROR
     * @param body the alert dialog's body as a magnolia {@link View}; alternatively one may wrap any Vaadin component as a View using {@link ViewAdapter}
     * @param okButton the OK button text
     * @param callback the callback to execute when the OK button is pressed, or when the dialog is closed
     */
    @Deprecated
    void openAlert(MessageStyleType type, View body, String okButton, AlertCallback callback);

    /**
     * Opens an alert dialog of given {@link MessageStyleType type}, with given title and body.
     * 
     * @param type the message level, i.e. INFO, WARNING or ERROR
     * @param title the alert dialog's title
     * @param body the alert dialog's text body
     * @param okButton the OK button text
     * @param callback the callback to execute when the OK button is pressed, or when the dialog is closed
     */
    void openAlert(MessageStyleType type, String title, String body, String okButton, AlertCallback callback);

    /**
     * Opens an alert dialog of given {@link MessageStyleType type}, with given title and body.
     * 
     * @param type the message level, i.e. INFO, WARNING or ERROR
     * @param title the alert dialog's title
     * @param body the alert dialog's body as a magnolia {@link View}; alternatively one may wrap any Vaadin component as a View using {@link ViewAdapter}
     * @param okButton the OK button text
     * @param callback the callback to execute when the OK button is pressed, or when the dialog is closed
     */
    void openAlert(MessageStyleType type, String title, View body, String okButton, AlertCallback callback);

    /**
     * Opens a confirmation dialog of given {@link MessageStyleType type}, with given body but with empty title.
     * 
     * @deprecated since 5.3, you should probably pass the dialog title via {@link #openConfirmation(MessageStyleType, String, View, String, String, boolean, ConfirmationCallback)}.
     * @param type the message level, i.e. INFO, WARNING or ERROR
     * @param body the confirmation dialog's body as a magnolia {@link View}; alternatively one may wrap any Vaadin component as a View using {@link ViewAdapter}
     * @param confirmButton the confirm button text
     * @param cancelButton the cancel button text
     * @param cancelIsDefault whether the cancel button should be focused by default
     * @param callback the callback to execute when any button is pressed, or when the dialog is closed
     */
    @Deprecated
    void openConfirmation(MessageStyleType type, View body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback);

    /**
     * Opens a confirmation dialog of given {@link MessageStyleType type}, with given title and body.
     * 
     * @param type the message level, i.e. INFO, WARNING or ERROR
     * @param title the confirmation dialog's title
     * @param body the confirmation dialog's body text
     * @param confirmButton the confirm button text
     * @param cancelButton the cancel button text
     * @param cancelIsDefault whether the cancel button should be focused by default
     * @param callback the callback to execute when any button is pressed, or when the dialog is closed
     */
    void openConfirmation(MessageStyleType type, String title, String body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback);

    /**
     * Opens a confirmation dialog of given {@link MessageStyleType type}, with given title and body.
     * 
     * @param type the message level, i.e. INFO, WARNING or ERROR
     * @param title the confirmation dialog's title
     * @param body the confirmation dialog's body as a magnolia {@link View}; alternatively one may wrap any Vaadin component as a View using {@link ViewAdapter}
     * @param confirmButton the confirm button text
     * @param cancelButton the cancel button text
     * @param cancelIsDefault whether the cancel button should be focused by default
     * @param callback the callback to execute when any button is pressed, or when the dialog is closed
     */
    void openConfirmation(MessageStyleType type, String title, View body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback);

    /**
     * Notification indicator is a message banner that only shows a message to user.
     * Message is shown until user clicks close button or timeout expires.
     * 
     * @param viewToShow Content to show as View.
     */
    void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow);

    /**
     * Notification indicator is a message banner that only shows a message to user.
     * Message is shown until user clicks close button or timeout expires.
     *
     * @param title Content to show as string.
     */
    void openNotification(MessageStyleType type, boolean doesTimeout, String title);

    /**
     * Notification indicator is a message banner that only shows a message to user.
     * Message is shown until user clicks close button or timeout expires.
     *
     * @param title Content to show as string.
     * @param linkText Text to show in a link button.
     * @param cb Callback for when user clicks on link.
     */
    void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb);
}
