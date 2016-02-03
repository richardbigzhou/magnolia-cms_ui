/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.Callbacks;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellView;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.HTML;

/**
 * Simple notification object that pops up when warnings/errors occur.
 */
public abstract class ShellMessageWidget extends HTML {

    /**
     * Enumeration of possible message types.
     */
    public enum MessageType {
        WARNING, ERROR, INFO
    }

    private static final String STYLE_NAME = "v-shell-notification";

    private HandlerRegistration eventPreviewReg = null;

    private final MagnoliaShellView shell;

    private Element messageTypeEl = DOM.createElement("b");

    private Element header = DOM.createDiv();

    private Element topicEl = DOM.createSpan();

    private Element closeEl = DOM.createDiv();

    private final String id;

    private final String message;

    private final String topic;

    public ShellMessageWidget(MagnoliaShellView shell, String topic, String message, String id) {
        super();
        this.topic = topic;
        this.shell = shell;
        this.message = message;
        this.id = id;
        eventPreviewReg = Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    final Element targetEl = event.getNativeEvent().getEventTarget().cast();
                    if (getElement().isOrHasChild(targetEl)) {
                        onMessageClicked(targetEl);
                    }
                }
            }
        });
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        sinkEvents(Event.MOUSEEVENTS);
        construct();
        show();
    }

    protected void onMessageClicked(Element targetEl) {
        close();
    }

    protected void close() {
        hide();
        getShell().closeMessageEager(getId());
    }

    protected void construct() {
        topicEl.setInnerText(topic);
        header.appendChild(messageTypeEl);
        header.appendChild(topicEl);

        addStyleName(STYLE_NAME);

        header.setClassName("header");
        getElement().appendChild(header);

        applyCloseIconStyles(closeEl);
        header.appendChild(closeEl);

        messageTypeEl.setInnerHTML(getMessageTypeCaption());
    }

    protected void applyCloseIconStyles(Element element) {
        element.setClassName("close");
    }

    protected abstract String getMessageTypeCaption();

    protected int getHeaderHeight() {
        return JQueryWrapper.select(header).cssInt("height");
    }

    public void show() {
        getElement().getStyle().setDisplay(Display.NONE);
        JQueryWrapper.select(this).slideDown(300, null);
    }

    public void hide() {
        JQueryWrapper.select(this).slideUp(300, Callbacks.create(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                removeFromParent();
            }
        }));
    }

    public void hideWithoutTransition() {
        removeFromParent();
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        int eventCode = event.getTypeInt();
        if (eventCode == Event.ONTOUCHEND) {
            final Element target = event.getEventTarget().cast();
            if (target == closeEl) {
                close();
            }
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (eventPreviewReg != null) {
            eventPreviewReg.removeHandler();
        }
    }

    public Element getHeader() {
        return header;
    }

    protected final MagnoliaShellView getShell() {
        return shell;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

}
