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

import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
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

    private static int Z_INDEX_HI = 300;

    private static int Z_INDEX_LO = 100;
    
    protected String paintableId = null;

    protected ApplicationConnection client;

    protected Element container = DOM.createDiv();

    private Widget visibleWidget = null;

    private List<Paintable> paintables = new LinkedList<Paintable>();

    private ContentAnimationDelegate animationDelegate;

    private boolean forceContentAlign = true;

    private EventBus eventBus;

    private boolean isActive = false;
    
    public VShellViewport() {
        super();
        setElement(container);
        addStyleName("v-shell-vieport");
    }

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
        this.paintableId = uidl.getId();
        this.client = client;
        if (!client.updateComponent(this, uidl, true)) {
            final List<Paintable> orpanCandidates = new LinkedList<Paintable>(paintables);
            if (uidl.getChildCount() > 0) {
                int idx = 0;
                for (;idx < uidl.getChildCount(); ++idx) {
                    final UIDL childUIdl = uidl.getChildUIDL(idx);
                    final Paintable paintable = client.getPaintable(childUIdl);
                    orpanCandidates.remove(paintable);
                    final Widget w = (Widget)paintable;
                    updatePosition(w);
                    paintable.updateFromUIDL(childUIdl, client);
                    if (forceContentAlign) {alignChild(w);}
                    if (idx == 0) {setWidgetVisibleWithTransition(w);}
                }
            } else {
                visibleWidget = null;
            }

            for (final Paintable paintable : orpanCandidates) {
                client.unregisterPaintable(paintable);
                remove((Widget)paintable);
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
        if (w != visibleWidget) {
            w.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }
        return result;
    }

    protected void setWidgetVisibleWithTransition(final Widget w) {
        if (hasChildComponent(w)) {
            if (w != visibleWidget) {
                hideCurrentContent();
            }
            w.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            animationDelegate.show(w, Callbacks.create(new JQueryCallback() {
                @Override
                public void execute(JQueryWrapper query) {
                    setVisibleWidget(w);
                }
            }));
        }
    }
    
    protected void setVisibleWidget(Widget w) {
        this.visibleWidget = w;
    }

    public void hideEntireContents() {
        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            it.next().getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }
    }
    
    public Widget getVisibleWidget() {
        return visibleWidget;
    }

    protected void hideCurrentContent() {
        if (visibleWidget != null) {
            final Widget formerVisible = visibleWidget;
            if (animationDelegate != null) {
                animationDelegate.hide(formerVisible, Callbacks.create(new JQueryCallback() {
                    @Override
                    public void execute(JQueryWrapper query) {
                        final Style style = formerVisible.getElement().getStyle();
                        style.setVisibility(Visibility.HIDDEN);
                    }
                }));
            }
        }
    }

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
    public void updateCaption(Paintable component, UIDL uidl) {}

    @Override
    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {}

    @Override
    public void clear() {
        super.clear();
        paintables.clear();
    }

    @Override
    protected void insert(Widget child, Element container, int beforeIndex, boolean domInsert) {
        super.insert(child, container, beforeIndex, domInsert);
        if (child instanceof Paintable) {
            paintables.add((Paintable)child);
        }
    }

    @Override
    protected void add(final Widget child, Element container) {
        if (child instanceof Paintable) {
            paintables.add((Paintable)child);
            child.getElement().getStyle().setPosition(Position.ABSOLUTE);
        }
        super.add(child, container);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        getElement().getStyle().setZIndex(isActive ? Z_INDEX_HI : Z_INDEX_LO);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setForceContentAlign(boolean forceContentAlign) {
        this.forceContentAlign = forceContentAlign;
    }

    public boolean hasContent() {
        return getWidgetCount() > 0;
    }

    public void setContentAnimationDelegate(final ContentAnimationDelegate animationDelegate) {
        this.animationDelegate = animationDelegate;
    }

    @Override
    public void iLayout() {
        if (forceContentAlign) {
            for (final Widget w : getChildren()) {
                alignChild(w);
            }
        }
    }
    
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
    
    protected ContentAnimationDelegate getAnimationDelegate() {
        return animationDelegate;
    }
}
