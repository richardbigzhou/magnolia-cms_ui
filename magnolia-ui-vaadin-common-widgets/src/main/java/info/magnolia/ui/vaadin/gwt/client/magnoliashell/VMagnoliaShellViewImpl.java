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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.VMagnoliaShell.ViewportType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.VMainLauncher.ShellAppType;
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.VAppsViewport;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.VAppsViewport.PreloaderCallback;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.VDialogViewport;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.VShellAppsViewport;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.VShellViewport;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.vaadin.artur.icepush.client.ui.VICEPush;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 */
public class VMagnoliaShellViewImpl extends TouchPanel implements VMagnoliaShellView, ViewportCloseHandler {

    public static final String CLASSNAME = "v-magnolia-shell";

    private final Map<ViewportType, VShellViewport> viewports = new EnumMap<ViewportType, VShellViewport>(ViewportType.class);

    private ViewportType activeViewportType = null;

    private final VMainLauncher mainAppLauncher;

    private Presenter presenter;

    private final EventBus eventBus;

    private VShellMessage lowPriorityMessage;

    private VShellMessage hiPriorityMessage;

    public VMagnoliaShellViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.mainAppLauncher = new VMainLauncher(eventBus);
        setStyleName(CLASSNAME);
        add(mainAppLauncher, getElement());
        bindEventHandlers();

        // TODO:Very useful for debugging/development - but perhaps should be removed - Christopher
        // Zimmermann
        if (Window.Location.getQueryString().indexOf("tablet=true") >= 0) {
            RootPanel.get().addStyleName("tablet");
        }

        final IFrameElement iframe = Document.get().createIFrameElement();
        getElement().appendChild(iframe);
        iframe.setPropertyString("width", "100%");
        iframe.setPropertyString("height", "100%");
        iframe.getStyle().setProperty("border", "none");
        // iframe.setSrc("http://www.sport-express.ru");
        iframe.getStyle().setPosition(Position.ABSOLUTE);
        iframe.getStyle().setLeft(0, Unit.PX);
        iframe.getStyle().setTop(0, Unit.PX);
        iframe.getStyle().setWidth(100, Unit.PCT);
        iframe.getStyle().setHeight(100, Unit.PCT);
        // iframe.getStyle().setZIndex(1000);
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

    protected VAppsViewport getAppViewport() {
        return (VAppsViewport) viewports.get(ViewportType.APP_VIEWPORT);
    }

    protected VShellAppsViewport getShellAppViewport() {
        return (VShellAppsViewport) viewports.get(ViewportType.SHELL_APP_VIEWPORT);
    }

    protected VDialogViewport getDialogViewport() {
        return (VDialogViewport) viewports.get(ViewportType.DIALOG_VIEWPORT);
    }

    @Override
    public void setActiveViewport(final ViewportType type) {
        if (activeViewportType != type) {
            final VShellViewport shellAppViewport = getShellAppViewport();
            final VShellViewport appViewport = getAppViewport();

            boolean appViewportActive = type.equals(ViewportType.APP_VIEWPORT);
            if (appViewportActive) {
                mainAppLauncher.deactivateControls();
            }
            shellAppViewport.setActive(!appViewportActive);
            appViewport.setActive(appViewportActive);

            activeViewportType = type;
        }
    }

    @Override
    public int getViewportHeight() {
        int errorMessageHeight = hiPriorityMessage == null &&
                (getWidgetIndex(hiPriorityMessage) > -1) ? hiPriorityMessage.getOffsetHeight() : 0;
        return getOffsetHeight() - mainAppLauncher.getExpandedHeight() - errorMessageHeight;
    }

    @Override
    public int getViewportWidth() {
        return getOffsetWidth();
    }

    @Override
    public boolean remove(Widget w) {
        presenter.destroyChild(w);
        return super.remove(w);
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
    public void setWidth(String width) {
        Integer pxWidth = JQueryWrapper.parseInt(width);
        boolean widthChanged = pxWidth != null && pxWidth != getOffsetWidth();
        super.setWidth(width);
        if (widthChanged) {
            mainAppLauncher.updateDivet();
        }
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
    public void updateViewport(VShellViewport viewport, ViewportType type) {
        doUpdateViewport(viewport, type);
    }

    private final ShellNavigationHandler navigationHandler = new ShellNavigationHandler() {

        @Override
        public void onAppActivated(AppActivatedEvent event) {
            final String fragment = activeViewportType.getFragmentPrefix()
                    + event.getAppId()
                    + ":"
                    + event.getSubAppId()
                    + ";"
                    + event.getParameter();
            History.newItem(fragment, false);
        }

        @Override
        public void onShellAppNavigation(final ShellAppNavigationEvent event) {

            VShellAppsViewport viewport = getShellAppViewport();
            Widget shellApp = viewport.getShellAppByType(event.getType());
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
                    setActiveViewport(ViewportType.SHELL_APP_VIEWPORT);
                }
            } else {
                viewport.setRefreshEvent(null);
                getShellAppViewport().showLoadingPane();
                presenter.loadShellApp(event.getType(), event.getToken());
            }
        }

    };

    @Override
    public void onViewportClose(ViewportCloseEvent event) {
        final VMagnoliaShell.ViewportType viewportType = event.getViewportType();
        if (viewportType == ViewportType.SHELL_APP_VIEWPORT) {
            getShellAppViewport().setClosing(true);
            presenter.closeCurrentShellApp();
        } else if (viewportType == ViewportType.APP_VIEWPORT) {
            presenter.closeCurrentApp();
        }
    }

    @Override
    public void navigate(String appId, String subAppId, String parameter) {
        eventBus.fireEvent(new AppActivatedEvent(activeViewportType == ViewportType.SHELL_APP_VIEWPORT, appId, subAppId, parameter));
    }

    @Override
    public Collection<VShellViewport> getViewports() {
        return Collections.unmodifiableCollection(viewports.values());
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void shiftViewportsVertically(int shiftPx, boolean animated) {
        for (final VShellViewport viewport : viewports.values()) {
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

    @Override
    public void setPusher(final VICEPush pusher) {
        if (getWidgetIndex(pusher) != -1) {
            insert(pusher, 0);
        }
    }

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
        presenter.loadShellApp(ShellAppType.PULSE, "messages/" + id);
    }

    private void doUpdateViewport(VShellViewport viewport, ViewportType shellAppViewport) {
        final VShellViewport oldViewport = viewports.get(shellAppViewport);
        if (oldViewport != viewport) {
            replaceWidget(oldViewport, viewport);
            viewport.setEventBus(eventBus);
            viewports.put(shellAppViewport, viewport);
        }
    }

    @Override
    public void showAppPreloader(String prefix, PreloaderCallback preloaderCallback) {
        setActiveViewport(ViewportType.APP_VIEWPORT);
        getAppViewport().showAppPreloader(prefix, preloaderCallback);
    }
}
