/**
 * This file Copyright (c) 2014 Magnolia International
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsPublishedRule}.

 */
public class IsPublishedRuleTest  extends RepositoryTestCase {

    private IsPublishedRule rule;
    private Session webSiteSession;
    private Node publishedNode;
    private Node modifiedNode;
    private Node unpublishedNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        rule = new IsPublishedRule();

        publishedNode = webSiteSession.getRootNode().addNode("a_a", NodeTypes.Page.NAME);
        modifiedNode = webSiteSession.getRootNode().addNode("a_b", NodeTypes.Page.NAME);
        unpublishedNode = webSiteSession.getRootNode().addNode("a_b/a_b_a", NodeTypes.Page.NAME);
        publishedNode.addMixin(NodeTypes.Activatable.NAME);
        publishedNode.addMixin(NodeTypes.LastModified.NAME);
        modifiedNode.addMixin(NodeTypes.Activatable.NAME);
        modifiedNode.addMixin(NodeTypes.LastModified.NAME);
        unpublishedNode.addMixin(NodeTypes.Activatable.NAME);
        Calendar before = Calendar.getInstance();
        before.set(2000, Calendar.JANUARY, 1);
        Calendar now = Calendar.getInstance();
        now.set(2000, Calendar.JANUARY, 2);
        Calendar after = Calendar.getInstance();
        after.set(2000, Calendar.JANUARY, 3);
        publishedNode.setProperty(NodeTypes.Activatable.LAST_ACTIVATED, now);
        publishedNode.setProperty(NodeTypes.LastModified.LAST_MODIFIED, before);
        publishedNode.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, true);
        modifiedNode.setProperty(NodeTypes.Activatable.LAST_ACTIVATED, now);
        modifiedNode.setProperty(NodeTypes.LastModified.LAST_MODIFIED, after);
        modifiedNode.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, true);
        unpublishedNode.setProperty(NodeTypes.Activatable.LAST_ACTIVATED, now);
        unpublishedNode.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, false);
    }

    @Test
    public void testIsAvailableForItemThatIsPublished() throws RepositoryException {
        // GIVEN

        // WHEN
        Object itemId = JcrItemUtil.getItemId(publishedNode);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailableForItemThatIsModified() throws RepositoryException {
        // GIVEN

        // WHEN
        Object itemId = JcrItemUtil.getItemId(modifiedNode);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsNotAvailableForItemThatIsNotPublished() throws RepositoryException {
        // GIVEN

        // WHEN
        Object itemId = JcrItemUtil.getItemId(unpublishedNode);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(isAvailable);
    }
}
