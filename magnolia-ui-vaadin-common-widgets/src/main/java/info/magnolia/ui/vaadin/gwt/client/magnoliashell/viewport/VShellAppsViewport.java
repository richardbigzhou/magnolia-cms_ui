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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.VMainLauncher.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppNavigationEvent;

import com.google.gwt.user.client.ui.Widget;

/**
 * Shell apps viewport client side.
 */
public class VShellAppsViewport extends VShellViewport {

    private ShellAppNavigationEvent refreshEvent;

    public VShellAppsViewport() {
        super();
        setForceContentAlign(true);
        setTransitionDelegate(TransitionDelegate.SHELL_APPS_TRANSITION_DELEGATE);
    }

    /**
     * Gets a shell app widget if it has already been loaded in the viewport.
     * 
     * @param type
     *            the shell app type
     * @return the shell app widget
     */
    public Widget getShellAppByType(ShellAppType type) {
        return (Widget) client.getPaintable("PID_S" + type.getClassId());
    }

    /* SERVER REFRESH AFTER CLIENT TRANSITIONS */

    public ShellAppNavigationEvent getRefreshEvent() {
        return refreshEvent;
    }

    public void setRefreshEvent(ShellAppNavigationEvent event) {
        this.refreshEvent = event;
    }

    public void refreshShellApp() {
        if (refreshEvent != null) {
            getEventBus().fireEvent(refreshEvent);
        }
    }

    @Override
    void doSetActive(boolean active) {
        super.doSetActive(active);
        if (getTransitionDelegate() == null && active) {
            refreshShellApp();
        }
    }

    @Override
    void doSetVisibleApp(Widget w) {
        super.doSetVisibleApp(w);
        iLayout();
        if (getTransitionDelegate() == null) {
            refreshShellApp();
        }
    }

}
