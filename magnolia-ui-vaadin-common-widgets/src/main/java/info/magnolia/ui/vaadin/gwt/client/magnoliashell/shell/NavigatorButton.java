/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.icon.widget.BadgeIconWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * NavigatorButton.
 */
public class NavigatorButton extends FlowPanel {

    private final BadgeIconWidget indicator = new BadgeIconWidget();

    private final TouchDelegate delegate = new TouchDelegate(this);

    public NavigatorButton(final ShellAppType type) {
        super();
        addStyleName("btn-shell");
        Element root = getElement();
        root.setId("btn-" + type.getCssClass());
        root.addClassName("icon-" + type.getCssClass());

        indicator.setFillColor("#fff");
        indicator.setStrokeColor("#689600");
        indicator.setOutline(true);
        root.appendChild(indicator.getElement());

        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS);
    }

    public void setIndication(int indication) {
        indicator.setValue(indication);
    }

    public HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
        return delegate.addTouchEndHandler(handler);
    }
}
