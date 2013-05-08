/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.app.ItemChosenListener;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of {@link ChooseDialogPresenterFactory}.
 */
@Singleton
public class WorkbenchChooseDialogPresenterFactory implements ChooseDialogPresenterFactory {

    private final ComponentProvider componentProvider;

    @Inject
    public WorkbenchChooseDialogPresenterFactory(final ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public WorkbenchChooseDialogPresenter createChooseDialogPresenter(String path, final ItemChosenListener listener) {
        final WorkbenchChooseDialogPresenter workbenchChooseDialogPresenter = componentProvider.newInstance(WorkbenchChooseDialogPresenter.class);

        workbenchChooseDialogPresenter.addActionCallback(WorkbenchChooseDialogView.CHOOSE_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onItemChosen(workbenchChooseDialogPresenter.getValue());
            }
        });

        workbenchChooseDialogPresenter.addActionCallback(WorkbenchChooseDialogView.CANCEL_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onChooseCanceled();
            }
        });

        return workbenchChooseDialogPresenter;
    }

}
