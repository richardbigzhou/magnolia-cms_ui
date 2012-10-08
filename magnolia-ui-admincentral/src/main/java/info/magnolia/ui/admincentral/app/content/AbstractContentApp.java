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
package info.magnolia.ui.admincentral.app.content;

import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.dialog.PickerDialogFactory;
import info.magnolia.ui.admincentral.dialog.ValuePickerDialogPresenter;
import info.magnolia.ui.admincentral.dialog.WorkbenchPickerDialogPresenter;
import info.magnolia.ui.framework.app.AbstractApp;
import info.magnolia.ui.framework.shell.Shell;

import javax.inject.Inject;

import com.vaadin.data.Item;


/**
 * Abstract base app class for content apps.
 */
public abstract class AbstractContentApp extends AbstractApp {

    private PickerDialogFactory pickerDialogFactory;

    @Inject
    private Shell shell;
    
    @Inject
    public AbstractContentApp(PickerDialogFactory pickerDialogFactory) {
        this.pickerDialogFactory = pickerDialogFactory;
    }

    public ValuePickerDialogPresenter<Item> openWorkbenchPickerDialog() {
        final WorkbenchPickerDialogPresenter picker = pickerDialogFactory.createWorkbenchValuePickerDialog();
        ((MagnoliaShell)shell).openDialog(picker);
        return picker;
    }

}
