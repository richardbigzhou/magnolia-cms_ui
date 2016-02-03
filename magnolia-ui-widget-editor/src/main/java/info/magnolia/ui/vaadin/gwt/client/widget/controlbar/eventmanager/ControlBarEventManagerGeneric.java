/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * Generic implementation of {@link ControlBarEventManager}. Used by all the browsers except for IE8.
 */
public class ControlBarEventManagerGeneric implements ControlBarEventManager {

    private List<HandlerRegistration> dndHandlers = new LinkedList<HandlerRegistration>();

    private List<HandlerRegistration> moveHandlers = new LinkedList<HandlerRegistration>();

    private HandlerRegistration mouseMoveRegistration = null;

    @Override
    public void addClickOrTouchHandler(Widget target, final ControlBarEventHandler listener) {
        TouchDelegate td = new TouchDelegate(target);
        td.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent touchEndEvent) {
                listener.handle(touchEndEvent.getNativeEvent());
            }
        });
    }

    @Override
    public void unregisterMoveHandlers(Widget widget) {
        Iterator<HandlerRegistration> it = moveHandlers.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
            it.remove();
        }
    }

    @Override
    public void unregisterDnDHandlers(Widget widget) {
        Iterator<HandlerRegistration> it = dndHandlers.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
            it.remove();
        }
    }

    @Override
    public void addMouseDownHandler(Widget target, final ControlBarEventHandler listener) {
        moveHandlers.add(target.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, MouseDownEvent.getType()));
    }

    @Override
    public void addMouseOverHandler(Widget target, final ControlBarEventHandler listener) {
        moveHandlers.add(target.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, MouseOverEvent.getType()));
    }

    @Override
    public void addMouseOutHandler(Widget target, final ControlBarEventHandler listener) {
        moveHandlers.add(target.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, MouseOutEvent.getType()));
    }

    @Override
    public void addDragStartHandler(Widget target, final ControlBarEventHandler listener) {
        dndHandlers.add(target.addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, DragStartEvent.getType()));
    }

    @Override
    public void addDragEndHandler(Widget target, final ControlBarEventHandler listener) {
        dndHandlers.add(target.addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, DragEndEvent.getType()));
    }

    @Override
    public void addDragOverHandler(Widget target, final ControlBarEventHandler listener) {
        dndHandlers.add(target.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, DragOverEvent.getType()));
    }

    @Override
    public void addDragLeaveHandler(Widget target, final ControlBarEventHandler listener) {
        dndHandlers.add(target.addDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, DragLeaveEvent.getType()));
    }

    @Override
    public void addDropHandler(Widget target, final ControlBarEventHandler listener) {
        dndHandlers.add(target.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, DropEvent.getType()));
    }

    @Override
    public void addMouseMoveHandler(Widget target, final ControlBarEventHandler listener) {
        mouseMoveRegistration = target.addDomHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                listener.handle(event.getNativeEvent());
            }
        }, MouseMoveEvent.getType());
    }

    @Override
    public void removeMouseMoveHandler(Widget widget) {
        if (mouseMoveRegistration != null) {
            mouseMoveRegistration.removeHandler();
        }
    }
}
