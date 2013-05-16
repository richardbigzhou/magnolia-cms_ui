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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;
import com.vaadin.client.Util;

/**
 * Shell apps viewport client side.
 */
public class ShellAppsViewportWidget extends ViewportWidget {

    private Listener listener;

    private boolean active;

    /**
     * Listener interface for {@link ShellAppsViewportWidget}.
     */
    public interface Listener {

        void onShellAppLoaded(Widget shellAppWidget);

        void curtainClicked();
    }

    public ShellAppsViewportWidget(final Listener listener) {
        this.listener = listener;
        new TouchDelegate(this).addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                final Element target = event.getNativeEvent().getEventTarget().cast();
                if (target.isOrHasChild(getElement())) {
                    listener.curtainClicked();
                }
            }
        });
        DOM.sinkEvents(getElement(), Event.TOUCHEVENTS | Event.MOUSEEVENTS);
    }

    public boolean isActive() {
        return active;
    }

    public void setActiveNoTransition(boolean isActive) {
        setVisible(isActive);
        this.active = isActive;
    }

    public void setActive(boolean active) {
        getTransitionDelegate().setActive(this, active);
        this.active = active;
    }

    public void onShellAppLoaded(Element element) {
        Widget w = Util.findWidget(element, null);
        if (w != null) {
            listener.onShellAppLoaded(w);
        }
    }
}
