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
import info.magnolia.ui.framework.overlay.OverlayCloser;
import info.magnolia.ui.framework.overlay.OverlayLayer;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.overlay.AlertCallback;
import info.magnolia.ui.vaadin.overlay.ConfirmationCallback;
import info.magnolia.ui.vaadin.overlay.MessageStyleType;
import info.magnolia.ui.vaadin.overlay.NotificationCallback;
import info.magnolia.ui.vaadin.overlay.Overlay;
import info.magnolia.ui.vaadin.overlay.Overlay.ModalityLevel;
import info.magnolia.ui.vaadin.view.View;

/**
 * Implementation of {@link SubAppContext}.
 * See MGNLUI-379.
 */
public class SubAppContextImpl implements SubAppContext {

    private SubApp subApp;

    private String instanceId;

    private Location location;

    private SubAppDescriptor subAppDescriptor;

    private AppContext appContext;

    private Shell shell;

    private OverlayLayer overlayPresenter;

    public SubAppContextImpl(SubAppDescriptor subAppDescriptor, Shell shell) {
        this.subAppDescriptor = subAppDescriptor;
        this.shell = shell;
        overlayPresenter = new OverlayPresenter() {

        };

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
    public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
        // Get the MagnoliaTab for the view
        View overlayParent = getAppContext().getView().getSubAppViewContainer(instanceId);
        return shell.openOverlayOnView(view, overlayParent, Overlay.ModalityDomain.SUB_APP, modalityLevel);
    }

    @Override
    public void close() {
        appContext.closeSubApp(instanceId);
    }

    @Override
    public OverlayCloser openOverlay(View view) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OverlayCloser openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        return overlayPresenter.openAlert(type, viewToShow, confirmButtonText, cb)
    }

    @Override
    public OverlayCloser openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OverlayCloser openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OverlayCloser openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OverlayCloser openNotification(MessageStyleType type, int timeout_msec, View viewToShow) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OverlayCloser openNotification(MessageStyleType type, int timeout_msec, String title) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OverlayCloser openNotification(MessageStyleType type, int timeout_msec, String title, String linkText, NotificationCallback cb) {
        // TODO Auto-generated method stub
        return null;
    }

}
