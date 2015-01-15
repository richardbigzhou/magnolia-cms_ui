/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * The GwtIcon widget.
 */
public class IconWidget extends Widget {

    private static final String CLASSNAME = "icon";

    private static final int SIZE_DEFAULT = 24;

    private final Element root = DOM.createSpan();

    private String iconName;

    public IconWidget() {
        setElement(root);
        setStylePrimaryName(CLASSNAME);
        initDefaultStyles();
    }

    private void initDefaultStyles() {
        Style style = root.getStyle();
        style.setFontSize(SIZE_DEFAULT, Unit.PX);
        style.setProperty("lineHeight", "1");
    }

    public void setIconName(String iconName) {
        if (this.iconName != null) {
            removeStyleDependentName(this.iconName);
        }
        addStyleDependentName(iconName);
        this.iconName = iconName;
    }

    public void setSize(int value) {
        if (value >= 0) {
            root.getStyle().setFontSize(value, Unit.PX);
        } else {
            root.getStyle().setFontSize(1, Unit.EM);
        }
    }

    public void setColor(String value) {
        root.getStyle().setColor(value);
    }

}
