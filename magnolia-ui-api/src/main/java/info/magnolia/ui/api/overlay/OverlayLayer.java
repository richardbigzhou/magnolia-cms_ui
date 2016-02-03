/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
        STRONG("modality-strong"),
        LIGHT("modality-light center-vertical"),
        NON_MODAL("modality-non-modal");

        private String cssClass;

        private ModalityLevel(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
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
     * Alert dialog is a dialog where user is given a message and confirm button no chance to cancel.
     * AlertCallback is invoked on confirm.
     * This method takes content of this dialog as a caller defined View.
     */
    void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb);

    /**
     * Alert dialog is a dialog where user is given a message and confirm button no chance to cancel.
     * AlertCallback is invoked on confirm.
     * This method takes the content as a string.
     */
    void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb);

    /**
     * Confirmation dialog is a dialog where user is presented a message and chance to confirm or to cancel.
     * ConfirmationCallback is invoked on user action.
     * This method takes content of this dialog as a caller defined View.
     */
    void openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb);

    /**
     * Confirmation dialog is a dialog where user is presented a message and chance to confirm or to cancel.
     * ConfirmationCallback is invoked on user action.
     * This method takes the content as a string.
     */
    void openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb);


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
