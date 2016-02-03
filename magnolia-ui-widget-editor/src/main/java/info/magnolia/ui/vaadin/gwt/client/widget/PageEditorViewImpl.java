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
package info.magnolia.ui.vaadin.gwt.client.widget;

import info.magnolia.ui.vaadin.gwt.client.editor.jsni.AbstractFrameEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ComputedStyle;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 */
public class PageEditorViewImpl extends Composite implements PageEditorView {

    public static final String PAGE_EDITOR_CLASS_NAME = "pageEditor";

    private Listener listener;

    private PageEditorFrame iframe = new PageEditorFrame();

    private String url;

    private SimplePanel content;

    private AbstractFrameEventHandler handler;
    private int lastScrollPosition;

    public PageEditorViewImpl(EventBus eventBus) {
        super();
        this.handler = GWT.create(AbstractFrameEventHandler.class);
        this.content = new SimplePanel();
        handler.setView(this);
        handler.setEventBus(eventBus);
        content.setWidget(iframe);
        initWidget(content);
        setStyleName(PAGE_EDITOR_CLASS_NAME);

        final Element iframeElement = iframe.getElement();
        iframeElement.setAttribute("width", "100%");
        iframeElement.setAttribute("height", "100%");
        iframeElement.setAttribute("allowTransparency", "true");
        iframeElement.setAttribute("frameborder", "0");

        handler.init();
        if (BrowserInfo.get().isIE8()) {
            registerPageEditorIframe(iframeElement);
        }
    }

    private native void registerPageEditorIframe(Element iframeElement) /*-{
        $wnd.__page_editor_iframe = iframeElement;
    }-*/;

    @Override
    public PageEditorFrame getFrame() {
        return iframe;
    }

    @Override
    public Widget getContent() {
        return content;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setUrl(String url) {
        getFrame().setUrl(url);
        this.url = url;
        handler.notifyUrlChange();
    }

    @Override
    public void reload() {
        handler.reloadIFrame(iframe.getElement());
        handler.notifyUrlChange();
    }

    @Override
    public void setLastScrollPosition(int lastScrollPosition) {
        this.lastScrollPosition = lastScrollPosition;
    }

    @Override
    public void resetScrollTop() {
        if (BrowserInfo.get().isTouchDevice()) {
            getFrame().getElement().getStyle().setHeight(getFrame().getBody().getOffsetHeight(), Style.Unit.PX);
            new ComputedStyle(getFrame().getElement());
        }
        content.getElement().setScrollTop(lastScrollPosition);
    }

    @Override
    public void initDomEventListeners() {
        if (BrowserInfo.get().isTouchDevice()) {
            handler.initNativeTouchSelectionListener(iframe.getBody(), listener);
        } else {
            handler.initNativeMouseSelectionListener(iframe.getElement(), iframe.getBody(), listener);
        }
        handler.initNativeKeyListener(iframe.getElement());
        handler.initScrollListener(content.getElement());
    }

    @Override
    public void initKeyEventListeners() {
        handler.initNativeKeyListener(iframe.getElement());
    }

}
