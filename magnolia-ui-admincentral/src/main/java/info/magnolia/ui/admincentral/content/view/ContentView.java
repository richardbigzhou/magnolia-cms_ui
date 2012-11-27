/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.content.view;

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainer;

import com.vaadin.data.Item;


/**
 * UI component that displays content (from JCR).
 */
public interface ContentView extends View {

    /**
     * Enumeration for the default view types.
     * <ul>
     * <li>tree
     * <li>list
     * <li>thumbnail
     * <li>search
     * </ul>
     */
    public enum ViewType {
        LIST("list"),
        TREE("tree"),
        THUMBNAIL("thumbnail"),
        SEARCH("search");

        private final String text;

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
            else {
                return defaultViewType();
            }
            throw new IllegalArgumentException("No view type could be found for " + text);
        }

        private static ViewType defaultViewType() {
            return TREE;
        }
    }

    void setListener(Listener listener);

    /**
     * Selects the item with given path in the content view.
     * 
     * @param path relative to the tree root, must start with '/'
     */
    void select(String path);

    void refresh();

    AbstractJcrContainer getContainer();

    ViewType getViewType();

    /**
     * Listener for the ContentView.
     */
    public interface Listener {

        void onItemSelection(Item item);

        void onDoubleClick(Item item);
        
        void onItemEdited(Item item);

    }
}
