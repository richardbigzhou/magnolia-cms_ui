/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DefaultActionToDelegateActionTask}.
 */
public class DefaultActionToDelegateActionTaskTest {

    private MockSession session;
    private DefaultActionToDelegateActionTask task;
    private InstallContext context;
    private HashMap<String, String> mappings;

    @Before
    public void setUp() throws Exception {
        context = mock(InstallContext.class);
        ModuleDefinition moduleDefinition = mock(ModuleDefinition.class);
        session = new MockSession(RepositoryConstants.CONFIG);
        mappings = new HashMap<>();
        mappings.put(NodeTypes.Folder.NAME, "expandNodeAction");
        mappings.put("mgnl:customNodeType", "someAction");
        task = new DefaultActionToDelegateActionTask("", "", "myApp", "mySubApp", mappings);

        when(context.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(session);
        when(context.getCurrentModuleDefinition()).thenReturn(moduleDefinition);
        when(moduleDefinition.getName()).thenReturn("myModule");

    }

    @Test
    public void defaultActionToDelegateAction() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), "/modules/myModule/apps/myApp/subApps/mySubApp/actions", NodeTypes.ContentNode.NAME, true);
        NodeUtil.createPath(session.getRootNode(), "/modules/myModule/apps/myApp/subApps/mySubApp/actionbar", NodeTypes.ContentNode.NAME, true);

        // WHEN
        task.execute(context);

        // THEN
        assertThat(session.getNode("/modules/myModule/apps/myApp/subApps/mySubApp/actions"), hasNode("delegateByNodeTypeAction"));
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            Node node = session.getNode(
                    String.format("/modules/myModule/apps/myApp/subApps/mySubApp/actions/%s/%s/%s",
                            "delegateByNodeTypeAction",
                            "nodeTypeToActionMappings",
                            Path.getValidatedLabel(entry.getKey())
                    ));
            assertThat(node, hasProperty("nodeType", entry.getKey()));
            assertThat(node, hasProperty("action", entry.getValue()));
        }
    }
}