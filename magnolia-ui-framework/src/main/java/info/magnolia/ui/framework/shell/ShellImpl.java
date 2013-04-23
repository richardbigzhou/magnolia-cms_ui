/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.model.overlay.AlertCallback;
import info.magnolia.ui.model.overlay.ConfirmationCallback;
import info.magnolia.ui.model.overlay.MessageStyleType;
import info.magnolia.ui.model.overlay.NotificationCallback;
import info.magnolia.ui.model.overlay.OverlayCloser;
import info.magnolia.ui.model.overlay.OverlayLayer;
import info.magnolia.ui.model.overlay.View;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShell;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;
import info.magnolia.ui.vaadin.overlay.OverlayPresenter;
import info.magnolia.ui.vaadin.view.Viewport;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Component;


/**
 * Admin shell.
 */
@Singleton
public class ShellImpl implements Shell, MessageEventHandler {

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

    private OverlayLayer overlayPresenter;

    @Inject
    public ShellImpl(@Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, final AppController appController, final MessagesManager messagesManager) {
        super();
        this.messagesManager = messagesManager;
        this.admincentralEventBus = admincentralEventBus;
        this.appController = appController;
        this.admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            @Override
            public void onAppFocused(AppLifecycleEvent event) {
                magnoliaShell.setActiveViewport(magnoliaShell.getAppViewport());
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

        this.admincentralEventBus.addHandler(MessageEvent.class, this);

        this.magnoliaShell = new MagnoliaShell();
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

        overlayPresenter = new OverlayPresenter() {

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
        return magnoliaShell.getActiveViewport().getCurrentShellFragment();
    }

    @Override
    public void setFragment(String fragment) {
        Fragment f = Fragment.fromString(fragment);
        f.setAppId(DefaultLocation.extractAppId(fragment));
        f.setSubAppId(DefaultLocation.extractSubAppId(fragment));
        f.setParameter(DefaultLocation.extractParameter(fragment));

        getMagnoliaShell().getUI().getPage().setUriFragment(fragment, false);
        magnoliaShell.getActiveViewport().setCurrentShellFragment(f.toFragment());
        magnoliaShell.propagateFragmentToClient(f);
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
    public void registerApps(List<String> appNames) {
        magnoliaShell.doRegisterApps(appNames);
    }

    private void goToApp(Fragment fragment) {
        restoreAppParameter(fragment);
        magnoliaShell.doNavigate(magnoliaShell.getAppViewport(), fragment);
    }

    private void goToShellApp(Fragment fragment) {
        restoreShellAppParameter(fragment);
        magnoliaShell.doNavigate(magnoliaShell.getShellAppViewport(), fragment);
    }

    private void stopCurrentShellApp() {
        ShellViewport appViewport = magnoliaShell.getAppViewport();
        if (!appViewport.isEmpty()) {
            // An app is open.
            magnoliaShell.setActiveViewport(appViewport);
            appController.focusCurrentApp();
        } else {
            // No apps are open.
            String appLauncherNameLower = ShellAppType.APPLAUNCHER.name().toLowerCase();
            // Only navigate if the requested location is not the applauncher
            if (magnoliaShell.getActiveViewport() != null) {
                String fragmentCurrent = magnoliaShell.getActiveViewport().getCurrentShellFragment();
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
            Location location = shellAppLocationProvider.getShellAppLocation(f.getAppId());
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

    @Override
    public void showInfo(Message message) {
        magnoliaShell.showInfo(message.getId(), message.getSubject(), message.getMessage());
    }

    @Override
    public void showError(Message message) {
        magnoliaShell.showError(message.getId(), message.getSubject(), message.getMessage());
    }

    @Override
    public void showWarning(Message message) {
        magnoliaShell.showWarning(message.getId(), message.getSubject(), message.getMessage());
    }

    public MagnoliaShell getMagnoliaShell() {
        return magnoliaShell;
    }

    @Override
    public OverlayCloser openOverlay(View view) {
        return overlayPresenter.openOverlay(view);
    }

    @Override
    public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
        return overlayPresenter.openOverlay(view, modalityLevel);
    }

    @Override
    public void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        overlayPresenter.openAlert(type, viewToShow, confirmButtonText, cb);
    }

    @Override
    public void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        overlayPresenter.openAlert(type, title, body, confirmButtonText, cb);
    }

    @Override
    public void openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        overlayPresenter.openConfirmation(type, viewToShow, confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }

    @Override
    public void openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        overlayPresenter.openConfirmation(type, title, body, confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow) {
        overlayPresenter.openNotification(type, doesTimeout, viewToShow);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
        overlayPresenter.openNotification(type, doesTimeout, title);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb) {
        overlayPresenter.openNotification(type, doesTimeout, title, linkText, cb);
    }

}
