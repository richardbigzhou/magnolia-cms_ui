/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.widget.magnoliashell.gwt.client.shellmessage;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryCallback;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMagnoliaShellView;

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
 * 
 * @author apchelintcev
 * 
 */
public class VShellMessage extends HTML {

    private static final String STYLE_NAME = "v-shell-notification";

    /**
     * Enumeration of possible message types.
     * 
     * @author apchelintcev
     * 
     */
    public enum MessageType {
        WARNING, ERROR;
    }

    private HandlerRegistration eventPreviewReg = null;

    private final VMagnoliaShellView shell;

    private final MessageType type;

    private final String topic;

    private Element header = DOM.createDiv();
    
    private Element closeEl = DOM.createDiv();

    private Element detailsEl = DOM.createDiv();

    private Element topicEl = DOM.createSpan();

    private Element detailsExpanderEl = DOM.createElement("b");

    private Element messageTypeEl = DOM.createElement("b");

    private final String id;
    
    private final String message;
    
    
    public VShellMessage(VMagnoliaShellView shell, MessageType type, String topic, String message, String id) {
        super();
        this.type = type;
        this.topic = topic;
        this.shell = shell;
        this.message = message;
        this.id = id;
        construct();
    }

    protected final VMagnoliaShellView getShell() {
        return shell;
    }

    private void construct() {
        setStyleName(STYLE_NAME);
        
        header.setClassName("header");
        getElement().appendChild(header);
        
        topicEl.setInnerText(topic);
        header.appendChild(messageTypeEl);
        header.appendChild(topicEl);

        detailsExpanderEl.setInnerText("[MORE]");
        detailsExpanderEl.setClassName("details-expander");
        header.appendChild(detailsExpanderEl);

        closeEl.setClassName("close");
        header.appendChild(closeEl);
        
        detailsEl.setInnerText(message);
        getElement().appendChild(detailsEl);
        
        switch (type) {
        case WARNING:
            addStyleName("warning");
            messageTypeEl.setInnerHTML("Warning: ");
            setAutoExpanding(true);
            break;
        case ERROR:
            addStyleName("error");
            setAutoExpanding(false);
            messageTypeEl.setInnerHTML("Error: ");
            break;
        }
        
    }

    private void setAutoExpanding(boolean autoExpanding) {
        if (autoExpanding) {
            expand();
        } else {
            detailsEl.getStyle().setDisplay(Display.NONE);
            detailsExpanderEl.getStyle().setDisplay(Display.INLINE);
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        sinkEvents(Event.MOUSEEVENTS);
        eventPreviewReg = Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    final Element targetEl = event.getNativeEvent().getEventTarget().cast();
                    if (getElement().isOrHasChild(targetEl) && type == MessageType.WARNING) {
                        close();
                    }
                }
            }
        });
        show();
    }

    protected void close() {
        hide();
        shell.closeMessageEager(id);
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        int eventCode = event.getTypeInt();
        if (eventCode == Event.ONMOUSEDOWN) {
            final Element target = event.getEventTarget().cast();
            if (target == closeEl) {
                hide();
            } else if (target == detailsExpanderEl) {
                expand();
            }
        }
    }

    protected Element getDetailsElement() {
        return detailsEl;
    }
    
    protected void expand() {
        detailsExpanderEl.getStyle().setDisplay(Display.NONE);
        detailsEl.getStyle().setDisplay(Display.BLOCK);
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

    protected int getHeaderHeight() {
        return JQueryWrapper.select(header).cssInt("height");
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        if (eventPreviewReg != null) {
            eventPreviewReg.removeHandler();
        }
    }
}
