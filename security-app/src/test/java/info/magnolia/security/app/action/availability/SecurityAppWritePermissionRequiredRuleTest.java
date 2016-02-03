/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.security.app.action.availability;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link SecurityAppWritePermissionRequiredRule}
 */
public class SecurityAppWritePermissionRequiredRuleTest extends RepositoryTestCase {

    private SecurityAppWritePermissionRequiredRule rule;
    private AccessManager accessManager;
    private UserManager userManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        rule = new SecurityAppWritePermissionRequiredRule();
        rule.setWritePermissionRequired(true);

        SecuritySupportImpl security = new SecuritySupportImpl();
        ComponentsTestUtil.setInstance(SecuritySupport.class, security);
        userManager = mock(UserManager.class);
        security.addUserManager(Realm.REALM_ALL.getName(), userManager);

        accessManager = mock(AccessManager.class);
        Context ctx = spy(MgnlContext.getInstance());
        doReturn(accessManager).when(ctx).getAccessManager(anyString());
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void isAvailableForItemReturnsFalseIfNodeIsNotWritableAndUserHasNotSuperuserRole() throws RepositoryException {
        // GIVEN
        User user = mock(User.class);
        when(user.hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)).thenReturn(false);
        ((MockContext)MgnlContext.getInstance()).setUser(user);

        Session session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        Node node = session.getRootNode().addNode("superuser", NodeTypes.Page.NAME);
        when(accessManager.isGranted("/superuser", Permission.WRITE)).thenReturn(false);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void isAvailableForItemReturnsTrueIfNodeIsNotWritableButUserHasSuperuserRole() throws RepositoryException {
        // GIVEN
        User user = mock(User.class);
        when(user.hasRole(AccessDefinition.DEFAULT_SUPERUSER_ROLE)).thenReturn(true);
        ((MockContext)MgnlContext.getInstance()).setUser(user);

        Session session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        Node node = session.getRootNode().addNode("superuser", NodeTypes.Page.NAME);
        when(accessManager.isGranted("/superuser", Permission.WRITE)).thenReturn(false);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }
}
