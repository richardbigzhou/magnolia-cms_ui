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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.content.view.ChooseDialogContentPresenter;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView.Listener;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.vaadin.dialog.BaseDialog;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Implementation of {@link ChooseDialogFactory}.
 */
@Singleton
public class ChooseDialogFactoryImpl implements ChooseDialogFactory {

    private final ComponentProvider componentProvider;

    private final DialogActionFactory actionFactory;

    private final EventBus chooseDialogEventBus;

    @Inject
    public ChooseDialogFactoryImpl(final ComponentProvider componentProvider, final DialogActionFactory actionFactory, final @Named("choosedialog") EventBus chooseDialogEventBus) {
        this.componentProvider = componentProvider;
        this.actionFactory = actionFactory;
        this.chooseDialogEventBus = chooseDialogEventBus;
    }

    @Override
    public WorkbenchChooseDialogPresenter createWorkbenchChooseDialog(String defaultPath) {
        final ChooseDialogView selectionDialogView = componentProvider.getComponent(ChooseDialogView.class);
        final WorkbenchChooseDialogPresenter workbenchChooseDialogPresenter = new WorkbenchChooseDialogPresenter(actionFactory, selectionDialogView, chooseDialogEventBus);

        final ChooseDialogContentPresenter presenter = componentProvider.getComponent(ChooseDialogContentPresenter.class);
        final BaseDialog dialog = (BaseDialog) workbenchChooseDialogPresenter.getView();

        ((ContentWorkbenchView) dialog.getContent()).setListener(new Listener() {

            @Override
            public void onViewTypeChanged(ViewType viewType) {

            }

            @Override
            public void onSearch(String searchExpression) {

            }
        });
        presenter.initContentView((ContentWorkbenchView) dialog.getContent());
        dialog.setHeight("500px");

        if (defaultPath != null && !defaultPath.isEmpty()) {
            ((ContentWorkbenchView) dialog.getContent()).selectPath(defaultPath);
        }
        return workbenchChooseDialogPresenter;
    }

}
