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
package info.magnolia.m5vaadin.shell.gwt.client;

import info.magnolia.m5vaadin.shell.gwt.client.FragmentDTO.FragmentType;
import info.magnolia.m5vaadin.shell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.m5vaadin.shell.gwt.client.VShellMessage.MessageType;
import info.magnolia.m5vaadin.shell.gwt.client.VShellViewport.ContentAnimationDelegate;
import info.magnolia.m5vaadin.shell.gwt.client.event.AppActivatedEvent;
import info.magnolia.m5vaadin.shell.gwt.client.event.ShellAppNavigationEvent;
import info.magnolia.m5vaadin.shell.gwt.client.event.handler.ShellNavigationHandler;

import org.vaadin.addon.jquerywrapper.client.ui.AnimationSettings;
import org.vaadin.addon.jquerywrapper.client.ui.Callbacks;
import org.vaadin.addon.jquerywrapper.client.ui.JQueryCallback;
import org.vaadin.addon.jquerywrapper.client.ui.JQueryWrapper;

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
public class VMagnoliaShellViewImpl extends FlowPanel implements VMagnoliaShellView {
    
    private static int Z_INDEX_LO = 100;

    private static int Z_INDEX_HI = 300;

    public static final String CLASSNAME = "v-magnolia-shell";

    private VMainLauncher mainAppLauncher;

    private Element root = DOM.createDiv();

    private VShellViewport shellAppViewport = null;

    private VShellViewport appViewport = null;

    private VShellViewport activeViewport = null;

    private HandlerRegistration fragmentHandlerRegistration;

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
    };

    private final ShellNavigationHandler navHandler = new ShellNavigationHandler() {
        @Override
        public void onShellAppNavigation(ShellAppNavigationEvent event) {
            presenter.loadShellApp(event.getType(), event.getParameters());
        }

        @Override
        public void onAppActivated(AppActivatedEvent event) {
            final String fragment = (event.isShellApp() ? "shell:" : "app:") + event.getToken();
            switchViewports(!event.isShellApp());
            History.newItem(fragment, false);
        }
    };
    
    private void bindEventHandlers() {
        eventBus.addHandler(ShellAppNavigationEvent.TYPE, navHandler);
        eventBus.addHandler(AppActivatedEvent.TYPE, navHandler);
        fragmentHandlerRegistration = History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String fragment = event.getValue();
                final FragmentDTO dto = FragmentDTO.fromFragment(fragment);
                if (dto.getType() == FragmentType.SHELL_APP) {
                    eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.valueOf(dto.getId()), dto.getParam()));
                } else {
                    presenter.loadApp(dto.getPath());
                }
            }
        });
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (fragmentHandlerRegistration != null) {
            fragmentHandlerRegistration.removeHandler();
        }
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
    public void updateAppViewport(VShellViewport viewport) {
        viewport.getElement().setId("app-viewport");
        doUpdateViewport(appViewport, viewport);
        appViewport = viewport;
        appViewport.setContentAnimationDelegate(appViewportAnimationDelegate);
    }

    protected void replaceWidget(final Widget oldWidget, final Widget newWidget) {
        if (oldWidget != null) {
            remove(oldWidget);
        }
        add(newWidget, root);
    }

    @Override
    public void updateShellAppViewport(VShellViewport viewport) {
        viewport.getElement().setId("shell-app-viewport");
        doUpdateViewport(shellAppViewport, viewport);
        shellAppViewport = viewport;
        shellAppViewport.setContentAnimationDelegate(shellAppViewportAnimationDelegate);
        if (activeViewport == null) {
            activeViewport = shellAppViewport;
        }
    }

    @Override
    public boolean remove(Widget w) {
        presenter.destroyChild(w);
        return super.remove(w);
    }

    private void doUpdateViewport(VShellViewport oldViewport, VShellViewport newViewport) {
        if (oldViewport != newViewport) {
            replaceWidget(oldViewport, newViewport);
        }
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        mainAppLauncher.setWidth(width);
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

    private final static int FADE_SPEED = 300;

    private final static int SLIDE_SPEED = 500;

    private final ContentAnimationDelegate shellAppViewportAnimationDelegate = new ContentAnimationDelegate() {
        @Override
        public void show(final Widget widget, final Callbacks callbacks) {
            if (widget != null) {
                JQueryWrapper.select(widget.getElement()).fadeIn(FADE_SPEED, callbacks);
            }
        }

        @Override
        public void hide(Widget w, Callbacks callbacks) {
            JQueryWrapper.select(w.getElement()).fadeOut(FADE_SPEED, callbacks);
        }
    };

    private final ContentAnimationDelegate appViewportAnimationDelegate = new ContentAnimationDelegate() {
        @Override
        public void show(final Widget widget, final Callbacks callbacks) {
            if (widget != null) {
                JQueryWrapper.select(widget).slideDown(SLIDE_SPEED, callbacks);
            }
        }

        @Override
        public void hide(Widget w, Callbacks callbacks) {
            JQueryWrapper.select(w).fadeOut(FADE_SPEED, callbacks);
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
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    };

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
        };

        private int errorMessageCount() {
            return JQueryWrapper.select(".error").get().length();
        }
    }
   
}
