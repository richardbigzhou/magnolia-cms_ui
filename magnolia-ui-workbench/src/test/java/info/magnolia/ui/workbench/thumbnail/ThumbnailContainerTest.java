/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.workbench.thumbnail;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.imageprovider.DefaultImageProvider;
import info.magnolia.ui.workbench.definition.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Main test class for {@link ThumbnailContainer}.
 */
public class ThumbnailContainerTest extends RepositoryTestCase {

    private Session session;
    private ConfiguredWorkbenchDefinition configuredWorkbench;
    private ThumbnailContainer container;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);

        List<NodeTypeDefinition> nodeTypes = new ArrayList<NodeTypeDefinition>();
        ConfiguredNodeTypeDefinition nodeType = new ConfiguredNodeTypeDefinition();
        nodeType.setName(NodeTypes.Content.NAME);
        nodeTypes.add(nodeType);

        configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(RepositoryConstants.CONFIG);
        configuredWorkbench.setPath("/");
        configuredWorkbench.setNodeTypes(nodeTypes);

        container = new ThumbnailContainer(configuredWorkbench, new DefaultImageProvider());
    }

    @Test
    public void testGetAllIdentifiersForRootPath() throws RepositoryException {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), "/content1/content11", NodeTypes.Content.NAME);
        NodeUtil.createPath(session.getRootNode(), "/content2/content21", NodeTypes.Content.NAME);
        Node contentNode = NodeUtil.createPath(session.getRootNode(), "/content2/contentNode", NodeTypes.ContentNode.NAME);
        Node folderNode = NodeUtil.createPath(session.getRootNode(), "/content2/folderNode", NodeTypes.Folder.NAME);
        session.save();
        // WHEN
        List<String> res = container.getAllIdentifiers(RepositoryConstants.CONFIG);

        // THEN
        assertNotNull(res);
        assertEquals(4, res.size());
        assertTrue(res.contains(session.getNode("/content2/content21").getIdentifier()));
        assertFalse(res.contains(contentNode.getIdentifier()));
        assertFalse(res.contains(folderNode.getIdentifier()));

    }

    @Test
    public void testGetAllIdentifiersForSubPath() throws RepositoryException {
        // GIVEN
        configuredWorkbench.setPath("/content2");
        NodeUtil.createPath(session.getRootNode(), "/content2/content21", NodeTypes.Content.NAME);
        Node contentNode = NodeUtil.createPath(session.getRootNode(), "/content2/contentNode", NodeTypes.ContentNode.NAME);
        Node folderNode = NodeUtil.createPath(session.getRootNode(), "/content2/folderNode", NodeTypes.Folder.NAME);
        session.save();
        // WHEN
        List<String> res = container.getAllIdentifiers(RepositoryConstants.CONFIG);

        // THEN
        assertNotNull(res);
        assertEquals(1, res.size());
        assertTrue(res.contains(session.getNode("/content2/content21").getIdentifier()));
        assertFalse(res.contains(contentNode.getIdentifier()));
        assertFalse(res.contains(folderNode.getIdentifier()));
    }


}
