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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

/**
 * ConfirmationView.
 */
public class ConfirmationView extends BaseAlertView {

    public ConfirmationView(final Component contents, String confirmLabel, String cancelLabel, MessageStyleType styleType, boolean cancelIsDefault) {
        super(contents, styleType);
        init(confirmLabel, cancelLabel, cancelIsDefault);
    }

    private void init(String confirmLabel, String cancelLabel, boolean cancelIsDefault) {

        final Button confirmButton = new Button(confirmLabel, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                fireConfirmationEvent(new ConfirmationEvent(asVaadinComponent(), true));
            }
        });
        confirmButton.addStyleName("btn-dialog");
        confirmButton.addStyleName("confirm");
        addButton(confirmButton);
        setButtonAlignment(confirmButton, Alignment.MIDDLE_RIGHT);

        final Button cancelButton = new Button(cancelLabel, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                fireConfirmationEvent(new ConfirmationEvent(asVaadinComponent(), false));
            }
        });
        cancelButton.addStyleName("btn-dialog");
        cancelButton.addStyleName("cancel");
        addButton(cancelButton);
        setButtonAlignment(cancelButton, Alignment.MIDDLE_LEFT);

        if (cancelIsDefault) {
            cancelButton.focus();
        } else {
            confirmButton.focus();
        }

        addStyleName("confirmation");
    }

}
