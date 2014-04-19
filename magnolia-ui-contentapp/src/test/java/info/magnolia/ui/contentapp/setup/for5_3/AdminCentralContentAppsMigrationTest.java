/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.contentapp.setup.for5_3;

import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.contentapp.setup.ContentAppModuleVersionHandler;

import java.util.Arrays;
import java.util.List;

/**
 * This test class is testing content-app-migration handled by ContentAppMigrationTask exemplary for a content-app of the module "ui-admincentral".
 */
public class AdminCentralContentAppsMigrationTest extends AbstractContentAppMigrationTaskTest {
    @Override
    public String getModulePath() {
        return "/modules/ui-admincentral";
    }

    @Override
    public String getAppName() {
        return "testApp";
    }

    @Override
    public String getMainSubAppName() {
        return "browser";
    }

    @Override
    public String getDetailSubAppName() {
        return null;
    }

    @Override
    public String getWorkspaceName() {
        return RepositoryConstants.CONFIG;
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public Version getCurrentlyInstalledVersion() {
        return Version.parseVersion("5.2.3");
    }

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-contentapp.xml";
    }


    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml",
                "/META-INF/magnolia/ui-framework.xml"
        );
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new ContentAppModuleVersionHandler();
    }
}

