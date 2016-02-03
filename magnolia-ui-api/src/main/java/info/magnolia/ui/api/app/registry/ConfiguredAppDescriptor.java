/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.api.app.registry;

import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubAppDescriptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple implementation of {@link AppDescriptor}.
 */
public class ConfiguredAppDescriptor implements AppDescriptor {

    private String name;

    private String label;

    private boolean enabled = true;

    private String icon;

    private String theme;

    private Class<? extends App> appClass;

    private Map<String, SubAppDescriptor> subApps = new LinkedHashMap<String, SubAppDescriptor>();

    private AccessDefinition permissions;

    private String i18nBasename;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public Class<? extends App> getAppClass() {
        return appClass;
    }

    public void setAppClass(Class<? extends App> appClass) {
        this.appClass = appClass;
    }

    @Override
    public Map<String, SubAppDescriptor> getSubApps() {
        return subApps;
    }

    public void setSubApps(Map<String, SubAppDescriptor> subApps) {
        this.subApps = subApps;
    }

    public void addSubApp(SubAppDescriptor subApp) {
        subApps.put(subApp.getName(), subApp);
    }

    @Override
    public AccessDefinition getPermissions() {
        return permissions;
    }

    public void setPermissions(AccessDefinition permissions) {
        this.permissions = permissions;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }
}
