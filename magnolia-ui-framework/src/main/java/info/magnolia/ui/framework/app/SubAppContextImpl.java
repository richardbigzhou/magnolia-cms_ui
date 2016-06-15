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
package info.magnolia.ui.framework.app;

import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.context.AbstractUIContext;
import info.magnolia.ui.framework.overlay.OverlayPresenter;

import java.util.Locale;

/**
 * Implementation of {@link info.magnolia.ui.api.app.SubAppContext}.
 * See MGNLUI-379.
 */
public class SubAppContextImpl extends AbstractUIContext implements SubAppContext {

    private SubApp subApp;

    private String instanceId;

    private Location location;

    private SubAppDescriptor subAppDescriptor;

    private Locale authoringLocale;

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

    /*
     * Temporarily only in SubAppContextImpl, by the time method is generalized to {@link SubAppContext} interface in 5.4.
     */
    public Locale getAuthoringLocale() {
        return authoringLocale;
    }

    /*
     * Temporarily only in SubAppContextImpl, by the time method is generalized to {@link SubAppContext} interface in 5.4.
     */
    public void setAuthoringLocale(Locale authoringLocale) {
        this.authoringLocale = authoringLocale;
    }

    @Override
    public void close() {
        appContext.closeSubApp(instanceId);
    }

    protected Shell getShell() {
        return shell;
    }

    @Override
    protected OverlayPresenter initializeOverlayPresenter() {
        return new OverlayPresenter() {
            @Override
            public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
                // Get the MagnoliaTab for the view
                View overlayParent = getAppContext().getView().getSubAppViewContainer(instanceId);
                return SubAppContextImpl.this.shell.openOverlayOnView(view, overlayParent, ModalityDomain.SUB_APP, modalityLevel);
            }

        };
    }
}
