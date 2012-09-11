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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import info.magnolia.ui.widget.editor.gwt.client.VPageEditorView;

/**
 * NativeDomHandler.
 */
abstract public class NativeDomHandler {


    private EventBus eventBus;

    private VPageEditorView view;
    private boolean touchScrolling = false;
    private int lastY = 0;

    /**
     * Takes care of the mouse up and touchend events for selecting elements inside the page editor.
     * Unfortunately the GWT handlers do not work, so using jsni.
     * @param element
     * @param listener
     */
    public native void initNativeSelectionListener(Element element, VPageEditorView.Listener listener) /*-{
        if (element != 'undefined') {
            var ref = this;
            var that = listener;
            element.contentDocument.onmouseup = function(event) {
                that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditor::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);

            }
            element.contentDocument.ontouchend = function(event) {
                var isTouchScrolling = ref.@info.magnolia.ui.widget.editor.gwt.client.jsni.NativeDomHandler::isTouchScrolling()();
                if (!isTouchScrolling) {
                    that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditor::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);
                    ref.@info.magnolia.ui.widget.editor.gwt.client.jsni.NativeDomHandler::resetScrollTop()();
                }
            }
        }
    }-*/;

    public native void reloadIFrame(Element iframeElement) /*-{
        iframeElement.contentWindow.location.reload(true);
    }-*/;

    public abstract void notifyUrlChange();

    /**
     * This functionality was added in 4.5. Not triggered at the moment.
     */
    private native void onPageEditorReady() /*-{
        var callbacks = $wnd.mgnl.PageEditor.onPageEditorReadyCallbacks
        if(typeof callbacks != 'undefined') {
            for(var i=0; i < callbacks.length; i++) {
                callbacks[i].apply();
            }
        }
    }-*/;

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setView(VPageEditorView view) {
        this.view = view;
    }

    public VPageEditorView getView() {
        return view;
    }

    public void init() {
        IFrameElement frameElement = IFrameElement.as(getView().getIframe().getElement());
       // Document contentDocument = frameElement.getContentDocument();
       // addIframeTouchMoveListener(contentDocument, getView().getContent().getElement());
    }


    private final native void addIframeTouchMoveListener(Document doc, Element cont) /*-{
        var content = cont;
        var that = this;
        var X = 0;
        var Y = 0;
        var lastY = 0;
        doc.body.addEventListener('touchmove',
                function(event) {
                    that.@info.magnolia.ui.widget.editor.gwt.client.jsni.NativeDomHandler::touchScrolling = true;
                    event.preventDefault();
                    var newX = event.targetTouches[0].pageX;
                    var newY = event.targetTouches[0].pageY;
                    var deltaY = newY - Y;
                    var deltaX = newX - X;
                    cont.scrollLeft -= deltaX;
                    cont.scrollTop -= deltaY;

                    that.@info.magnolia.ui.widget.editor.gwt.client.jsni.NativeDomHandler::lastY = cont.scrollTop;

                    X = newX - deltaX;
                    Y = newY - deltaY;
                });

        doc.body.addEventListener('touchstart',
                function (event) {
                    that.@info.magnolia.ui.widget.editor.gwt.client.jsni.NativeDomHandler::touchScrolling = false;
                    parent.window.scrollTo(0, 1);
                    X = event.targetTouches[0].pageX;
                    Y = event.targetTouches[0].pageY;
                });
    }-*/;
    public boolean isTouchScrolling() {
        return touchScrolling;
    }

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
}
