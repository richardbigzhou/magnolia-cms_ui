/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.contentapp;


import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.choosedialog.ContentAppChooseDialogPresenter;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.framework.app.BaseApp;

import javax.inject.Inject;

/**
 * Extends the {@link BaseApp} by the ability to open a choose dialog.
 */
public class ContentApp extends BaseApp {

    private ComponentProvider componentProvider;

    @Inject
    public ContentApp(AppContext appContext, AppView view, ComponentProvider componentProvider) {
        super(appContext, view);
        this.componentProvider = componentProvider;
    }

    @Override
    public void openChooseDialog(UiContext overlayLayer, String selectedId, final ChooseDialogCallback callback) {
        ChooseDialogPresenter presenter;
        ChooseDialogDefinition chooseDialogDefinition;
        if (appContext.getAppDescriptor() instanceof ContentAppDescriptor) {
            ContentAppDescriptor contentAppDescriptor = (ContentAppDescriptor)appContext.getAppDescriptor();
            presenter = componentProvider.getComponent(contentAppDescriptor.getChooseDialog().getPresenterClass());
            chooseDialogDefinition = contentAppDescriptor.getChooseDialog();
        } else {
            chooseDialogDefinition = new ConfiguredChooseDialogDefinition();
            presenter = componentProvider.newInstance(ContentAppChooseDialogPresenter.class);
        }
        presenter.start(callback, chooseDialogDefinition, overlayLayer, selectedId) ;
    }
}
