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
import info.magnolia.event.EventBus;
import info.magnolia.event.EventHandlerCollection;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.ui.admincentral.app.simple.ShellAppController;
import info.magnolia.ui.admincentral.dialog.BaseDialogPresenter;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.AdminCentralEventBusConfigurer;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.view.Viewport;
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

import com.vaadin.ui.Component;
import org.apache.commons.lang.StringUtils;

/**
 * Admin shell.
 */
@Singleton
public class ShellImpl implements Shell, MessageEventHandler {

    private final EventBus admincentralEventBus;

    private final AppController appController;

    private final Provider<ShellAppController> shellAppControllerProvider;

    private final MessagesManager messagesManager;

    private final MagnoliaShellBase magnoliaShellBase;

    private final EventHandlerCollection<FragmentChangedHandler> handlers = new EventHandlerCollection<FragmentChangedHandler>();

    @Inject
    public ShellImpl(@Named(AdminCentralEventBusConfigurer.EVENT_BUS_NAME) EventBus admincentralEventBus, Provider<ShellAppController> shellAppControllerProvider, final AppController appController, final MessagesManager messagesManager) {
        super();
        this.messagesManager = messagesManager;
        this.admincentralEventBus = admincentralEventBus;
        this.appController = appController;
        this.shellAppControllerProvider = shellAppControllerProvider;
        this.admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            @Override
            public void onAppFocused(AppLifecycleEvent event) {
                magnoliaShellBase.setActiveViewport(magnoliaShellBase.getAppViewport());
            }

            @Override
            public void onAppStarted(AppLifecycleEvent event) {
                magnoliaShellBase.onAppStarted(event.getAppDescriptor().getName());
            }

            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                magnoliaShellBase.onAppStopped(event.getAppDescriptor().getName());
            }
        });

        this.admincentralEventBus.addHandler(MessageEvent.class, this);

        this.magnoliaShellBase = new MagnoliaShellBase();
        this.magnoliaShellBase.setListener(new MagnoliaShellBase.Listener() {

            @Override
            public void onFragmentChanged(String fragment) {
                handlers.dispatch(new FragmentChangedEvent(fragment));
            }

            @Override
            public void stopCurrentShellApp() {
                ShellImpl.this.stopCurrentShellApp();
            }

            @Override
            public void stopCurrentApp() {
                ShellImpl.this.stopCurrentApp();
            }

            @Override
            public void removeMessage(String messageId) {
                ShellImpl.this.removeMessage(messageId);
            }

            @Override
            public void goToApp(Fragment fragment) {
                ShellImpl.this.goToApp(fragment);
            }

            @Override
            public void goToShellApp(Fragment fragment) {
                ShellImpl.this.goToShellApp(fragment);
            }
        });
    }

    private void stopCurrentApp() {
        magnoliaShellBase.getAppViewport().pop();
        appController.stopCurrentApp();
        if (magnoliaShellBase.getAppViewport().isEmpty()) {
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
        return magnoliaShellBase.getActiveViewport().getCurrentShellFragment();
    }

    @Override
    public void setFragment(String fragment) {
        Fragment f = Fragment.fromString(fragment);
        f.setAppId(DefaultLocation.extractAppId(fragment));
        f.setSubAppId(DefaultLocation.extractSubAppId(fragment));
        f.setParameter(DefaultLocation.extractParameter(fragment));

        magnoliaShellBase.getActiveViewport().setCurrentShellFragment(f.toFragment());
        magnoliaShellBase.propagateFragmentToClient(f);
    }

    @Override
    public HandlerRegistration addFragmentChangedHandler(final FragmentChangedHandler handler) {
        return handlers.add(handler);
    }

    private void removeMessage(String messageId) {
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
        magnoliaShellBase.addDialog(dialogPresenter.getView().asVaadinComponent());
    }

    public void removeDialog(BaseDialog dialog) {
        magnoliaShellBase.removeDialog(dialog.asVaadinComponent());
    }

    @Override
    public void messageSent(MessageEvent event) {
        final Message message = event.getMessage();
        switch (message.getType()) {
        case WARNING:
            magnoliaShellBase.showWarning(message.getId(), message.getSubject(), message.getMessage());
            break;
        case ERROR:
            magnoliaShellBase.showError(message.getId(), message.getSubject(), message.getMessage());
            break;
        case INFO:
            magnoliaShellBase.showInfo(message.getId(), message.getSubject(), message.getMessage());
        default:
            break;
        }
    }

    @Override
    public void messageCleared(MessageEvent event) {
    }

    @Override
    public void registerApps(List<String> appNames) {
        magnoliaShellBase.doRegisterApps(appNames);
    }

    private void goToApp(Fragment fragment) {
        restoreAppParameter(fragment);
        magnoliaShellBase.doNavigate(magnoliaShellBase.getAppViewport(), fragment);
    }

    private void goToShellApp(Fragment fragment) {
        restoreShellAppParameter(fragment);
        magnoliaShellBase.doNavigate(magnoliaShellBase.getShellAppViewport(), fragment);
    }

    private void stopCurrentShellApp() {
        ShellViewport appViewport = magnoliaShellBase.getAppViewport();
        if (!appViewport.isEmpty()) {
            // An app is open.
            magnoliaShellBase.setActiveViewport(appViewport);
            appController.focusCurrentApp();
        } else {
            // No apps are open.
            String appLauncherNameLower = ShellAppType.APPLAUNCHER.name().toLowerCase();
            // Only navigate if the requested location is not the applauncher
            if (magnoliaShellBase.getActiveViewport() != null) {
                String fragmentCurrent = magnoliaShellBase.getActiveViewport().getCurrentShellFragment();
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
     * Shell's client side doesn't remember the parameter of an app,
     * so we need to restore it from the framework internals.
     */
    private void restoreAppParameter(Fragment f) {
        String actualParam = f.getParameter();
        if (StringUtils.isEmpty(actualParam)) {
            Location location = appController.getAppLocation(f.getAppId());
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

    public Viewport getShellAppViewport() {
        return magnoliaShellBase.getShellAppViewport();
    }

    public Viewport getAppViewport() {
        return magnoliaShellBase.getAppViewport();
    }

    public void setIndication(ShellAppType type, int indication) {
        magnoliaShellBase.setIndication(type, indication);
    }

    public void updateShellAppIndication(ShellAppType type, int incrementOrDecrement) {
        magnoliaShellBase.updateShellAppIndication(type, incrementOrDecrement);
    }

    public void registerShellApp(ShellAppType type, Component component) {
        magnoliaShellBase.registerShellApp(type, component);
    }

    public void hideAllMessages() {
        magnoliaShellBase.hideAllMessages();
    }

    @Override
    public void showInfo(Message message) {
        magnoliaShellBase.showInfo(message.getId(), message.getSubject(), message.getMessage());
    }

    @Override
    public void showError(Message message) {
        magnoliaShellBase.showError(message.getId(), message.getSubject(), message.getMessage());
    }

    @Override
    public void showWarning(Message message) {
        magnoliaShellBase.showWarning(message.getId(), message.getSubject(), message.getMessage());
    }

    public MagnoliaShellBase getMagnoliaShellBase() {
        return magnoliaShellBase;
    }
}
