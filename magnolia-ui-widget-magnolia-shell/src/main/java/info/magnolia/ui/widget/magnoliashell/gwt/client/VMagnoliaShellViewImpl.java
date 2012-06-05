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

import info.magnolia.ui.widget.magnoliashell.gwt.client.FragmentDTO.FragmentType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VShellMessage.MessageType;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VShellViewport.ContentAnimationDelegate;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.AppActivatedEvent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ShellAppNavigationEvent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ViewportCloseEvent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.handler.ShellNavigationHandler;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.handler.ViewportCloseHandler;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 * 
 * @author apchelintcev
 * 
 */
public class VMagnoliaShellViewImpl extends FlowPanel implements VMagnoliaShellView, ViewportCloseHandler {

    public static final String CLASSNAME = "v-magnolia-shell";

    private final static int FADE_SPEED = 300;

    private final static int SLIDE_SPEED = 500;

    private static int Z_INDEX_HI = 300;

    private static int Z_INDEX_LO = 100;

    private VShellViewport activeViewport = null;

    private VShellViewport appViewport = null;

    private VShellViewport shellAppViewport = null;
    
    private VShellViewport dialogViewport = null;

    private HandlerRegistration fragmentHandlerRegistration;

    private Element root = DOM.createDiv();
    
    private VMainLauncher mainAppLauncher;

    private Presenter presenter;
    
