/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ViewportCloseEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.ContainerResizedListener;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * An overlay that displays the open app in the shell on top of each other.
 */
public class VShellViewport extends VPanelWithCurtain implements Container, ContainerResizedListener {

    private static int CURTAIN_FADE_IN_SPEED = 500;

    private static int CURTAIN_FADE_OUT_SPEED = 400;

    public static int Z_INDEX_HI = 300;

    public static int Z_INDEX_LO = 100;

    private String paintableId;

    private ApplicationConnection client;

    private final Element container = DOM.createDiv();

    private boolean closingWidget;

    private Widget visibleWidget = null;

    private final List<Paintable> paintables = new LinkedList<Paintable>();

    private AnimationDelegate viewportShowAnimationDelegate;

    private AnimationDelegate viewportHideAnimationDelegate;

    private AnimationDelegate contentShowAnimationDelegate;

    private AnimationDelegate contentHideAnimationDelegate;

    private boolean forceContentAlign = true;

    private EventBus eventBus;

    private boolean active = false;

    private boolean curtainVisible = false;

    private boolean curtainAnimated = false;

    private boolean animatingViewport = false;

    private final TouchDelegate delegate = new TouchDelegate(this);

    public VShellViewport() {
        super();
        setElement(container);
        addStyleName("v-shell-viewport");
        DOM.sinkEvents(this.getElement(), Event.TOUCHEVENTS);
        bindHandlers();
    }

