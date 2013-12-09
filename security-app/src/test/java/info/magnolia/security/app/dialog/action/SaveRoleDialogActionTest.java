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
package info.magnolia.security.app.dialog.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.Permission;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for SaveRoleDialogActionTest.
 */
public class SaveRoleDialogActionTest {
    private static final String SOMEWORKSPACE = "someworkspace";
    private Session session;
    private Context context;
    private SaveRoleDialogAction saveRoleDialogAction;
    private Node aclNode;

    @Before
    public void setUp() throws Exception {
        context = mock(Context.class);
        session = mock(Session.class);

        aclNode = mock(Node.class);
        when(aclNode.getName()).thenReturn("acl_" + SOMEWORKSPACE);

        when(context.getJCRSession(SOMEWORKSPACE)).thenReturn(session);
        MgnlContext.setInstance(context);

        saveRoleDialogAction = new SaveRoleDialogAction(null, null, null, null);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void denyRoleCreationForUserNotHavingRightPermissions() throws Exception {
        // GIVEN user can read but not write given node
        when(session.hasPermission("/foo", Session.ACTION_READ)).thenReturn(true);

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isRoleCreatorEntitledToGrantRights(aclNode, Permission.WRITE, "/foo");

        // THEN
        assertFalse(isEntitled);
    }

    @Test
    public void allowRoleCreationForUserHavingRightPermissions() throws Exception {
        // GIVEN
        when(session.hasPermission("/foo", Session.ACTION_READ)).thenReturn(true);

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isRoleCreatorEntitledToGrantRights(aclNode, Permission.READ, "/foo");

        // THEN
        assertTrue(isEntitled);
    }

    @Test
    public void allowRoleCreationForUserWithNoPermissionsOnAGivenNodeWhenDenyingGrants() throws Exception {
        // GIVEN
        when(session.hasPermission("/foo", Session.ACTION_READ)).thenReturn(false);

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isRoleCreatorEntitledToGrantRights(aclNode, Permission.NONE, "/foo");

        // THEN
        assertTrue(isEntitled);
    }
}
