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
package info.magnolia.m5vaadin.shell.gwt.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.vaadin.addon.jquerywrapper.client.ui.Callbacks;
import org.vaadin.addon.jquerywrapper.client.ui.JQueryCallback;
import org.vaadin.addon.jquerywrapper.client.ui.JQueryWrapper;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.ContainerResizedListener;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * An overlay that displays the open app in the shell on top of each other.
 * 
 * @author apchelintcev
 * 
 */
public class VShellViewport extends ComplexPanel implements Container, ContainerResizedListener {

    /**
     * Viewports might have different ways of displaying the content. 
     * This interface helps to define them from outside.
     * @author apchelintcev
     *
     */
    public interface ContentAnimationDelegate {
        
        void show(final Widget w, final Callbacks callbacks);
        
        void hide(final Widget w, final Callbacks callbacks);
    }
    
    private Element modalityCurtain = DOM.createDiv();
    
    protected String paintableId = null;
    
    protected ApplicationConnection client;
    
    protected Element container = DOM.createDiv();
    
    private final Element closeWrapper = DOM.createDiv();
    
    private Widget visibleWidget = null;
    
    private List<Paintable> paintables = new LinkedList<Paintable>();
    
    private ContentAnimationDelegate animationDelegate;
    
    private boolean forceContentAlign = true;
    
    public VShellViewport() {
        super();
        setElement(container);
        modalityCurtain.setId("modality-curtain");
        addStyleName("v-shell-vieport");
        final Element closeButton = DOM.createButton();
        closeWrapper.setClassName("close");
        closeButton.setClassName("action-close");
        closeWrapper.appendChild(closeButton);
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (closeWrapper.isOrHasChild((Element)event.getNativeEvent().getEventTarget().cast())) {
                    History.back();
                }
            }
        }, ClickEvent.getType());
        modalityCurtain.getStyle().setVisibility(Visibility.HIDDEN);
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        RootPanel.get().getElement().appendChild(modalityCurtain);
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        RootPanel.get().getElement().removeChild(modalityCurtain);
    }

    @Override
    public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) { 
        this.paintableId = uidl.getId();
        this.client = client;
        if (!client.updateComponent(this, uidl, true)) {
            int idx = 0;
            final List<Paintable> orpanCandidates = new LinkedList<Paintable>(paintables);
            if (uidl.getChildCount() > 0) { 
                for (;idx < uidl.getChildCount(); ++idx) {
                    final UIDL childUIdl = uidl.getChildUIDL(idx);
                    final Paintable paintable = client.getPaintable(childUIdl);
                    orpanCandidates.remove(paintable);
                    final Widget w = (Widget)paintable;
                    ensureAdded(w);
                    paintable.updateFromUIDL(childUIdl, client);
                    if (w != visibleWidget) {
                        w.getElement().getStyle().setVisibility(Visibility.HIDDEN);   
                    }   
                    if (idx == 0) {
                        setWidgetVisible(w);
                    }
                    
                    if (forceContentAlign) {
                        alignChild(w);   
                    }
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

    private boolean ensureAdded(Widget w) {
        boolean result = !hasChildComponent(w);
        if (result) {
            add(w, container);
        }
        return result;
    }

    protected void setWidgetVisible(final Widget w) {
        if (hasChildComponent(w)) {
            hideCurrentContent();
            final Element el = w.getElement();
            final Style style = el.getStyle();
            el.appendChild(closeWrapper);
            style.setDisplay(Display.NONE);
            style.setVisibility(Visibility.VISIBLE);
            animationDelegate.show(w, Callbacks.create(new JQueryCallback() {
                @Override
                public void execute(JQueryWrapper query) {
                    visibleWidget = w;
                }
            }));
        }
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
                        style.setDisplay(Display.BLOCK);
                    }
                }));
            }
        }
    }
    
    @Override
    public void setWidth(String width) {
        super.setWidth(width);
    }
    
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (visibleWidget != null) {
            visibleWidget.setHeight(height);
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
            super.add(child, container);
        }
    }
    
    public void setForceContentAlign(boolean forceContentAlign) {
        this.forceContentAlign = forceContentAlign;
    }
    
    public void showCurtain() {
        modalityCurtain.getStyle().setVisibility(Visibility.VISIBLE);
    }

    public void hideCurtain() {
        modalityCurtain.getStyle().setVisibility(Visibility.HIDDEN);
    }

    public boolean hasContent() {
        return visibleWidget != null;
    }

    void setContentAnimationDelegate(final ContentAnimationDelegate animationDelegate) {
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
}
