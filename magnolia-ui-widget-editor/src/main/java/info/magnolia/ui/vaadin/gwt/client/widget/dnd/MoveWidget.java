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
package info.magnolia.ui.vaadin.gwt.client.widget.dnd;

import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorFrame;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager.ControlBarEventHandler;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager.ControlBarEventManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget used for moving {@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar}.
 * Wraps the moved widget and attaches {@link MouseMoveHandler} to the {@link BodyElement} of the {@link PageEditorFrame}.
 */
public class MoveWidget extends Widget {

    public static final String CLASS_NAME = "mgnlEditorMoveDiv";
    public static final int OFFSET_FROM_MOUSE = 15;

    private FrameBodyWrapper wrapper;

    private PageEditorFrame frame;

    private ControlBarEventManager eventManager = GWT.create(ControlBarEventManager.class);

    public MoveWidget(Element element) {
        setElement(element);
        getElement().setId(CLASS_NAME);
    }

    public void attach(final PageEditorFrame frame, final int width, final int height) {
        this.frame = frame;

        // no other way to get the value marked !important
        String widthImp = "width: " +  width + Unit.PX.getType() + " !important;";
        String heightImp = "height: " +  height + Unit.PX.getType() + " !important;";
        getElement().setAttribute("style", widthImp + heightImp);

        final BodyElement frameBody = frame.getBody();
        frameBody.appendChild(this.getElement());

        this.wrapper = new FrameBodyWrapper(frameBody);
        wrapper.onAttach();

        eventManager.addMouseMoveHandler(wrapper, new ControlBarEventHandler() {
            @Override
            public void handle(NativeEvent event) {
                int x = event.getClientX() + frame.getContentDocument().getScrollLeft();
                int y = event.getClientY() + OFFSET_FROM_MOUSE + frame.getContentDocument().getScrollTop();
                int maxX = frame.getBody().getOffsetWidth() - width;
                int maxY = frame.getBody().getOffsetHeight() - height - OFFSET_FROM_MOUSE;

                x = (x > maxX) ? maxX : x;
                y = (y > maxY) ? maxY : y;

                getElement().getStyle().setTop(y, Unit.PX);
                getElement().getStyle().setLeft(x, Unit.PX);
            }
        });
        super.onAttach();

    }

    public void detach() {
        frame.getBody().removeChild(this.getElement());
        eventManager.removeMouseMoveHandler(wrapper);
        wrapper.onDetach();
        super.onDetach();
    }

    /**
     * Wrapper to attach {@link HasMouseMoveHandlers} to the frames body element.
     */
    class FrameBodyWrapper extends Widget implements HasMouseMoveHandlers {

        FrameBodyWrapper(Element frame) {
            setElement(frame);
        }

        @Override
        public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
            return addDomHandler(handler, MouseMoveEvent.getType());
        }

        @Override
        protected void onAttach() {
            super.onAttach();
        }

        @Override
        protected void onDetach() {
            super.onDetach();
        }
    }

}
