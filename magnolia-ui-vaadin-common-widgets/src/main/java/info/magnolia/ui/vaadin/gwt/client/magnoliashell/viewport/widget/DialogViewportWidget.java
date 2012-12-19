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



import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;


/**
 * Dialogs viewport.
 * 
 */
public class DialogViewportWidget extends ViewportWidget {

    private Element curtain;

    public DialogViewportWidget() {
        getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }

    @Override
    public void doSetActive(boolean active) {
        super.doSetActive(active);
        if (active) {
            getElement().getStyle().clearVisibility();
            setCurtainVisible(true);
        } else {
            getElement().getStyle().setVisibility(Visibility.HIDDEN);
            setCurtainVisible(false);
        }
    }

    @Override
    public void doSetVisibleApp(Widget w) {
        if (w != null) {
            setActive(true);
        }
        super.doSetVisibleApp(w);
    }

    /* CURTAIN */

    public Element getCurtain() {
        if (curtain == null) {
            curtain = DOM.createDiv();
            curtain.setClassName("v-curtain v-curtain-black");
        }
        return curtain;
    }

    public void setCurtainVisible(boolean visible) {
        // NO EFFORT HERE FOR TRANSITION DELEGATES SINCE DIALOG VIEWPORT IS TO GO
        doSetCurtainVisible(visible);
    }

    /**
     * Default non-transitioning behavior, accessible to transition delegates as a fall back.
     */
    void doSetCurtainVisible(boolean visible) {
        if (visible && getCurtain().getParentElement() != getElement()) {
            getElement().appendChild(getCurtain());
        } else if (!visible && getCurtain().getParentElement() == getElement()) {
            getElement().removeChild(getCurtain());
        }
    }
}