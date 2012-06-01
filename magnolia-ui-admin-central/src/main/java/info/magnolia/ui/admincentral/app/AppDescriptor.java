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
package info.magnolia.ui.admincentral.app;

import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Describes an app.
 *
 * @version $Id$
 */
public class AppDescriptor {

    /**
     * unique identifier.
     */
    private String name;

    private String label;

    private boolean enabled = true;

    private String icon;

    private Class<? extends AppLifecycle> appClass;

    private List<PlaceActivityMapping> activityMappings = new ArrayList<PlaceActivityMapping>();

    private List<WorkbenchDefinition> workbenches = new ArrayList<WorkbenchDefinition>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Class<? extends AppLifecycle> getAppClass() {
        return appClass;
    }

    public void setAppClass(Class<? extends AppLifecycle> appClass) {
        this.appClass = appClass;
    }

    public List<PlaceActivityMapping> getActivityMappings() {
        return activityMappings;
    }

    public void addActivityMapping(PlaceActivityMapping mapping) {
        activityMappings.add(mapping);
    }

    public void addWorkbench(WorkbenchDefinition workbenchDefinition) {
        workbenches.add(workbenchDefinition);
    }

    public List<WorkbenchDefinition> getWorkbenches() {
        return workbenches;
    }

    public Class<? extends Activity> getMappedActivityClass(final Class<? extends Place> placeClass) {
        final Iterator<PlaceActivityMapping> it = activityMappings.iterator();
        Class<? extends Activity> result = null;
        while (it.hasNext() && result == null) {
            final PlaceActivityMapping mapping = it.next();
            if (mapping.getPlace().equals(placeClass)) {
                result = mapping.getActivity();
            }
        }
        return result;
    }
}