    private void bindHandlers() {
        delegate.addTouchStartHandler(new TouchStartHandler() {

            @Override
            public void onTouchStart(TouchStartEvent event) {
                final Element target = event.getNativeEvent().getEventTarget().cast();
                if (target == getElement()) {
                    eventBus.fireEvent(new ViewportCloseEvent(VMagnoliaShell.ViewportType.SHELL_APP_VIEWPORT));
                }
            }
        });
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected void setClosingWidget(boolean closingWidget) {
        this.closingWidget = closingWidget;
    }

    public void setViewportShowAnimationDelegate(AnimationDelegate animationDelegate) {
        GWT.log((curtainAnimated ? "SHELL" : "APPS") + ": setting viewport show animation delegate to " + animationDelegate);
        this.viewportShowAnimationDelegate = animationDelegate;
    }

    public void setViewportHideAnimationDelegate(AnimationDelegate animationDelegate) {
        GWT.log((curtainAnimated ? "SHELL" : "APPS") + ": setting viewport hide animation delegate to " + animationDelegate);
        this.viewportHideAnimationDelegate = animationDelegate;
    }

    public void setContentShowAnimationDelegate(AnimationDelegate animationDelegate) {
        GWT.log((curtainAnimated ? "SHELL" : "APPS") + ": setting content show animation delegate to " + animationDelegate);
        this.contentShowAnimationDelegate = animationDelegate;
    }

    public void setContentHideAnimationDelegate(AnimationDelegate animationDelegate) {
        GWT.log((curtainAnimated ? "SHELL" : "APPS") + ": setting content hide animation delegate to " + animationDelegate);
        this.contentHideAnimationDelegate = animationDelegate;
    }

    public boolean isCurtainVisible() {
        return curtainVisible;
    }

    public void setCurtainVisible(boolean curtainVisible) {
        this.curtainVisible = curtainVisible;
    }

    public boolean isCurtainAnimated() {
        return curtainAnimated;
    }

    public void setCurtainAnimated(boolean curtainAnimated) {
        this.curtainAnimated = curtainAnimated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        GWT.log((curtainAnimated ? "SHELL" : "APPS") + ": setting " + (active ? "active" : "inactive"));
        if (this.active != active) {
            if (active) {
                showViewport();
            } else {
                hideViewport();
            }
        }
        this.active = active;
    }

    public Widget getVisibleWidget() {
        return visibleWidget;
    }

    public void setVisibleWidget(Widget w) {
        GWT.log((curtainAnimated ? "SHELL" : "APPS") + ": setting visible widget to" + w);
        if (w != visibleWidget) {
            if (visibleWidget != null) {
                hideWidget(visibleWidget);
            }
            if (w != null) {
                showWidget(w);
            }
        }
        this.visibleWidget = w;
    }

    /* SHOW-HIDE VIEWPORT & CURTAIN */

    private void showViewport() {
        if (viewportShowAnimationDelegate != null) {
            animatingViewport = true;
            Callbacks callback = Callbacks.create(new JQueryCallback() {

                @Override
                public void execute(JQueryWrapper query) {
                    animatingViewport = false;
                }
            });

            if (curtainVisible && curtainAnimated) {

                getModalityCurtain().getStyle().setProperty("opacity", "");
                JQueryWrapper jq = JQueryWrapper.select(getModalityCurtain());
                final double initialOpacity = Double.valueOf(jq.css("opacity"));
                jq.setCss("opacity", "0");
                showCurtain();
                jq.animate(CURTAIN_FADE_IN_SPEED, new AnimationSettings() {

                    {
                        setProperty("opacity", initialOpacity);
                    }
                });
                viewportShowAnimationDelegate.show(VShellViewport.this, callback);

            } else {
                if (curtainVisible) {
                    showCurtain();
                }
                viewportShowAnimationDelegate.show(this, callback);
            }
        } else {
            if (curtainVisible) {
                if (curtainAnimated) {
                    getModalityCurtain().getStyle().setProperty("opacity", "");
                    JQueryWrapper jq = JQueryWrapper.select(getModalityCurtain());
                    final double initialOpacity = Double.valueOf(jq.css("opacity"));
                    jq.setCss("opacity", "0");
                    showCurtain();
                    jq.animate(CURTAIN_FADE_IN_SPEED, new AnimationSettings() {

                        {
                            setProperty("opacity", initialOpacity);
                        }
                    });
                } else {
                    showCurtain();
                }
            }
            getElement().getStyle().setZIndex(Z_INDEX_HI);
            getElement().getStyle().setProperty("opacity", "");
            getElement().getStyle().setVisibility(Visibility.VISIBLE);
        }
    }

    private void hideViewport() {
        if (viewportHideAnimationDelegate != null) {
            animatingViewport = true;
            Callbacks callbacks = Callbacks.create(new JQueryCallback() {

                @Override
                public void execute(JQueryWrapper query) {
                    animatingViewport = false;
                }

            });
            if (curtainAnimated) {
                if (viewportHideAnimationDelegate != AnimationDelegate.FADING_DELEGATE) {
                    getModalityCurtain().getStyle().setZIndex(Z_INDEX_HI + 9);
                }
                final JQueryCallback callback = new JQueryCallback() {

                    @Override
                    public void execute(JQueryWrapper query) {
                        JQueryWrapper.select(getModalityCurtain()).animate(CURTAIN_FADE_OUT_SPEED, new AnimationSettings() {

                            {
                                setProperty("opacity", "0");
                                setCallbacks(Callbacks.create(new JQueryCallback() {

                                    @Override
                                    public void execute(JQueryWrapper query) {
                                        hideCurtain();
                                    }
                                }));
                            }
                        });
                    }
                };
                if (viewportHideAnimationDelegate == AnimationDelegate.SLIDING_DELEGATE) {
                    JQueryWrapper.select(getModalityCurtain()).animate(200, new AnimationSettings() {

                        {
                            setProperty("text-indent", "-200px");
                            setCallbacks(Callbacks.create(callback));
                        }
                    });
                } else {
                    callback.execute(JQueryWrapper.select(getModalityCurtain()));
                }
            }
            viewportHideAnimationDelegate.hide(this, callbacks);
        } else {
            hideCurtain();
            getElement().getStyle().setZIndex(Z_INDEX_LO);
        }
    }

    /* SHOW-HIDE ONE WIDGET INSIDE THE VIEWPORT */

    private void showWidget(final Widget w) {
        if (active && contentShowAnimationDelegate != null && !animatingViewport) {
            contentShowAnimationDelegate.show(w, Callbacks.create());
        } else {
            w.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            w.getElement().getStyle().setZIndex(Z_INDEX_HI);
            w.getElement().getStyle().setProperty("opacity", "");
        }
    }

    private void hideWidget(final Widget w) {
        if (active && contentHideAnimationDelegate != null && !animatingViewport) {
            contentHideAnimationDelegate.hide(w, Callbacks.create());
        } else {
            w.getElement().getStyle().setVisibility(Visibility.HIDDEN);
            w.getElement().getStyle().setZIndex(Z_INDEX_LO);
        }
    }

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
        this.paintableId = uidl.getId();
        this.client = client;
        if (!client.updateComponent(this, uidl, true)) {
            final List<Paintable> orpanCandidates = new LinkedList<Paintable>(paintables);
            Widget formerWidget = visibleWidget;
            if (uidl.getChildCount() > 0) {
                int idx = 0;
                for (; idx < uidl.getChildCount(); ++idx) {
                    final UIDL childUIdl = uidl.getChildUIDL(idx);
                    final Paintable paintable = client.getPaintable(childUIdl);
                    orpanCandidates.remove(paintable);
                    final Widget w = (Widget) paintable;
                    updatePosition(w);
                    paintable.updateFromUIDL(childUIdl, client);
                    if (forceContentAlign) {
                        alignChild(w);
                    }
                    if (idx == 0) {
                        setVisibleWidget(w);
                    } else {
                        w.getElement().getStyle().setProperty("zIndex", "");
                    }
                }
            } else {
                visibleWidget = null;
            }

            for (final Paintable paintable : orpanCandidates) {
                client.unregisterPaintable(paintable);
                if (closingWidget && formerWidget == paintable) {
                    new Timer() {

                        @Override
                        public void run() {
                            remove((Widget) paintable);
                            setClosingWidget(false);
                        }
                    }.schedule(500);
                } else {
                    remove((Widget) paintable);
                }
            }
        }
    }

