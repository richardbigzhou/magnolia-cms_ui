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
package info.magnolia.ui.widget.actionbar;

import info.magnolia.ui.framework.view.View;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;


/**
 * Base interface for an action bar view.
 */
public interface ActionbarView extends View {

    void setListener(Listener listener);

    void addSection(String sectionName, String label);

    void addAction(String actionName, String label, Resource icon, String groupName, String sectionName);

    void addPreview(Component component, String sectionName);

    // ENABLE / DISABLE
    void enable(String actionName);

    void enable(String actionName, String groupName);

    void enable(String actionName, String groupName, String sectionName);

    void enableGroup(String groupName);

    void enableGroup(String groupName, String sectionName);

    void disable(String actionName);

    void disable(String actionName, String groupName);

    void disable(String actionName, String groupName, String sectionName);

    void disableGroup(String groupName);

    void disableGroup(String groupName, String sectionName);

    // SHOW / HIDE SECTIONS
    void showSection(String sectionName);

    void hideSection(String sectionName);

    /**
     * Base interface for an action bar listener.
     */
    interface Listener {

        /**
         * Returns the view for the parent presenter to lay it out somewhere.
         * 
         * @return the actionbar view
         */
        ActionbarView start();

        /**
         * Event handler invoked on clicking an item in the action bar.
         * 
         * @param actionName the action name
         */
        void onActionbarItemClicked(String actionName);

    }
}