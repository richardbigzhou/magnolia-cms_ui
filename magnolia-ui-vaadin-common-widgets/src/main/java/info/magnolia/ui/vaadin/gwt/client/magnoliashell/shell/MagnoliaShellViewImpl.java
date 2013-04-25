/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ViewportCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VInfoMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellErrorMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VWarningMessage;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation.JQueryAnimation;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget.PreloaderCallback;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ShellAppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import java.util.EnumMap;
import java.util.Map;

import com.github.wolfie.refresher.Refresher;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 */
public class MagnoliaShellViewImpl extends TouchPanel implements MagnoliaShellView, ViewportCloseEvent.Handler {

    public static final String CLASSNAME = "v-magnolia-shell";

    private final Map<ViewportType, ViewportWidget> viewports = new EnumMap<ViewportType, ViewportWidget>(ViewportType.class);

    private final ShellAppLauncher mainAppLauncher;

    private final EventBus eventBus;

    private ShellMessageWidget lowPriorityMessage;

    private ShellMessageWidget hiPriorityMessage;

    private ViewportWidget activeViewport = null;

    private Presenter presenter;

    private JQueryAnimation viewportShifter = new JQueryAnimation();

    private final Element viewportSlot = DOM.createDiv();

    public MagnoliaShellViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.mainAppLauncher = new ShellAppLauncher(eventBus);
        setStyleName(CLASSNAME);
        add(mainAppLauncher, getElement());
        getElement().appendChild(viewportSlot);
        viewportSlot.setClassName("v-shell-viewport-slot");
        bindEventHandlers();
        viewportShifter.addCallback(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                presenter.updateViewportLayout(getAppViewport());
            }
        });
    }

    private void bindEventHandlers() {
        eventBus.addHandler(ViewportCloseEvent.TYPE, this);
    }

    protected AppsViewportWidget getAppViewport() {
        return (AppsViewportWidget) viewports.get(ViewportType.APP);
    }

    protected ShellAppsViewportWidget getShellAppViewport() {
        return (ShellAppsViewportWidget) viewports.get(ViewportType.SHELL_APP);
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
            add(newWidget, viewportSlot);
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showMessage(MessageType type, String topic, String message, String id) {
        final ShellMessageWidget msg;
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
        if (lowPriorityMessage != null && getWidgetIndex(lowPriorityMessage) != -1) {
            lowPriorityMessage.hideWithoutTransition();
        }

        hiPriorityMessage = null;
        lowPriorityMessage = null;
    }

    @Override
    public void updateViewport(ViewportWidget viewport, ViewportType type) {
        ViewportWidget oldViewport = viewports.get(type);
        if (oldViewport != viewport) {
            replaceWidget(oldViewport, viewport);
            viewport.addCloseHandler(this);
            viewports.put(type, viewport);
        }
    }

    @Override
    public void onViewportClose(ViewportCloseEvent event) {
        final ViewportType viewportType = event.getViewportType();
        switch (viewportType) {
        case SHELL_APP:
            getShellAppViewport().setClosing(true);
            presenter.closeCurrentShellApp();
            break;
        case APP:
            presenter.closeCurrentApp();
            break;
        }
    }

    @Override
    public void shiftViewportsVertically(int shiftPx, boolean animated) {
        viewportShifter.setProperty("top", mainAppLauncher.getOffsetHeight() + shiftPx);
        viewportShifter.run(animated ? 300 : 0, viewportSlot);
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
        presenter.activateShellApp(Fragment.fromString("shell:pulse:messages/" + id));
    }

    @Override
    public void showAppPreloader(String prefix, PreloaderCallback preloaderCallback) {
        getAppViewport().showAppPreloader(prefix, preloaderCallback);
    }

    @Override
    public void updateShellDivet() {
        mainAppLauncher.updateDivet();
    }

    @Override
    public void openOverlayOnWidget(Widget overlayWidget, Widget overlayParent) {
        // Insert a child widget into a parent element.
        add(overlayWidget, overlayParent.getElement());
    }

    @Override
    public void showShellApp(ShellAppType type) {
        Widget shellApp = presenter.getShellAppWidget(type);
        ShellAppsViewportWidget viewport = getShellAppViewport();
        mainAppLauncher.activateControl(type);
        viewport.setVisibleChild(shellApp);
        viewport.setClosing(false);
        if (!viewport.isActive()) {
            setActiveViewport(viewport);
        }
    }

    @Override
    public void setActiveViewport(boolean isAppViewport) {
        setActiveViewport(isAppViewport ? getAppViewport() : getShellAppViewport());
    }
}
