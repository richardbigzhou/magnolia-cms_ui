/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.widget.actionbar.gwt.client;

import info.magnolia.ui.widget.actionbar.gwt.client.event.ActionTriggerEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.ui.Icon;


/**
 * The Class VAction, which displays a single action with label and icon within an action group.
 */
public class VActionbarItem extends Widget {

    private static final String CLASSNAME = "v-action";

    private final Element root = DOM.createElement("li");

    private final Element text = DOM.createSpan();

    private final Element icon = DOM.createSpan();

    private final Icon iconImage;

    private final VActionbarItemJSO data;

    private final EventBus eventBus;

    private HandlerRegistration handler;

    /**
     * Instantiates a new action in action bar.
     * 
     * @param data the data json object
     * @param eventBus the event bus
     * @param icon the icon
     * 
     * Use {@link #VActionbarItem(VActionbarItemJSO, EventBus)} instead.
     */
    @Deprecated
    public VActionbarItem(VActionbarItemJSO data, EventBus eventBus, Icon icon) {
        super();
        this.data = data;
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
     * @param eventBus the event bus
     */
    public VActionbarItem(VActionbarItemJSO data, EventBus eventBus) {
        super();
        this.data = data;
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
        if (iconImage == null) {
            root.appendChild(icon);
        } else {
            root.appendChild(iconImage.getElement());
        }
        root.appendChild(text);
    }

    private void bindHandlers() {
        handler = addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (data.isEnabled()) {
                    eventBus.fireEvent(new ActionTriggerEvent(data.getName(), VActionbarItem.this));
                }
            }
        }, ClickEvent.getType());
    }

    public String getName() {
        return data.getName();
    }

    public void setEnabled(boolean enabled) {
        data.setEnabled(enabled);
        update();
    }

    public void update() {
        text.setInnerText(data.getLabel());

        if (data.getIcon() != null) {
            icon.setClassName("v-icon");
            icon.addClassName(data.getIcon());
        } else if (iconImage != null) {
            iconImage.setUri(data.getIcon());
        }

        if (data.isEnabled() && root.getClassName().contains(ApplicationConnection.DISABLED_CLASSNAME)) {
            root.removeClassName(ApplicationConnection.DISABLED_CLASSNAME);
            bindHandlers();
        } else if (!data.isEnabled() && !root.getClassName().contains(ApplicationConnection.DISABLED_CLASSNAME)) {
            root.addClassName(ApplicationConnection.DISABLED_CLASSNAME);
            handler.removeHandler();
        }
    }

}
