/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.jsni;

import info.magnolia.ui.vaadin.gwt.client.editor.jsni.event.FrameLoadedEvent;
import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Frame;
import com.vaadin.client.BrowserInfo;

/**
 * WebkitFrameEventHandler. Provides separated implementations to overcome different bugs in the
 * handling of iframes on webkit browsers including the iPad.
 * See SCRUM-1593 for details.
 */
public class WebkitFrameEventHandler extends AbstractFrameEventHandler {

    private MyTimer timer = new MyTimer();
    private boolean loaded = false;
    private boolean touchScrolling = false;
    private int lastY = 0;

    @Override
    public void init() {
        registerLoadHandler();

        if (BrowserInfo.get().isTouchDevice()) {

            getEventBus().addHandler(FrameLoadedEvent.TYPE, new FrameLoadedEvent.Handler() {

                @Override
                public void handle(FrameLoadedEvent event) {
                    addIframeTouchMoveListener(event.getFrameDocument(), getView().getContent().getElement());
                }
            });
        }
    }


    @Override
    public void onFrameReady() {
        super.onFrameReady();
        timer.cancel();
        registerUnloadHandler(getView().getFrame().getElement(), timer);
    }

    /**
     * Registers two separated implementations of an onload event.
     * In case the onload is not triggered it's supposed to fallback on a readystate poller.
     * We have to make sure, that they don't interfere by using a loaded boolean.
     */
    public void registerLoadHandler() {
        Frame frame = getView().getFrame();

        frame.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                if (loaded) {
                    return;
                }
                loaded = true;
                onFrameReady();
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
     * Doesn't work, that's why browsing inside the iframe is broken for webkit.
     */
    public native void registerUnloadHandler(Element element, Timer timer) /*-{
        var that = timer;
        var poll = function(){
            that.@com.google.gwt.user.client.Timer::scheduleRepeating(I)(100);
        };

        element.contentWindow.addEventListener('unload', poll, false);

    }-*/;

    /**
     * Javascript hack for handling scrolling in iframes on iPad.
     * This needs to be initialized after the iframe has been loaded, otherwise the document will be null.
     * The touchscrolling boolean is used to circumvent a selection being triggered while scrolling.
     * @param doc
     * @param cont
     */
    private final native void addIframeTouchMoveListener(Document doc, Element cont) /*-{
        var content = cont;
        var that = this;
        var X = 0;
        var Y = 0;
        var lastY = 0;
        doc.body.addEventListener('touchmove',
                function(event) {
                    that.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.WebkitFrameEventHandler::touchScrolling = true;
                    event.preventDefault();
                    var newX = event.targetTouches[0].pageX;
                    var newY = event.targetTouches[0].pageY;
                    var deltaY = newY - Y;
                    var deltaX = newX - X;
                    cont.scrollLeft -= deltaX;
                    cont.scrollTop -= deltaY;

                    that.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.WebkitFrameEventHandler::lastY = cont.scrollTop;

                    X = newX - deltaX;
                    Y = newY - deltaY;
                });

        doc.body.addEventListener('touchstart',
                function (event) {
                    that.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.WebkitFrameEventHandler::touchScrolling = false;
                    parent.window.scrollTo(0, 1);
                    X = event.targetTouches[0].pageX;
                    Y = event.targetTouches[0].pageY;
                });
    }-*/;


    /**
     * Custom implementation for iPads of the touchend handling. Surpresses the selection, when scrolling.
     */
    @Override
    public native void initNativeTouchSelectionListener(Element element, PageEditorView.Listener listener) /*-{
        if (element != 'undefined') {
            var ref = this;
            var that = listener;
            element.contentDocument.ontouchend = function(event) {
                var isTouchScrolling = ref.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.WebkitFrameEventHandler::isTouchScrolling()();
                if (!isTouchScrolling) {
                    that.@info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView.Listener::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);
                    ref.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.WebkitFrameEventHandler::resetScrollTop()();
                }
            }
        }
    }-*/;

    public boolean isTouchScrolling() {
        return touchScrolling;
    }

    /**
     * Hack to prevent the page editor to jump back to top, when scrolling.
     */
    public void resetScrollTop() {

        Timer timer = new Timer(){
            @Override
            public void run() {
                getView().getContent().getElement().setScrollTop(lastY);
            }
        };
        timer.schedule(1);

        Timer timer2 = new Timer(){
            @Override
            public void run() {
                getView().getContent().getElement().setScrollTop(lastY);
            }
        };
        timer2.schedule(100);
    }

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
