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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Frame;
import info.magnolia.ui.widget.editor.gwt.client.VPageEditorView;

/**
 * NativeDomHandlerWebkit.
 */
public class NativeDomHandlerWebkit extends NativeDomHandler {

    private int X = 0;

    private int Y = 0;
    private boolean registered = false;

    private MyTimer timer = new MyTimer();

    public final native void addIframeTouchMoveListener(Document doc) /*-{
        var X = 0;
        var Y = 0;
        var w = $wnd;
        var content = view.element;
        var that = view;
        doc.body.addEventListener('touchmove',
                function(event) {
                    event.preventDefault();
                    var newX = event.targetTouches[0].pageX;
                    var newY = event.targetTouches[0].pageY;
                    var deltaY = newY - Y;
                    var deltaX = newY - X;
                    cont.scrollLeft -= deltaX;
                    cont.scrollTop -= deltaY;
                    w.console.log("top " + cont.scrollTop + " newY " + newY + " delta " + (newY - Y));
                    X = newX - deltaX;
                    Y = newY - deltaY;
                });

        doc.body.addEventListener('touchstart',
                function (event) {
                    event.preventDefault();
                    //parent.window.scrollTo(0, 0);
                    X = event.targetTouches[0].pageX;
                    Y = event.targetTouches[0].pageY;
                });
    }-*/;

    @Override
    public void registerOnReady(Frame frame, final VPageEditorView.Listener listener) {
/*
        timer.setIframe(frame);
        timer.setListener(listener);
        timer.scheduleRepeating(100);
*/

        new ReadyStateWatch(frame).addReadyStateChangeHandler(
            new ValueChangeHandler<ReadyStateWatch.ReadyState>() {


                @Override
                public void onValueChange(ValueChangeEvent<ReadyStateWatch.ReadyState> event) {
                    GWT.log(event.getValue().toString());
                }
            });

/*        frame.addLoadHandler(new LoadHandler() {
    @Override
    public void onLoad(LoadEvent event) {
        timer.cancel();

        listener.onFrameLoaded();
    }
});*/

        registerOnUnload(frame.getElement(), timer);

    }


    private native void registerOnUnload(Element element, Timer timer) /*-{
        var that = timer;
        var poll = function(){
            that.@com.google.gwt.user.client.Timer::scheduleRepeating(I)(100);
        };


        element.addEventListener('unload', poll, false);

    }-*/;

    private static class MyTimer extends Timer {

        public void setIframe(Frame iframe) {
            this.iframe = iframe;
        }

        Frame iframe;

        public void setListener(VPageEditorView.Listener listener) {
            this.listener = listener;
        }

        VPageEditorView.Listener listener;

        @Override
        public void run() {
            GWT.log("timer started");
            String readystate = iframe.getElement().getPropertyString("readystate");


            String readyState = iframe.getElement().getPropertyString("readyState");
            if (readyState != null && !readyState.isEmpty()) {
                GWT.log("doc.readyState" + readyState);
            }
            if (readystate != null && !readystate.isEmpty()) {
                GWT.log("doc.readystate" + readystate);
            }
            if ("interactive".equals(readyState)) {
                listener.onFrameLoaded();
                cancel();
            }
        }
    }
}
