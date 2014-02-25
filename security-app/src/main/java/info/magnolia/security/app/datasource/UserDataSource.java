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
package info.magnolia.security.app.datasource;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.security.app.container.UserContainer;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.contentapp.datasource.JcrDataSource;
import info.magnolia.ui.vaadin.integration.datasource.ContainerConfiguration;

import javax.inject.Inject;

import com.vaadin.data.Container;

/**
 * {@link JcrDataSource} extension used by Users/System users sub-apps of Security app.
 */
public class UserDataSource extends JcrDataSource {

    @Inject
    public UserDataSource(SubAppContext subAppContext, VersionManager versionManager) {
        super(subAppContext, versionManager);
    }

    @Override
    public Container createContentViewContainer(ContainerConfiguration config) {
        if (TREEVIEW_ID.equalsIgnoreCase(config.getViewTypeId())) {
            UserContainer userContainer = new UserContainer(getWorkbenchDefinition());
            configureContainer(userContainer, config);
            return userContainer;
        }
        return super.createContentViewContainer(config);
    }
}
