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

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WritePermissionRequiredRule}.
 */
public class WritePermissionRequiredRuleTest {

    private WritePermissionRequiredRule rule;
    private Session session;
    private AccessManager accessManager;
    private User user;

    @Before
    public void setUp() throws Exception {

        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);

        rule = new WritePermissionRequiredRule();
        rule.setWritePermissionRequired(true);

        accessManager = mock(AccessManager.class);
        ctx.setAccessManager(accessManager);

        user = mock(User.class);
        ctx.setUser(user);
    }

    @Test
    public void isAvailableForItemReturnsFalseIfNodeIsNotWritable() throws RepositoryException {
        // GIVEN
        Node node = session.getRootNode().addNode("node1", NodeTypes.Page.NAME);
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
        Node node = session.getRootNode().addNode("node2", NodeTypes.Page.NAME);
        when(user.hasRole(eq("superuser"))).thenReturn(true);
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
        Node node = session.getRootNode().addNode("node3", NodeTypes.Page.NAME);
        when(accessManager.isGranted("/node3", Permission.WRITE)).thenReturn(true);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

}
