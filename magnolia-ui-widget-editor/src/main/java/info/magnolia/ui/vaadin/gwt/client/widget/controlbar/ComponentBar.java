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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

/**
 * Control bar for components. Injected at the beginning of an area.
 */
public class ComponentBar extends AbstractBar {

    private final ComponentListener listener;

    public ComponentBar(MgnlComponent mgnlElement) {
        super(mgnlElement);

        this.listener = mgnlElement;
        addStyleName(COMPONENT_CLASS_NAME);

        initLayout();

        if (DragDropEventBase.isSupported()) {
            createDragAndDropHandlers();
            this.getElement().setDraggable(Element.DRAGGABLE_TRUE);
            getStyle().setCursor(Style.Cursor.MOVE);
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
    }

    private void createDragAndDropHandlers() {
        addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                //bar.toggleButtons(false);

                setStyleName("moveSource", true);

                listener.onDragStart();

                int x = getAbsoluteLeft();
                int y = getAbsoluteTop();
                event.setData("text", listener.getNodeName() + "," + x + "," + y);
                event.getDataTransfer().setDragImage(getElement(), 10, 10);

            }
        }, DragStartEvent.getType());

        addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                //toggleButtons(true);

                setStyleName("moveSource", false);

                listener.onDragEnd();
            }
        }, DragEndEvent.getType());

        addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String idSource = tokens[0];

                if (!listener.getNodeName().equals(idSource)) {
                    setStyleName("moveOver", true);
                }
                event.stopPropagation();
            }
        }, DragOverEvent.getType());

        addDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                setStyleName("moveOver", false);
                event.stopPropagation();
            }
        }, DragLeaveEvent.getType());

        addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String sourcePath = tokens[0];

                if (!listener.getNodeName().equals(sourcePath)) {
                    int xTarget = getAbsoluteLeft();
                    int yTarget = getAbsoluteTop();
                    int xOrigin = Integer.valueOf(tokens[1]);
                    int yOrigin = Integer.valueOf(tokens[2]);

                    boolean isDragUp = yOrigin > yTarget;
                    boolean isDragDown = !isDragUp;
                    boolean isDragLeft = xOrigin > xTarget;
                    boolean isDragRight = !isDragLeft;

                    String order = null;

                    if (isDragUp || isDragLeft) {
                        order = "before";
                    } else if (isDragDown || isDragRight) {
                        order = "after";
                    }
                    //String parentPath = bar.getPath().substring(0, bar.getPath().lastIndexOf("/"));

                    listener.sortComponent(sourcePath, order);
                }

                event.preventDefault();
            }
        }, DropEvent.getType());    }


   /*
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

}
