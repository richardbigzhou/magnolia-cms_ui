/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.actionbar.widget;

import info.magnolia.ui.vaadin.gwt.client.actionbar.event.ActionTriggerEvent;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ui.Icon;
import com.vaadin.client.ui.aria.AriaHelper;

/**
 * The Class VAction, which displays a single action with label and icon within an action group.
 */
public class ActionbarItemWidget extends FocusWidget {

    private static final String CLASSNAME = "v-action";

    private final Element root = DOM.createElement("li");

    private final Element text = DOM.createSpan();

    // private final Element flyoutIndicator = DOM.createSpan();

    private final Element icon = DOM.createSpan();

    private final Icon iconImage;

    protected final ActionbarItem data;

    protected final EventBus eventBus;

    protected VActionbarGroup group;

    protected TouchDelegate delegate = new TouchDelegate(this);

    protected boolean isEnabled = true;

    /**
     * Instantiates a new action in action bar.
     *
     * @param data the data json object
     * @param eventBus the event bus
     * @param icon the icon
     *
     * Use {@link #VActionbarItem(VActionbarItemJSO, VActionbarGroup, EventBus)} instead.
     */
    @Deprecated
    public ActionbarItemWidget(ActionbarItem data, VActionbarGroup group, EventBus eventBus, Icon icon) {
        super();
        this.data = data;
        this.group = group;
        this.eventBus = eventBus;
        this.iconImage = icon;

        constructDOM();
        bindHandlers();
        update();
    }

    /**
     * Instantiates a new action in action bar.
     *
     * @param data the data json object
     * @param group the group
     * @param eventBus the event bus
     */
    public ActionbarItemWidget(ActionbarItem data, VActionbarGroup group, EventBus eventBus) {
        super();
        this.data = data;
        this.group = group;
        this.eventBus = eventBus;
        this.iconImage = null;

        constructDOM();
        bindHandlers();
        update();
    }

    private void constructDOM() {
        setElement(root);
        setStyleName(CLASSNAME);

        text.addClassName("v-text");
        icon.addClassName("v-icon");
        root.appendChild(iconImage == null ? icon : iconImage.getElement());
        root.appendChild(text);
        AriaHelper.bindCaption(this, text);
    }

    protected void bindHandlers() {

        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);

        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                addStyleName("mousedown");
            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                removeStyleName("mousedown");
            }
        }, MouseOutEvent.getType());

        addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                removeStyleName("mousedown");
                if (isEnabled) {
                    eventBus.fireEvent(new ActionTriggerEvent(data.getName(), ActionbarItemWidget.this));
                }
            }
        }, MouseUpEvent.getType());
        
        addDomHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode())
                    eventBus.fireEvent(new ActionTriggerEvent(data.getName(), ActionbarItemWidget.this));
            }
        }, KeyPressEvent.getType());
               
    }

    public String getName() {
        return data.getName();
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        update();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void update() {
        text.setInnerText(data.getLabel());
        if (data.getIconFontId() != null) {
            icon.setClassName("v-icon");
            if (data.getIconFontId() != null && !data.getIconFontId().isEmpty()) {
                icon.addClassName(data.getIconFontId());
            }
        } else if (iconImage != null) {
            iconImage.setUri(data.getResourceUrl());
        }

        if (isEnabled() && root.getClassName().contains(ApplicationConnection.DISABLED_CLASSNAME)) {
            root.removeClassName(ApplicationConnection.DISABLED_CLASSNAME);
        } else if (!isEnabled() && !root.getClassName().contains(ApplicationConnection.DISABLED_CLASSNAME)) {
            root.addClassName(ApplicationConnection.DISABLED_CLASSNAME);
        }
    }

    public ActionbarItem getData() {
        return data;
    }     
}
