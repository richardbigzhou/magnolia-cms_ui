/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.touchwidget;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchWidgetImpl;

/**
 * Special implementation of {@link TouchWidgetImpl} that prevents conflicts with the inbuilt GWT touch functionality.
 */
public class MobileSafariTouchWidgetImplProxy implements TouchWidgetImpl {

    @Override
    public HandlerRegistration addTouchStartHandler(Widget w, TouchStartHandler handler) {
        return w.addDomHandler(new TouchStartHandlerProxy(handler), com.google.gwt.event.dom.client.TouchStartEvent.getType());
    }

    @Override
    public HandlerRegistration addTouchMoveHandler(Widget w, TouchMoveHandler handler) {
        return w.addDomHandler(new TouchMoveHandlerProxy(handler), com.google.gwt.event.dom.client.TouchMoveEvent.getType());
    }

    @Override
    public HandlerRegistration addTouchCancelHandler(Widget w, TouchCancelHandler handler) {
        return w.addDomHandler(new TouchCancelHandlerProxy(handler), com.google.gwt.event.dom.client.TouchCancelEvent.getType());
    }

    @Override
    public HandlerRegistration addTouchEndHandler(Widget w, TouchEndHandler handler) {
        return w.addDomHandler(new TouchEndHandlerProxy(handler), com.google.gwt.event.dom.client.TouchEndEvent.getType());
    }

}
