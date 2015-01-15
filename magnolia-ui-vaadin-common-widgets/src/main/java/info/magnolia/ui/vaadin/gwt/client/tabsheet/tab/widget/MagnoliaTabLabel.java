/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.widget;

import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.event.TabCloseEvent;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * Tab label in the tab bar.
 */
public class MagnoliaTabLabel extends SimplePanel {

    private Element indicatorsWrapper = DOM.createDiv();

    private final Element notificationBox = DOM.createDiv();

    private final Element closeElement = DOM.createSpan();

    private final Element errorIndicator = DOM.createDiv();

    private final Element textWrapper = DOM.createSpan();

    private MagnoliaTabWidget tab;

    private final TouchDelegate touchDelegate = new TouchDelegate(this);

    private EventBus eventBus;

    public MagnoliaTabLabel() {
        super(DOM.createElement("li"));

        indicatorsWrapper.addClassName("indicators-wrapper");
        textWrapper.setClassName("tab-title");
        getElement().appendChild(textWrapper);

        // TODO 20120816 mgeljic: implement subtitle

        indicatorsWrapper = getElement();

        closeElement.setClassName("v-shell-tab-close");
        closeElement.addClassName("icon-close");
        notificationBox.setClassName("v-shell-tab-notification");
        errorIndicator.setClassName("v-shell-tab-error");

        getElement().appendChild(closeElement);
        getElement().appendChild(notificationBox);
        getElement().appendChild(errorIndicator);

        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS | Event.TOUCHEVENTS);
        hideNotification();
        setHasError(false);
        // MGNLUI-786: Fixes tab label sizing issue in Chrome.
        setWidth("100px");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        bindHandlers();
    }

    private void bindHandlers() {
        touchDelegate.addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                final Element target = (Element) event.getNativeEvent().getEventTarget().cast();
                if (closeElement.isOrHasChild(target)) {
                    eventBus.fireEvent(new TabCloseEvent(tab));
                } else {
                    eventBus.fireEvent(new ActiveTabChangedEvent(tab));
                }
            }
        });
    }

    public void setTab(final MagnoliaTabWidget tab) {
        this.tab = tab;
    }

    public void updateCaption(final String caption) {
        textWrapper.setInnerText(caption);
        setWidth("");
    }

    public void setClosable(boolean isClosable) {
        closeElement.getStyle().setDisplay(isClosable ? Display.INLINE : Display.NONE);
    }

    public void updateNotification(final String text) {
        if (text != null && !text.isEmpty()) {
            notificationBox.getStyle().setDisplay(Display.INLINE_BLOCK);
            notificationBox.setInnerText(text);
        }
    }

    public void hideNotification() {
        notificationBox.getStyle().setDisplay(Display.NONE);
    }

    public void setHasError(boolean hasError) {
        errorIndicator.getStyle().setDisplay(hasError ? Display.INLINE_BLOCK : Display.NONE);
    }

    /**
     * @param eventBus
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
