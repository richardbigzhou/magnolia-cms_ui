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
package info.magnolia.ui.vaadin.gwt.client.editor.jsni;

import info.magnolia.ui.vaadin.gwt.client.editor.jsni.event.FrameLoadedEvent;
import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView;

import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.EventBus;

/**
 * AbstractFrameEventHandler.
 */
abstract public class AbstractFrameEventHandler {


    private EventBus eventBus;

    private PageEditorView view;


    /**
     * Force iframe to be reloaded. for example when content has been updated.
     */
    public native void reloadIFrame(Element iframeElement) /*-{
        iframeElement.contentWindow.location.reload();
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

    public void setView(PageEditorView view) {
        this.view = view;
    }

    public PageEditorView getView() {
        return view;
    }

    public abstract void init();

    public void onFrameReady() {
        eventBus.fireEvent(new FrameLoadedEvent(view.getFrame()));
    }

    /**
     * Takes care of the mouse up events for selecting elements inside the page editor.
     * Unfortunately the GWT handlers do not work, so using jsni.
     */
    public native void initNativeTouchSelectionListener(Element element, PageEditorView.Listener listener) /*-{
        if (element != 'undefined') {
            var ref = this;
            var that = listener;
            element.contentDocument.ontouchend = function(event) {
                that.@info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView.Listener::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);
            }
        }
    }-*/;

    /**
     * Takes care of the touch end events for selecting elements inside the page editor.
     * Unfortunately the GWT handlers do not work, so using jsni.
     */
    public native void initNativeMouseSelectionListener(Element element, PageEditorView.Listener listener) /*-{
        if (element != 'undefined') {
            var that = listener;
            element.onmouseup = function(event) {
                that.@info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView.Listener::selectElement(Lcom/google/gwt/dom/client/Element;)(event.target);
            }
        }
    }-*/;

    /**
     * Catches key events on the contentDocument of the frame {@link Element} and fires it on the frame to enable event bubbling
     * from the frame up to the DOM.
     */
    public abstract void initNativeKeyListener(Element element);

    public native void initScrollListener(Element element) /*-{
        if (element != 'undefined') {
            var that = this;
            var view = that.@info.magnolia.ui.vaadin.gwt.client.editor.jsni.AbstractFrameEventHandler::view;
            element.onscroll = function(event) {
                var scrollTop = event.target.scrollTop;
                if (scrollTop > 0) {
                    view.@info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView::setLastScrollPosition(I)(scrollTop);
                }
            }
        }
    }-*/;
}
