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
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.ComponentListener;

import com.google.gwt.event.shared.EventBus;

/**
 * MgnlComponent.
 */
public class MgnlComponent extends MgnlElement implements ComponentListener {
    private EventBus eventBus;

    /**
     * MgnlElement. Represents a node in the tree built on cms-tags.
     */
    public MgnlComponent(MgnlElement parent, EventBus eventBus) {
        super(parent);
        this.eventBus = eventBus;
    }

    @Override
    public AbstractElement getTypedElement() {
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
    public boolean isPage() {
        return false;
    }

    @Override
    public boolean isArea() {
        return false;
    }

    @Override
    public boolean isComponent() {
        return true;
    }

    @Override
    public void editComponent() {
        String workspace = getAttribute("workspace");
        String path = getAttribute("path");
        String dialog = getAttribute("dialog");
        eventBus.fireEvent(new EditComponentEvent(workspace, path, dialog));
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
}
