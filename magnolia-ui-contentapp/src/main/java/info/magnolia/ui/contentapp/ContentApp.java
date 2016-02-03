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


import info.magnolia.ui.contentapp.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogPresenterFactory;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.framework.app.BaseApp;
import info.magnolia.ui.api.app.ItemChosenListener;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;

import javax.inject.Inject;

/**
 * Extends the {@link BaseApp} by the ability to open a choose dialog.
 */
public class ContentApp extends BaseApp {

    private final ChooseDialogPresenterFactory chooseDialogPresenterFactory;

    @Inject
    public ContentApp(AppContext appContext, AppView view, ChooseDialogPresenterFactory chooseDialogPresenterFactory) {
        super(appContext, view);
        this.chooseDialogPresenterFactory = chooseDialogPresenterFactory;
    }

    @Override
    public void openChooseDialog(String path, OverlayLayer overlayLayer, String selectedId, final ItemChosenListener listener) {

        final ChooseDialogPresenter chooseDialogPresenter = chooseDialogPresenterFactory.createChooseDialogPresenter(path, listener, selectedId);

        final OverlayCloser overlayCloser = overlayLayer.openOverlay(chooseDialogPresenter.start());

        chooseDialogPresenter.setListener(new ChooseDialogPresenter.Listener() {

            @Override
            public void onClose() {
                overlayCloser.close();
            }
        });
    }
}
