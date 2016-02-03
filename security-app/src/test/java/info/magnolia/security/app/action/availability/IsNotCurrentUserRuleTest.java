/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the {@link IsNotCurrentUserRule}.
 */
public class IsNotCurrentUserRuleTest extends MgnlTestCase {

    private IsNotCurrentUserRule rule = new IsNotCurrentUserRule();
    private Item item;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockContext ctx = MockUtil.getMockContext();
        final User user = mock(User.class);
        when(user.getName()).thenReturn("test");
        ctx.setUser(user);
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void testTrueOnNull() {
        // GIVEN

        // WHEN
        item = null;

        // THEN
        assertTrue(rule.isAvailable(item));
    }

    @Test
    public void testTrueOnProperty() {
        // GIVEN

        // WHEN
        item = new MockProperty("foo", "bar", new MockNode());

        // THEN
        assertTrue(rule.isAvailable(item));
    }

    @Test
    public void testTrueOnNodeWithDifferentName() {
        // GIVEN

        // WHEN
        item = new MockNode("foo");

        // THEN
        assertTrue(rule.isAvailable(item));
    }

    @Test
    public void testFalseOnNodeWithSameName() {
        // GIVEN

        // WHEN
        item = new MockNode("test");

        // THEN
        assertFalse(rule.isAvailable(item));
    }

    @Test
    public void testFalseOnException() throws RepositoryException {
        // GIVEN
        item = mock(Node.class);
        when(item.isNode()).thenReturn(true);

        // WHEN
        when(item.getName()).thenThrow(new RepositoryException("Test exception."));

        // THEN
        assertFalse(rule.isAvailable(item));
    }
}
