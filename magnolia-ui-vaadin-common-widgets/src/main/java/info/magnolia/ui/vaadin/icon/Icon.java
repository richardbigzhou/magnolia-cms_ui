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
package info.magnolia.ui.vaadin.icon;

import info.magnolia.ui.vaadin.gwt.client.icon.connector.IconState;

import com.vaadin.ui.AbstractComponent;

/**
 * The Icon is a lightweight component that outputs a simple scalable icon. The client-side
 * implementation is based on the icon font technique, which means it only allows for monochromatic
 * icons. For multilayer icons, please head to the CompositeIcon.
 */
public class Icon extends AbstractComponent {

    public transient static final String COLOR_ERROR = "#9a3332";

    public transient static final String COLOR_WARNING = "#ffbf28";

    public transient static final String COLOR_INFO = "#999";

    public transient static final String COLOR_GREEN_BADGE = "#689600";

    public transient static final String COLOR_HELP = "#4b8e9e";

    public transient static final int SIZE_DEFAULT = 24;

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
    protected IconState getState(boolean markDirty) {
        return (IconState) super.getState(markDirty);
    }

    @Override
    protected IconState getState() {
        return (IconState) super.getState();
    }

    public String getIconName() {
        return getState(false).iconName;
    }

    public void setIconName(String iconName) {
        getState().iconName = iconName;
    }

    public int getSize() {
        return getState(false).size;
    }

    public void setSize(int size) {
        getState().size = size;
    }

    public String getColor() {
        return getState(false).color;
    }

    public void setColor(String color) {
        getState().color = color;
    }

}
