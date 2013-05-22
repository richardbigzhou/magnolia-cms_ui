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
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SortComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.ComponentListener;

import com.google.gwt.event.shared.EventBus;

/**
 * Represents a component inside the {@link CmsNode}-tree.
 * Implements a listener interface for the associated {@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar}
 * and provides wrapper functions used by the {@link info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel}.
 */
public class MgnlComponent extends MgnlElement implements ComponentListener {

    private final EventBus eventBus;

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

        boolean movable = true;
        if (getAttributes().containsKey(OperationPermissionDefinition.MOVEABLE)) {
            movable = Boolean.parseBoolean(getAttribute(OperationPermissionDefinition.MOVEABLE));
        }

        component.setDeletable(deletable);
        component.setWritable(writable);
        component.setMoveable(movable);
        return component;
    }

    @Override
    public void editComponent() {
        eventBus.fireEvent(new EditComponentEvent(getTypedElement()));
    }

    @Override
    public void sortComponent(String sourcePath, String order) {
        MgnlArea area = getParentArea();
        if (area != null) {
            area.onDragStart(true);

            AreaElement areaElement = area.getTypedElement();
            areaElement.setTargetComponent(getTypedElement());
            areaElement.setSortOrder(order);

            for (MgnlComponent component : area.getComponents()) {
                if (component.getNodeName().equals(sourcePath)) {
                    areaElement.setSourceComponent(component.getTypedElement());
                    break;
                }
            }
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
    public void onDragStart() {
        MgnlArea area = getParentArea();
        if (area != null) {
            area.onDragStart(true);
            for (MgnlComponent component : area.getComponents()) {
                component.setMoveTarget(true);
            }
        }
    }

    @Override
    public String getNodeName() {
        return getAttribute("path");
    }

    @Override
    public void onDragEnd() {
        MgnlArea area = getParentArea();
        if (area != null) {
            area.onDragStart(false);
            for (MgnlComponent component : area.getComponents()) {
                component.setMoveTarget(false);
            }
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
        getControlBar().setStyleName("moveTarget", moveTarget);
    }
}
