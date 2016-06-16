/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.TransitionDelegate;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * An overlay that displays the open app in the shell on top of each other.
 *
 * @param <T> type of {@link TransitionDelegate} which backs up a concrete viewport widget
 */
public class ViewportWidget<T extends TransitionDelegate> extends FlowPanel {

    private Widget visibleChild;

    private T transitionDelegate;

    public ViewportWidget() {
        super();
        addStyleName("v-viewport");
    }

    public T getTransitionDelegate() {
        return transitionDelegate;
    }

    public void setTransitionDelegate(T transitionDelegate) {
        this.transitionDelegate = transitionDelegate;
    }

    public Widget getVisibleChild() {
        return visibleChild;
    }

    public void showChild(Widget w) {
        if (w != visibleChild) {
            transitionDelegate.setVisibleChild(w);
            visibleChild = w;
        }
    }

    public void removeChild(Widget w) {
        removeChildNoTransition(w);
    }

    /**
     * Default non-transitioning behavior, accessible to transition delegates as a fall back.
     */
    public void showChildNoTransition(Widget w) {
        if (visibleChild != null) {
            visibleChild.setVisible(false);
        }
        w.setVisible(true);
    }

    public void removeChildNoTransition(Widget w) {
        super.remove(w);
    }


}
