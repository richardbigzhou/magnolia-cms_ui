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
package info.magnolia.ui.admincentral.app.dialog;

import info.magnolia.ui.admincentral.dialog.DialogPresenter;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.framework.app.AbstractAppActivity;

import javax.inject.Inject;

import com.vaadin.data.Item;

/**
 * Activity for the Pages app.
 *
 * @version $Id$
 */
public class DialogTestActivity extends AbstractAppActivity<DialogTestPresenter> implements DialogTestPresenter {

    private DialogPresenterFactory factory;
    private Item selectedBean;

    @Inject
    public DialogTestActivity(DialogTestView view, DialogPresenterFactory factory) {
        super(view);
        this.factory = factory;
    }

    @Override
    public void openDialog() {
        DialogPresenter presenter = factory.createDialog("admin-central:userpreferences");

        presenter.showDialog(selectedBean);
    }

    @Override
    public DialogTestPresenter getReference() {
        return this;
    }

    /* (non-Javadoc)
     * @see info.magnolia.ui.admincentral.app.dialog.DialogTestPresenter#setSelected(com.vaadin.data.Property)
     */
    @Override
    public void setSelected(Item item) {
        this.selectedBean = item;
    }
}
