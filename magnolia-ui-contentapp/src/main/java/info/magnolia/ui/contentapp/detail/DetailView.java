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
package info.magnolia.ui.contentapp.detail;

import info.magnolia.ui.api.view.View;

import com.vaadin.ui.Component;

/**
 * ItemView.
 *
 * @see DetailViewImpl
 */
public interface DetailView extends View {

    void setItemView(Component formView, ViewType viewType);

    /**
     * Enumeration for edit or preview types.
     * <ul>
     * <li>edit
     * <li>view (preview)
     * </ul>
     */
    enum ViewType {
        EDIT("edit"),
        VIEW("view");

        private String text;

        ViewType(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static ViewType fromString(String text) {
            if (text != null && !text.isEmpty()) {
                for (ViewType type : ViewType.values()) {
                    if (text.equalsIgnoreCase(type.text)) {
                        return type;
                    }
                }
            }
            return defaultViewType();

        }

        private static ViewType defaultViewType() {
            return EDIT;
        }

    }

    void setListener(Listener listener);

    void refresh();

    ViewType getViewType();

    void setWide(boolean isWide);

    /**
     * Listener for the ContentView.
     */
    public interface Listener {

    }
}
