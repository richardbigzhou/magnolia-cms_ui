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
package info.magnolia.ui.widget.magnoliashell;


import info.magnolia.ui.framework.event.EventHandlerCollection;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell.ViewportType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.widget.magnoliashell.viewport.AppsViewport;
import info.magnolia.ui.widget.magnoliashell.viewport.DialogViewport;
import info.magnolia.ui.widget.magnoliashell.viewport.ShellAppsViewport;
import info.magnolia.ui.widget.magnoliashell.viewport.ShellViewport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.artur.icepush.ICEPush;
import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gson.Gson;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

/**
 * Server side implementation of the MagnoliaShell container.
 */
@SuppressWarnings("serial")
@ClientWidget(value=VMagnoliaShell.class, loadStyle = LoadStyle.EAGER)
public abstract class BaseMagnoliaShell extends AbstractComponent implements ServerSideHandler {

    private EventHandlerCollection<FragmentChangedHandler> handlers = new EventHandlerCollection<FragmentChangedHandler>();

    private Map<ViewportType, ShellViewport> viewports = new EnumMap<ViewportType, ShellViewport>(ViewportType.class) {{
        put(ViewportType.SHELL_APP_VIEWPORT, new ShellAppsViewport(BaseMagnoliaShell.this));
        put(ViewportType.APP_VIEWPORT, new AppsViewport(BaseMagnoliaShell.this));
        put(ViewportType.DIALOG_VIEWPORT, new DialogViewport(BaseMagnoliaShell.this));
    }};
    
    private ShellViewport activeViewport = null;
    
    private ICEPush pusher = new ICEPush();
    
    private List<String> registeredApps = new ArrayList<String>();
    
    private List<String> runningApps = new ArrayList<String>();
    
    public BaseMagnoliaShell() {
        setImmediate(true);
    }

    public void navigateToApp(String prefix, String token) {
        doNavigateWithinViewport(getAppViewport(), DefaultLocation.LOCATION_TYPE_APP, prefix, token);
    }

    public void navigateToShellApp(final String prefix, String token) {
        doNavigateWithinViewport(getShellAppViewport(), DefaultLocation.LOCATION_TYPE_SHELL_APP, prefix , token);
    }

    public void doNavigateWithinViewport(final ShellViewport viewport, String type,  String prefix, String token) {
        System.out.println("[NAVIGATING TO] " + prefix + " " + type + " " + token);
        viewport.setCurrentShellFragment(prefix + ":" + token);
        setActiveViewport(viewport);
        notifyOnFragmentChanged(type + ":" + prefix + ":" + token);
        viewport.requestRepaint();
        requestRepaint();
    }

    public void showInfo(Message message) {
        synchronized (getApplication()) {
            proxy.call("showMessage", MessageType.INFO.name(), message.getSubject(), message.getMessage(), message.getId());
            pusher.push();
        }
    }
    
    public void showError(Message message) {
        synchronized (getApplication()) {
            proxy.call("showMessage", MessageType.ERROR.name(), message.getSubject(), message.getMessage(), message.getId());
            pusher.push();
        }
    }

    public void showWarning(Message message) {
        synchronized (getApplication()) {
            proxy.call("showMessage", MessageType.WARNING.name(), message.getSubject(), message.getMessage(), message.getId());
            pusher.push();
        }
    }

    public void updateShellAppIndication(ShellAppType type, int increment) {
        synchronized (getApplication()) {
            proxy.call("updateIndication", type.name(), increment);
            pusher.push();
        }
    }
    
    public void removeDialog(Component dialog) {
        viewports.get(ViewportType.DIALOG_VIEWPORT).removeComponent(dialog);
        requestRepaint();
    }

    public void addDialog(Component dialog) {
        viewports.get(ViewportType.DIALOG_VIEWPORT).addComponent(dialog);
        requestRepaint();
    }

    public abstract void closeCurrentShellApp();

