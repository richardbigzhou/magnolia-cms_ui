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
package info.magnolia.ui.vaadin.gwt.client.editor.dom;

import info.magnolia.cms.security.operations.OperationPermissionDefinition;
import info.magnolia.ui.vaadin.gwt.client.editor.event.ComponentStartMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.ComponentStopMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SortComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.ComponentListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Represents a component inside the {@link CmsNode}-tree.
 * Implements a listener interface for the associated {@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar}.
 * Handles DnD and move Events for components and provides wrapper functions used by the {@link info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel}.
 */
public class MgnlComponent extends MgnlElement implements ComponentListener {

    private final EventBus eventBus;
    private List<HandlerRegistration> handlers = new LinkedList<HandlerRegistration>();

    public MgnlComponent(MgnlElement parent, EventBus eventBus) {
        super(parent);
        this.eventBus = eventBus;
    }

    @Override
    public ComponentElement getTypedElement() {
        ComponentElement component = new ComponentElement(getAttribute("workspace"), getAttribute("path"), getAttribute("dialog"));
        boolean inherited = Boolean.parseBoolean(getAttribute("inherited"));

        boolean deletable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.DELETABLE)) {
            deletable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.DELETABLE));
        }

        component.setDeletable(deletable && !inherited);
        component.setWritable(hasEditButton());
        component.setMoveable(isMovable());
        return component;
    }

    @Override
    public void editComponent() {
        eventBus.fireEvent(new EditComponentEvent(getTypedElement()));
    }

    /**
     * Fires a {@link SortComponentEvent} with the parent {@link AreaElement}.
     * Sets the source component this component and the target to the component passed with the {@link ComponentStopMoveEvent}.
     */
    private void sortComponent(MgnlComponent target) {
        MgnlArea area = getParentArea();
        if (area != null) {
            // area.onDragStart(true);

            AreaElement areaElement = area.getTypedElement();
            areaElement.setSourceComponent(getTypedElement());
            areaElement.setTargetComponent(target.getTypedElement());
            areaElement.setSortOrder(getSortOrder(target));

            SortComponentEvent sortComponentEvent = new SortComponentEvent(areaElement);
            eventBus.fireEvent(sortComponentEvent);
        }
    }

    @Override
    public String getLabel() {
        return getAttribute("label");
    }

    @Override
    public boolean hasEditButton() {
        boolean inherited = Boolean.parseBoolean(getAttribute("inherited"));
        boolean hasDialog = getAttribute("dialog") != null && getAttribute("dialog") != "";
        boolean writable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.WRITABLE)) {
            writable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.WRITABLE));
        }
        return writable && hasDialog && !inherited;
    }

    @Override
    public boolean isMovable() {
        boolean inherited = Boolean.parseBoolean(getAttribute("inherited"));
        MgnlArea parentArea = this.getParentArea();
        boolean parentAreaIsTypeSingle = parentArea != null && "single".equals(parentArea.getAttribute("type"));
        boolean movable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.MOVEABLE)) {
            movable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.MOVEABLE));
        }
        return movable && !inherited && !parentAreaIsTypeSingle;
    }

    /**
     * Callback for {@link ComponentBar} when starting a drag or move event. Depending on whether it is a drag or a move
     * it will either notify the server by firing a {@link ComponentStartMoveEvent} or register the handlers in {@link #doStartMove(boolean)}.
     * 
     * @param isDrag whether we are dragging the component or moving it
     */
    @Override
    public void onMoveStart(boolean isDrag) {
        if (isDrag) {
            doStartMove(isDrag);
        } else {
            eventBus.fireEvent(new ComponentStartMoveEvent());
        }
    }

    /**
     * Registers the sibling components as move targets and registers a handler for {@link ComponentStopMoveEvent} on the source component which will call {@link #sortComponent(MgnlComponent)}.
     * 
     * @param isDrag whether we are dragging the component or moving it
     */
    public void doStartMove(boolean isDrag) {
        setMoveSource(true);

        for (MgnlComponent component : getSiblingComponents()) {
            component.registerMoveTarget(isDrag);
        }

        handlers.add(eventBus.addHandler(ComponentStopMoveEvent.TYPE, new ComponentStopMoveEvent.ComponentStopMoveEventHandler() {
            @Override
            public void onStop(ComponentStopMoveEvent componentStopMoveEvent) {

                setMoveSource(false);

                Iterator<HandlerRegistration> it = handlers.iterator();
                while (it.hasNext()) {
                    it.next().removeHandler();
                    it.remove();
                }
                MgnlComponent target = componentStopMoveEvent.getTargetComponent();
                if (target != null) {
                    sortComponent(target);
                }
            }
        }));
    }

    /**
     * Callback for {@link ComponentBar} targets when a move or drag event is dropped on or moved to this target.
     * Fires {@link ComponentStopMoveEvent} to notify the system. Holds itself as payload for handling by the source,
     * see handler registered in {@link #doStartMove}.
     */
    @Override
    public void onMoveStop() {
        eventBus.fireEvent(new ComponentStopMoveEvent(this, false));
    }

    /**
     * Callback for {@link ComponentBar} source when a drag is stopped.
     * Fires {@link ComponentStopMoveEvent} to notify the system about the cancel. Will cause target components to
     * unregister themselves as targets.
     * 
     * @see #unregisterMoveTarget(boolean)
     */
    @Override
    public void onMoveCancel() {
        eventBus.fireEvent(new ComponentStopMoveEvent(null, false));
    }

    /**
     * Registers a {@link MgnlComponent} as a target.
     * Registers the ui events for move or DnD on {@link ComponentBar} and adds an handler for {@link ComponentStopMoveEvent}.
     */
    private void registerMoveTarget(final boolean isDrag) {
        setMoveTarget(true);

        if (isDrag) {
            registerDragAndDropHandlers();
        } else {
            setDraggable(false);
            registerMoveHandlers();
        }
        handlers.add(eventBus.addHandler(ComponentStopMoveEvent.TYPE, new ComponentStopMoveEvent.ComponentStopMoveEventHandler() {
            @Override
            public void onStop(ComponentStopMoveEvent componentMoveEvent) {
                unregisterMoveTarget(isDrag);
                setMoveOver(false);
            }
        }));
    }

    /**
     * Unregisters a {@link MgnlComponent} as a target.
     * Removes the ui event handlers for move or DnD on {@link ComponentBar} and removes the handler for {@link ComponentStopMoveEvent}.
     */
    private void unregisterMoveTarget(boolean isDrag) {
        setMoveTarget(false);

        if (isDrag) {
            unregisterDragAndDropHandlers();
        } else {
            setDraggable(true);
            unregisterMoveHandlers();
        }

        Iterator<HandlerRegistration> it = handlers.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
            it.remove();
        }
    }

    private void registerMoveHandlers() {
        if (getControlBar() != null) {
            getControlBar().registerMoveHandlers();
        }
    }

    private void unregisterMoveHandlers() {
        if (getControlBar() != null) {
            getControlBar().unregisterMoveHandlers();
        }
    }

    private void registerDragAndDropHandlers() {
        if (getControlBar() != null) {
            getControlBar().registerDragAndDropHandlers();
        }
    }

    private void unregisterDragAndDropHandlers() {
        if (getControlBar() != null) {
            getControlBar().unregisterDragAndDropHandlers();
        }
    }

    private void setDraggable(boolean draggable) {
        if (getControlBar() != null) {
            getControlBar().setDraggable(draggable);
        }
    }

    public void setVisible(boolean visible) {
        if (getControlBar() != null) {
            getControlBar().setVisible(visible);
        }
    }

    public void removeFocus() {
        if (getControlBar() != null) {
            getControlBar().removeFocus();
        }
    }

    public void setFocus() {
        if (getControlBar() != null) {
            getControlBar().setFocus(false);
        }
    }

    public void setMoveTarget(boolean moveTarget) {
        if (getControlBar() != null) {
            getControlBar().setMoveTarget(moveTarget);
        }
    }

    public void setMoveOver(boolean moveTarget) {
        if (getControlBar() != null) {
            getControlBar().setMoveOver(moveTarget);
        }
    }

    private void setMoveSource(boolean source) {
        if (getControlBar() != null) {
            getControlBar().setMoveSource(source);
        }
    }

    @Override
    public ComponentBar getControlBar() {
        return (ComponentBar) super.getControlBar();
    }

    private String getSortOrder(MgnlComponent target) {

        int xTarget = target.getControlBar().getAbsoluteLeft();
        int yTarget = target.getControlBar().getAbsoluteTop();
        int xThis = getControlBar().getAbsoluteLeft();
        int yThis = getControlBar().getAbsoluteTop();

        boolean isDragUp = yThis > yTarget;
        boolean isDragDown = !isDragUp;
        boolean isDragLeft = xThis > xTarget;
        boolean isDragRight = !isDragLeft;

        String order = null;

        if (isDragUp || isDragLeft) {
            order = "before";
        } else if (isDragDown || isDragRight) {
            order = "after";
        }
        return order;
    }

    private List<MgnlComponent> getSiblingComponents() {
        List<MgnlComponent> siblings = new LinkedList<MgnlComponent>();
        MgnlArea area = getParentArea();
        for (MgnlComponent component : area.getComponents()) {
            if (component != this) {
                siblings.add(component);
            }
        }
        return siblings;
    }

    public int getHeight() {
        if (getControlBar() != null) {
            return getControlBar().getOffsetHeight();
        }
        return 0;
    }

    public int getWidth() {
        if (getControlBar() != null) {
            return getControlBar().getOffsetWidth();
        }
        return 0;
    }
}
