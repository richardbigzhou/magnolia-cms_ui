/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.framework.availability;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IsNotSystemPropertyTest {

    private IsNotSystemProperty isNotSystemProperty;
    private Node fooNode;

    @Before
    public void setUp() throws Exception {
        this.isNotSystemProperty = new IsNotSystemProperty();

        final Session session = new MockSession("foo");
        this.fooNode = session.getRootNode().addNode("foo");

        final MockContext context = new MockContext();
        context.addSession("foo", session);
        MgnlContext.setInstance(context);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void rejectsSystemProperties() throws Exception {
        // GIVEN
        fooNode.setProperty("mgnl:baz", "baz");
        fooNode.setProperty("jcr:qux", "baz");

        // WHEN
        boolean isMgnlPropertyAllowed = isNotSystemProperty.isAvailableForItem(JcrItemUtil.getItemId(fooNode.getProperty("mgnl:baz")));
        boolean isJcrPropertyAllowed = isNotSystemProperty.isAvailableForItem(JcrItemUtil.getItemId(fooNode.getProperty("jcr:qux")));

        // THEN
        assertThat(isMgnlPropertyAllowed, is(false));
        assertThat(isJcrPropertyAllowed, is(false));
    }

    @Test
    public void allowsNonSystemProperties() throws Exception {
        // GIVEN
        fooNode.setProperty("baz", "baz");

        // WHEN
        boolean isBazAllowed = isNotSystemProperty.isAvailableForItem(JcrItemUtil.getItemId(fooNode.getProperty("baz")));

        // THEN
        assertThat(isBazAllowed, is(true));
    }

    @Test
    public void allowsNodes() throws Exception {
        // GIVEN
        final Node baz = fooNode.addNode("baz");

        // WHEN
        boolean isBazAllowed = isNotSystemProperty.isAvailableForItem(JcrItemUtil.getItemId(baz));

        // THEN
        assertThat(isBazAllowed, is(true));
    }

}