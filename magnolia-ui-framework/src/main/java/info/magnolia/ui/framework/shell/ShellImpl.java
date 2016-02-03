/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.framework.shell;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.EventHandlerCollection;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.AppLifecycleEvent;
import info.magnolia.ui.api.app.AppLifecycleEventHandler;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.shell.ConfirmationHandler;
import info.magnolia.ui.api.shell.FragmentChangedEvent;
import info.magnolia.ui.api.shell.FragmentChangedHandler;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.framework.context.AbstractUIContext;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.overlay.OverlayPresenter;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShell;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.ui.Component;

/**
 * Admin shell.
 */
@Singleton
public class ShellImpl extends AbstractUIContext implements Shell, MessageEventHandler {

    /**
     * Provides the current location of shell apps.
     */
    public interface ShellAppLocationProvider {

        Location getShellAppLocation(String name);
    }

    private final EventBus admincentralEventBus;

    private final AppController appController;

    private final MessagesManager messagesManager;

    private final MagnoliaShell magnoliaShell;

    private final EventHandlerCollection<FragmentChangedHandler> handlers = new EventHandlerCollection<FragmentChangedHandler>();

    private ShellAppLocationProvider shellAppLocationProvider;

    @Inject
    public ShellImpl(@Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, final AppController appController, final MessagesManager messagesManager) {
        super();
        this.messagesManager = messagesManager;
        this.admincentralEventBus = admincentralEventBus;
        this.appController = appController;
        this.admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            @Override
            public void onAppFocused(AppLifecycleEvent event) {
            }

            @Override
            public void onAppStarted(AppLifecycleEvent event) {
                magnoliaShell.onAppStarted(event.getAppDescriptor().getName());
            }

            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                magnoliaShell.onAppStopped(event.getAppDescriptor().getName());
            }
        });

        this.magnoliaShell = new MagnoliaShell();
        this.admincentralEventBus.addHandler(MessageEvent.class, this);

        this.magnoliaShell.setListener(new MagnoliaShell.Listener() {

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

    @Override
    protected OverlayPresenter initializeOverlayPresenter() {
        return new OverlayPresenter() {
            @Override
            public OverlayCloser openOverlay(final View view, ModalityLevel modalityLevel) {
                return magnoliaShell.openOverlay(view, magnoliaShell, ModalityDomain.SHELL, modalityLevel);
            }
        };
    }

    public void setShellAppLocationProvider(ShellAppLocationProvider shellAppLocationProvider) {
        this.shellAppLocationProvider = shellAppLocationProvider;
    }

    private void stopCurrentApp() {
        appController.stopCurrentApp();
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
        return getMagnoliaShell().getUriFragment();
    }

    @Override
    public void setFragment(String fragment) {
        Fragment f = Fragment.fromString(fragment);
        getMagnoliaShell().setUriFragment(f);
    }

    @Override
    public HandlerRegistration addFragmentChangedHandler(final FragmentChangedHandler handler) {
        return handlers.add(handler);
    }

    private void removeMessage(String messageId) {
        messagesManager.clearMessage(MgnlContext.getUser().getName(), messageId);
    }

    @Override
    public OverlayCloser openOverlayOnView(View view, View parent, ModalityDomain modalityLocation, ModalityLevel modalityLevel) {
        return magnoliaShell.openOverlay(view, parent, modalityLocation, modalityLevel);
    }

    @Override
    public void setUserMenu(View view) {
        magnoliaShell.setUserMenu(view);
    }

    @Override
    public void messageSent(MessageEvent event) {
        final Message message = event.getMessage();
        switch (message.getType()) {
        case WARNING:
            magnoliaShell.showWarning(message.getId(), message.getSubject(), message.getMessage());
            break;
        case ERROR:
            magnoliaShell.showError(message.getId(), message.getSubject(), message.getMessage());
            break;
        case INFO:
            magnoliaShell.showInfo(message.getId(), message.getSubject(), message.getMessage());
        default:
            break;
        }
    }

    @Override
    public void messageCleared(MessageEvent event) {
    }

    @Override
    public void messageRemoved(MessageEvent messageEvent) {
        // no op
    }

    @Override
    public void registerApps(List<String> appNames) {
        magnoliaShell.doRegisterApps(appNames);
    }

    private void goToApp(Fragment fragment) {
        restoreAppParameter(fragment);
        magnoliaShell.notifyOnFragmentChanged(fragment.toFragment());
    }

    private void goToShellApp(Fragment fragment) {
        restoreShellAppParameter(fragment);
        magnoliaShell.notifyOnFragmentChanged(fragment.toFragment());
        exitFullScreenMode();
    }

    private void stopCurrentShellApp() {
        ShellViewport appViewport = magnoliaShell.getAppViewport();
        if (!appViewport.isEmpty()) {
            // An app is open.
            appController.focusCurrentApp();
        } else {
            // No apps are open.
            String appLauncherNameLower = ShellAppType.APPLAUNCHER.name().toLowerCase();
            // Only navigate if the requested location is not the applauncher
            String fragmentCurrent = getFragment();
            if (fragmentCurrent != null && !fragmentCurrent.startsWith(appLauncherNameLower)) {
                goToShellApp(Fragment.fromString("shell:applauncher"));
            }
        }
    }

    @Override
    public void pushToClient() {
    }

    /**
     * Shell's client side doesn't remember the parameter of an app,
     * so we need to restore it from the framework internals.
     */
    private void restoreAppParameter(Fragment f) {
        String actualParam = f.getParameter();
        if (StringUtils.isEmpty(actualParam)) {
            Location location = appController.getAppLocation(f.getAppName());
            if (location != null) {
                f.setParameter(location.getParameter());
            }
        }
    }

    private void restoreShellAppParameter(Fragment f) {
        String actualParam = f.getParameter();
        if (StringUtils.isEmpty(actualParam)) {
            Location location = shellAppLocationProvider.getShellAppLocation(f.getAppName());
            if (location != null) {
                f.setParameter(location.getParameter());
            }
        }
    }

    public Viewport getShellAppViewport() {
        return magnoliaShell.getShellAppViewport();
    }

    public Viewport getAppViewport() {
        return magnoliaShell.getAppViewport();
    }

    public void setIndication(ShellAppType type, int indication) {
        magnoliaShell.setIndication(type, indication);
    }

    public void updateShellAppIndication(ShellAppType type, int incrementOrDecrement) {
        magnoliaShell.updateShellAppIndication(type, incrementOrDecrement);
    }

    public void registerShellApp(ShellAppType type, Component component) {
        magnoliaShell.registerShellApp(type, component);
    }

    public void hideAllMessages() {
        magnoliaShell.hideAllMessages();
    }

    public void setFullScreen(boolean isFullScreen) {
        magnoliaShell.setFullScreen(isFullScreen);
    }

    public MagnoliaShell getMagnoliaShell() {
        return magnoliaShell;
    }

    @Override
    public void enterFullScreenMode() {
        setFullScreen(true);

    }

    @Override
    public void exitFullScreenMode() {
        setFullScreen(false);
    }
}
