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

import com.google.gwt.user.client.*;

import com.googlecode.mgwt.dom.client.event.touch.*;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import info.magnolia.ui.widget.actionbar.gwt.client.event.ActionTriggerEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

    //private final Element flyoutIndicator = DOM.createSpan();

    private final Icon icon;

    private final VActionbarItemJSO data;

    private final EventBus eventBus;

    private HandlerRegistration handler;

    private VActionbarGroup group;

    TouchDelegate delegate = new TouchDelegate(this);

    HandlerRegistration touchEndHandler;

    /**
     * Instantiates a new action in action bar.
     * 
     * @param data the data json object
     * @param eventBus the event bus
     * @param icon the icon
     * @param cssClasses css classes to be added to the item
     */
    public VActionbarItem(VActionbarItemJSO data, VActionbarGroup group, EventBus eventBus, Icon icon, String cssClasses) {
        super();
        this.data = data;
        this.group = group;
        this.eventBus = eventBus;
        this.icon = icon;

        constructDOM(cssClasses);
        bindHandlers();
        update();
    }

    private void constructDOM(String cssClasses) {
        setElement(root);
        setStyleName(CLASSNAME);
        addStyleName(cssClasses);

        text.addClassName("v-text");
        if (icon != null) {
            root.appendChild(icon.getElement());
        }
        root.appendChild(text);

        /*flyoutIndicator.addClassName("v-flyout-indicator");
        flyoutIndicator.setInnerText("v"); //TODO: CLZ - add flyout icon. Toggle it based on row state.
        root.appendChild(flyoutIndicator);     */
    }

    private void bindHandlers() {
        /* handler = addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
               if (data.isEnabled()) {
                    eventBus.fireEvent(new ActionTriggerEvent(data.getName(), VActionbarItem.this));
                }
            }
        }, ClickEvent.getType());
*/

        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);

        delegate.addTouchStartHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent event) {

                // Expand group row on timeout.
                final Timer t = new Timer() {
                    public void run() {

                        group.toggleHorizontalCollapse();

                        touchEndHandler.removeHandler();
                    }
                };
                t.schedule(400);

                // Fire standard action on click.
                touchEndHandler  = delegate.addTouchEndHandler(new com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler() {
                    @Override
                    public void onTouchEnd(com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent event) {

                        t.cancel();
                        touchEndHandler.removeHandler();

                        if (data.isEnabled()) {
                            eventBus.fireEvent(new ActionTriggerEvent(data.getName(), VActionbarItem.this));
                        }

                        group.horizontalCollapse();
                    }
                });

            }
        });
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
        if (icon != null) {
            icon.setUri(data.getIcon());
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
