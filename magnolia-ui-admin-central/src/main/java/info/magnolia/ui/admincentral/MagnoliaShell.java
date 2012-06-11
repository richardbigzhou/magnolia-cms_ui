/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral;

import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.magnoliashell.BaseMagnoliaShell;
import info.magnolia.ui.widget.magnoliashell.ShellViewport;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;

/**
 * Admin shell.
 * @version $Id$
 */
@SuppressWarnings("serial")
@Singleton

public class MagnoliaShell extends BaseMagnoliaShell implements Shell {

    private EventBus eventBus;

    private AppController appController;

    @Inject
    public MagnoliaShell(final EventBus eventBus, final AppController appController) {
        super();
        this.eventBus = eventBus;
        this.appController = appController;
        this.eventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {
            @Override
            public void onAppFocused(AppLifecycleEvent event) {
                setActiveViewport(getAppViewport());
            }
        });
    }


    @Override
    protected void closeCurrentApp() {
        super.closeCurrentApp();
        appController.stopCurrentApplication();
        if (getAppViewport().isEmpty()) {
            navigateToShellApp(ShellAppType.APPLAUNCHER.name().toLowerCase(), "");
        }
    }

    @Override
    public void askForConfirmation(String message, ConfirmationHandler listener) {
    }

    @Override
    public void showNotification(String message) {
        showWarning(message);
    }

    @Override
    public void showError(String message, Exception e) {
        showError(message);
    }

    @Override
    public void openWindow(String uri, String windowName) {
        getWindow().open(new ExternalResource(uri), windowName);
    }

    @Override
    public String getFragment() {
        final ShellViewport activeViewport = getActiveViewport();
        return activeViewport == null ? "" : activeViewport.getCurrentShellFragment();
    }

    @Override
    public void setFragment(String fragment) {
        final ShellViewport activeViewport = getActiveViewport();
        proxy.call("navigate", fragment, activeViewport.getCurrentAppName());
    }

    @Override
    public HandlerRegistration addFragmentChangedHandler(final FragmentChangedHandler handler) {
        super.addFragmentChangedHanlder(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                removeFragmentChangedHanlder(handler);
            }
        };
    }

    @Override
    public Shell createSubShell(String id) {
        throw new UnsupportedOperationException("MagnoliaShell is not capable of opening the subshells.");
    }

    public void openDialog(Component component) {
        addDialog(component);
    }

    public void removeDialog(IsVaadinComponent dialog) {
       removeDialog(dialog.asVaadinComponent());
    }
}
