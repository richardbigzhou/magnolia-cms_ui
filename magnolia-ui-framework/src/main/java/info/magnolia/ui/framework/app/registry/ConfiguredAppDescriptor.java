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
package info.magnolia.ui.framework.app.registry;

import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.SubAppDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes an app.
 */
public class ConfiguredAppDescriptor implements AppDescriptor {

    private static final long serialVersionUID = 1L;

    /**
     * unique identifier.
     */
    private String name;

    private String label;

    private boolean enabled = true;

    private String icon;

    private Class<? extends App> appClass;

    private Map<String, SubAppDescriptor> subApps = new HashMap<String, SubAppDescriptor>();

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
    public boolean equals(Object o) {
        return o instanceof ConfiguredAppDescriptor && this.getName().equals(((ConfiguredAppDescriptor) o).getName());
    }

    @Override
    public int hashCode() {
        return (this.getName() == null) ? 0 : this.getName().hashCode();
    }

}
