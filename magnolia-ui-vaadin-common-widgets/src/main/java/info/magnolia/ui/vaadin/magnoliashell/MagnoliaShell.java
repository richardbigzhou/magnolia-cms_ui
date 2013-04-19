/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
import info.magnolia.ui.vaadin.overlay.OverlayCloser;
import info.magnolia.ui.vaadin.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.annotations.JavaScript;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.UI;

/**
 * Server side implementation of the MagnoliaShell container.
 */
@JavaScript({"jquery-1.7.2.min.js", "jquery.transition.js"})
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
        final ShellAppsViewport shellAppsViewport = new ShellAppsViewport(MagnoliaShell.this);
        final AppsViewport appsViewport = new AppsViewport(MagnoliaShell.this);

        getState().viewports.put(ViewportType.SHELL_APP, shellAppsViewport);
        getState().viewports.put(ViewportType.APP, appsViewport);

        getState().indications.put(ShellAppType.APPLAUNCHER, 0);
        getState().indications.put(ShellAppType.PULSE, 0);
        getState().indications.put(ShellAppType.FAVORITE, 0);

        shellAppsViewport.setParent(this);
        appsViewport.setParent(this);
    }

    public void propagateFragmentToClient(Fragment fragment) {
        getRpcProxy(ShellClientRpc.class).setFragmentFromServer(fragment);
    }

    public void goToApp(Fragment fragment) {
        listener.goToApp(fragment);
    }

    public void goToShellApp(Fragment fragment) {
        listener.goToShellApp(fragment);
    }

    public void doNavigate(final ShellViewport viewport, Fragment fragment) {
        viewport.setCurrentShellFragment(fragment.toFragment());
        setActiveViewport(viewport);
        notifyOnFragmentChanged(fragment.toFragment());
    }

    public void showInfo(String id, String subject, String message) {
        synchronized (UI.getCurrent()) {
            getRpcProxy(ShellClientRpc.class).showMessage(MessageType.INFO.name(), subject, message, id);
        }
    }

    public void showError(String id, String subject, String message) {
        synchronized (UI.getCurrent()) {
            getRpcProxy(ShellClientRpc.class).showMessage(MessageType.ERROR.name(), subject, message, id);
        }
    }

    public void showWarning(String id, String subject, String message) {
        synchronized (UI.getCurrent()) {
            getRpcProxy(ShellClientRpc.class).showMessage(MessageType.WARNING.name(), subject, message, id);
        }
    }

    public void hideAllMessages() {
        synchronized (UI.getCurrent()) {
            getRpcProxy(ShellClientRpc.class).hideAllMessages();
        }
    }

    public void updateShellAppIndication(ShellAppType type, int incrementOrDecrement) {
        Integer value = getState().indications.get(type);

        if (value == 0 && incrementOrDecrement < 0) {
            return;
        }

        getState().indications.put(type, incrementOrDecrement + value);
        synchronized (UI.getCurrent()) {
        }
    }

    public void setIndication(ShellAppType type, int indication) {
        getState().indications.put(type, indication);
        synchronized (UI.getCurrent()) {
        }
    }


    /**
     * Open an Overlay on top of a specific View.
     * 
     * @param child
     * View to be displayed over another view.
     * @param parent
     * The View to open the Overlay on top of.
     */
    public OverlayCloser openOverlay(final View child, View parent, Overlay.ModalityDomain modalityLocation, Overlay.ModalityLevel modalityLevel) {
        Overlay overlay = new Overlay(child.asVaadinComponent(), parent.asVaadinComponent(), modalityLocation, modalityLevel);
        getState().overlays.add(overlay);

        // overlay has Vaadin parent of MagnoliaShell
        overlay.setParent(this);

        return new OverlayCloser() {
            @Override
            public void close() {
                MagnoliaShell.this.closeOverlay(child.asVaadinComponent());
            }
        };
    }

    /**
     * Close an open overlay, such as a dialog.
     * 
     * @param overlayComponent The component of the view which was opened in an overlay.
     */
    public void closeOverlay(Component overlayComponent) {
        Overlay overlay = (Overlay) overlayComponent.getParent();
        getState().overlays.remove(overlay);
    }

    public void setActiveViewport(ShellViewport viewport) {
        final Connector currentActive = getState().activeViewport;
        if (currentActive != viewport) {
            getState().activeViewport = viewport;
        }
    }

    public ShellViewport getActiveViewport() {
        return (ShellViewport) getState(false).activeViewport;
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

    private void notifyOnFragmentChanged(String fragment) {
        listener.onFragmentChanged(fragment);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void doRegisterApps(List<String> appNames) {
        getState().registeredAppNames = appNames;
    }

    public void onAppStarted(String appName) {
        if (!getState().runningAppNames.contains(appName)) {
            getState().runningAppNames.add(appName);
        }
    }

    public void onAppStopped(String appName) {
        getState().runningAppNames.remove(appName);
    }

    public void registerShellApp(ShellAppType type, Component component) {
        getState().shellApps.put(type, component);
    }

    /**
     * Must also include any overlays that have been attached to the shell.
     */
    @Override
    public Iterator<Component> iterator() {

        Iterator<Connector> viewportIterator = getState(false).viewports.values().iterator();
        Iterator<Connector> overlayIterator = getState(false).overlays.iterator();

        ArrayList<Connector> connectors = new ArrayList<Connector>();

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

    public ShellViewport getShellAppViewport() {
        return (ShellViewport) getState(false).viewports.get(ViewportType.SHELL_APP);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
