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
package info.magnolia.ui.vaadin.magnoliashell;

import info.magnolia.ui.framework.event.EventHandlerCollection;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.viewport.AppsViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.DialogViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellAppsViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;

import java.util.Iterator;
import java.util.List;

import com.vaadin.annotations.JavaScript;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;


/**
 * Server side implementation of the MagnoliaShell container.
 */
@JavaScript({"jquery-1.7.2.min.js", "jquery.transition.js"})
public abstract class BaseMagnoliaShell extends AbstractLayout {

    private ShellServerRpc rpc = new ShellServerRpc() {
        
        @Override
        public void removeMessage(String id) {
            BaseMagnoliaShell.this.removeMessage(id);
        }

        @Override
        public void closeCurrentShellApp() {
            BaseMagnoliaShell.this.stopCurrentShellApp();
        }

        @Override
        public void closeCurrentApp() {
            BaseMagnoliaShell.this.closeCurrentApp();
        }

        @Override
        public void activateRunningApp(Fragment f) {
            BaseMagnoliaShell.this.navigateToApp(f);
        }

        @Override
        public void activateShellApp(Fragment f) {
            BaseMagnoliaShell.this.navigateToShellApp(f);
        }
        
        @Override
        public void startApp(Fragment f) {
            //setActiveViewport(getAppViewport());
            //doStartApp(f);
            activateShellApp(f);
        }
    };
    
    private final EventHandlerCollection<FragmentChangedHandler> handlers = new EventHandlerCollection<FragmentChangedHandler>();

    //private final ICEPush pusher = new ICEPush();

    public BaseMagnoliaShell() {
        setImmediate(true);
        registerRpc(rpc);
        initializeViewports();
    }

    private void initializeViewports() {
        final ShellAppsViewport shellAppsViewport = new ShellAppsViewport(BaseMagnoliaShell.this);
        final AppsViewport appsViewport = new AppsViewport(BaseMagnoliaShell.this);
        final DialogViewport dialogViewport = new DialogViewport(BaseMagnoliaShell.this);
        getState().viewports.put(ViewportType.SHELL_APP_VIEWPORT, shellAppsViewport);
        getState().viewports.put(ViewportType.APP_VIEWPORT, appsViewport);
        getState().viewports.put(ViewportType.DIALOG_VIEWPORT, dialogViewport);
        super.addComponent(shellAppsViewport);
        super.addComponent(appsViewport);
        super.addComponent(dialogViewport);
    }

    public void navigateToApp(Fragment fragment) {
        doNavigateWithinViewport(getAppViewport(), fragment);
    }

    public void navigateToShellApp(Fragment fragment) {
        doNavigateWithinViewport(getShellAppViewport(), fragment);
    }

    // the fragment generation should not be hardcoded. Create a util method in DefaultLocation.
    public void doNavigateWithinViewport(final ShellViewport viewport, Fragment dto) {
        viewport.setCurrentShellFragment(dto.toFragment());
        setActiveViewport(viewport);
        notifyOnFragmentChanged(dto.getAppViewportType().getFragmentPrefix() + dto.toFragment());
    }

    public void showInfo(Message message) {
        synchronized (UI.getCurrent()) {
        getRpcProxy(ShellClientRpc.class).showMessage(MessageType.INFO.name(), message.getSubject(), message.getMessage(), message.getId());
            //pusher.push();
        }
    }

    public void showError(Message message) {
        synchronized (UI.getCurrent()) {
            getRpcProxy(ShellClientRpc.class).showMessage(MessageType.ERROR.name(), message.getSubject(), message.getMessage(), message.getId());
            //pusher.push();
        }
    }

    public void showWarning(Message message) {
        synchronized (UI.getCurrent()) {
            getRpcProxy(ShellClientRpc.class).showMessage(MessageType.WARNING.name(), message.getSubject(), message.getMessage(), message.getId());
            //pusher.push();
        }
    }

