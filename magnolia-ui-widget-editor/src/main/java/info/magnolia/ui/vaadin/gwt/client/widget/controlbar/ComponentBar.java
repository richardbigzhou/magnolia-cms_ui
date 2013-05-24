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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.ComponentListener;
import info.magnolia.ui.vaadin.gwt.client.widget.dnd.ComponentMoveHelper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

/**
 * Control bar for components. Injected at the beginning of an area.
 */
public class ComponentBar extends AbstractBar {

    private static final String MOVE_CLASS_NAME = "icon-move";

    private final ComponentListener listener;
    List<HandlerRegistration> dndHandlers = new LinkedList<HandlerRegistration>();
    List<HandlerRegistration> moveHandlers = new LinkedList<HandlerRegistration>();

    public ComponentBar(MgnlComponent mgnlElement) {
        super(mgnlElement);

        this.listener = mgnlElement;
        addStyleName(COMPONENT_CLASS_NAME);

        initLayout();

        if (DragDropEventBase.isSupported()) {
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
            edit.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    listener.editComponent();
                }
            });

            addButton(edit);
        }
        final Label move = new Label();
        move.setStyleName(ICON_CLASS_NAME);
        move.addStyleName(MOVE_CLASS_NAME);

        move.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //toggleButtons(false);
                //toggleStyles(bar, true);

                listener.startMove();

                //int height = getOffsetHeight();
                //int width = getOffsetWidth();
                //moveDiv = new MoveWidget(height, width);
            }
        });
        addButton(move);

    }
    private void registerDragStartHandler() {

        addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                //bar.toggleButtons(false);

                setStyleName("moveSource", true);

                listener.onDragStart();

                int x = getAbsoluteLeft();
                int y = getAbsoluteTop();
                event.setData("text", listener.getNodePath() + "," + x + "," + y);
                event.getDataTransfer().setDragImage(getElement(), 10, 10);

            }
        }, DragStartEvent.getType());

    }
    public void registerDragAndDropHandlers() {
        dndHandlers.add(addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                //toggleButtons(true);

                setStyleName("moveSource", false);

                listener.onDragEnd();
            }
        }, DragEndEvent.getType()));

        dndHandlers.add(addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String idSource = tokens[0];

                if (!listener.getNodePath().equals(idSource)) {
                    setStyleName("moveOver", true);
                }
                event.stopPropagation();
            }
        }, DragOverEvent.getType()));

        dndHandlers.add(addDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                setStyleName("moveOver", false);
                event.stopPropagation();
            }
        }, DragLeaveEvent.getType()));

        dndHandlers.add(addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String sourcePath = tokens[0];

                if (!listener.getNodePath().equals(sourcePath)) {
                    int xTarget = getAbsoluteLeft();
                    int yTarget = getAbsoluteTop();
                    int xOrigin = Integer.valueOf(tokens[1]);
                    int yOrigin = Integer.valueOf(tokens[2]);

                    String order = getSortOrder(xOrigin, yOrigin, xTarget, yTarget);

                    listener.sortComponent(sourcePath, order);
                }

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
                if (ComponentMoveHelper.isMoving()) {

                    String idSource = ComponentMoveHelper.getNodePath();

                    if (!listener.getNodePath().equals(idSource)) {
                        int xTarget = getAbsoluteLeft();
                        int yTarget = getAbsoluteTop();
                        int xOrigin = ComponentMoveHelper.getAbsoluteLeft();
                        int yOrigin = ComponentMoveHelper.getAbsoluteTop();

                        String order = getSortOrder(xOrigin, yOrigin, xTarget, yTarget);

                        listener.stopMove();
                        listener.sortComponent(idSource, order);
                    }
                }
            }
        }, MouseDownEvent.getType()));

        moveHandlers.add(addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (ComponentMoveHelper.isMoving()) {
                    String idSource = ComponentMoveHelper.getNodePath();

                    if (!listener.getNodePath().equals(idSource)) {
                        setStyleName("moveOver", true);
                    }
                }
            }
        }, MouseOverEvent.getType()));

        moveHandlers.add(addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                if (ComponentMoveHelper.isMoving()) {
                    String idSource = ComponentMoveHelper.getNodePath();

                    if (!listener.getNodePath().equals(idSource)) {
                        setStyleName("moveOver", false);
                    }
                }
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

    private String getSortOrder(int xSource, int ySource, int xTarget, int yTarget) {
        boolean isDragUp = ySource > yTarget;
        boolean isDragDown = !isDragUp;
        boolean isDragLeft = xSource > xTarget;
        boolean isDragRight = !isDragLeft;

        String order = null;

        if (isDragUp || isDragLeft) {
            order = "before";
        } else if (isDragDown || isDragRight) {
            order = "after";
        }
        return order;
    }
}
