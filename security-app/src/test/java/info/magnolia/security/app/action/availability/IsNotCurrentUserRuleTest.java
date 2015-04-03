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
package info.magnolia.security.app.action.availability;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;

import java.util.Arrays;

import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the {@link IsNotCurrentUserRule}.
 */
public class IsNotCurrentUserRuleTest extends MgnlTestCase {

    public static final String WORKSPACE = "workspace";
    private IsNotCurrentUserRule rule = new IsNotCurrentUserRule();
    private Object itemId;
    private MockSession session;
    private MockNode mockNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockContext ctx = MockUtil.getMockContext();
        final User user = mock(User.class);
        when(user.getName()).thenReturn("test");
        ctx.setUser(user);

        session = new MockSession(WORKSPACE);
        mockNode = new MockNode(session);
        mockNode.setName("foo");

        ctx.addSession(WORKSPACE, session);
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void testTrueOnNull() {
        // GIVEN

        // WHEN
        itemId = null;

        // THEN
        assertTrue(rule.isAvailable(Arrays.asList(itemId)));
    }

    @Test
    public void testTrueOnProperty() throws RepositoryException {
        // GIVEN

        // WHEN
        itemId = JcrItemUtil.getItemId(new MockProperty("foo", "bar", mockNode));

        // THEN
        assertTrue(rule.isAvailable(Arrays.asList(itemId)));
    }

    @Test
    public void testTrueOnNodeWithDifferentName() throws Exception {
        // GIVEN

        // WHEN
        itemId = JcrItemUtil.getItemId(mockNode);

        // THEN
        assertTrue(rule.isAvailable(Arrays.asList(itemId)));
    }

    @Test
    public void testFalseOnNodeWithSameName() throws RepositoryException {
        // GIVEN
        MockNode testNode = new MockNode(session);
        testNode.setName("test");

        // WHEN
        itemId = JcrItemUtil.getItemId(testNode);

        // THEN
        assertFalse(rule.isAvailable(Arrays.asList(itemId)));
    }

    @Test
    @Ignore
    // The node is now fetched within the rule, so hard to mock exception.
    public void testFalseOnException() {
        // GIVEN
        MockNode testNode = mock(MockNode.class);
        mockNode.addNode(testNode);
        doReturn(true).when(testNode).isNode();
        doReturn(session).when(testNode).getSession();
        doReturn("uuid").when(testNode).getIdentifier();

        itemId = new JcrNodeItemId("uuid", WORKSPACE);

        // WHEN
        when(testNode.getName()).thenThrow(new RepositoryException("Test exception."));

        // THEN
        assertFalse(rule.isAvailable(Arrays.asList(itemId)));
    }
}
