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

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchCancelEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchMoveEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;

/**
 * Special implementation of {@link com.vaadin.client.ui.VUI} that would preinitialize MGWT touch events, so that those will not interfere
 * with native GWT touch events.
 */
public class VMobileSafariUI extends VMgwtStylesUI {

    private HandlerManager handlerManager;

    private static final String TABLET_STYLENAME = "tablet";

    static {
        new TouchStartEvent() {
        };
        new TouchEndEvent() {
        };
        new TouchMoveEvent() {
        };
        new TouchCancelEvent() {
        };
    }

    public VMobileSafariUI() {
        super();
        addStyleName(TABLET_STYLENAME);

        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (event.getTypeInt() == Event.ONTOUCHMOVE) {
                    lockPageScroll(event.getNativeEvent());
                }
            }
        });

        if (handlerManager != null && handlerManager.getHandlerCount(com.google.gwt.event.dom.client.TouchStartEvent.getType()) > 0) {
            com.google.gwt.event.dom.client.TouchStartHandler eh = handlerManager.getHandler(com.google.gwt.event.dom.client.TouchStartEvent.getType(), 0);
            handlerManager.removeHandler(com.google.gwt.event.dom.client.TouchStartEvent.getType(), eh);
        }
    }

    @Override
    public void setStyleName(String style) {
        super.setStyleName(style);
        addStyleName(TABLET_STYLENAME);
    }

    @Override
    protected HandlerManager createHandlerManager() {
        handlerManager = super.createHandlerManager();
        return handlerManager;
    }

    /**
     * Checks if event target is within a v-scrollable view.
     */
    private static native void lockPageScroll(Object e)
    /*-{
       jq = e.target.ownerDocument.defaultView.jQuery;
       if (jq(e.target).parents(".v-scrollable").length < 1) {
           e.preventDefault();
       }
    }-*/;
}
