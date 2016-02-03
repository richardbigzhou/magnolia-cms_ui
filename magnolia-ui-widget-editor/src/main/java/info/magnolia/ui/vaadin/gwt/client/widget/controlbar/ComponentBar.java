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
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager.ControlBarEventHandler;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager.ControlBarEventManager;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.ComponentListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

/**
 * Control bar for components. Injected at the beginning of a component.
 */
public class ComponentBar extends AbstractBar {

    private static final String MOVE_ICON_CLASS_NAME = "icon-move";
    private static final String MOVE_SOURCE_CLASS_NAME = "moveSource";
    private static final String MOVE_TARGET_CLASS_NAME = "moveTarget";
    private static final String MOVE_OVER_CLASS_NAME = "moveOver";

    private final ComponentListener listener;

    private ControlBarEventManager eventManager = GWT.create(ControlBarEventManager.class);

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
            eventManager.addClickOrTouchHandler(edit, new ControlBarEventHandler() {
                @Override
                public void handle(NativeEvent event) {
                    listener.editComponent();
                }
            });
            addButton(edit);
        }
        if (listener.isMovable()) {
            final Label move = new Label();
            move.setStyleName(ICON_CLASS_NAME);
            move.addStyleName(MOVE_ICON_CLASS_NAME);
            eventManager.addClickOrTouchHandler(move, new ControlBarEventHandler() {
                @Override
                public void handle(NativeEvent event) {
                    listener.onMoveStart(false);
                }
            });
            addButton(move);
        }

    }

    private void registerDragStartHandler() {
        eventManager.addDragStartHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                event.getDataTransfer().setData("text", "dummyPayload");
                event.getDataTransfer().setDragImage(getElement(), 10, 10);
                listener.onMoveStart(true);
            }
        });

        eventManager.addDragEndHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                listener.onMoveCancel();
            }
        });
    }

    public void registerDragAndDropHandlers() {
        eventManager.addDragOverHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                setMoveOver(true);
                event.stopPropagation();
            }
        });


        eventManager.addDragLeaveHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                setMoveOver(false);
                event.stopPropagation();
            }
        });

        eventManager.addDropHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                listener.onMoveStop();
                event.preventDefault();
            }
        });
    }

    public void unregisterDragAndDropHandlers() {
        eventManager.unregisterDnDHandlers(this);
    }

    public void registerMoveHandlers() {
        eventManager.addMouseDownHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                listener.onMoveStop();
            }
        });

        eventManager.addMouseOverHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                setMoveOver(true);
            }
        });

        eventManager.addMouseOutHandler(this, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                setMoveOver(false);
            }
        });
    }

    public void unregisterMoveHandlers() {
        eventManager.unregisterMoveHandlers(this);
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
