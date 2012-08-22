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

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.app.simple.ShellAppController;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.widget.dialog.Dialog;
import info.magnolia.ui.widget.magnoliashell.BaseMagnoliaShell;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.magnoliashell.viewport.ShellViewport;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.rpc.client.Method;

import com.google.gson.Gson;

/**
 * Admin shell.
 */
@SuppressWarnings("serial")
@Singleton
public class MagnoliaShell extends BaseMagnoliaShell implements Shell, MessageEventHandler {

    private static final Logger log = LoggerFactory.getLogger(MagnoliaShell.class);

    private final EventBus admincentralEventBus;

    private final AppController appController;
    private final Provider<ShellAppController> shellAppControllerProvider;

    private final MessagesManager messagesManager;
    
    @Inject
    public MagnoliaShell(@Named("admincentral") EventBus admincentralEventBus, Provider<ShellAppController> shellAppControllerProvider, AppController appController, MessagesManager messagesManager) {
        super();
        this.messagesManager = messagesManager;
        this.admincentralEventBus = admincentralEventBus;
        this.appController = appController;
        this.shellAppControllerProvider = shellAppControllerProvider;
        this.admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            @Override
            public void onAppFocused(AppLifecycleEvent event) {
                setActiveViewport(getAppViewport());
            }
            
            @Override
            public void onAppStarted(AppLifecycleEvent event) {
                proxy.call("onAppStarted", event.getAppDescriptor().getName());
            }
            
            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                proxy.call("onAppStopped", event.getAppDescriptor().getName());
            }
        });
        
        proxy.register("startApp", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                setActiveViewport(getAppViewport());
                final String appName = String.valueOf(params[0]);
                final String token = String.valueOf(params[1]);
                MagnoliaShell.this.appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, appName, token));
            }
        });
        
        this.admincentralEventBus.addHandler(MessageEvent.class, this);
    }

    @Override
    public void closeCurrentApp() {
        super.closeCurrentApp();
        appController.stopCurrentApp();
        if (getAppViewport().isEmpty()) {
            navigateToShellApp(ShellAppType.APPLAUNCHER.name().toLowerCase(), "");
        }
    }

    @Override
    public void askForConfirmation(String message, ConfirmationHandler listener) {
    }

    @Override
    public void showNotification(String messageText) {
        showMessage(messageText, MessageType.INFO);
    }

    @Override
    public void showError(String messageText, Exception e) {
        showMessage(messageText, MessageType.ERROR);
    }

    private void showMessage(String messageText, MessageType type) {
        final Message message = new Message();
        message.setMessage(messageText);
        message.setType(type);
        messagesManager.sendLocalMessage(message);
    }


    @Override
    public String getFragment() {
        final ShellViewport activeViewport = getActiveViewport();
        String viewPortName = "";
        if (activeViewport == getShellAppViewport()) {
            viewPortName = "shell";
        } else if (activeViewport == getAppViewport()) {
            viewPortName = "app";
        } else if (activeViewport == getDialogViewport()) {
            viewPortName = "dialog";
        }
        return viewPortName + ":" + (activeViewport == null ? "" : activeViewport.getCurrentShellFragment());
    }

    @Override
    public void setFragment(String fragment) {

        String prefix = DefaultLocation.extractPrefix(fragment);
        String token = DefaultLocation.extractToken(fragment);

        final ShellViewport activeViewport = getActiveViewport();
        activeViewport.setCurrentShellFragment(prefix + ":" + token);

        proxy.call("navigate", prefix, token);
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
    public void removeMessage(String messageId) {
        super.removeMessage(messageId);
        messagesManager.clearMessage(MgnlContext.getUser().getName(), messageId);
    }
    
    public void openDialog(Dialog component) {
        addDialog(component.asVaadinComponent());
    }

    public void removeDialog(Dialog dialog) {
        removeDialog(dialog.asVaadinComponent());
    }

    @Override
    public void messageSent(MessageEvent event) {
        final Message message = event.getMessage();
        switch (message.getType()) {
            case WARNING:
                showWarning(message);
                break;
            case ERROR:
                showError(message);
                break;
            case INFO:
                showInfo(message);
            default:
                break;
        }
    }

    @Override
    public void messageCleared(MessageEvent event) {
    }

    @Override
    public void registerApps(List<String> appNames) {
        proxy.call("registerApps", new Gson().toJson(appNames));
    }

    @Override
    public void navigateToApp(String prefix, String token) {
        if (StringUtils.isEmpty(token)) {
            ShellViewport viewport = getAppViewport();
            viewport.setCurrentShellFragment(prefix + ":" + token);
            setActiveViewport(viewport);
            appController.startIfNotAlreadyRunningThenFocus(prefix, new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, prefix, token));
            requestRepaint();
        } else {
            super.navigateToApp(prefix, token);
        }
    }

    @Override
    public void navigateToShellApp(String prefix, String token) {
        if (StringUtils.isEmpty(token)) {
            ShellViewport viewport = getShellAppViewport();
            viewport.setCurrentShellFragment(prefix + ":" + token);
            setActiveViewport(viewport);
            shellAppControllerProvider.get().focusShellApp(prefix);
            requestRepaint();
        } else {
            super.navigateToShellApp(prefix, token);
        }
    }

    @Override
    public void closeCurrentShellApp() {
        if (!getAppViewport().isEmpty()) {
            // An app is open.
            setActiveViewport(getAppViewport());
            appController.focusCurrentApp();

        } else {
            // No apps are open.
            String appLauncherNameLower = ShellAppType.APPLAUNCHER.name().toLowerCase();
            navigateToShellApp(appLauncherNameLower, "");
        }
    }

    @Override
    public void pushToClient() {
        synchronized (getApplication()) {
            getPusher().push();
        }
    }
}
