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

import info.magnolia.ui.vaadin.gwt.client.icon.connector.BadgeIconState;

import com.vaadin.ui.AbstractComponent;

/**
 * The BadgeIcon is a lightweight component that outputs a simple indicator badge icon to display
 * notifications counters for instance. The client-side implementation is scalable and only relies
 * on browser support for rounded corners.
 */
public class BadgeIcon extends AbstractComponent {

    /**
     * Creates a new badge icon, with default style.
     */
    public BadgeIcon() {
    }

    /**
     * Creates a new badge icon with specific size, fill and stroke css colors, and optional
     * outline.
     *
     * @param size the size of the inner badge shape, not including the outline, in pixels
     * @param fill the fill color, as a css-compliant color code
     * @param stroke the stroke color used for text and outline, as a css-compliant color code
     * @param outline whether the outline should be rendered.
     */
    public BadgeIcon(int size, String fill, String stroke, boolean outline) {
        getState().size = size;
        getState().fillColor = fill;
        getState().strokeColor = stroke;
        getState().outline = outline;
    }

    @Override
    protected BadgeIconState getState() {
        return (BadgeIconState) super.getState();
    }

    public void setValue(int value) {
        getState().value = value;
    }
}