    private EventBus eventBus;

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
        eventBus.addHandler(ShellAppNavigationEvent.TYPE, navHandler);
        eventBus.addHandler(AppActivatedEvent.TYPE, navHandler);
        fragmentHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String fragment = event.getValue();
                final FragmentDTO dto = FragmentDTO.fromFragment(fragment);
                if (dto.getType() == FragmentType.SHELL_APP) {
                    eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.resolveType(dto.getId()), dto.getParam()));
                } else {
                    presenter.loadApp(dto.getPath());
                }
            }
        });
    }

    private void doUpdateViewport(VShellViewport oldViewport, VShellViewport newViewport) {
        replaceWidget(oldViewport, newViewport);
        newViewport.setEventBus(eventBus);
    }

    @Override
    public void changeActiveViewport() {
        switchViewports(activeViewport == shellAppViewport);
    }
    
    @Override
    public int getViewportHeight() {
        return getOffsetHeight() - mainAppLauncher.getExpandedHeight();
    }

    @Override
    public int getViewportWidth() {
        return getOffsetWidth();
    }

    @Override
    public boolean hasDialogs() {
        return dialogViewport != null && dialogViewport.getWidgetCount() > 0;
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        if (fragmentHandlerRegistration != null) {
            fragmentHandlerRegistration.removeHandler();
        }
    }

    @Override
    public boolean remove(Widget w) {
        presenter.destroyChild(w);
        return super.remove(w);
    }

    @Override
    public void removeDialogViewport() {
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
    public void showMessage(final MessageType type, String text) {
        VShellMessage msg = null;
        switch (type) {
        case WARNING:
            msg = new VShellMessage(type, text);
            break;
        case ERROR:
            msg = new ErrorMessage(type, text);
            break;
        }
        add(msg, getElement());
    }

    protected void switchViewports(boolean appViewportOnTop) {
        shellAppViewport.getElement().getStyle().setZIndex(appViewportOnTop ? Z_INDEX_LO : Z_INDEX_HI);
        appViewport.getElement().getStyle().setZIndex(appViewportOnTop ? Z_INDEX_HI : Z_INDEX_LO);
        if (appViewportOnTop) {
            activeViewport = appViewport;
            mainAppLauncher.deactivateControls();
        } else {
            activeViewport = shellAppViewport;
            if (appViewport.hasContent()) {
                shellAppViewport.showCurtain();
            } else {
                shellAppViewport.hideCurtain();
            }
        }
    }

    @Override
    public void updateAppViewport(VShellViewport viewport) {
        if (appViewport != viewport) {
            doUpdateViewport(appViewport, viewport);
            appViewport = viewport;
            appViewport.setForceContentAlign(false);
            appViewport.setContentAnimationDelegate(slidingDelegate);
        }
    };

    @Override
    public void updateDialogs(VShellViewport viewport) {
        if (dialogViewport != viewport) {
            doUpdateViewport(dialogViewport, viewport);
            dialogViewport = viewport;
            dialogViewport.getElement().getStyle().setZIndex(500);
            dialogViewport.setContentAnimationDelegate(fadingDelegate);
            if (viewport != null) {
                viewport.showCurtain();
            }
        }
    }

    @Override
    public void updateShellAppViewport(VShellViewport viewport) {
        if (shellAppViewport != viewport) {
            doUpdateViewport(shellAppViewport, viewport);
            shellAppViewport = viewport;
            shellAppViewport.setContentAnimationDelegate(fadingDelegate);   
        }
    }
    
    private class ErrorMessage extends VShellMessage {

        public ErrorMessage(final MessageType type, String text) {
            super(type, text);
            final JQueryWrapper jq = JQueryWrapper.select(this);
            jq.ready(Callbacks.create(new JQueryCallback() {
                @Override
                public void execute(JQueryWrapper query) {
                    if (type == MessageType.ERROR && errorMessageCount() == 0) {
                        final AnimationSettings settings = new AnimationSettings();
                        Integer messageHeight = jq.cssInt("height");
                        settings.setProperty("top", "+=" + messageHeight);
                        settings.setProperty("height", "-=" + messageHeight);
                        settings.addCallback(relayoutCallback);
                        JQueryWrapper.select(activeViewport).animate(300, settings);
                    }
                    show();
                }
            }));
        }

        private int errorMessageCount() {
            return JQueryWrapper.select(".error").get().length();
        };

        @Override
        public void hide() {
            if (errorMessageCount() < 2) {
                final JQueryWrapper jq = JQueryWrapper.select(activeViewport);
                final JQueryWrapper viewportJq = JQueryWrapper.select(activeViewport);
                final Integer messageHeight = JQueryWrapper.select(this).cssInt("height");
                viewportJq.setCssPx("top", jq.cssInt("top") - messageHeight);
                viewportJq.setCssPx("height", jq.cssInt("height") + messageHeight);
                presenter.updateViewportLayout(activeViewport);
            }
            super.hide();
        }
    }
    
    private final ContentAnimationDelegate slidingDelegate = new ContentAnimationDelegate() {
        @Override
        public void hide(Widget w, Callbacks callbacks) {
            JQueryWrapper.select(w).fadeOut(FADE_SPEED, callbacks);
        }

        @Override
        public void show(final Widget widget, final Callbacks callbacks) {
            if (widget != null) {
                JQueryWrapper.select(widget).slideDown(SLIDE_SPEED, callbacks);
            }
        }
    };

    private final ContentAnimationDelegate fadingDelegate = new ContentAnimationDelegate() {
        @Override
        public void hide(Widget w, Callbacks callbacks) {
            JQueryWrapper.select(w.getElement()).fadeOut(FADE_SPEED, callbacks);
        }

        @Override
        public void show(final Widget widget, final Callbacks callbacks) {
            if (widget != null) {
                JQueryWrapper.select(widget.getElement()).fadeIn(FADE_SPEED, callbacks);
            }
        }
    };
    
    private final ShellNavigationHandler navHandler = new ShellNavigationHandler() {
        @Override
        public void onAppActivated(AppActivatedEvent event) {
            final String prefix = activeViewport == shellAppViewport ? "shell:" : "app:";
            final String fragment = prefix + event.getToken();
            History.newItem(fragment, false);
        }

        @Override
        public void onShellAppNavigation(ShellAppNavigationEvent event) {
            presenter.loadShellApp(event.getType(), event.getParameters());
        }
    };
    
    private JQueryCallback relayoutCallback = new JQueryCallback() {
        @Override
        public void execute(JQueryWrapper query) {
            if (activeViewport != null) {
                presenter.updateViewportLayout(activeViewport);
            }
        }
    };

    @Override
    public void onViewportClose(ViewportCloseEvent event) {
        final VShellViewport viewport = event.getViewport();
        if (viewport == shellAppViewport) {
            presenter.closeCurrentShellApp();
        } else if (viewport == appViewport) {
            presenter.closeCurrentApp();
        }
    }

    @Override
    public void navigate(String historyToken, String title) {
        eventBus.fireEvent(new AppActivatedEvent(activeViewport == shellAppViewport, historyToken, title));        
    }

}
