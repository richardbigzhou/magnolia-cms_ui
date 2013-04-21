/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.loading.LoadingPane;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ViewportCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.TransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * An overlay that displays the open app in the shell on top of each other.
 */
public class ViewportWidget extends FlowPanel {

    private final LoadingPane loadingPane = new LoadingPane();

    private Widget visibleChild;

    private TransitionDelegate transitionDelegate;

    private boolean active;

    private boolean closing;

    public ViewportWidget() {
        super();
        addStyleName("v-viewport");
        loadingPane.appendTo(this);
        DOM.sinkEvents(this.getElement(), Event.TOUCHEVENTS);
        new TouchDelegate(this).addTouchEndHandler(new TouchEndHandler() {
            @Override
            public void onTouchEnd(TouchEndEvent event) {
                final Element target = event.getNativeEvent().getEventTarget().cast();
                if (target == getElement()) {
                    fireEvent(new ViewportCloseEvent(ViewportType.SHELL_APP));
                }
            }
        });
    }
    
    public void showLoadingPane() {
        loadingPane.show();
    }

    public void hideLoadingPane() {
        loadingPane.hide();
    }

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public TransitionDelegate getTransitionDelegate() {
        return transitionDelegate;
    }

    public void setTransitionDelegate(TransitionDelegate transitionDelegate) {
        this.transitionDelegate = transitionDelegate;
    }

    /* VIEWPORT ACTIVATION */

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        transitionDelegate.setActive(this, active);
        this.active = active;
    }

    /**
     * Default non-transitioning behavior, accessible to transition delegates as a fall back.
     */
    public void doSetActive(boolean active) {
        setVisible(active);
    }

    /* CHANGING VISIBLE APP */

    public Widget getVisibleChild() {
        return visibleChild;
    }

    public void setVisibleChild(Widget w) {
        if (w != visibleChild) {
            if (isActive()) {
                transitionDelegate.setVisibleApp(this, w);
            } else {
                setChildVisibleNoTransition(w);
            }
            visibleChild = w;
        }
    }

    /**
     * Default non-transitioning behavior, accessible to transition delegates as a fall back.
     */
    public void setChildVisibleNoTransition(Widget w) {
        if (visibleChild != null) {
            visibleChild.setVisible(false);
        }
        w.setVisible(true);
    }

    public void removeWidget(Widget w) {
        removeWithoutTransition(w);
    }

    public void removeWithoutTransition(Widget w) {
        super.remove(w);
    }

    public HandlerRegistration addCloseHandler(ViewportCloseEvent.Handler handler) {
        return addHandler(handler, ViewportCloseEvent.TYPE);
    }

}
