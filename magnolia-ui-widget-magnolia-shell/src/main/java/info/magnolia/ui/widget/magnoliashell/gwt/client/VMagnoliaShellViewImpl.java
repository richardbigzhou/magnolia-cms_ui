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
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellErrorMessage;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellMessage;
import info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage.VShellMessage.MessageType;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
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

    private static int Z_INDEX_HI = 300;

    private static int Z_INDEX_LO = 100;

    private Map<ViewportType, VShellViewport> viewports = new EnumMap<ViewportType, VShellViewport>(ViewportType.class);
    
    private ViewportType activeViewportType = null;

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
                    eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.resolveType(dto.getPrefix()), dto.getToken()));
                } else {
                    presenter.loadApp(dto.getPrefix(), dto.getToken());
                }
            }
        });
    }

    protected VShellViewport getAppViewport() {
        return viewports.get(ViewportType.APP_VIEWPORT);
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
            switchViewports(activeViewportType == ViewportType.SHELL_APP_VIEWPORT);
            activeViewportType = type;
        }
    }
    
    @Override
    public int getViewportHeight() {
        final JsArray<Element> errors = JQueryWrapper.select(".error").get();
        int maxErrorHeight = 0;
        for (int i = 0; i < errors.length(); ++i) {
            maxErrorHeight = Math.max(maxErrorHeight, errors.get(i).getOffsetHeight());
        }
        return getOffsetHeight() - mainAppLauncher.getExpandedHeight() - maxErrorHeight;
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
    public void showMessage(final MessageType type, String text) {
        VShellMessage msg = null;
        switch (type) {
        case WARNING:
            msg = new VShellMessage(this, type, text);
            break;
        case ERROR:
            msg = new VShellErrorMessage(this, text);
            break;
        }
        add(msg, getElement());
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
    public void updateAppViewport(VShellViewport viewport) {
        final VShellViewport appViewport = getAppViewport();
        if (appViewport != viewport) {
            doUpdateViewport(appViewport, viewport);
            viewports.put(ViewportType.APP_VIEWPORT, viewport);
            viewport.setContentAnimationDelegate(ContentAnimationDelegate.SlidingDelegate);
            viewport.setForceContentAlign(false);
        }
    };

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

    @Override
    public void updateShellAppViewport(VShellViewport viewport) {
        final VShellViewport shellAppViewport = getShellAppViewport();
        if (shellAppViewport != viewport) {
            doUpdateViewport(shellAppViewport, viewport);
            viewports.put(ViewportType.SHELL_APP_VIEWPORT, viewport);
            viewport.setContentAnimationDelegate(ContentAnimationDelegate.FadingDelegate);   
        }
    }
    
    
    private final ShellNavigationHandler navHandler = new ShellNavigationHandler() {
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
    public int getErrorMessageCount() {
        return JQueryWrapper.select(".error").get().length();
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
}
