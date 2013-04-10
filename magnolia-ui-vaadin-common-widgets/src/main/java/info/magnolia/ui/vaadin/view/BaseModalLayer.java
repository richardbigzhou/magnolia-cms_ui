/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.vaadin.view;

import info.magnolia.ui.vaadin.dialog.ConfirmationDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog.ConfirmationEvent;
import info.magnolia.ui.vaadin.dialog.Modal.ModalityLevel;

/**
 * Implementers can open modal views over their display area.
 */
public abstract class BaseModalLayer implements ModalLayer {

    /**
     * Open a Modal on top of the ModalLayer implementer.
     * 
     * @param view View of the component to be displayed modally.
     */

    @Override
    public ModalCloser openConfirmation(View contentView, String confirmButtonText, String cancelButtonText, final ConfirmationCallback callback) {

        // Show confirmation dialog

        // ConfirmationDialog dialog = new ConfirmationDialog("UPDATE THIS. Really edit an item?");
        ConfirmationDialog dialog = new ConfirmationDialog(contentView);
        dialog.addStyleName("confirmation");
        dialog.setConfirmActionLabel(confirmButtonText);
        dialog.setRejectActionLabel(cancelButtonText);

        dialog.showCloseButton();

        final ModalCloser modalCloser = openModal(dialog, ModalityLevel.LIGHT);

        dialog.addConfirmationHandler(new ConfirmationDialog.ConfirmationEvent.Handler() {

            @Override
            public void onConfirmation(ConfirmationEvent event) {

                if (event.isConfirmed()) {
                    callback.onSuccess("");
                } else {
                    callback.onCancel();
                }

                modalCloser.close();

            }
        });

        return modalCloser;

    }

    /**
     * Convenience method to open a modal with the default strong modality level.
     */
    @Override
    public ModalCloser openModal(View view) {
        return openModal(view, ModalityLevel.STRONG);
    }

}
