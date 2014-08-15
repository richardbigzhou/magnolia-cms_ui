/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.autosuggest;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * StickPopPanel.
 */
public class VAutoSuggestToolTip extends SimplePanel {

    private Element container = DOM.createDiv();
    private WrapSimplePanel wrapSimplePanel;

    public VAutoSuggestToolTip() {
        setVisible(false);
        getElement().getStyle().setZIndex(Integer.MAX_VALUE);
        getElement().getStyle().setPosition(Position.ABSOLUTE);
    }

    public void showRelativeTo(Element relative, int top, int left) {
        if (relative != null) {
            if (!this.isAttached()) {
                container.getStyle().setWidth(0D, Unit.PX);
                container.getStyle().setHeight(0D, Unit.PX);
                relative.appendChild(container);
                wrapSimplePanel = WrapSimplePanel.wrap(container);
                wrapSimplePanel.add(this);
            }
            this.getElement().getStyle().setTop(top, Unit.PX);
            this.getElement().getStyle().setLeft(left, Unit.PX);
            this.setVisible(true);
        }
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (wrapSimplePanel != null) {
            wrapSimplePanel.removeFromParent();
        }
        container.removeFromParent();
    }
}
