/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * IE8 implementation of {@link ControlBarEventManager}.
 * Attaches/detaches event listeners with JSNI.
 */
public class ControlBarEventManagerIE8 implements ControlBarEventManager {

    @Override
    public void addClickOrTouchHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("onmouseup", target.getElement(), listener);
    }

    @Override
    public void unregisterMoveHandlers(Widget widget) {
        Element element = widget.getElement();
        unregisterEventHandler("onmousedown", element);
        unregisterEventHandler("onmouseover", element);
        unregisterEventHandler("onmouseout", element);
    }

    @Override
    public void unregisterDnDHandlers(Widget widget) {
        Element element = widget.getElement();
        unregisterEventHandler("ondragstart", element);
        unregisterEventHandler("ondragend", element);
        unregisterEventHandler("ondragover", element);
        unregisterEventHandler("ondragleave", element);
        unregisterEventHandler("ondragenter", element);
        unregisterEventHandler("ondrop", element);
    }

    @Override
    public void addMouseDownHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("onmousedown", target.getElement(), listener);
    }

    @Override
    public void addMouseOverHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("onmouseover", target.getElement(), listener);
    }

    @Override
    public void addMouseOutHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("onmouseout", target.getElement(), listener);
    }

    @Override
    public void addDragStartHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("ondragstart", target.getElement(), listener);
    }

    @Override
    public void addDragEndHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("ondragend", target.getElement(), listener);
    }

    @Override
    public void addDragOverHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("ondragenter", target.getElement(), listener);
    }

    @Override
    public void addDragLeaveHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("ondragleave", target.getElement(), listener);
    }

    @Override
    public void addDropHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("ondrop", target.getElement(), listener);
    }

    @Override
    public void addMouseMoveHandler(Widget target, ControlBarEventHandler listener) {
        addEventListener("onmousemove", target.getElement(), listener);
    }

    @Override
    public void removeMouseMoveHandler(Widget widget) {
        unregisterEventHandler("onmousemove", widget.getElement());
    }

    protected native void addEventListener(String mouseEvent, Element element, ControlBarEventHandler controlBarEventHandler) /*-{
        element[mouseEvent] = $entry(function () {
            controlBarEventHandler.@info.magnolia.ui.vaadin.gwt.client.widget.controlbar.eventmanager.ControlBarEventHandler::handle(Lcom/google/gwt/dom/client/NativeEvent;)($wnd.__page_editor_iframe.contentWindow.event);
            return false;
        })
    }-*/;

    protected native void unregisterEventHandler(String eventId, Element element) /*-{
        element[eventId] = null;
    }-*/;
}
