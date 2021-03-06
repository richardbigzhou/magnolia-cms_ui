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
package info.magnolia.ui.vaadin.gwt.client.icon.widget;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * The GwtBadgeIcon widget.
 */
public class BadgeIconWidget extends Widget {

    private static final String CLASSNAME = "badge-icon";

    private final Element root = DOM.createSpan();

    private final Element text = DOM.createSpan();

    public BadgeIconWidget() {
        setElement(root);
        setStylePrimaryName(CLASSNAME);
        text.addClassName("text");
        root.appendChild(text);
    }

    public void setValue(int value) {
        String s = String.valueOf(value);
        setVisible(value != 0);
        if (value > 99) {
            s = "99+";
        }
        text.setInnerHTML(s);
    }

    public void setSize(int value) {
        root.getStyle().setFontSize(value, Unit.PX);
    }

    public void setFillColor(String value) {
        root.getStyle().setBackgroundColor(value);
    }

    public void setStrokeColor(String value) {
        root.getStyle().setColor(value);
        root.getStyle().setBorderColor(value);
    }

    public void setOutline(boolean outline) {
        if (outline) {
            root.getStyle().setBorderWidth(0.13, Unit.EM);
        } else {
            root.getStyle().clearBorderWidth();
        }
    }

}
