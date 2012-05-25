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
package info.magnolia.ui.admincentral.framework;

import info.magnolia.ui.admincentral.app.AbstractAppActivity;
import info.magnolia.ui.admincentral.app.AppController;
import info.magnolia.ui.admincentral.app.AppDescriptor;
import info.magnolia.ui.admincentral.app.AppLifecycle;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.place.Place;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

/**
 * AppActivityMapper.
 * 
 * @version $Id$
 */
@SuppressWarnings("serial")
public class AppActivityMapper implements ActivityMapper {
    
    private AppController appController;
    
    private ComponentProvider componentProvider;

    private Map<AppDescriptor, AppContext> contextMap = new HashMap<AppDescriptor, AppContext>();
    
    @Inject
    public AppActivityMapper(AppController appController, ComponentProvider componentProvider) {
        this.appController = appController;
        this.componentProvider = componentProvider;
    }

    @Override
    public Activity getActivity(final Place place) {
        final Iterator<Map.Entry<AppDescriptor, AppContext>> it = contextMap.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<AppDescriptor, AppContext> entry = it.next();
            final AppDescriptor descriptor = entry.getKey();
            final AppContext context = entry.getValue();
            final Class<? extends Activity> clazz = descriptor.getMappedActivityClass(place.getClass());
            if (clazz != null) {
                Activity activity = context.getActivityForPlace(place.getClass());
                if (activity == null) {
                    activity = componentProvider.newInstance(clazz);
                    if (activity instanceof AbstractAppActivity) {
                        ((AbstractAppActivity<?>)activity).setName(descriptor.getName());
                    }
                    context.addActivityMapping(activity, place.getClass());
                }
                return activity;
            }
        }
        return null;
    }

    public void registerAppStart(final AppLifecycle lifecycle) {
        final AppDescriptor descriptor = appController.getAppDescriptor(lifecycle);
        AppContext context = contextMap.get(descriptor);
        if (context == null) {
            context = new AppContext();
            contextMap.put(descriptor, context);
        }
    }

    public void uregisterApp(final AppLifecycle lifecycle) {
        final AppDescriptor descriptor = appController.getAppDescriptor(lifecycle);
        contextMap.remove(descriptor);
    }
    
    private static class AppContext implements Serializable {
        
        private Map<Class<? extends Place>, Activity> placeActivityMap = new HashMap<Class<? extends Place>, Activity>();
        
        public AppContext() {
            super();
        }
        
        public Activity getActivityForPlace(final Class<? extends Place> placeClass) {
            return placeActivityMap.get(placeClass);
        }
        
        public void addActivityMapping(final Activity activity, final Class<? extends Place> placeClass) {
            placeActivityMap.put(placeClass, activity);
        }
    }
}
