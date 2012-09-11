/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.editor.gwt.client.jsni;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Frame;
import info.magnolia.ui.widget.editor.gwt.client.jsni.event.FrameLoadedEvent;

/**
 * NativeDomHandlerWebkit. This class takes care of handling the load events on webkit browsers.
 * See SCRUM-1593 for details.
 */
public class NativeDomHandlerWebkit extends NativeDomHandler {

    private MyTimer timer = new MyTimer();
    private boolean loaded = false;


    @Override
    public void init() {
        super.init();
        registerLoadHandler();


    }

    /**
     * Registers two separated implementations of an onload event.
     * In case the onload is not triggered it's supposed to fallback on a readystate poller.
     * We have to make sure, that they don't interfere by using a loaded boolean.
     */
    public void registerLoadHandler() {
        Frame frame = getView().getIframe();

        frame.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                if (loaded) {
                    return;
                }
                loaded = true;
                timer.cancel();
                getEventBus().fireEvent(new FrameLoadedEvent());
                registerUnloadHandler(getView().getIframe().getElement(), timer);
            }
        });
        timer.setIframe(frame);
        timer.scheduleRepeating(100);

    }

    @Override
    public void notifyUrlChange() {
        loaded = false;
        timer.scheduleRepeating(100);
    }

    /**
     * This function is supposed to trigger an unload event, when the page inside the Iframe is changed.
     * Doesn't work, that's why browsing inside the iframe is broke for webkit.
     */
    public native void registerUnloadHandler(Element element, Timer timer) /*-{
        var that = timer;
        var poll = function(){
            that.@com.google.gwt.user.client.Timer::scheduleRepeating(I)(100);
        };

        element.contentWindow.addEventListener('unload', poll, false);

    }-*/;


    /**
     * Poller to check the readystate of the contentdocument in the iframe.
     */
    private class MyTimer extends Timer {

        Frame iframe;

        @Override
        public void run() {
            GWT.log("timer started");
            String readyState = getReadyState(iframe.getElement());

            if (readyState != null && !readyState.isEmpty()) {
                GWT.log("doc.readyState" + readyState);
            }

            if ("interactive".equals(readyState)) {
                NativeEvent event = Document.get().createLoadEvent();
                DomEvent.fireNativeEvent(event, iframe);
            }
        }


        public void setIframe(Frame iframe) {
            this.iframe = iframe;
        }

        public final native String getReadyState(Element element) /*-{
            if (element.contentWindow != null) {
                return element.contentWindow.document.readyState;
            }
            return '';
        }-*/;


    }
}
