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
package info.magnolia.jcrbrowser.app.contentconnector;

import static info.magnolia.test.hamcrest.ExecutionMatcher.throwsNothing;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.hamcrest.Execution;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JcrBrowserContentConnectorTest {

    private JcrBrowserContentConnector jcrBrowserContentConnector;

    private ConfiguredJcrContentConnectorDefinition jcrContentConnectorDefinition;

    private Session session;


    @Before
    public void setUp() throws Exception {
        this.jcrContentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        this.jcrBrowserContentConnector = new JcrBrowserContentConnector(null, jcrContentConnectorDefinition);
        this.session = new MockSession(RepositoryConstants.CONFIG);

        final MockContext context = new MockContext();
        context.addSession(RepositoryConstants.CONFIG, session);
        MgnlContext.setInstance(context);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void originalContentConnectorStaysIntact() throws Exception {
        // GIVEN
        jcrContentConnectorDefinition.setWorkspace("foo");

        // WHEN
        jcrBrowserContentConnector.getContentConnectorDefinition().setWorkspace("bar");

        // THEN
        assertThat(jcrContentConnectorDefinition.getWorkspace(), equalTo("foo"));
    }

    @Test
    public void escapesColonInSystemPropertyUrlFragment() throws Exception {
        // GIVEN
        final Node foo = session.getRootNode().addNode("foo");
        foo.setProperty("jcr:bar", "bar");
        foo.setProperty("mgnl:baz", "baz");

        // WHEN
        final String barUrlFragment = jcrBrowserContentConnector.getItemUrlFragment(JcrItemUtil.getItemId(foo.getProperty("jcr:bar")));
        final String bazUrlFragment = jcrBrowserContentConnector.getItemUrlFragment(JcrItemUtil.getItemId(foo.getProperty("mgnl:baz")));

        // THEN
        assertThat(barUrlFragment, equalTo("/foo@jcr---bar"));
        assertThat(bazUrlFragment, equalTo("/foo@mgnl---baz"));
    }

    @Test
    public void urlFragmentConversionsDoNotFailForNonExistingPropertyIds() throws Exception {
        final Node node = session.getRootNode().addNode("foo");
        final JcrPropertyItemId propertyItemId = new JcrPropertyItemId(node.getIdentifier(), RepositoryConstants.CONFIG, "bar");

        // WHEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                jcrBrowserContentConnector.getItemUrlFragment(propertyItemId);
            }
        }, throwsNothing());
    }

    @Test
    public void resolvesSystemPropertyIdFromModifiedUrlFragment() throws Exception {
        // GIVEN
        jcrContentConnectorDefinition.setWorkspace(RepositoryConstants.CONFIG);

        final Node foo = session.getRootNode().addNode("foo");

        foo.setProperty("jcr:bar", "bar");
        foo.setProperty("mgnl:baz", "baz");

        // WHEN
        final JcrItemId barId = jcrBrowserContentConnector.getItemIdByUrlFragment("/foo@jcr---bar");
        final JcrItemId bazId = jcrBrowserContentConnector.getItemIdByUrlFragment("/foo@mgnl---baz");

        // THEN
        assertThat(barId, allOf(instanceOf(JcrPropertyItemId.class), hasProperty("propertyName", equalTo("jcr:bar"))));
        assertThat(bazId, allOf(instanceOf(JcrPropertyItemId.class), hasProperty("propertyName", equalTo("mgnl:baz"))));
    }
}