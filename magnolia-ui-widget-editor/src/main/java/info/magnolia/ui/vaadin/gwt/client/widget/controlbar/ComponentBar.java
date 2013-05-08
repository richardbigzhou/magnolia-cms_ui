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

import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.user.client.ui.Label;

/**
 * Edit bar.
 */
public class ComponentBar extends AbstractBar {

    private final ComponentListener listener;

    private boolean isInherited;

    private boolean editable = true;

    private boolean canDelete = true;
    private boolean canMove = true;
    private boolean canEdit = true;

    public ComponentBar(MgnlComponent mgnlElement) {

        super(mgnlElement);
        this.listener = mgnlElement;
        addStyleName("component");

        /*
         * if (DragDropEventBase.isSupported()) {
         * createDragAndDropHandlers();
         *
         * }
         */
        if (!this.isInherited) {
            createControls();
            // createMouseEventsHandlers();
        }

        setVisible(false);

    }

    public void setDraggable(boolean draggable) {
        if (DragDropEventBase.isSupported()) {
            if (draggable) {
                this.getElement().setDraggable(Element.DRAGGABLE_TRUE);
                getStyle().setCursor(Cursor.MOVE);
            } else {
                this.getElement().setDraggable(Element.DRAGGABLE_FALSE);
                getStyle().setCursor(Cursor.DEFAULT);
            }
        }
    }

    @Override
    protected void setFields(Map<String, String> attributes) {

        //setId("__" + nodeName);

        this.isInherited = Boolean.parseBoolean(attributes.get("inherited"));

        if (attributes.containsKey("editable")) {
            this.editable = Boolean.parseBoolean(attributes.get("editable"));
        }

        if (attributes.containsKey("rights")) {
            final String rights = attributes.get("rights");
            this.canDelete = rights.contains("canDelete");
            this.canMove = rights.contains("canMove");
            this.canEdit = rights.contains("canEdit");
        }
    }

/*    private void createDragAndDropHandlers() {
        DragAndDrop.dragAndDrop(getEventBus(), this);
    }

    private void createMouseEventsHandlers() {

        addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                LegacyDragAndDrop.moveComponentEnd(ComponentBar.this);
            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                LegacyDragAndDrop.moveComponentOver(ComponentBar.this);

            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                LegacyDragAndDrop.moveComponentOut(ComponentBar.this);
            }
        }, MouseOutEvent.getType());
    }*/


    private void createControls() {

        final Label edit = new Label();
        edit.setStyleName(ICON_CLASSNAME);
        edit.addStyleName(EDIT_CLASSNAME);
        edit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                listener.editComponent();
            }
        });
        if (!canEdit) {
            edit.setVisible(false);
        }
        addButton(edit);

    }

}
