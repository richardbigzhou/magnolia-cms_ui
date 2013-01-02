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
package info.magnolia.ui.vaadin.icon;

import info.magnolia.ui.vaadin.gwt.client.icon.VIcon;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * The Icon is a lightweight component that outputs a simple scalable icon. The client-side
 * implementation is based on the icon font technique, which means it only allows for monochromatic
 * icons. For multilayer icons, please head to the CompositeIcon.
 */
@ClientWidget(value = VIcon.class, loadStyle = LoadStyle.EAGER)
public class Icon extends AbstractComponent {

    public static final String COLOR_ERROR = "#9a3332";

    public static final String COLOR_WARNING = "#ffbf28";

    public static final String COLOR_INFO = "#999";

    public static final String COLOR_GREEN_BADGE = "#689600";

    public static final String COLOR_HELP = "#4b8e9e";

    public static final int SIZE_DEFAULT = 24;

    private String iconName;

    private int size;

    private String color;

    public Icon(String iconName) {
        this(iconName, SIZE_DEFAULT);
    }

    public Icon(String iconName, int size) {
        this(iconName, size, null);
    }

    public Icon(String iconName, String color) {
        this(iconName, SIZE_DEFAULT, color);
    }

    public Icon(String iconName, int size, String color) {
        setIconName(iconName);
        setSize(size);
        setColor(color);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("iconName", iconName);
        if (size != SIZE_DEFAULT) {
            target.addAttribute("size", size);
        }
        if (color != null) {
            target.addAttribute("color", color);
        }
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
