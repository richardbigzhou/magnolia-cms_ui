/**
 * This file Copyright (c) 2013 Magnolia International
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
 * Implements a listener interface for the associated {@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar}
 * and provides wrapper functions used by the {@link info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel}.
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

        boolean deletable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.DELETABLE)) {
            deletable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.DELETABLE));
        }

        boolean writable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.WRITABLE)) {
            writable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.WRITABLE));
        }

        component.setDeletable(deletable);
        component.setWritable(writable);
        component.setMoveable(isMovable());
        return component;
    }

    @Override
    public void editComponent() {
        eventBus.fireEvent(new EditComponentEvent(getTypedElement()));
    }

    private void sortComponent(MgnlComponent target) {
        MgnlArea area = getParentArea();
        if (area != null) {
            //area.onDragStart(true);

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
        boolean writable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.WRITABLE)) {
            writable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.WRITABLE));
        }
        return !inherited && writable;
    }

    @Override
    public boolean isMovable() {
        boolean movable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.MOVEABLE)) {
            movable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.MOVEABLE));
        }
        return movable;
    }

    @Override
    public void onMoveStart(boolean isDrag) {
        if (isDrag) {
            doMove(isDrag);
        } else {
            eventBus.fireEvent(new ComponentStartMoveEvent(isDrag));
        }
    }

    public void doMove(boolean isDrag) {
        for (MgnlComponent component : getSiblingComponents()) {
            component.registerMoveTarget(isDrag);
        }

        handlers.add(eventBus.addHandler(ComponentStopMoveEvent.TYPE, new ComponentStopMoveEvent.CompnentStopMoveEventHandler() {
            @Override
            public void onStop(ComponentStopMoveEvent componentStopMoveEvent) {
                MgnlComponent target = componentStopMoveEvent.getTargetComponent();
                if (target != null) {
                    sortComponent(target);
                }
            }
        }));
    }

    @Override
    public void onMoveStop() {
        eventBus.fireEvent(new ComponentStopMoveEvent(this));
    }

    @Override
    public void onMoveCancel() {
        eventBus.fireEvent(new ComponentStopMoveEvent());
    }

    private void registerMoveTarget(final boolean isDrag) {
        setMoveTarget(true);

        if (isDrag) {
            registerDragAndDropHandlers();
        } else {
            setDraggable(false);
            registerMoveHandlers();
        }
        handlers.add(eventBus.addHandler(ComponentStopMoveEvent.TYPE, new ComponentStopMoveEvent.CompnentStopMoveEventHandler() {
            @Override
            public void onStop(ComponentStopMoveEvent componentMoveEvent) {
                unregisterMoveTarget(isDrag);
            }
        }));
    }

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

    @Override
    protected ComponentBar getControlBar() {
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
        for(MgnlComponent component : area.getComponents()) {
            if (component != this) {
                siblings.add(component);
            }
        }
        return siblings;
    }
}
