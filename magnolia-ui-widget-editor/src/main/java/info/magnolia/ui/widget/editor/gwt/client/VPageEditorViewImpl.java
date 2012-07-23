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


import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.web.bindery.event.shared.EventBus;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 *
 */
public class VPageEditorViewImpl extends FlowPanel implements VPageEditorView {


    private Listener listener;
    private EventBus eventBus;
    private Frame iframe;


    public VPageEditorViewImpl(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        iframe = new Frame();
        iframe.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {

                //other handlers are initialized here b/c we need to know the document inside the iframe.
                //make sure we process  html only when the document inside the iframe is loaded.
                listener.onFrameLoaded(iframe);

            }
        });

        final Element iframeElement = iframe.getElement();
        iframeElement.setAttribute("width", "100%");
        iframeElement.setAttribute("height", "100%");
        iframeElement.setAttribute("allowTransparency", "true");
        iframeElement.setAttribute("frameborder", "0");
        add(iframe);

    }

    @Override
    public Frame getIframe() {
        return iframe;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

}