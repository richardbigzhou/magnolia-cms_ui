/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.security.app.dialog.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DuplicateRoleAction}.
 */
public class DuplicateRoleActionTest extends RepositoryTestCase {

    private SecuritySupport securitySupport;

    private Session session;

    private MgnlRoleManager roleManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        roleManager = new MgnlRoleManager();
        securitySupport = mock(SecuritySupport.class);
        when(securitySupport.getRoleManager()).thenReturn(roleManager);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);

        session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
    }

    @Test
    public void testUserRoleAclPreserved() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        final Node testRoleNode = session.getRootNode().getNode("testRole");

        // WHEN
        final JcrNodeAdapter roleItem = new JcrNodeAdapter(testRoleNode);
        final DuplicateRoleAction action = new DuplicateRoleAction(
                new DuplicateRoleActionDefinition(), roleItem, mock(EventBus.class));
        action.execute();

        // THEN
        final Node userRoleAcls = session.getNode("/testRole0/acl_userroles/");
        final String userRoleAcl = userRoleAcls.getNode("0").getProperty("path").getString();
        assertEquals(userRoleAcl, "/testRole0");
    }
}
