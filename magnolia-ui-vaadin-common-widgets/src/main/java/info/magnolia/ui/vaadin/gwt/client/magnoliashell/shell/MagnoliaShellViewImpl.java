/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.AppActivatedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppNavigationEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ViewportCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.handler.ShellNavigationHandler;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.handler.ViewportCloseHandler;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VInfoMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellErrorMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VWarningMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget.PreloaderCallback;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.DialogViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ShellAppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 */
public class MagnoliaShellViewImpl extends TouchPanel implements MagnoliaShellView, ViewportCloseHandler {

    public static final String CLASSNAME = "v-magnolia-shell";

    private final Map<ViewportType, ViewportWidget> viewports = new EnumMap<ViewportType, ViewportWidget>(ViewportType.class);

    private final ShellAppLauncher mainAppLauncher;

    private final EventBus eventBus;

    private VShellMessage lowPriorityMessage;

    private VShellMessage hiPriorityMessage;

    private ViewportWidget activeViewport = null;

    private Presenter presenter;

    public MagnoliaShellViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.mainAppLauncher = new ShellAppLauncher(eventBus);
        setStyleName(CLASSNAME);
        add(mainAppLauncher, getElement());
        bindEventHandlers();
    }

    private void bindEventHandlers() {
        eventBus.addHandler(ViewportCloseEvent.TYPE, this);
        eventBus.addHandler(ShellAppNavigationEvent.TYPE, navigationHandler);
        eventBus.addHandler(AppActivatedEvent.TYPE, navigationHandler);

        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                presenter.handleHistoryChange(event.getValue());
            }
        });
    }

    protected AppsViewportWidget getAppViewport() {
        return (AppsViewportWidget) viewports.get(ViewportType.APP_VIEWPORT);
    }

    protected ShellAppsViewportWidget getShellAppViewport() {
        return (ShellAppsViewportWidget) viewports.get(ViewportType.SHELL_APP_VIEWPORT);
    }

    protected DialogViewportWidget getDialogViewport() {
        return (DialogViewportWidget) viewports.get(ViewportType.DIALOG_VIEWPORT);
    }

    @Override
    public void setActiveViewport(ViewportWidget viewport) {
        if (activeViewport != viewport) {
            final ViewportWidget shellAppViewport = getShellAppViewport();
            final ViewportWidget appViewport = getAppViewport();

            boolean appViewportActive = appViewport == viewport;
            if (appViewportActive) {
                mainAppLauncher.deactivateControls();
            }
            shellAppViewport.setActive(!appViewportActive);
            appViewport.setActive(appViewportActive);

            activeViewport = viewport;
        }
    }

    protected void replaceWidget(final Widget oldWidget, final Widget newWidget) {
        if (oldWidget != newWidget) {
            if (oldWidget != null) {
                remove(oldWidget);
            }
        }
        if (getWidgetIndex(newWidget) < 0) {
            add(newWidget, getElement());
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showMessage(MessageType type, String topic, String message, String id) {
        final VShellMessage msg;
        switch (type) {
        case WARNING:
            msg = new VWarningMessage(this, topic, message, id);
            if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
                lowPriorityMessage.hide();
            }
            lowPriorityMessage = msg;
            break;
        case INFO:
            msg = new VInfoMessage(this, topic, message, id);
            if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
                lowPriorityMessage.hide();
            }
            lowPriorityMessage = msg;
            break;
        case ERROR:
            msg = new VShellErrorMessage(this, topic, message, id);
            if (hiPriorityMessage != null && getWidgetIndex(hiPriorityMessage) != -1) {
                hiPriorityMessage.hide();
            }
            hiPriorityMessage = msg;
            break;
        default:
            msg = null;
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (msg != null) {
                    add(msg, getElement());
                }
            }
        });
    }

    @Override
    public void hideAllMessages() {
        if (hiPriorityMessage != null && getWidgetIndex(hiPriorityMessage) != -1) {
            hiPriorityMessage.hideWithoutTransition();
        }
        hiPriorityMessage = null;

        if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
            lowPriorityMessage.hideWithoutTransition();
        }
        lowPriorityMessage = null;
    }

    @Override
    public void updateViewport(ViewportWidget viewport, ViewportType type) {
        doUpdateViewport(viewport, type);
    }

    private final ShellNavigationHandler navigationHandler = new ShellNavigationHandler() {
        @Override
        public void onAppActivated(AppActivatedEvent event) {
            String prefix = activeViewport == getAppViewport() ? "app" : "shell";
            String fragment = prefix + event.getAppId() + ":" + event.getSubAppId() + ";" + event.getParameter();
            History.newItem(fragment, false);
        }

        @Override
        public void onShellAppNavigation(final ShellAppNavigationEvent event) {
            Widget shellApp = presenter.getShellAppWidget(event.getType());
            ShellAppsViewportWidget viewport = getShellAppViewport();
            ShellAppNavigationEvent refreshEvent = viewport.getRefreshEvent();

            // if interrupting to another shell app before refresh event comes
            if (!event.equals(refreshEvent)) {
                refreshEvent = null;
            }
            if (shellApp != null && refreshEvent == null) {
                viewport.setRefreshEvent(event);

                // Need to update state of button and divet in the main launcher
                mainAppLauncher.activateControl(event.getType());

                viewport.setVisibleApp(shellApp);
                // trigger viewport active transition if it was not.
                if (!viewport.isActive()) {
                    setActiveViewport(getShellAppViewport());
                }
            } else {
                viewport.setRefreshEvent(null);
                getShellAppViewport().showLoadingPane();
                presenter.loadShellApp(Fragment.fromFragment("shell:" + event.getType().name().toLowerCase() + ":" + event.getToken()));
            }
        }

    };

    @Override
    public void onViewportClose(ViewportCloseEvent event) {
        final ViewportType viewportType = event.getViewportType();
        if (viewportType == ViewportType.SHELL_APP_VIEWPORT) {
            getShellAppViewport().setClosing(true);
            presenter.closeCurrentShellApp();
        } else if (viewportType == ViewportType.APP_VIEWPORT) {
            presenter.closeCurrentApp();
        }
    }

    @Override
    public void navigate(String appId, String subAppId, String parameter) {
        eventBus.fireEvent(new AppActivatedEvent(activeViewport == getShellAppViewport(), appId, subAppId, parameter));
    }

    @Override
    public Collection<ViewportWidget> getViewports() {
        return Collections.unmodifiableCollection(viewports.values());
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void shiftViewportsVertically(int shiftPx, boolean animated) {
        for (final ViewportWidget viewport : viewports.values()) {
            final AnimationSettings settings = new AnimationSettings();
            settings.setProperty("top", "+=" + shiftPx);
            settings.setProperty("height", "+=" + -shiftPx);
            settings.addCallback(new JQueryCallback() {

                @Override
                public void execute(JQueryWrapper query) {
                    getPresenter().updateViewportLayout(viewport);
                }
            });
            JQueryWrapper.select(viewport).animate(animated ? 300 : 0, settings);
        }
    }

    /*
     * @Override public void setPusher(final VICEPush pusher) { if
     * (getWidgetIndex(pusher) != -1) { insert(pusher, 0); } }
     */

    @Override
    public void setShellAppIndication(ShellAppType type, int indication) {
        mainAppLauncher.setIndication(type, indication);
    }

    @Override
    public void closeMessageEager(final String id) {
        presenter.removeMessage(id);
    }

    @Override
    public void navigateToMessageDetails(String id) {
        presenter.loadShellApp(Fragment.fromFragment("shell:pulse:messages/" + id));
    }

    private void doUpdateViewport(ViewportWidget viewport, ViewportType type) {
        final ViewportWidget oldViewport = viewports.get(type);
        if (oldViewport != viewport) {
            replaceWidget(oldViewport, viewport);
            viewport.setEventBus(eventBus);
            viewports.put(type, viewport);
        }
    }

    @Override
    public void showAppPreloader(String prefix, PreloaderCallback preloaderCallback) {
        // setActiveViewport(ViewportType.APP_VIEWPORT);
        getAppViewport().showAppPreloader(prefix, preloaderCallback);
    }

    @Override
    public void updateShellDivet() {
        mainAppLauncher.updateDivet();
    }
}
