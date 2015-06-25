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
package info.magnolia.ui.api.app.launcherlayout;

import info.magnolia.cms.security.User;

/**
 * Manages the launcher layout displayed in the app launcher. Fires an {@link AppLauncherLayoutChangedEvent} on the
 * system event bus when the layout changes.
 *
 * @see AppLauncherLayout
 * @see AppLauncherLayoutChangedEvent
 * @see AppLauncherLayoutChangedEventHandler
 */
public interface AppLauncherLayoutManager {

    /**
     * The same functionality as {@link #getLayoutForUser(info.magnolia.cms.security.User)} provided
     * for the user currently set in the {@link info.magnolia.context.Context}.
     */
    AppLauncherLayout getLayoutForCurrentUser();

    /**
     * Returns the {@link AppLauncherLayout} for the specified user. The following principles apply:
     * <ul>
     * <li>empty groups or groups where the current user doesn't have access to any of the apps are not returned;</li>
     * <li>disabled apps, apps that the user does not have access to and apps that are not present in the {@link info.magnolia.ui.api.app.registry.AppDescriptorRegistry} are not included;</li>
     * <li>the returned object is also populated with {@link info.magnolia.ui.api.app.AppDescriptor} references for quick access.</li>
     * </ul>
     *
     * @param user user object
     * @return app-launcher layout which corresponds to the provided user
     */
    AppLauncherLayout getLayoutForUser(User user);

    /**
     * Called to update the launcher layout when it has been changed in the repository.
     */
    void setLayout(AppLauncherLayoutDefinition layout);
}
