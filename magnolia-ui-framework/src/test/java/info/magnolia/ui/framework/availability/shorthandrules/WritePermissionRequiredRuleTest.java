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
package info.magnolia.ui.framework.availability.shorthandrules;

import static info.magnolia.cms.security.SecurityConstants.NODE_ROLES;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WritePermissionRequiredRule}.
 */
public class WritePermissionRequiredRuleTest extends RepositoryTestCase {

    private WritePermissionRequiredRule rule;
    private Session webSiteSession;
    private AccessManager accessManager;
    private UserManager userManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        rule = new WritePermissionRequiredRule();
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
    public void isAvailableForItemReturnsFalseIfNodeIsNotWritable() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("node1", NodeTypes.Page.NAME);
        when(accessManager.isGranted("/node1", Permission.WRITE)).thenReturn(false);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void isAvailableForItemAlsoReturnsFalseForSuperuserIfNodeIsNotWritable() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("node2", NodeTypes.Page.NAME);
        node.addMixin(NodeTypes.Deleted.NAME);
        when(userManager.hasAny(anyString(), matches("superuser"), matches(NODE_ROLES))).thenReturn(true);
        when(accessManager.isGranted("/node2", Permission.WRITE)).thenReturn(false);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void isAvailableForItemReturnsTrueIfNodeIsWritable() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("node3", NodeTypes.Page.NAME);
        webSiteSession.save();
        when(accessManager.isGranted("/node3", Permission.WRITE)).thenReturn(true);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

}
