/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.statusbar;

import info.magnolia.pages.app.editor.PagesEditorSubApp;
import info.magnolia.pages.app.editor.statusbar.activationstatus.ActivationStatus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.workbench.StatusBarView;

import javax.inject.Inject;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;

/**
 * A presenter class for the {@link StatusBarView} displayed at the bottom of the page editor. Takes care of loading and
 * displaying the components inside the status bar.
 */
public class StatusBarPresenter {

    private StatusBarView view;
    private ActivationStatus activationStatus;
    private PagesEditorSubApp listener;

    @Inject
    public StatusBarPresenter(final StatusBarView view, ActivationStatus activationStatus) {
        this.view = view;
        this.activationStatus = activationStatus;

        view.asVaadinComponent().setHeight(24, Sizeable.Unit.PIXELS);
    }

    public StatusBarView start(DetailLocation location) {
        View activationStatusView = activationStatus.start(location);
        if (activationStatusView != null) {
            activationStatus.setListener(this);
            view.addComponent(activationStatusView.asVaadinComponent(), Alignment.MIDDLE_CENTER);
        }
        return view;
    }


    public DetailLocation getCurrentLocation() {
        return listener.getCurrentLocation();
    }

    public void setListener(PagesEditorSubApp listener) {
        this.listener = listener;
    }

    public void deactivateComponents() {
        activationStatus.deactivate();
    }
}
