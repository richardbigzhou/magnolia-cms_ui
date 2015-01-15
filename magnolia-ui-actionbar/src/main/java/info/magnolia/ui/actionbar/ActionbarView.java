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
package info.magnolia.ui.actionbar;

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;

import com.vaadin.server.Resource;

/**
 * Base interface for an action bar view.
 */
public interface ActionbarView extends View {

    void setListener(Listener listener);

    void addSection(String sectionName, String label);

    void removeSection(String sectionName);

    void setPreview(final Resource previewResource);

    void addAction(ActionbarItem action, String sectionName);

    void removeAction(String actionName);


    // ENABLE / DISABLE
    void setActionEnabled(String actionName, boolean isEnabled);

    void setActionEnabled(String actionName, String sectionName, boolean isEnabled);

    void setGroupEnabled(String groupName, boolean isEnabled);

    void setGroupEnabled(String groupName, String sectionName, boolean isEnabled);

    // SHOW / HIDE SECTIONS
    void setSectionVisible(String sectionName, boolean isVisible);

    boolean isSectionVisible(String sectionName);

    void setOpen(boolean isOpen);

    /**
     * Base interface for an action bar listener.
     */
    interface Listener {

        /**
         * Event handler invoked on clicking an item in the action bar.
         *
         * @param actionToken the action token
         */
        void onActionbarItemClicked(String actionToken);
    }
}
