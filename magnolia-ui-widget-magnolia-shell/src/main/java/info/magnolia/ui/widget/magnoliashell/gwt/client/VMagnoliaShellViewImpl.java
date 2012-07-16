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
package info.magnolia.ui.widget.magnoliashell.gwt.client;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;
import info.magnolia.ui.widget.magnoliashell.gwt.client.FragmentDTO.FragmentType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell.ViewportType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.AppActivatedEvent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ShellAppNavigationEvent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ViewportCloseEvent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.handler.ShellNavigationHandler;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.handler.ViewportCloseHandler;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VInfoMessage;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellErrorMessage;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellMessage;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VWarningMessage;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.ContentAnimationDelegate;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VAppsViewport;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VAppsViewport.PreloaderCallback;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VShellAppsViewport;
import info.magnolia.ui.widget.magnoliashell.gwt.client.viewport.VShellViewport;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.vaadin.artur.icepush.client.ui.VICEPush;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 */
public class VMagnoliaShellViewImpl extends FlowPanel implements VMagnoliaShellView, ViewportCloseHandler {

    public static final String CLASSNAME = "v-magnolia-shell";

    private static int Z_INDEX_HI = 300;

    private static int Z_INDEX_LO = 100;

    private Map<ViewportType, VShellViewport> viewports = new EnumMap<ViewportType, VShellViewport>(ViewportType.class);

    private ViewportType activeViewportType = null;

    private Element root = DOM.createDiv();

    private VMainLauncher mainAppLauncher;

    private Presenter presenter;

    private EventBus eventBus;

    private VShellMessage lowPriorityMessage;

    private VShellMessage hiPriorityMessage;
    
