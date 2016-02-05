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
package info.magnolia.ui.api.app.launcherlayout;

import info.magnolia.i18nsystem.AbstractI18nKeyGenerator;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Key generator for {@link AppLauncherGroupDefinition}.
 */
public class AppLauncherGroupDefinitionKeyGenerator extends AbstractI18nKeyGenerator<AppLauncherGroupDefinition> {

    /**
     * @deprecated since 5.4.5. Should not be exposed.
     */
    @Deprecated
    public static final String APPLAUNCHER_PREFIX = "app-launcher";
    private static final String APP_LAUNCHER_LAYOUT = "appLauncherLayout";
    private static final String GROUPS = "groups";

    @Override
    protected void keysFor(List<String> list, AppLauncherGroupDefinition group, AnnotatedElement el) {
        String groupName = group.getName();
        addKey(list, false, APP_LAUNCHER_LAYOUT, GROUPS, groupName, fieldOrGetterName(el));
        addKey(list, APPLAUNCHER_PREFIX, groupName, fieldOrGetterName(el)); //deprecated

    }

}
