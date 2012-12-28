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
import info.magnolia.ui.vaadin.common.ComponentIterator;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.rpc.MagnoliaShellRpcDelegate;
import info.magnolia.ui.vaadin.magnoliashell.viewport.AppsViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.DialogViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellAppsViewport;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;

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
public abstract class MagnoliaShellBase extends AbstractComponent implements HasComponents {
    
    private final EventHandlerCollection<FragmentChangedHandler> handlers = new EventHandlerCollection<FragmentChangedHandler>();

    //private final ICEPush pusher = new ICEPush();

    public MagnoliaShellBase() {
        setImmediate(true);
        registerRpc(new MagnoliaShellRpcDelegate(this));
        initializeViewports();
    }

    public abstract void stopCurrentShellApp();

    public abstract void stopCurrentApp();
    
    public abstract void removeMessage(String messageId);
    
    private void initializeViewports() {
        final ShellAppsViewport shellAppsViewport = new ShellAppsViewport(MagnoliaShellBase.this);
        final AppsViewport appsViewport = new AppsViewport(MagnoliaShellBase.this);
        final DialogViewport dialogViewport = new DialogViewport(MagnoliaShellBase.this);
        
        getState().viewports.put(ViewportType.SHELL_APP, shellAppsViewport);
        getState().viewports.put(ViewportType.APP, appsViewport);
        getState().viewports.put(ViewportType.DIALOG, dialogViewport);
        
        getState().indications.put(ShellAppType.APPLAUNCHER, 0);
        getState().indications.put(ShellAppType.PULSE, 0);
        getState().indications.put(ShellAppType.FAVORITE, 0);
        
        shellAppsViewport.setParent(this);
        appsViewport.setParent(this);
        dialogViewport.setParent(this);
    }

    public void propagateFragmentToClient(Fragment fragment) {
        getRpcProxy(ShellClientRpc.class).setFragmentFromServer(fragment);
    }
    
    public void goToApp(Fragment fragment) {
        doNavigate(getState(false).appViewport(), fragment);
    }

    public void goToShellApp(Fragment fragment) {
        doNavigate(getState(false).shellAppViewport(), fragment);
    }

    public void doNavigate(final ShellViewport viewport, Fragment fragment) {
        viewport.setCurrentShellFragment(fragment.toFragment());
        setActiveViewport(viewport);
        notifyOnFragmentChanged(fragment.toFragment());
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
        synchronized (UI.getCurrent()) {
            // pusher.push();
        }
    }

    public void setIndication(ShellAppType type, int indication) {
        getState().indications.put(type, indication);
        synchronized (UI.getCurrent()) {
            // pusher.push();
        }
    }

    public void removeDialog(Component dialog) {
        ((ShellViewport)getState().viewports.get(ViewportType.DIALOG)).removeComponent(dialog);
    }

    public void addDialog(Component dialog) {
        ((ShellViewport)getState().viewports.get(ViewportType.DIALOG)).addComponent(dialog);
    }

    public void setActiveViewport(ShellViewport viewport) {
        final Connector currentActive = getState().activeViewport;
        if (currentActive != viewport) {
            getState().activeViewport = viewport;
        }
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
    
    public void registerShellApp(ShellAppType type, Component component) {
        getState().shellApps.put(type, component);
    }
    
    @Override
    public Iterator<Component> iterator() {
        return new ComponentIterator<Connector>(getState(false).viewports.values().iterator());
    }
}
