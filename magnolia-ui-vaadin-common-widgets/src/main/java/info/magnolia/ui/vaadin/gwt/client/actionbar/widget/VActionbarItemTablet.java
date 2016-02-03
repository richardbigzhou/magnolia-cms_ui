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
package info.magnolia.ui.vaadin.gwt.client.actionbar.widget;

import info.magnolia.ui.vaadin.gwt.client.actionbar.event.ActionTriggerEvent;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.vaadin.client.ui.Icon;

/**
 * The Class VActionbarItemTablet, which displays a single tablet action.
 */
public class VActionbarItemTablet extends ActionbarItemWidget {

    private boolean holdingTouch;

    private Timer holdTimer;

    private int row;

    private int column;

    public VActionbarItemTablet(ActionbarItem data, VActionbarGroup group, EventBus eventBus, Icon icon) {
        super(data, group, eventBus, icon);
        initialize();
    }

    public VActionbarItemTablet(ActionbarItem data, VActionbarGroup group, EventBus eventBus) {
        super(data, group, eventBus);
        initialize();
    }

    public void setRow(int row) {
        removeStyleName("row-" + this.row);
        addStyleName("row-" + row);
        this.row = row;
    }

    public void setColumn(int column) {
        removeStyleName("col-" + this.column);
        addStyleName("col-" + column);
        this.column = column;
    }

    private void initialize() {
        // Expand group row on timeout.
        holdTimer = new Timer() {

            @Override
            public void run() {
                holdingTouch = true;
                removeStyleName("hover");
                group.toggleHorizontal();
            }
        };
    }

    @Override
    protected void bindHandlers() {

        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);

        delegate.addTouchStartHandler(new TouchStartHandler() {

            @Override
            public void onTouchStart(com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent event) {
                holdingTouch = false;
                addStyleName("hover");
                if (column == 0) {
                    holdTimer.schedule(400);
                }
            }
        });

        delegate.addTouchEndHandler(new com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler() {

            @Override
            public void onTouchEnd(com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent event) {
                removeStyleName("hover");
                holdTimer.cancel();

                if (!holdingTouch) {
                    eventBus.fireEvent(new ActionTriggerEvent(data.getName(), VActionbarItemTablet.this));
                    group.setOpenHorizontally(false);
                }
            }
        });

    }
}
