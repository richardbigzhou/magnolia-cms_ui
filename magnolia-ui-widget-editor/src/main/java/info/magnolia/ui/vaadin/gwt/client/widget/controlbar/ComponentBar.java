/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.ComponentListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.DragDropEventBase;
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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * Control bar for components. Injected at the beginning of a component.
 */
public class ComponentBar extends AbstractBar {

    private static final String MOVE_ICON_CLASS_NAME = "icon-move";
    private static final String MOVE_SOURCE_CLASS_NAME = "moveSource";
    private static final String MOVE_TARGET_CLASS_NAME = "moveTarget";
    private static final String MOVE_OVER_CLASS_NAME = "moveOver";

    private final ComponentListener listener;
    List<HandlerRegistration> dndHandlers = new LinkedList<HandlerRegistration>();
    List<HandlerRegistration> moveHandlers = new LinkedList<HandlerRegistration>();

    public ComponentBar(MgnlComponent mgnlElement) {
        super(mgnlElement);

        this.listener = mgnlElement;
        addStyleName(COMPONENT_CLASS_NAME);

        initLayout();

        if (listener.isMovable() && DragDropEventBase.isSupported()) {
            registerDragStartHandler();
            setDraggable(true);
        }
    }

    public void setDraggable(boolean draggable) {
        if (DragDropEventBase.isSupported()) {
            if (draggable) {
                this.getElement().setDraggable(Element.DRAGGABLE_TRUE);
                getStyle().setCursor(Style.Cursor.MOVE);
            } else {
                this.getElement().setDraggable(Element.DRAGGABLE_FALSE);
                getStyle().setCursor(Style.Cursor.DEFAULT);
            }
        }
    }

    @Override
    protected String getLabel() {
        return listener.getLabel();
    }

    @Override
    protected void createControls() {
        if (listener.hasEditButton()) {
            final Label edit = new Label();
            edit.setStyleName(ICON_CLASS_NAME);
            edit.addStyleName(EDIT_CLASS_NAME);

            TouchDelegate td = new TouchDelegate(edit);
            td.addTouchEndHandler(new TouchEndHandler() {
                @Override
                public void onTouchEnd(TouchEndEvent touchEndEvent) {
                    listener.editComponent();
                }
            });

            addButton(edit);
        }
        if (listener.isMovable()) {
            final Label move = new Label();
            move.setStyleName(ICON_CLASS_NAME);
            move.addStyleName(MOVE_ICON_CLASS_NAME);

            TouchDelegate td = new TouchDelegate(move);
            td.addTouchEndHandler(new TouchEndHandler() {
                @Override
                public void onTouchEnd(TouchEndEvent touchEndEvent) {
                    listener.onMoveStart(false);
                }
            });

            addButton(move);
        }

    }
    private void registerDragStartHandler() {

        addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                event.setData("text", "dummyPayload");
                event.getDataTransfer().setDragImage(getElement(), 10, 10);

                listener.onMoveStart(true);
            }
        }, DragStartEvent.getType());

        addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                listener.onMoveCancel();
            }
        }, DragEndEvent.getType());

    }
    public void registerDragAndDropHandlers() {

        dndHandlers.add(addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                setMoveOver(true);
                event.stopPropagation();
            }
        }, DragOverEvent.getType()));

        dndHandlers.add(addDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                setMoveOver(false);
                event.stopPropagation();
            }
        }, DragLeaveEvent.getType()));

        dndHandlers.add(addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                listener.onMoveStop();
                event.preventDefault();
            }
        }, DropEvent.getType()));
    }

    public void unregisterDragAndDropHandlers() {
        Iterator<HandlerRegistration> it = dndHandlers.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
            it.remove();
        }
    }

    public void registerMoveHandlers() {

        moveHandlers.add(addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                listener.onMoveStop();
            }
        }, MouseDownEvent.getType()));

        moveHandlers.add(addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                setMoveOver(true);
            }
        }, MouseOverEvent.getType()));

        moveHandlers.add(addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                setMoveOver(false);
            }
        }, MouseOutEvent.getType()));
    }

    public void unregisterMoveHandlers() {
        Iterator<HandlerRegistration> it = moveHandlers.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
            it.remove();
        }
    }

    public void setMoveTarget(boolean target) {
        setStyleName(MOVE_TARGET_CLASS_NAME, target);
    }

    public void setMoveOver(boolean over) {
        setStyleName(MOVE_OVER_CLASS_NAME, over);
    }

    public void setMoveSource(boolean source) {
        setStyleName(MOVE_SOURCE_CLASS_NAME, source);
    }

}
