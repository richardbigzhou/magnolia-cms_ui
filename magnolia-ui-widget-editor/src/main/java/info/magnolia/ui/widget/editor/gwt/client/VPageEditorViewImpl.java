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


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Frame;
import info.magnolia.ui.widget.editor.gwt.client.jsni.NativeDomHandler;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Composite;

import com.vaadin.terminal.gwt.client.BrowserInfo;


/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 *
 */
public class VPageEditorViewImpl extends Composite implements VPageEditorView {


    private Listener listener;
    private Frame iframe = new Frame();
    private String url;
    private NativeDomHandler handler = GWT.create(NativeDomHandler.class);

    final SimplePanel content; 
            
    public VPageEditorViewImpl() {
        super();
        if (BrowserInfo.get().isTouchDevice()) {
            content = new ScrollPanel();
        } else {
            content = new SimplePanel();
        }
        content.setWidget(iframe);
        initWidget(content);
        setStyleName("pageEditor");

        final Element iframeElement = iframe.getElement();
        iframeElement.setAttribute("width", "100%");
        iframeElement.setAttribute("height", "100%");
        iframeElement.setAttribute("allowTransparency", "true");
        iframeElement.setAttribute("frameborder", "0");

    }

    @Override
    public Frame getIframe() {
        return iframe;
    }

    @Override
    public void registerHandlers() {
        handler.registerOnReady(iframe, listener);
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
        handler.reloadIFrame(iframe.getElement());
    }
}