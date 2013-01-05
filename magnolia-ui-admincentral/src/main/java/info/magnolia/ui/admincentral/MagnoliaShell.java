/**
 * This file Copyright (c) 2012 Magnolia International
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
import info.magnolia.ui.admincentral.dialog.BaseDialogPresenter;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShellBase;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

/**
 * Admin shell.
 */
@Singleton
public class MagnoliaShell extends MagnoliaShellBase implements Shell, MessageEventHandler {

    private final EventBus admincentralEventBus;

    private final AppController appController;

    private final Provider<ShellAppController> shellAppControllerProvider;

    private final MessagesManager messagesManager;

    @Inject
    public MagnoliaShell(@Named("admincentral") EventBus admincentralEventBus, Provider<ShellAppController> shellAppControllerProvider,
            AppController appController, MessagesManager messagesManager) {
        super();
        this.messagesManager = messagesManager;
        this.admincentralEventBus = admincentralEventBus;
        this.appController = appController;
        this.shellAppControllerProvider = shellAppControllerProvider;
        this.admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            @Override
            public void onAppFocused(AppLifecycleEvent event) {
                setActiveViewport(appViewport());
            }

            @Override
            public void onAppStarted(AppLifecycleEvent event) {
                MagnoliaShell.this.onAppStarted(event.getAppDescriptor().getName());
            }

            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                MagnoliaShell.this.onAppStopped(event.getAppDescriptor().getName());
            }
        });

        this.admincentralEventBus.addHandler(MessageEvent.class, this);
    }

    @Override
    public void stopCurrentApp() {
        appViewport().pop();
        appController.stopCurrentApp();
        if (appViewport().isEmpty()) {
            goToShellApp(Fragment.fromString("shell:applauncher"));
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
        return getActiveViewport().getCurrentShellFragment();
    }

    @Override
    public void setFragment(String fragment) {
        Fragment f = Fragment.fromString(fragment);
        f.setAppId(DefaultLocation.extractAppId(fragment));
        f.setSubAppId(DefaultLocation.extractSubAppId(fragment));
        f.setParameter(DefaultLocation.extractParameter(fragment));

        getActiveViewport().setCurrentShellFragment(f.toFragment());
        propagateFragmentToClient(f);
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
        messagesManager.clearMessage(MgnlContext.getUser().getName(), messageId);
    }

    public void openDialog(final BaseDialogPresenter dialogPresenter) {
        dialogPresenter.addDialogCloseHandler(new DialogCloseEvent.Handler() {
            @Override
            public void onClose(DialogCloseEvent event) {
                removeDialog(event.getView().asVaadinComponent());
                event.getView().asVaadinComponent().removeDialogCloseHandler(this);
            }
        });
        addDialog(dialogPresenter.getView().asVaadinComponent());
    }

    public void removeDialog(BaseDialog dialog) {
        super.removeDialog(dialog.asVaadinComponent());
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
    public void messageCleared(MessageEvent event) {}

    @Override
    public void registerApps(List<String> appNames) {
        doRegisterApps(appNames);
    }

    @Override
    public void goToApp(Fragment f) {
        restoreAppParameter(f);
        super.goToApp(f);
    }

    @Override
    public void goToShellApp(Fragment f) {
        restoreShellAppParameter(f);
        super.goToShellApp(f);
    }

    @Override
    public void stopCurrentShellApp() {
        ShellViewport appViewport = appViewport();
        if (!appViewport.isEmpty()) {
            // An app is open.
            setActiveViewport(appViewport);
            appController.focusCurrentApp();
        } else {
            // No apps are open.
            String appLauncherNameLower = ShellAppType.APPLAUNCHER.name().toLowerCase();
            // Only navigate if the requested location is not the applauncher
            if (getActiveViewport() != null) {
                String fragmentCurrent = getActiveViewport().getCurrentShellFragment();
                if (fragmentCurrent != null && !fragmentCurrent.startsWith(appLauncherNameLower)) {
                    goToShellApp(Fragment.fromString("shell:applauncher"));
                }
            }
        }
    }

    @Override
    public void pushToClient() {
        // synchronized (getApplication()) {
        // getPusher().push();
        // }
    }

    /**
     * Shell's client side doesn't remeber the parameter of an app,
     * so we need to restore it from the framework internals.
     */
    private void restoreAppParameter(Fragment f) {
        String actualParam = f.getParameter();
        if (StringUtils.isEmpty(actualParam)) {
            Location location = appController.getCurrentLocation(f.getAppId());
            if (location != null) {
                f.setParameter(location.getParameter());
            }
        }
    }

    private void restoreShellAppParameter(Fragment f) {
        String actualParam = f.getParameter();
        if (StringUtils.isEmpty(actualParam)) {
            Location location = shellAppControllerProvider.get().getCurrentLocation(f.getAppId());
            if (location != null) {
                f.setParameter(location.getParameter());
            }
        }
    }

    public ViewPort getShellAppViewport() {
        return shellAppViewport();
    }

    public ViewPort getAppViewport() {
        return appViewport();
    }
}
