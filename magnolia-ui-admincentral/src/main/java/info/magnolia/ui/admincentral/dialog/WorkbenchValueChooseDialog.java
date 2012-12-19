/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView;
import info.magnolia.ui.vaadin.dialog.BaseDialog;

import javax.inject.Inject;

/**
 * Chooses a value from a workbench.
 *
 */
public class WorkbenchValueChooseDialog extends BaseDialog implements ChooseDialogView {

    public static final String CHOOSE_ACTION_NAME = "commit";
    public static final String CANCEL_ACTION_NAME = "cancel";
    
    private final ContentWorkbenchView view;
    
    @Inject
    public WorkbenchValueChooseDialog(ContentWorkbenchView view) {
        this.view = view;
        addStyleName("content-view-field-wrapper");
        setContent(this.view.asVaadinComponent());
        addAction(CHOOSE_ACTION_NAME, "Choose");
        addAction(CANCEL_ACTION_NAME, "Cancel");
    }
    
    @Override
    public void setCancelActionLabel(String newLabel) {
        setActionLabel(CANCEL_ACTION_NAME, newLabel);
    }

    @Override
    public void setSelectionActionLabel(String newLabel) {
        setActionLabel(CANCEL_ACTION_NAME, newLabel);
    }
    
}
