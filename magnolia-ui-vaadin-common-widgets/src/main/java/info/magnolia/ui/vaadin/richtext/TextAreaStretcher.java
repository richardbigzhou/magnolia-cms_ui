/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.richtext;

import info.magnolia.ui.vaadin.gwt.client.richtext.TextAreaStretcherServerRpc;
import info.magnolia.ui.vaadin.gwt.client.richtext.TextAreaStretcherState;

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Extension that applies to text areas and rich-text editors in forms. It expands them to the full-size
 * of the forms horizontally and stretches to the bottom of the screen.
 */
public class TextAreaStretcher extends AbstractExtension {

    public TextAreaStretcher() {
        registerRpc(new TextAreaStretcherServerRpc() {
            @Override
            public void toggle(int initialWidth, int initialHeight) {
                getState().isCollapsed = !getState().isCollapsed;
                final Component parent = (Component) getParent();
                if (getState().isCollapsed) {
                    // Restore size info from the state.
                    parent.setWidth(getState().collapsedStateWidth);
                    parent.setHeight(getState().collapsedStateHeight);
                } else {

                    // We save the initial dimensions and clear size info from the state.
                    getState().collapsedStateHeight = parent.getHeight() + parent.getHeightUnits().toString();
                    getState().collapsedStateWidth = parent.getWidth() + parent.getWidthUnits().toString();
                    parent.setWidth("");
                    parent.setHeight("");
                }
            }
        });
    }

    public static void extend(Field field) {
        new TextAreaStretcher().extend((AbstractClientConnector) field);
    }

    @Override
    protected TextAreaStretcherState getState() {
        return (TextAreaStretcherState) super.getState();
    }
}
