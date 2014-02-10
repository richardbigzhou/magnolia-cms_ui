/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.contentapp.dsmanager;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.AppLifecycleEvent;
import info.magnolia.ui.api.app.AppLifecycleEventHandler;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.contentapp.ContentAppDescriptor;
import info.magnolia.ui.vaadin.integration.dsmanager.DataSourceManager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 09/02/14
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class DataSourceManagerProvider {

    private Map<AppDescriptor, DataSourceManager> managers = new HashMap<AppDescriptor, DataSourceManager>();
    private ComponentProvider provider;

    @Inject
    public DataSourceManagerProvider(final ComponentProvider provider, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus) {
        this.provider = provider;
        admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {
            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                managers.remove(event.getAppDescriptor());
            }
        });
    }

    public DataSourceManager getDSManager(AppContext ctx) {
        AppDescriptor descriptor = ctx.getAppDescriptor();
        if (!managers.containsKey(descriptor)) {
            DataSourceManager dsManager;
            if (descriptor instanceof ContentAppDescriptor) {
                dsManager = provider.newInstance(((ContentAppDescriptor)descriptor).getDataSourceManagerClass(), ctx.getAppDescriptor());
            } else {
                dsManager = provider.newInstance(JcrDataSourceManager.class, ctx.getAppDescriptor());
            }
            managers.put(descriptor, dsManager);
        }
        return managers.get(descriptor);
    }


}
