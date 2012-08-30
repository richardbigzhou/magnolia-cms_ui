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
package info.magnolia.ui.widget.editor.gwt.client;


import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 *
 */
public class VPageEditorViewImpl extends ScrollPanel implements VPageEditorView {


    private Listener listener;
    private Frame iframe;
    private String url;

    public VPageEditorViewImpl() {
        super();
        setStyleName("pageEditor");

        iframe = new Frame();

        iframe.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {

                //other handlers are initialized here b/c we need to know the document inside the iframe.
                //make sure we process  html only when the document inside the iframe is loaded.
                listener.onFrameLoaded(iframe);
                addIframeTouchMoveListener(((IFrameElement)iframe.getElement().cast()).getContentDocument(), getElement());
            }
        });

        final Element iframeElement = iframe.getElement();
        iframeElement.setAttribute("width", "100%");
        iframeElement.setAttribute("height", "100%");
        iframeElement.setAttribute("allowTransparency", "true");
        iframeElement.setAttribute("frameborder", "0");
        add(iframe);

    }

    private int X = 0;
    
    private int Y = 0;
    
    private final native void addIframeTouchMoveListener(Document doc, Element cont) /*-{
        var w = $wnd;     
        var content = cont;
        var that = this;
        doc.body.addEventListener('touchmove',
        function(event) {
            event.preventDefault();
            var newX = event.targetTouches[0].pageX;
            var newY = event.targetTouches[0].pageY;
            var deltaY = newY - that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::Y;
            var deltaX = newY - that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::X;
            cont.scrollLeft -= deltaX;
            cont.scrollTop -= deltaY;
            w.console.log("top " + cont.scrollTop + " newY " + newY + " delta " + (newY - that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::Y));
            that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::X = newX - deltaX;
            that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::Y = newY - deltaY;
        });
    
        doc.body.addEventListener('touchstart',
        function (event) {
            event.preventDefault();
            //parent.window.scrollTo(0, 0);
            that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::X = event.targetTouches[0].pageX;
            that.@info.magnolia.ui.widget.editor.gwt.client.VPageEditorViewImpl::Y = event.targetTouches[0].pageY;
        });
    }-*/;
    
    @Override
    public Frame getIframe() {
        return iframe;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setUrl(String url) {
        // if the page is already loaded, force a reload
        if (url.equals(this.url)) {
            reload();
        }
        else {
            getIframe().setUrl(url);
            this.url = url;

        }
    }

    @Override
    public void reload() {
        reloadIFrame(getIframe().getElement());
    }

    protected native void reloadIFrame(Element iframeElement) /*-{
        iframeElement.contentWindow.location.reload(true);
    }-*/;


}