    public void removeMessage(String messageId) {}
    
    public void closeCurrentApp() {
        getAppViewport().pop();
    }

    public void setActiveViewport(ShellViewport activeViewport) {
        if (this.activeViewport != activeViewport) {
            this.activeViewport = activeViewport;
            for (final ViewportType type : ViewportType.values()) {
                if (this.activeViewport == viewports.get(type)) {
                    proxy.call("activeViewportChanged", type.name());
                    break;
                }
            }
        }
    }
    
    public ShellViewport getAppViewport() {
        return viewports.get(ViewportType.APP_VIEWPORT);
    }

    public ShellViewport getShellAppViewport() {
        return viewports.get(ViewportType.SHELL_APP_VIEWPORT);
    }

    public ShellViewport getDialogViewport() {
        return viewports.get(ViewportType.DIALOG_VIEWPORT);
    }

    public ShellViewport getActiveViewport() {
        return activeViewport;
    }
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.startTag("pusher");
        pusher.paint(target);
        target.endTag("pusher");
        final Iterator<Entry<ViewportType, ShellViewport>> it = viewports.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ViewportType, ShellViewport> entry = it.next();
            final String tagName = entry.getKey().name();
            target.startTag(tagName);
            entry.getValue().paint(target);
            if (entry.getValue() == activeViewport) {
                target.addAttribute("active", entry.getKey().name());    
            }
            target.endTag(tagName);
        }

        proxy.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public void attach() {
        super.attach();
        pusher.attach();
        pusher.setParent(this);
        for (final ShellViewport viewport : viewports.values()) {
            viewport.setParent(this);
            viewport.attach();
        }
    }

    @Override
    public void detach() {
        super.detach();
        pusher.detach();
        for (final ShellViewport viewport : viewports.values()) {
            viewport.detach();
        }
    }
    
    private void notifyOnFragmentChanged(final String fragment) {
        handlers.dispatch(new FragmentChangedEvent(fragment));
    }
    
    @Override
    public Object[] initRequestFromClient() {
        proxy.call("registerApps", new Gson().toJson(registeredApps));
        for (final String runningAppName : runningApps) {
            proxy.call("onAppStarted", runningAppName);
        }
        if (activeViewport != null) {
            for (final ViewportType type : ViewportType.values()) {
                if (this.activeViewport == viewports.get(type)) {
                    proxy.call("activeViewportChanged", type.name());
                    break;
                }
            }   
        } 
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        System.out.println("Client called " + method);
    }

    public void addFragmentChangedHanlder(final FragmentChangedHandler handler) {
        handlers.add(handler);
    }

    public void removeFragmentChangedHanlder(final FragmentChangedHandler handler) {
        handlers.remove(handler);
    }
    
    protected ICEPush getPusher() {
        return pusher; 
    }
    
    protected void doRegisterApps(List<String> appNames) {
        registeredApps.clear();
        registeredApps.addAll(appNames);
        proxy.call("registerApps", new Gson().toJson(appNames));
    }
    
    protected void onAppStarted(String appName) {
        proxy.call("onAppStarted", appName);
        if (!runningApps.contains(appName)) {
            runningApps.add(appName);
        }
    }
    
    protected void onAppStopped(String appName) {
        proxy.call("onAppStopped", appName);
        runningApps.remove(appName);
    }
    
    protected ServerSideProxy proxy = new ServerSideProxy(this) {{
        register("activateShellApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                navigateToShellApp(String.valueOf(params[0]), String.valueOf(params[1]));
            }
        });

        register("activateApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                navigateToApp(String.valueOf(params[0]), String.valueOf(params[1]));
            }
        });
        
        register("removeMessage", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                removeMessage(String.valueOf(params[0]));
            }
        });

        register("closeCurrentApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                closeCurrentApp();
            }
        });

        register("closeCurrentShellApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                closeCurrentShellApp();
            }
        });
    }};
}
