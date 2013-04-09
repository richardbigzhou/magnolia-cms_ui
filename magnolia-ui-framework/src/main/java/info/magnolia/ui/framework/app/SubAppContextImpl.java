/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.framework.app;

import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog;
import info.magnolia.ui.vaadin.dialog.Modal;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog.ConfirmationEvent;
import info.magnolia.ui.vaadin.view.BaseModalLayer;
import info.magnolia.ui.vaadin.view.ConfirmationCallback;
import info.magnolia.ui.vaadin.view.ModalCloser;
import info.magnolia.ui.vaadin.view.View;

/**
 * Implementation of {@link SubAppContext}.
 * See MGNLUI-379.
 */
public class SubAppContextImpl extends BaseModalLayer implements SubAppContext {

    private SubApp subApp;

    private String instanceId;

    private Location location;

    private SubAppDescriptor subAppDescriptor;

    private AppContext appContext;

    private Shell shell;


    public SubAppContextImpl(SubAppDescriptor subAppDescriptor, Shell shell) {
        this.subAppDescriptor = subAppDescriptor;
        this.shell = shell;
    }

    @Override
    public SubAppDescriptor getSubAppDescriptor() {
        return subAppDescriptor;
    }

    @Override
    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public SubApp getSubApp() {
        return subApp;
    }

    @Override
    public void setSubApp(SubApp subApp) {
        this.subApp = subApp;
    }

    @Override
    public String getSubAppId() {
        return subAppDescriptor.getName();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public ModalCloser openModal(View view) {
        // Get the MagnoliaTab for the view
        View modalityParent = getAppContext().getView().getSubAppViewContainer(instanceId);
        return shell.openModalOnView(view, modalityParent, Modal.ModalityLevel.SUB_APP);
    }

    // @Override
    // public ModalCloser openConfirmation(View contentView, String confirmButtonText, String cancelButtonText, final ConfirmationCallback callback) {
    //
    // // Show confirmation dialog
    //
    // // ConfirmationDialog dialog = new ConfirmationDialog("UPDATE THIS. Really edit an item?");
    // ConfirmationDialog dialog = new ConfirmationDialog(contentView);
    // dialog.setConfirmActionLabel(confirmButtonText);
    // dialog.setRejectActionLabel(cancelButtonText);
    //
    // dialog.showCloseButton();
    //
    // final ModalCloser modalCloser = openModal(dialog);
    //
    // dialog.addConfirmationHandler(new ConfirmationDialog.ConfirmationEvent.Handler() {
    //
    // @Override
    // public void onConfirmation(ConfirmationEvent event) {
    //
    // if (event.isConfirmed()) {
    // callback.onSuccess("");
    // } else {
    // callback.onCancel();
    // }
    //
    // modalCloser.close();
    //
    // }
    // });
    //
    // return modalCloser;
    // }

    @Override
    public void close() {
        appContext.closeSubApp(instanceId);
    }


}
