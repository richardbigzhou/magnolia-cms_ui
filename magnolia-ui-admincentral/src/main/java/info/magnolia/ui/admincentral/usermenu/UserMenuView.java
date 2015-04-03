/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.admincentral.usermenu;

import info.magnolia.ui.api.view.View;

/**
 * View displaying the current logged in user and providing user related actions.
 * Allows registering actions.
 */
public interface UserMenuView extends View {

    void setListener(Listener listener);

    /**
     * Adds an action to the user menu with given action name as configured and label.
     * 
     * @deprecated since 5.2.4 icons are supported in user menu so one should use {@link #addAction(String, String, String)}.
     */
    @Deprecated
    void addAction(String name, String label);

    /**
     * Adds an action to the user menu with given action name as configured, label and icon.
     */
    void addAction(String name, String label, String icon);

    void setCaption(String caption);

    /**
     * Listener callback interface for presenter.
     */
    interface Listener {
        void onAction(String actionName);
    }
}
