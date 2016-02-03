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
package info.magnolia.ui.api.availability;

import static org.junit.Assert.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link HasVersionsRule}.
 */
public class HasVersionsRuleTest extends RepositoryTestCase {

    private VersionManager versionManager;
    private HasVersionsRule rule;
    private Session webSiteSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        versionManager = VersionManager.getInstance();
        rule = new HasVersionsRule(versionManager);
    }

    @Test
    public void testIsAvailableForItemNoVersion() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("nodeWithNoVersion", NodeTypes.Page.NAME);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailableForItemVersionOnlyOneVersion() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("nodeWithOneVersion", NodeTypes.Page.NAME);
        versionManager.addVersion(node);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailableForItemVersionTwoVersions() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("nodeWithTwoVersions", NodeTypes.Page.NAME);
        versionManager.addVersion(node);
        versionManager.addVersion(node);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailableForItemNoNode() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("deletedNode", NodeTypes.Page.NAME);
        NodeTypes.Created.set(node);
        node.remove();

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailableForNullNode() throws RepositoryException {
        // GIVEN

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(null);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailableForNodeWithoutVersioning() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("nodeThatDoesntSupportVersioning", NodeType.NT_UNSTRUCTURED);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertFalse(isAvailable);
    }

}
