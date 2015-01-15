/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.magnoliashell;

import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.common.ComponentIterator;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.rpc.MagnoliaShellRpcDelegate;
import info.magnolia.ui.vaadin.magnoliashell.viewport.AppsViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellAppsViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;
import info.magnolia.ui.vaadin.overlay.Overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.Page;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

/**
 * Server side implementation of the MagnoliaShell container.
 */
@JavaScript({"jquery-1.7.2.min.js", "jquery.animate-enhanced.min.js"})
public class MagnoliaShell extends AbstractComponent implements HasComponents, View {

    /**
     * Listener for events.
     */
    public interface Listener {

        void onFragmentChanged(String fragment);

        void stopCurrentShellApp();

        void stopCurrentApp();

        void removeMessage(String messageId);

        void goToApp(Fragment fragment);

        void goToShellApp(Fragment fragment);

    }
    private Listener listener;

    public MagnoliaShell() {
        setImmediate(true);
        setSizeFull();
        registerRpc(new MagnoliaShellRpcDelegate(this));
        initializeViewports();
    }

    public void setUserMenu(View view) {
        Component userMenuComponent = view.asVaadinComponent();
        userMenuComponent.setParent(this);
        getState().userMenu = userMenuComponent;
    }

    public void stopCurrentShellApp() {
        listener.stopCurrentShellApp();
    }

    public void stopCurrentApp() {
        listener.stopCurrentApp();
    }

    public void removeMessage(String messageId) {
        listener.removeMessage(messageId);
    }

    private void initializeViewports() {
        final ShellAppsViewport shellAppsViewport = new ShellAppsViewport();
        final AppsViewport appsViewport = new AppsViewport();

        getState().viewports.put(ViewportType.SHELL_APP, shellAppsViewport);
        getState().viewports.put(ViewportType.APP, appsViewport);

        getState().indications.put(ShellAppType.APPLAUNCHER, 0);
        getState().indications.put(ShellAppType.PULSE, 0);
        getState().indications.put(ShellAppType.FAVORITE, 0);

        shellAppsViewport.setParent(this);
        appsViewport.setParent(this);
    }

    public void goToApp(Fragment fragment) {
        listener.goToApp(fragment);
    }

    public void goToShellApp(Fragment fragment) {
        listener.goToShellApp(fragment);
    }

    public void showInfo(String id, String subject, String message) {
        getRpcProxy(ShellClientRpc.class).showMessage(MessageType.INFO.name(), subject, message, id);
    }

    public void showError(String id, String subject, String message) {
        getRpcProxy(ShellClientRpc.class).showMessage(MessageType.ERROR.name(), subject, message, id);
    }

    public void showWarning(String id, String subject, String message) {
        getRpcProxy(ShellClientRpc.class).showMessage(MessageType.WARNING.name(), subject, message, id);
    }

    public void hideAllMessages() {
        getRpcProxy(ShellClientRpc.class).hideAllMessages();
    }

    public void setFullScreen(boolean isFullScreen) {
        if (isFullScreen != getState().isFullScreen) {
            getState().isFullScreen = isFullScreen;
            getRpcProxy(ShellClientRpc.class).setFullScreen(isFullScreen);
        }
    }

    public void updateShellAppIndication(ShellAppType type, int incrementOrDecrement) {
        Integer value = getState().indications.get(type);

        if (value == 0 && incrementOrDecrement < 0) {
            return;
        }

        getState().indications.put(type, incrementOrDecrement + value);
    }

    public void setIndication(ShellAppType type, int indication) {
        getState().indications.put(type, indication);
    }


    public void setUriFragment(Fragment fragment) {
        if (fragment.isApp()) {
            getState().currentAppUriFragment = fragment;
        }
        Page current = Page.getCurrent();
        if (current != null) {
            current.setUriFragment(fragment.toFragment(), false);
        }
    }

    public String getUriFragment() {
        Page current = Page.getCurrent();
        if (current != null) {
            return current.getUriFragment();
        }
        return null;
    }

    /**
     * Open an Overlay on top of a specific View.
     * 
     * @param child
     * View to be displayed over another view.
     * @param parent
     * The View to open the Overlay on top of.
     */
    public OverlayCloser openOverlay(final View child, View parent, OverlayLayer.ModalityDomain modalityDomain, OverlayLayer.ModalityLevel modalityLevel) {
        final Overlay overlay = new Overlay(child.asVaadinComponent(), parent.asVaadinComponent(), modalityDomain, modalityLevel);
        addOverlay(overlay);
        parent.asVaadinComponent().addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent event) {
                removeOverlay(overlay);
            }
        });
        final OverlayCloser closer = new OverlayCloser() {
            @Override
            public void close() {
                overlay.close();
            }

            @Override
            public void setCloseTimeout(int timeoutMillis) {
                overlay.setCloseTimeout(timeoutMillis);
            }
        };
        overlay.setListener(new Overlay.Listener() {
            @Override
            public void onOverlayClosed() {
                removeOverlay(overlay);
            }
        });

        return closer;
    }

    private void addOverlay(Overlay overlay) {
        getState().overlays.add(overlay);
        overlay.setParent(this);
    }

    public void removeOverlay(Overlay overlay) {
        if (overlay.getParent() == this) {
            getState().overlays.remove(overlay);
            overlay.setParent(null);
        }
    }


    @Override
    protected MagnoliaShellState createState() {
        return new MagnoliaShellState();
    }

    @Override
    protected MagnoliaShellState getState() {
        return (MagnoliaShellState) super.getState();
    }

    @Override
    protected MagnoliaShellState getState(boolean markDirty) {
        return (MagnoliaShellState) super.getState(markDirty);
    }

    public void notifyOnFragmentChanged(String fragment) {
        listener.onFragmentChanged(fragment);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void doRegisterApps(List<String> appNames) {
        ((AppsViewport)getAppViewport()).setRegisteredApps(appNames);
    }

    public void onAppStarted(String appName) {
        ((AppsViewport)getAppViewport()).addRunningApp(appName);
    }

    public void onAppStopped(String appName) {
        ((AppsViewport)getAppViewport()).removeRunningApp(appName);
    }

    public void registerShellApp(ShellAppType type, Component component) {
        getState().shellApps.put(type, component);
        getShellAppViewport().registerShellApp(type, component);
    }

    /**
     * Must also include any overlays that have been attached to the shell.
     */
    @Override
    public Iterator<Component> iterator() {
        Iterator<Connector> viewportIterator = getState(false).viewports.values().iterator();
        Iterator<Connector> overlayIterator = getState(false).overlays.iterator();
        List<Connector> connectors = new ArrayList<Connector>();

        connectors.add(getState(false).userMenu);

        // Add viewports
        while (viewportIterator.hasNext()) {
            connectors.add(viewportIterator.next());
        }

        // Add overlays
        while (overlayIterator.hasNext()) {
            connectors.add(overlayIterator.next());
        }

        return new ComponentIterator<Connector>(connectors.iterator());
    }

    public ShellViewport getAppViewport() {
        return (ShellViewport) getState(false).viewports.get(ViewportType.APP);
    }

    public ShellAppsViewport getShellAppViewport() {
        return (ShellAppsViewport) getState(false).viewports.get(ViewportType.SHELL_APP);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

}