    public void hideAllMessages() {
        synchronized (UI.getCurrent()) {
        getRpcProxy(ShellClientRpc.class).hideAllMessages();
            //pusher.push();
        }
    }

    public void updateShellAppIndication(ShellAppType type, int increment) {
        Integer value = getState().indications.get(type);
        getState().indications.put(type, increment + value);
        markAsDirty();
        synchronized (UI.getCurrent()) {
            // pusher.push();
        }
    }

    public void setIndication(ShellAppType type, int indication) {
        getState().indications.put(type, indication);
        markAsDirty();
        synchronized (UI.getCurrent()) {
            // pusher.push();
        }
    }

    public void removeDialog(Component dialog) {
        ((ShellViewport)getState().viewports.get(ViewportType.DIALOG_VIEWPORT)).removeComponent(dialog);
    }

    public void addDialog(Component dialog) {
        ((ShellViewport)getState().viewports.get(ViewportType.DIALOG_VIEWPORT)).addComponent(dialog);
    }

    public abstract void stopCurrentShellApp();

    public abstract void removeMessage(String messageId);

    public void closeCurrentApp() {
        getAppViewport().pop();
    }

    public void setActiveViewport(ShellViewport viewport) {
        final Connector currentActive = getState().activeViewport;
        if (currentActive != viewport) {
            getState().activeViewport = viewport;
        }
    }

    public ShellViewport getAppViewport() {
        return (ShellViewport)getState(false).viewports.get(ViewportType.APP_VIEWPORT);
    }

    public ShellViewport getShellAppViewport() {
        return (ShellViewport)getState(false).viewports.get(ViewportType.SHELL_APP_VIEWPORT);
    }

    public ShellViewport getDialogViewport() {
        return (ShellViewport)getState(false).viewports.get(ViewportType.DIALOG_VIEWPORT);
    }

    public ShellViewport getActiveViewport() {
        return (ShellViewport)getState(false).activeViewport;
    }

    @Override
    protected MagnoliaShellState getState() {
        return (MagnoliaShellState)super.getState();
    }
    
    @Override
    protected MagnoliaShellState getState(boolean markDirty) {
        return (MagnoliaShellState)super.getState(markDirty);
    }

    private void notifyOnFragmentChanged(String fragment) {
        handlers.dispatch(new FragmentChangedEvent(fragment));
    }

    public void addFragmentChangedHanlder(FragmentChangedHandler handler) {
        handlers.add(handler);
    }

    public void removeFragmentChangedHanlder(FragmentChangedHandler handler) {
        handlers.remove(handler);
    }

  /*protected ICEPush getPusher() {
        return pusher;
    }*/
    
    protected void doRegisterApps(List<String> appNames) {
        getState().registeredAppNames = appNames;
    }

    protected void onAppStarted(String appName) {
        if (!getState().runningAppNames.contains(appName)) {
            getState().runningAppNames.add(appName);
        }
    }

    protected void onAppStopped(String appName) {
        getState().runningAppNames.remove(appName);
    }

    @Override
    public void addComponent(Component c) {
        throw new UnsupportedOperationException("BaseMagnoliaShell doesn't support manipulating components.");
    }

    @Override
    public void removeComponent(Component c) {
        throw new UnsupportedOperationException("BaseMagnoliaShell doesn't support manipulating components.");
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException("BaseMagnoliaShell doesn't support manipulating components.");
    }

    @Override
    public int getComponentCount() {
        return getState(false).viewports.size();
    }
    
    public void registerShellApp(ShellAppType type, Component component) {
        getState().shellApps.put(type, component);
    }
    
    @Override
    public Iterator<Component> iterator() {
        return new Iterator<Component>() {
            private Iterator<Connector> wrappedIt = getState(false).viewports.values().iterator();
            
            @Override
            public boolean hasNext() {
                return wrappedIt.hasNext();
            }

            @Override
            public Component next() {
                return (Component)wrappedIt.next();
            }

            @Override
            public void remove() {
                wrappedIt.remove();
            }
        };
    }

}
