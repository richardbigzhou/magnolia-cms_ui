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

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;

/**
 * Test case for SaveRoleDialogActionTest.
 */
public class SaveRoleDialogActionTest extends MgnlTestCase {

    private Session session;
    private Context context;
    private RepositoryManager repositoryManager;
    private SaveRoleDialogAction saveRoleDialogAction;
    private Node aclNode;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = mock(Context.class);
        session = mock(Session.class);

        aclNode = mock(Node.class);
        when(aclNode.getName()).thenReturn("acl_" + RepositoryConstants.CONFIG);

        when(context.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(session);
        MgnlContext.setInstance(context);

        repositoryManager = mock(RepositoryManager.class);
        when(repositoryManager.hasWorkspace(RepositoryConstants.CONFIG)).thenReturn(true);
        ComponentsTestUtil.setInstance(RepositoryManager.class, repositoryManager);

        saveRoleDialogAction = new SaveRoleDialogAction(null, null, null, null, mock(SecuritySupport.class), repositoryManager);
    }
/*
    @Test
    public void denyRoleCreationForUserNotHavingRightPermissions() throws Exception {
        // GIVEN user can read but not write given node
        when(session.hasPermission("/foo", Session.ACTION_READ)).thenReturn(true);

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isCurrentUserEntitledToGrantRights(aclNode.getName(), "/foo", Permission.WRITE, accessType);

        // THEN
        assertFalse(isEntitled);
    }

    @Test
    public void allowRoleCreationForUserHavingRightPermissions() throws Exception {
        // GIVEN
        when(session.hasPermission("/foo", Session.ACTION_READ)).thenReturn(true);

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isCurrentUserEntitledToGrantRights(aclNode.getName(), "/foo", Permission.READ, accessType);

        // THEN
        assertTrue(isEntitled);
    }

    @Test
    public void allowRoleCreationForUserWithNoPermissionsOnAGivenNodeWhenDenyingGrants() throws Exception {
        // GIVEN
        when(session.hasPermission(eq("/foo"), anyString())).thenReturn(false);

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isCurrentUserEntitledToGrantRights(aclNode.getName(), "/foo", Permission.NONE, accessType);

        // THEN
        assertTrue(isEntitled);
    }

    @Test
    public void allowRoleCreationWhenWorkspaceDoesNotExist() throws Exception {
        // GIVEN
        when(repositoryManager.hasWorkspace(RepositoryConstants.CONFIG)).thenReturn(false);
        when(context.getJCRSession(RepositoryConstants.CONFIG)).thenThrow(new NoSuchWorkspaceException());

        // WHEN
        boolean isEntitled = saveRoleDialogAction.isCurrentUserEntitledToGrantRights(aclNode.getName(), "/foo", Permission.READ, accessType);

        // THEN
        assertTrue(isEntitled);
    }
*/
}