    private void alignChild(Widget w) {
        final Element el = w.getElement();
        if (w.isAttached()) {
            final Style style = el.getStyle();
            style.setLeft(50, Unit.PCT);
            style.setMarginLeft(-el.getOffsetWidth() / 2, Unit.PX);
        }
    }

    private boolean updatePosition(Widget w) {
        boolean result = !hasChildComponent(w);
        if (result) {
            add(w, container);
        }
        return result;
    }

    /* CONTAINER INTERFACE IMPL */

    @Override
    public boolean hasChildComponent(Widget component) {
        return getChildren().contains(component);
    }

    @Override
    public boolean requestLayout(Set<Paintable> children) {
        return false;
    }

    @Override
    public RenderSpace getAllocatedSpace(Widget child) {
        if (hasChildComponent(child)) {
            return new RenderSpace(getOffsetWidth(), getOffsetHeight());
        }
        return new RenderSpace();
    }

    @Override
    public void updateCaption(Paintable component, UIDL uidl) {
    }

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
    }

    @Override
    public void clear() {
        super.clear();
        paintables.clear();
    }

    @Override
    protected void insert(Widget child, Element container, int beforeIndex, boolean domInsert) {
        super.insert(child, container, beforeIndex, domInsert);
        if (child instanceof Paintable) {
            paintables.add((Paintable) child);
        }
    }

    @Override
    protected void add(final Widget child, Element container) {
        if (child instanceof Paintable) {
            child.getElement().getStyle().setPosition(Position.ABSOLUTE);
            paintables.add((Paintable) child);
        }
        super.add(child, container);
    }

    public void setForceContentAlign(boolean forceContentAlign) {
        this.forceContentAlign = forceContentAlign;
    }

    public boolean hasContent() {
        return getWidgetCount() - (closingWidget ? 1 : 0) > 0;
    }

    @Override
    public void iLayout() {
        if (forceContentAlign) {
            for (final Widget w : getChildren()) {
                alignChild(w);
            }
        }
    }
}
