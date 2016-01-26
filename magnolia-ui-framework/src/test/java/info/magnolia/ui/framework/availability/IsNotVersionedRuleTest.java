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
package info.magnolia.ui.framework.availability;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.VersionedChildJcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.VersionedJcrItemId;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsNotVersionedRule}.
 */
public class IsNotVersionedRuleTest extends RepositoryTestCase {

    private VersionManager versionManager;
    private IsNotVersionedRule rule;
    private Session webSiteSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        versionManager = VersionManager.getInstance();
        rule = new IsNotVersionedRule();
    }

    @Test
    public void testWithVersion() throws RepositoryException {
        // GIVEN
        Node node = NodeUtil.createPath(webSiteSession.getRootNode(), "/testWithVersion", NodeTypes.ContentNode.NAME);
        Version version = versionManager.addVersion(node);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(version);
        boolean isAvailable = rule.isAvailable(Arrays.asList(itemId));

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testWithNode() throws RepositoryException {
        // GIVEN
        Node node = NodeUtil.createPath(webSiteSession.getRootNode(), "/testNoVersion", NodeTypes.ContentNode.NAME);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailable(Arrays.asList(itemId));

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testWithFrozenVersionNode() throws RepositoryException {
        // GIVEN
        Node node = NodeUtil.createPath(webSiteSession.getRootNode(), "/testFrozenVersion", NodeTypes.ContentNode.NAME);
        Version version = versionManager.addVersion(node);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(version.getFrozenNode());
        boolean isAvailable = rule.isAvailable(Arrays.asList(itemId));

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testWithVersionedJcrItemId() {
        // GIVEN
        Object itemId = mock(VersionedJcrItemId.class);

        // WHEN
        boolean isAvailable = rule.isAvailable(Arrays.asList(itemId));

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testWithVersionedChildJcrItemId() {
        // GIVEN
        Object itemId = mock(VersionedChildJcrItemId.class);

        // WHEN
        boolean isAvailable = rule.isAvailable(Arrays.asList(itemId));

        // THEN
        assertFalse(isAvailable);
    }
}