    public VMagnoliaShellViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.root = getElement();
        this.mainAppLauncher = new VMainLauncher(eventBus);
        setStyleName(CLASSNAME);
        add(mainAppLauncher, root);
        bindEventHandlers();
    }
    
    private void bindEventHandlers() {
        eventBus.addHandler(ViewportCloseEvent.TYPE, this);
        eventBus.addHandler(ShellAppNavigationEvent.TYPE, navigationHandler);
        eventBus.addHandler(AppActivatedEvent.TYPE, navigationHandler);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String fragment = event.getValue();
                final FragmentDTO dto = FragmentDTO.fromFragment(fragment);
                if (dto.getType() == FragmentType.SHELL_APP) {
                    eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.resolveType(dto.getPrefix()), dto.getToken()));
                } else {
                    final String prefix = dto.getPrefix();
                    final String token = dto.getToken();
                    if (presenter.isAppRegistered(prefix)) {
                        changeActiveViewport(ViewportType.APP_VIEWPORT);
                        if (!presenter.isAppRunning(prefix)) {
                            getAppViewport().showAppPreloader(prefix, new PreloaderCallback() {
                                @Override
                                public void onPreloaderShown(String appName) {
                                    presenter.loadApp(appName, token);
                                }
                            });   
                        } else {
                            getAppViewport().hideEntireContents();
                            presenter.loadApp(prefix, token);
                        }
                    } else {
                        eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.APPLAUNCHER, dto.getToken()));
                    }
                }
            }
        });
    }

    protected VAppsViewport getAppViewport() {
        return (VAppsViewport)viewports.get(ViewportType.APP_VIEWPORT);
    }

    protected VShellViewport getShellAppViewport() {
        return viewports.get(ViewportType.SHELL_APP_VIEWPORT);
    }

    protected VShellViewport getDialogViewport() {
        return viewports.get(ViewportType.DIALOG_VIEWPORT);
    }

    private void doUpdateViewport(VShellViewport oldViewport, VShellViewport newViewport) {
        replaceWidget(oldViewport, newViewport);
        newViewport.setEventBus(eventBus);
    }

    @Override
    public void changeActiveViewport(final ViewportType type) {
        if (activeViewportType != type) {
            switchViewports(type == ViewportType.APP_VIEWPORT);
            activeViewportType = type;
        }
    }

    @Override
    public int getViewportHeight() {
        int errorMessageHeight = hiPriorityMessage == null && (getWidgetIndex(hiPriorityMessage) > -1) ? hiPriorityMessage.getOffsetHeight() : 0;
        return getOffsetHeight() - mainAppLauncher.getExpandedHeight() - errorMessageHeight;
    }

    @Override
    public int getViewportWidth() {
        return getOffsetWidth();
    }

    @Override
    public boolean hasDialogs() {
        final VShellViewport dialogViewport = getDialogViewport();
        return dialogViewport != null && dialogViewport.getWidgetCount() > 0;
    }

    @Override
    public boolean remove(Widget w) {
        presenter.destroyChild(w);
        return super.remove(w);
    }

    @Override
    public void removeDialogViewport() {
        final VShellViewport dialogViewport = getDialogViewport();
        if (dialogViewport != null) {
            remove(dialogViewport);
        }
    }

    protected void replaceWidget(final Widget oldWidget, final Widget newWidget) {
        if (oldWidget != null) {
            remove(oldWidget);
        }
        add(newWidget, root);
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


    protected void switchViewports(boolean appViewportOnTop) {
        final VShellViewport shellAppViewport = getShellAppViewport();
        final VShellViewport appViewport = getAppViewport();
        shellAppViewport.getElement().getStyle().setZIndex(appViewportOnTop ? Z_INDEX_LO : Z_INDEX_HI);
        appViewport.getElement().getStyle().setZIndex(appViewportOnTop ? Z_INDEX_HI : Z_INDEX_LO);
        if (appViewportOnTop) {
            mainAppLauncher.deactivateControls();
        } else {
            if (appViewport.hasContent()) {
                shellAppViewport.showCurtain();
            } else {
                shellAppViewport.hideCurtain();
            }
        }
    }

    @Override
    public void updateAppViewport(VAppsViewport viewport) {
        final VShellViewport appViewport = getAppViewport();
        if (appViewport != viewport) {
            doUpdateViewport(appViewport, viewport);
            viewports.put(ViewportType.APP_VIEWPORT, viewport);
        }
    };

    @Override
    public void updateShellAppViewport(VShellAppsViewport viewport) {
        final VShellViewport shellAppViewport = getShellAppViewport();
        if (shellAppViewport != viewport) {
            doUpdateViewport(shellAppViewport, viewport);
            viewports.put(ViewportType.SHELL_APP_VIEWPORT, viewport);
        }
    }
    
    @Override
    public void updateDialogs(VShellViewport viewport) {
        final VShellViewport dialogViewport = getDialogViewport();
        if (dialogViewport != viewport) {
            doUpdateViewport(dialogViewport, viewport);
            viewports.put(ViewportType.DIALOG_VIEWPORT, viewport);
            viewport.getElement().getStyle().setZIndex(500);
            viewport.setContentAnimationDelegate(ContentAnimationDelegate.FadingDelegate);
            if (viewport != null) {
                viewport.showCurtain();
            }
        }
    }


    private final ShellNavigationHandler navigationHandler = new ShellNavigationHandler() {
        @Override
        public void onAppActivated(AppActivatedEvent event) {
            final String fragment = activeViewportType.getFragmentPrefix() + event.getPrefix() + ":" + event.getToken();
            History.newItem(fragment, false);
        }

        @Override
        public void onShellAppNavigation(ShellAppNavigationEvent event) {
            presenter.loadShellApp(event.getType(), event.getToken());
        }
    };


    @Override
    public void onViewportClose(ViewportCloseEvent event) {
        final VShellViewport viewport = event.getViewport();
        if (viewport == getShellAppViewport()) {
            presenter.closeCurrentShellApp();
        } else if (viewport == getAppViewport()) {
            presenter.closeCurrentApp();
        }
    }

    @Override
    public void navigate(String prefix, String token) {
        eventBus.fireEvent(new AppActivatedEvent(activeViewportType == ViewportType.SHELL_APP_VIEWPORT, prefix, token));
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
    public void updateShellAppIndication(ShellAppType type, int increment) {
        mainAppLauncher.updateIndication(type, increment);
    }

    @Override
    public void closeMessageEager(final String id) {
        presenter.removeMessage(id);
    }

    @Override
    public void navigateToMessageDetails(String id) {
        presenter.loadShellApp(ShellAppType.PULSE, "messages/" + id);
    }

}
