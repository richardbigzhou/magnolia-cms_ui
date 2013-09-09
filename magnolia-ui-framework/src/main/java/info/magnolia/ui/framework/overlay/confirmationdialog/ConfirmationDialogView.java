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
package info.magnolia.ui.framework.overlay.confirmationdialog;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.LightDialog;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 5:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmationDialogView extends Panel implements View {

    private BaseDialog dialog = new LightDialog();

    private Button confirmButton = new Button();

    private Button cancelButton = new Button();

    public ConfirmationDialogView(final ConfirmationCallback callback) {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addComponent(confirmButton);
        footer.addComponent(cancelButton);
        footer.setComponentAlignment(confirmButton, Alignment.MIDDLE_RIGHT);
        footer.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
        dialog.setFooterToolbar(footer);
        confirmButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                callback.onSuccess();
            }
        });

        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                callback.onCancel();
            }
        });
    }


    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
