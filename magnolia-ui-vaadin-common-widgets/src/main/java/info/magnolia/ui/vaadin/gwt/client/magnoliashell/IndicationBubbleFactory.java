/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.shape.Ellipse;

/**
 * Factory for generating the pads for the indicators in the {@link MainLauncher}.
 */
public class IndicationBubbleFactory {

    private static final int BASE_WIDTH = 16;

    private static final int BASE_HEIGHT = 16;

    private static final int WIDTH_INCREMENT = 10;

    public static void createBubbleForValue(int value, final DrawingArea canvas) {
        canvas.clear();
        canvas.setWidth(BASE_WIDTH + (digitCount(value) - 1) * WIDTH_INCREMENT);
        canvas.setHeight(BASE_HEIGHT);
        int vRadius = (int) (canvas.getHeight() / 2d);
        int hRadius = (int) (canvas.getWidth() / 2d);
        final Ellipse ellipse = new Ellipse(hRadius, vRadius, hRadius, vRadius);
        ellipse.setStrokeOpacity(0);
        canvas.add(ellipse);
    }

    private static int digitCount(int value) {
        return String.valueOf(value).length();
    }

}
