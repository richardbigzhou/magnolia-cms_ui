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
package info.magnolia.ui.framework.app;

import javax.inject.Inject;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.activity.ActivityMapperImpl;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLauncherLayout;
import info.magnolia.ui.framework.event.EventBus;

/**
 * AppActivityMapper.
 *
 * @version $Id$
 */
public class AppActivityMapper extends ActivityMapperImpl {

    @Inject
    public AppActivityMapper(ComponentProvider componentProvider, AppLauncherLayout appLauncherLayout, EventBus eventBus) {
        super(componentProvider);
        super.setLongLivingActivities(true);

        // Add mappings for all places provided by apps
        for (AppCategory category : appLauncherLayout.getCategories()) {
            for (AppDescriptor descriptor : category.getApps()) {
                for (PlaceActivityMapping mapping : descriptor.getActivityMappings()) {
                    super.addMapping(mapping.getPlace(), mapping.getActivity());
                }
            }
        }

        // When an app stops we remove all its activity instances, when it's started again new fresh instances will be created
        eventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                for (PlaceActivityMapping mapping : event.getAppDescriptor().getActivityMappings()) {
                    removeActivityInstanceForPlace(mapping.getPlace());
                }
            }
        });
    }
}
