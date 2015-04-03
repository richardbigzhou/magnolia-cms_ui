/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.security.setup;

import static org.junit.Assert.assertFalse;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.test.RepositoryTestCase;

import java.io.IOException;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * Tests for making sure the bootstrap files are correct.
 */
public class BootstrapFileTest extends RepositoryTestCase {

    /**
     * A common developer mistake with this module is exporting the app and forgetting to remove the public users sub app
     * added by the public-user-registration module. This test will fail if the sub app is present in the bootstrap file.
     */
    @Test
    public void testDoesNotContainPublicUsersSubApp() throws IOException, RepositoryException {

        String[] resourcesToBootstrap = {"/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml"};
        BootstrapUtil.bootstrap(resourcesToBootstrap, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);

        assertFalse("Bootstrap file for the security app must not contain the public users sub app.", MgnlContext.getJCRSession("config").nodeExists("/modules/security-app/apps/security/subApps/public"));
    }
}
