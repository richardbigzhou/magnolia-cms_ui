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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.contentapp.definition.ContentSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.dsmanager.DataSourceManager;
import info.magnolia.ui.vaadin.integration.dsmanager.DataSourceManagerDefinition;
import info.magnolia.ui.workbench.dsmanager.DataSourceManagerProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides {@link DataSourceManager} to the apps. This class exists as a workaround - the process of app
 * {@link ComponentProvider} creation is hard to break in without major changes.
 */
@Singleton
public class DataSourceManagerProviderImpl implements DataSourceManagerProvider {

    private DataSourceManager manager;

    private SubAppContext ctx;
    private ComponentProvider provider;
    private EventBus subAppEventBus;

    @Inject
    public DataSourceManagerProviderImpl(SubAppContext ctx, final ComponentProvider provider, @Named(SubAppEventBus.NAME) EventBus subAppEventBus) {
        this.ctx = ctx;
        this.provider = provider;
        this.subAppEventBus = subAppEventBus;
    }

    public DataSourceManager getDSManager() {
        if (manager == null) {
            SubAppDescriptor subAppDescriptor = ctx.getSubAppDescriptor();
            if (subAppDescriptor instanceof ContentSubAppDescriptor) {
                DataSourceManagerDefinition dsManagerDefinition = ((ContentSubAppDescriptor)subAppDescriptor).getDataSourceManager();
                manager = provider.newInstance(dsManagerDefinition.getImplementationClass(), ctx, subAppEventBus, dsManagerDefinition);
            }
        }
        return manager;
    }


}
