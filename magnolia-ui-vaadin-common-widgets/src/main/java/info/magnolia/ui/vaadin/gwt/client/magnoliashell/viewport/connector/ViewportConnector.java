/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellViewport;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * ViewportConnector.
 */
@Connect(ShellViewport.class)
public class ViewportConnector extends AbstractLayoutConnector {

    protected EventBus eventBus;

    protected ElementResizeListener childCenterer = new ElementResizeListener() {
        @Override
        public void onElementResize(ElementResizeEvent e) {
            alignContent(e.getElement(), e.getLayoutManager());
        }
    };

    @Override
    protected void init() {
        addStateChangeHandler("activeComponent", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                final ComponentConnector newActiveComponent = (ComponentConnector) getState().activeComponent;
                if (newActiveComponent != null && getWidget().getVisibleChild() != newActiveComponent) {
                    newActiveComponent.getWidget().getElement().getStyle().clearOpacity();
                }
            }
        });
    }

    protected void alignContent(Element e, LayoutManager layoutManager) {
        if (getWidget().isVisible() && !Display.NONE.getCssName().equals(e.getStyle().getDisplay())) {
            int width = layoutManager.getInnerWidth(e);
            final Style style = e.getStyle();
            style.setLeft(50, Unit.PCT);
            style.setMarginLeft(-width / 2, Unit.PX);
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        final ViewportWidget viewport = getWidget();
        final List<ComponentConnector> children = getChildComponents();
        final List<ComponentConnector> oldChildren = event.getOldChildren();
        int index = 0;
        for (final ComponentConnector cc : children) {
            final Widget w = cc.getWidget();
            if (w.getParent() != viewport) {
                viewport.insert(w, index);
                getLayoutManager().addElementResizeListener(w.getElement(), childCenterer);
                w.getElement().getStyle().setOpacity(0d);
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (getWidget().getVisibleChild() != cc.getWidget()) {
                            w.getElement().getStyle().setDisplay(Display.NONE);
                        }
                        w.getElement().getStyle().clearOpacity();
                    }
                });

            }
            ++index;
        }

        oldChildren.removeAll(children);
        for (final ComponentConnector cc : oldChildren) {
            cc.getLayoutManager().removeElementResizeListener(cc.getWidget().getElement(), childCenterer);
            getWidget().removeChild(cc.getWidget());
        }
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public ViewportWidget getWidget() {
        return (ViewportWidget) super.getWidget();
    }

    @Override
    public ViewportState getState() {
        return (ViewportState) super.getState();
    }

    public ViewportType getType() {
        return getState().type;
    }

}
