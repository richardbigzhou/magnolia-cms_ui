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

import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShell;
import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ViewportCloseEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
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
public class VShellViewport extends ComplexPanel implements Container, ContainerResizedListener {

    private ApplicationConnection client;

    private boolean closing;

    private Widget visibleApp = null;

    private boolean forceContentAlign;

    private EventBus eventBus;

    private boolean active = false;

    private final TouchDelegate delegate = new TouchDelegate(this);

    private TransitionDelegate transitionDelegate;

    // private final LoadingPane loadingPane = new LoadingPane();

    // private boolean bTransitioning;

    // private final Element loadingIndicator = DOM.createDiv();

    public VShellViewport() {
        super();
        setElement(DOM.createDiv());
        addStyleName("v-viewport");
        DOM.sinkEvents(this.getElement(), Event.TOUCHEVENTS);
        bindHandlers();

        // loadingPane.appendTo(this);
    }

    public void showLoadingPane() {
        // loadingPane.show();
    }

    // loadingIndicator.addClassName("loading-indicator");

    private void bindHandlers() {
        delegate.addTouchEndHandler(new TouchEndHandler() {

            @Override
            public void onTouchEnd(TouchEndEvent event) {
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

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (transitionDelegate != null && !transitionDelegate.bypass()) {
            transitionDelegate.setActive(this, active);
        } else {
            if (active) {
                setVisible(true);
            } else {
                setVisible(false);
            }
        }
        this.active = active;
    }

    public Widget getVisibleApp() {
        return visibleApp;
    }

    public void setVisibleApp(Widget w) {
        if (w != visibleApp) {
            if (transitionDelegate != null && !transitionDelegate.bypass()) {
                transitionDelegate.setVisibleApp(this, w);
            } else {
                if (visibleApp != null) {
                    visibleApp.setVisible(false);
                }
                w.setVisible(true);
            }
            visibleApp = w;
        }
    }

    /* SHOW-HIDE VIEWPORT & CURTAIN */

    // getElement().getStyle().setZIndex(Z_INDEX_HI);
    // getElement().getStyle().clearOpacity();
    // getElement().getStyle().clearVisibility();
    // getElement().getStyle().setZIndex(Z_INDEX_LO);

    /* SHOW-HIDE ONE WIDGET INSIDE THE VIEWPORT */

    // w.getElement().getStyle().setVisibility(Visibility.VISIBLE);
    // w.getElement().getStyle().setZIndex(Z_INDEX_HI);
    // w.getElement().getStyle().setProperty("opacity", "");

    // w.getElement().getStyle().setVisibility(Visibility.HIDDEN);
    // w.getElement().getStyle().setZIndex(Z_INDEX_LO);

    // public void updateShellAppFromServer() {
    // eventBus.fireEvent(new ShellTransitionCompleteEvent());
    // }

    public TransitionDelegate getTransitionDelegate() {
        return transitionDelegate;
    }

    public void setTransitionDelegate(TransitionDelegate transitionDelegate) {
        this.transitionDelegate = transitionDelegate;
    }

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {

        this.client = client;
        if (!client.updateComponent(this, uidl, true)) {

            final Collection<Widget> oldWidgets = new HashSet<Widget>();
            for (final Iterator<Widget> iterator = iterator(); iterator.hasNext();) {
                oldWidgets.add(iterator.next());
            }

            // Widget formerWidget = visibleWidget;
            if (uidl.getChildCount() > 0) {
                Widget app = null;
                for (int i = 0; i < uidl.getChildCount(); i++) {
                    final UIDL childUIdl = uidl.getChildUIDL(i);
                    final Paintable paintable = client.getPaintable(childUIdl);
                    oldWidgets.remove(paintable);
                    final Widget w = (Widget) paintable;
                    if (i == 0) {
                        app = w;
                    }
                    if (w.getParent() != this) {
                        add(w, getElement());
                    }

                    // make sure handling of visibility is left to viewport
                    boolean visible = w.isVisible();
                    paintable.updateFromUIDL(childUIdl, client);
                    if (forceContentAlign) {
                        alignChild(w);
                    }
                    w.setVisible(visible);

                }
                if (app != null) {
                    setVisibleApp(app);
                }

            } else {
                visibleApp = null;
            }

            for (Widget w : oldWidgets) {
                remove(w);
                if (w instanceof Paintable) {
                    final Paintable p = (Paintable) w;
                    client.unregisterPaintable(p);
                }
            }
            // for (final Widget paintable : oldWidgets) {
            // client.unregisterPaintable(paintable);
            // if (closingWidget && formerWidget == paintable) {
            // new Timer() {
            //
            // @Override
            // public void run() {
            // remove((Widget) paintable);
            // setClosingWidget(false);
            // }
            // }.schedule(500);
            // } else {
            // remove((Widget) paintable);
            // }
            // }
        }

        // loadingPane.hide();
        // hideLoadingIndicator();
    }

    private void alignChild(Widget w) {
        final Element el = w.getElement();
        if (w.isAttached()) {
            final Style style = el.getStyle();
            style.setLeft(50, Unit.PCT);
            style.setMarginLeft(-el.getOffsetWidth() / 2, Unit.PX);
        }
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

    public void setForceContentAlign(boolean forceContentAlign) {
        this.forceContentAlign = forceContentAlign;
    }

    // public boolean hasContent() {
    // return getWidgetCount() - (closingWidget ? 1 : 0) > 0;
    // }

    @Override
    public void iLayout() {
        if (forceContentAlign) {
            for (final Widget w : getChildren()) {
                alignChild(w);
            }
        }
    }

}
