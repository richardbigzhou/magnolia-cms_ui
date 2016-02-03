/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.DomEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;

/**
 * Class that converts the GWT touch event to its correspondent from mgwt.
 */
public class GwtTouchEventConverter implements Serializable {

    private static final Map<Class<? extends DomEvent<?>>, ? extends TouchEvent<?>> map = new HashMap<Class<? extends DomEvent<?>>, TouchEvent<?>>() {
        {
            put(com.google.gwt.event.dom.client.TouchEndEvent.class, new TouchEndEvent() {
            });
            put(com.google.gwt.event.dom.client.TouchStartEvent.class, new TouchStartEvent() {
            });
            put(com.google.gwt.event.dom.client.TouchCancelEvent.class, new TouchCancelEvent() {
            });
            put(com.google.gwt.event.dom.client.TouchMoveEvent.class, new TouchMoveEvent() {
            });
        }
    };

    @SuppressWarnings("unchecked")
    public static <T extends TouchEvent<?>> T convertGWTEvent(DomEvent<?> gwtEvent) {
        if (map.containsKey(gwtEvent.getClass())) {
            final T result = (T) map.get(gwtEvent.getClass());
            result.setNativeEvent(gwtEvent.getNativeEvent());
            result.setRelativeElement(gwtEvent.getRelativeElement());
            return result;
        }
        return null;
    }
}
