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
package info.magnolia.m5admincentral.app;

import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.place.Place;

import java.util.HashMap;
import java.util.Map;

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

    private String icon;

    private String category;
    
    private Class<? extends AppLifecycle> appClass;

    private Map<Class<? extends Place>, Class<? extends Activity>> activityMappings = new HashMap<Class<? extends Place>, Class<? extends Activity>>();

    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
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

    public void addActivityMapping(Class<? extends Place> place, Class<? extends Activity> activity) {
        activityMappings.put(place, activity);
    }

    public Map<Class<? extends Place>, Class<? extends Activity>> getActivityMappings() {
        return activityMappings;
    }
}
